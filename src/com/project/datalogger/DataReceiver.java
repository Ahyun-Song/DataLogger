package com.project.datalogger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

public class DataReceiver {
	public static void startServer(int port) {
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			System.out.println("Server is listening on port " + port);

			while (true) {
				Socket socket = serverSocket.accept();
				System.out.println("New client connected");

				try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
					String jsonData;
					while ((jsonData = in.readLine()) != null) {
						System.out.println("Received JSON Data: " + jsonData);
						saveToDatabase(jsonData); // 데이터 저장 호출
					}
				} catch (Exception e) {
					System.err.println("Error processing client data: " + e.getMessage());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void saveToDatabase(String jsonData) {
		try (Connection connection = DatabaseManager.connect()) { // DatabaseManager의 연결 메서드 사용
			JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();

			// 슬러리 데이터 저장
			JsonObject slurry = jsonObject.getAsJsonObject("SlurryTank");
			saveSlurryData(connection, slurry);

			// 코팅 데이터 저장
			JsonObject coating = jsonObject.getAsJsonObject("CoatingProcess");
			saveCoatingData(connection, coating);

			// 건조 데이터 저장
			JsonObject drying = jsonObject.getAsJsonObject("DryingProcess");
			saveDryingData(connection, drying);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void saveSlurryData(Connection connection, JsonObject slurry) throws Exception {
		String sql = "INSERT INTO Slurry (Timestamp, SupplyRate, RemainingVolume, Temperature) VALUES (?, ?, ?, ?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setTimestamp(1, Timestamp.valueOf(slurry.get("Timestamp").getAsString()));
			stmt.setDouble(2, slurry.get("SupplySpeed").getAsDouble());
			stmt.setDouble(3, slurry.get("RemainingVolume").getAsDouble());
			stmt.setDouble(4, slurry.get("Temperature").getAsDouble());
			stmt.executeUpdate();
		}
	}

	private static void saveCoatingData(Connection connection, JsonObject coating) throws Exception {
		String sql = "INSERT INTO Coating (Timestamp, Speed, Thickness) VALUES (?, ?, ?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setTimestamp(1, Timestamp.valueOf(coating.get("Timestamp").getAsString()));
			stmt.setDouble(2, coating.get("Speed").getAsDouble());
			stmt.setDouble(3, coating.get("Thickness").getAsDouble());
			stmt.executeUpdate();
		}
	}

	private static void saveDryingData(Connection connection, JsonObject drying) throws Exception {
		String sql = "INSERT INTO Drying (Timestamp, Temperature) VALUES (?, ?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setTimestamp(1, Timestamp.valueOf(drying.get("Timestamp").getAsString()));
			stmt.setDouble(2, drying.get("Temperature").getAsDouble());
			stmt.executeUpdate();
		}
	}
}
