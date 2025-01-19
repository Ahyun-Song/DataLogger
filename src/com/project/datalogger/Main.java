package com.project.datalogger;

import java.util.Timer;
import java.util.TimerTask;

public class Main {
	public static void main(String[] args) {
		// TCP 서버 실행 (VirtualPLC 데이터 수신)
		new Thread(() -> {
			try {
				DataReceiver.startServer(8080); // TCP 서버 실행
			} catch (Exception e) {
				System.err.println("Failed to start DataReceiver: " + e.getMessage());
				e.printStackTrace();
			}
		}).start();

		// 5분마다 데이터 저장 로직 실행
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					// VirtualPLC의 데이터를 활용해 데이터베이스에 저장
					DataReceiver.savePeriodicData(); // 수신된 데이터를 5분마다 저장
					System.out.println("Periodic data saved successfully.");
				} catch (Exception e) {
					System.err.println("Error during periodic data save: " + e.getMessage());
					e.printStackTrace();
				}
			}
		}, 0, 300000); // 300,000ms = 5분
	}
}
