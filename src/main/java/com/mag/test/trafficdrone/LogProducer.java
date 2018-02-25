package com.mag.test.trafficdrone;

import java.util.Date;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.mag.test.trafficdrone.util.Constants;

/**
 * Produce the output log
 */
public class LogProducer {

	private static Logger logger = LoggerFactory.getLogger(LogProducer.class.getSimpleName());

	private ListMultimap<Date, String> logMultimap;
	
	public LogProducer() {
		logMultimap = MultimapBuilder.treeKeys().arrayListValues().build();
	}

	public void startLogThread() {
		Thread logThread = new Thread(() -> {
			do {
				try {
					Thread.sleep(10);
				}catch(InterruptedException ie) {
					// do nothing
				}
				
				synchronized(logMultimap) {
					Optional<Date> firstKey = logMultimap.keys().stream().sorted().findFirst();
					if(firstKey.isPresent()) {
						logMultimap.get(firstKey.get()).forEach( message -> {
							logger.info(message);
						});
						logMultimap.removeAll(firstKey.get());
					}
				}
			}while(true);
		});
		logThread.start();
	}

	public void addToLog(String id, Date approximateTime, Double speed, TrafficCondition trafficCondition) {
		String message = String.format("%s, %s,  %s, %s", id, Constants.formatter.format(approximateTime), String.format(Locale.US, "%.2f", speed), trafficCondition);
		synchronized(logMultimap) {
			logMultimap.put(approximateTime, message);
		}
		logger.debug(String.format("Added message [%s] to the log", message));

	}
}
