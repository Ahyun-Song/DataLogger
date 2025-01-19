package com.project.datalogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

import com.virtualplc.VirtualPLC;

public class DatabaseManager {
	private static final String DB_URL = "jdbc:sqlserver://<SERVER_IP>:<PORT>;databaseName=SlurryCoatingDB";
	private static final String DB_USER = "<USERNAME>";
	private static final String DB_PASSWORD = "<PASSWORD>";

	// 데이터 저장 메서드
	public static void saveData(VirtualPLC plc) throws Exception {
		try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
			saveSlurryData(connection, plc);
			saveCoatingData(connection, plc);
			saveDryingData(connection, plc);
		}
	}

	private static void saveSlurryData(Connection connection, VirtualPLC plc) throws Exception {
		String sql = "INSERT INTO Slurry (Timestamp, SupplyRate, RemainingVolume, Temperature) VALUES (?, ?, ?, ?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
			stmt.setDouble(2, plc.getSlurrySupplyRate());
			stmt.setDouble(3, plc.getSlurryVolume());
			stmt.setDouble(4, plc.getSlurryTemperature());
			stmt.executeUpdate();
		}
	}

	private static void saveCoatingData(Connection connection, VirtualPLC plc) throws Exception {
		String sql = "INSERT INTO Coating (Timestamp, Speed, Thickness) VALUES (?, ?, ?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
			stmt.setDouble(2, plc.getCoatingSpeed());
			stmt.setDouble(3, plc.getCoatingThickness());
			stmt.executeUpdate();
		}
	}

	private static void saveDryingData(Connection connection, VirtualPLC plc) throws Exception {
		String sql = "INSERT INTO Drying (Timestamp, Temperature) VALUES (?, ?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
			stmt.setDouble(2, plc.getDryingTemperature());
			stmt.executeUpdate();
		}
	}
}
