package com.mag.test.trafficdrone;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.davidmoten.rtree.geometry.Point;
import com.mag.test.trafficdrone.util.CSVUtil;
import com.mag.test.trafficdrone.util.Constants;

public class Dispatcher extends Thread {

	private static Logger logger = LoggerFactory.getLogger(Dispatcher.class.getSimpleName());

	private Map<Drone, TreeMap<Date, Point>> mapDroneRoutes = new HashMap<Drone, TreeMap<Date, Point>>();
	
	private InputStream defaultInputStream;
	
	public Dispatcher() {}
	
	public Drone initDrone(String id, URI droneIdURI) throws Exception {
		TreeMap<Date, Point> routeDrone = CSVUtil.loadDrone(Paths.get(droneIdURI));
		Drone drone = new Drone(id);
		mapDroneRoutes.put(drone, routeDrone);
		return drone;
	}
	
	public Entry<Date, Point> readNextRoute(Drone drone, Date current) {
		TreeMap<Date, Point> routeDrone = null;
		synchronized (mapDroneRoutes) {
			routeDrone = mapDroneRoutes.get(drone); 
		}
		Date ceilingKey = routeDrone.ceilingKey(current);
		Entry<Date, Point> returnValue = null;
		
		if(ceilingKey == null) {
			returnValue = routeDrone.firstEntry();
		}
		returnValue = routeDrone.tailMap(ceilingKey, false).firstEntry();
		return returnValue;
	}

	public void setDefaultInputStream(InputStream inputStream) throws Exception {
		this.defaultInputStream = inputStream;
	}
	
	@Override
	public void run() {
		super.run();
		try {
			listenDefaultInputStream();
		} catch (Exception e) {
			// do nothing
		}
	}
	
	@SuppressWarnings("resource")
	private void listenDefaultInputStream() throws Exception {
		Scanner s = new Scanner(defaultInputStream);
		String currentLine = "";
		while(true) {
			currentLine = s.nextLine();
			if("START".equals(currentLine)) {
				logger.debug("Start command");
				startAllDrones(Constants.eightAM);
				
			}else if("SHUTDOWN".equals(currentLine)){
				logger.debug("Shutdown command");
				stopAllDrones(Constants.eightTenAM);
			
			}else if("TEST".equals(currentLine)) {
				logger.debug("Debug simulation");
				Date eightAM = Constants.eightAM; 
				Drone d1 = mapDroneRoutes.keySet().iterator().next();
				Entry<Date, Point> entry = readNextRoute(d1, eightAM);
				logger.debug("Entry: " + entry);
				Entry<Date, Point> entry2 = readNextRoute(d1, entry.getKey());
				logger.debug("Entry2: " + entry2);
				
				List<com.github.davidmoten.rtree.Entry<String, Point>> intersectionWithTubeStation = Application.map.searchTube(entry.getValue(), entry2.getValue(), Constants.NEARBY_STATION_IN_METER);
				logger.debug("Intersection with tube station: " + intersectionWithTubeStation);
				
				Entry<Date, Point> entry3 = readNextRoute(d1, entry2.getKey());
				logger.debug("Entry3: " + entry3);
				
				intersectionWithTubeStation = Application.map.searchTube(entry2.getValue(), entry3.getValue(), Constants.NEARBY_STATION_IN_METER);
				logger.debug("Intersection with tube station: " + intersectionWithTubeStation);
				
				continue;
			}
		}
	}
	
	private void startAllDrones(Date time) {
		for (Drone drone : mapDroneRoutes.keySet()) {
			drone.setStartDate(time);
		}
	}
	private void stopAllDrones(Date time) {
		for (Drone drone : mapDroneRoutes.keySet()) {
			drone.setStopDate(time);
		}
	}
}
