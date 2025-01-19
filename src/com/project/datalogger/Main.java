package com.project.datalogger;

import java.util.Timer;
import java.util.TimerTask;

import com.virtualplc.VirtualPLC;

public class Main {

	public static void main(String[] args) {
		VirtualPLC plc = new VirtualPLC(); // PLC 객체 생성
		Timer timer = new Timer();

		// 5분마다 데이터 저장 실행
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					plc.updateProcesses(); // PLC 데이터 업데이트
					DatabaseManager.saveData(plc); // 데이터베이스에 저장
					AlertChecker.checkAlerts(plc); // 임계치 검사 및 알림 저장
					System.out.println("Data successfully logged.");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 0, 300000); // 300,000 밀리초 = 5분
	}
}
