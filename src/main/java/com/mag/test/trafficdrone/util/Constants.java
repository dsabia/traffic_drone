package com.mag.test.trafficdrone.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Constants {

	public static final int NEARBY_STATION_IN_METER = 350;
	public static final Integer DRONE_QUEUE_SIZE = 10;
	
	public static final double METER_FOR_LNG = 0.0139;
	public static final double METER_FOR_LAT = 0.00899;

	public final static DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static Date eightAM, eightTenAM;
	
	public static final boolean DEBUG = false;
	
	static {
		try {
			eightAM = formatter.parse("2011-03-22 08:00:00");
			eightTenAM = formatter.parse("2011-03-22 08:10:00");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
