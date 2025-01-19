package com.project.datalogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

import com.virtualplc.VirtualPLC;

public class AlertChecker {
	private static final String DB_URL = "jdbc:sqlserver://<SERVER_IP>:<PORT>;databaseName=SlurryCoatingDB";
	private static final String DB_USER = "<USERNAME>";
	private static final String DB_PASSWORD = "<PASSWORD>";

	public static void checkAlerts(VirtualPLC plc) throws Exception {
		try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
			if (plc.getSlurryVolume() < 10) {
				saveAlert(connection, "Slurry", "Slurry volume critically low.");
			}
			if (plc.getCoatingThickness() > 12) {
				saveAlert(connection, "Coating", "Coating thickness exceeds limit.");
			}
			if (plc.getDryingTemperature() > 100) {
				saveAlert(connection, "Drying", "Drying temperature exceeds limit.");
			}
		}
	}

	private static void saveAlert(Connection connection, String alertType, String alertMessage) throws Exception {
		String sql = "INSERT INTO Notification (Timestamp, AlertType, AlertMessage) VALUES (?, ?, ?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
			stmt.setString(2, alertType);
			stmt.setString(3, alertMessage);
			stmt.executeUpdate();
		}
	}
}
