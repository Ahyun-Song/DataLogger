package com.project.datalogger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DataReceiver {
	// 데이터 큐(캐싱)
	private static final ConcurrentLinkedQueue<JsonObject> dataQueue = new ConcurrentLinkedQueue<>();

	// TCP 서버 실행
	public static void startServer(int port) {
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			System.out.println("Server is listening on port " + port);

			while (true) {
				Socket socket = serverSocket.accept(); // VirtualPLC 연결 대기
				System.out.println("New client connected");

				// 데이터를 수신하여 큐에 저장
				try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
					String jsonData;
					while ((jsonData = in.readLine()) != null) {
						System.out.println("Received JSON Data: " + jsonData);

						// JSON 데이터 파싱 및 큐에 추가
						JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();
						dataQueue.add(jsonObject); // 큐에 데이터 추가
					}
				} catch (Exception e) {
					System.err.println("Error processing client data: " + e.getMessage());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 5분마다 큐에 있는 데이터를 데이터베이스에 저장
	public static void savePeriodicData() {
		while (!dataQueue.isEmpty()) {
			JsonObject jsonData = dataQueue.poll(); // 큐에서 데이터 가져오기
			saveToDatabase(jsonData); // 데이터베이스에 저장
		}
	}

	// 데이터베이스에 데이터 저장
	private static void saveToDatabase(JsonObject jsonData) {
		try (Connection connection = DatabaseManager.connect()) {
			// 슬러리 데이터 저장
			JsonObject slurry = jsonData.getAsJsonObject("SlurryTank");
			saveSlurryData(connection, slurry);

			// 코팅 데이터 저장
			JsonObject coating = jsonData.getAsJsonObject("CoatingProcess");
			saveCoatingData(connection, coating);

			// 건조 데이터 저장
			JsonObject drying = jsonData.getAsJsonObject("DryingProcess");
			saveDryingData(connection, drying);
		} catch (Exception e) {
			System.err.println("Error while saving data to the database: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// Timestamp 변환 메서드
	private static String formatTimestamp(String isoTimestamp) {
		return isoTimestamp.replace("T", " ").split("\\.")[0]; // "T"를 공백으로 대체하고 밀리초 제거
	}

	// 슬러리 데이터 저장
	private static void saveSlurryData(Connection connection, JsonObject slurry) throws Exception {
		String sql = "INSERT INTO Slurry (Timestamp, SupplyRate, RemainingVolume, Temperature) VALUES (?, ?, ?, ?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			String formattedTimestamp = formatTimestamp(slurry.get("Timestamp").getAsString());
			stmt.setTimestamp(1, Timestamp.valueOf(formattedTimestamp)); // 변환된 Timestamp 사용
			stmt.setDouble(2, slurry.get("SupplySpeed").getAsDouble());
			stmt.setDouble(3, slurry.get("RemainingVolume").getAsDouble());
			stmt.setDouble(4, slurry.get("Temperature").getAsDouble());
			stmt.executeUpdate();
		}
	}

	// 코팅 데이터 저장
	private static void saveCoatingData(Connection connection, JsonObject coating) throws Exception {
		String sql = "INSERT INTO Coating (Timestamp, Speed, Thickness) VALUES (?, ?, ?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			String formattedTimestamp = formatTimestamp(coating.get("Timestamp").getAsString());
			stmt.setTimestamp(1, Timestamp.valueOf(formattedTimestamp)); // 변환된 Timestamp 사용
			stmt.setDouble(2, coating.get("Speed").getAsDouble());
			stmt.setDouble(3, coating.get("Thickness").getAsDouble());
			stmt.executeUpdate();
		}
	}

	// 건조 데이터 저장
	private static void saveDryingData(Connection connection, JsonObject drying) throws Exception {
		String sql = "INSERT INTO Drying (Timestamp, Temperature) VALUES (?, ?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			String formattedTimestamp = formatTimestamp(drying.get("Timestamp").getAsString());
			stmt.setTimestamp(1, Timestamp.valueOf(formattedTimestamp)); // 변환된 Timestamp 사용
			stmt.setDouble(2, drying.get("Temperature").getAsDouble());
			stmt.executeUpdate();
		}
	}
}
