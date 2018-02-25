package com.mag.test.trafficdrone;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

	private static Logger logger = LoggerFactory.getLogger(Application.class.getSimpleName());
	
	public static LocationsMap map;
	public static Dispatcher dispatcher;
	public static LogProducer logProducer;
	
	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		
		Application application = new Application();
		application.initialize();
		
		// only for the simulation
		application.simulationSetting(args);
		
		application.start();
	}

	/**
	 * Initialization of the application
	 * @throws Exception
	 */
	private void initialize() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		URI tubeCsvURI = classLoader.getResource("location" + File.separator + "tube.csv").toURI();
		URI drone5937URI = classLoader.getResource("drone" + File.separator + "5937.csv").toURI();
		URI drone6043URI = classLoader.getResource("drone" + File.separator + "6043.csv").toURI();
		
		map = new LocationsMap("London");
		map.init(tubeCsvURI);
		logger.debug(String.format("Initialization of Tube Stations of %s completed", map.getLocation()));
		
		logProducer = new LogProducer();
		logger.debug("Initialization of Tube Station completed");
		
		dispatcher = new Dispatcher();
		dispatcher.initDrone("5937", drone5937URI).start();
		logger.debug("Initialization Drone 5937");

		dispatcher.initDrone("6043", drone6043URI).start();
		logger.debug("Initialization Drone 6043");
		
		dispatcher.setDefaultInputStream(System.in);
		logger.debug("Dispatcher is listening on System.in");
	}
	
	/**
	 * Simulate the event to send two signal to the command line 
	 * @param args 
	 * @throws Exception
	 */  
	private void simulationSetting(String[] args) throws Exception {
		String command = Arrays.asList(args).stream().collect(Collectors.joining("\n"));
		if(args.length == 0) {
			command = "START\n" +
					 "SHUTDOWN\n";
		}
		
		InputStream testInput = new ByteArrayInputStream(command.getBytes("UTF-8"));
		
		dispatcher.setDefaultInputStream(testInput);
		logger.debug("Listening input stream changed for the simulation");
	}

	private void start() {
		logProducer.startLogThread();
		dispatcher.start();
		logger.debug("Commands available [START, SHUTDOWN, TEST]");
	}
}
