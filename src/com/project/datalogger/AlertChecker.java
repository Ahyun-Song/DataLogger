package com.project.datalogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.virtualplc.VirtualPLC;

public class AlertChecker {
	public static void checkAlerts(VirtualPLC plc) {
		try (Connection connection = DatabaseManager.connect()) { // DatabaseManager의 연결 사용
			if (plc.getSlurryVolume() < 10) {
				saveAlert(connection, "Slurry", "Slurry volume critically low.");
			}
			if (plc.getCoatingThickness() > 12) {
				saveAlert(connection, "Coating", "Coating thickness exceeds limit.");
			}
			if (plc.getDryingTemperature() > 100) {
				saveAlert(connection, "Drying", "Drying temperature exceeds limit.");
			}
		} catch (Exception e) {
			System.err.println("Error checking alerts: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static void saveAlert(Connection connection, String alertType, String alertMessage) {
		String sql = "INSERT INTO Notification (Timestamp, AlertType, AlertMessage) VALUES (?, ?, ?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
			stmt.setString(2, alertType);
			stmt.setString(3, alertMessage);
			stmt.executeUpdate();
			System.out.println("Alert saved: " + alertType + " - " + alertMessage);
		} catch (Exception e) {
			System.err.println("Error saving alert: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
