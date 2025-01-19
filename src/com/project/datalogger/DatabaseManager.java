package com.project.datalogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.virtualplc.VirtualPLC;

public class DatabaseManager {
	// MSSQL 연결 정보
	private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=SlurryCoatingDB;integratedSecurity=true;trustServerCertificate=true;";
	
	// 데이터베이스 연결 메서드
	public static Connection connect() throws SQLException {
		try {
			Connection connection = DriverManager.getConnection(DB_URL);
			System.out.println("Database connected successfully using Windows Authentication.");
			return connection;
		} catch (SQLException e) {
			System.err.println("Database connection failed: " + e.getMessage());
			throw e;
		}
	}

	// VirtualPLC 데이터를 저장하는 메서드
	public static void saveData(VirtualPLC plc) {
		try (Connection connection = connect()) {
			saveSlurryData(connection, plc);
			saveCoatingData(connection, plc);
			saveDryingData(connection, plc);
		} catch (Exception e) {
			System.err.println("Error while saving data: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// 슬러리 데이터 저장
	private static void saveSlurryData(Connection connection, VirtualPLC plc) {
		String sql = "INSERT INTO Slurry (Timestamp, SupplyRate, RemainingVolume, Temperature) VALUES (?, ?, ?, ?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
			stmt.setDouble(2, plc.getSlurrySupplyRate());
			stmt.setDouble(3, plc.getSlurryVolume());
			stmt.setDouble(4, plc.getSlurryTemperature());
			stmt.executeUpdate();
			System.out.println("Slurry data saved.");
		} catch (SQLException e) {
			System.err.println("Error saving Slurry data: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// 코팅 데이터 저장
	private static void saveCoatingData(Connection connection, VirtualPLC plc) {
		String sql = "INSERT INTO Coating (Timestamp, Speed, Thickness) VALUES (?, ?, ?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
			stmt.setDouble(2, plc.getCoatingSpeed());
			stmt.setDouble(3, plc.getCoatingThickness());
			stmt.executeUpdate();
			System.out.println("Coating data saved.");
		} catch (SQLException e) {
			System.err.println("Error saving Coating data: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// 건조 데이터 저장
	private static void saveDryingData(Connection connection, VirtualPLC plc) {
		String sql = "INSERT INTO Drying (Timestamp, Temperature) VALUES (?, ?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
			stmt.setDouble(2, plc.getDryingTemperature());
			stmt.executeUpdate();
			System.out.println("Drying data saved.");
		} catch (SQLException e) {
			System.err.println("Error saving Drying data: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// 테스트 메서드
	public static void main(String[] args) {
		try {
			VirtualPLC plc = new VirtualPLC(); // VirtualPLC 객체 생성
			plc.updateProcesses(); // 데이터를 업데이트
			saveData(plc); // 데이터 저장 호출
		} catch (Exception e) {
			System.err.println("Error in main: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
