package com.project.datalogger;

public interface PLCData {
	double getSlurryVolume();
	double getSlurrySupplyRate();
	double getSlurryTemperature();
	double getCoatingSpeed();
	double getCoatingThickness();
	double getDryingTemperature();
}
