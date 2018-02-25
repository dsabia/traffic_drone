package com.mag.test.trafficdrone;

import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.davidmoten.rtree.geometry.Point;
import com.google.common.collect.Queues;
import com.mag.test.trafficdrone.util.Constants;
import com.mag.test.trafficdrone.util.DistanceCalculatorUtil;

public class Drone extends Thread {

	private static Logger logger = LoggerFactory.getLogger(Drone.class.getSimpleName());

	private final String id;
	
	private ArrayBlockingQueue<Entry<Date, Point>> queue = null; 
	private volatile Date startDate;
	private volatile Date stopDate;
	boolean running;
	
	public Drone(String id) {
		this.id = id;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	public void setStopDate(Date stopDate) {
		this.stopDate = stopDate;
	}
	private Date getStopDate() {
		return this.stopDate;
	}
	

	@Override
	public void run() {
		queue = Queues.newArrayBlockingQueue(Constants.DRONE_QUEUE_SIZE);
		ThreadGroup threadGroup = new ThreadGroup("Execution of Drone"); 
		
		Thread routeThread = new Thread(threadGroup, () -> {
			Date currentDate = startDate;
			do {
				try {
					Entry<Date, Point> entry = Application.dispatcher.readNextRoute(this, currentDate);
					if(entry != null) {
						queue.add(entry);
						currentDate = entry.getKey();
						logger.debug(String.format("Added entry %s in queue drone %s", entry, id));
					}
				}catch(IllegalStateException ise) {
					// do nothing
				}
			}while(getStopDate() == null || currentDate.before(getStopDate()));
		});
		
		Thread pathThread  = new Thread(threadGroup, () -> {
			Entry<Date, Point> previousEntry = null;
			Entry<Date, Point> currentEntry = null;
			do {
					
				// read one item from the queue 
				do{
					try {
						currentEntry = queue.remove();
						logger.debug(String.format("[%s] Current entry : %s", id, currentEntry));
					}catch(NoSuchElementException nsee) {
						// do nothing
					}
				}while(currentEntry == null);
				
				if(previousEntry == null) {
					previousEntry = currentEntry;
					currentEntry = null;
					logger.debug(String.format("[%s] Set previous entry: %s", id, previousEntry));
					continue;

				}else {
					logger.debug(String.format("[%s] Execution of Step: %s - %s", id, currentEntry, previousEntry));

					// calculate speed
					Long time = (currentEntry.getKey().getTime() - previousEntry.getKey().getTime()) / 1000;
					Double distance = DistanceCalculatorUtil.distanceInMetres(previousEntry.getValue(), currentEntry.getValue());
					Double speedMeterSec = distance/(time);
					logger.debug("Speed calculated (m/s): " + speedMeterSec);
					
					// search tube stations
					List<com.github.davidmoten.rtree.Entry<String, Point>> intersectionWithTubeStation = Application.map.searchTube(previousEntry.getValue(), currentEntry.getValue(), Constants.NEARBY_STATION_IN_METER);
					
					// add to Producer log
					addToProducerLogWithApproximateTime(intersectionWithTubeStation, previousEntry, speedMeterSec);
					
					// reset for the while the logic
					previousEntry = currentEntry;
					currentEntry = null;
				}
				
			}while( getStopDate() == null  ||  (getStopDate() != null && previousEntry.getKey().before(getStopDate())) );
		});
		
		while(startDate == null) {
			// wait
		}
		routeThread.start();
		pathThread.start();
	}

	private void addToProducerLogWithApproximateTime(final List<com.github.davidmoten.rtree.Entry<String, Point>> intersectionWithTubeStation, final Entry<Date, Point> previousEntry, final Double speed) {
		logger.debug(String.format("Found %d station.", intersectionWithTubeStation.size()));
		
		// for each station calculate the approximate time
		// and add to producer log
		intersectionWithTubeStation.forEach( currentTube -> {
			Double intermediateDistance = DistanceCalculatorUtil.distanceInMetres(previousEntry.getValue(), currentTube.geometry());
			Double intermediateTime = new Double(speed / intermediateDistance); 
			Date approximateTime = new Date(previousEntry.getKey().getTime() + intermediateTime.longValue());
			
			Application.logProducer.addToLog(id, approximateTime, speed, TrafficCondition.getRandomValue());
		});
	}
}
