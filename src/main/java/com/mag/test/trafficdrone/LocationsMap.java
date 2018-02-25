package com.mag.test.trafficdrone;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.google.common.collect.Lists;
import com.mag.test.trafficdrone.util.CSVUtil;
import com.mag.test.trafficdrone.util.Constants;

public class LocationsMap {

	private static Logger logger = LoggerFactory.getLogger(LocationsMap.class.getSimpleName());

	private String location;

	private RTree<String, Point> mapTubeStation;
	
	public LocationsMap(String location) {
		this.location = location;
	}

	public void init(URI tubeCsvURI) throws Exception {
		mapTubeStation = CSVUtil.loadLocations(Paths.get(tubeCsvURI));
	}

	public List<Entry<String, Point>> searchTube(Point initRoute, Point endRoute, int meter) {
		Rectangle rectangle = buildRectange(initRoute, endRoute, meter);
		
		Iterable<Entry<String, Point>> it = mapTubeStation.search(rectangle).toBlocking().toIterable();
		
		List<Entry<String, Point>> returnList = Lists.newLinkedList();
		it.forEach(x -> {
			returnList.add(x);
			logger.debug(String.format("Added the entry [%s] to the result list", x));
		});
		
		return Collections.unmodifiableList(returnList);
	}
	
	
	/**
	 * Rectangle creation with precondition
	 * @see com.github.davidmoten.rtree.geometry.RectangleImpl.RectangleImpl(float, float, float, float)
	 * @param initRoute
	 * @param endRoute
	 * @param meter
	 * @return
	 */
	private Rectangle buildRectange(Point initRoute, Point endRoute, int meter) {
		double latitudeIncrement = latitudeFromMeter(meter);
		double longitudeIncrement = longitudeFromMeter(meter);
		
		Rectangle rectangle = null;
		try {
			rectangle = Geometries.rectangle(initRoute.x() - longitudeIncrement, 
											 initRoute.y() - latitudeIncrement, 
											 endRoute.x() + longitudeIncrement, 
											 endRoute.y() + longitudeIncrement);
		}catch(Exception e) {
			try{
				rectangle = Geometries.rectangle(endRoute.x() - longitudeIncrement, 
												 initRoute.y() - latitudeIncrement, 
												 initRoute.x() + longitudeIncrement, 
												 endRoute.y() + longitudeIncrement);

			}catch(Exception ee) {
				try{
					rectangle = Geometries.rectangle(initRoute.x() - longitudeIncrement, 
													 endRoute.y() - latitudeIncrement, 
													 endRoute.x() + longitudeIncrement, 
													 initRoute.y() + longitudeIncrement);
				}catch(Exception eee) {
					rectangle = Geometries.rectangle(endRoute.x() - longitudeIncrement, 
													 endRoute.y() - latitudeIncrement, 
													 initRoute.x() + longitudeIncrement, 
													 initRoute.y() + longitudeIncrement);
				}	
			}
		}
		return rectangle;
	}
	
	/**
	 * @see https://knowledge.safe.com/articles/725/calculating-accurate-length-in-meters-for-lat-long.html
	 * @param meter
	 * @return
	 */
	private double longitudeFromMeter(int meter) {
		return Constants.METER_FOR_LNG * (meter/1000d);
	}
	
	/**
	 * @see https://knowledge.safe.com/articles/725/calculating-accurate-length-in-meters-for-lat-long.html
	 * @param meter
	 * @return
	 */
	private double latitudeFromMeter(int meter) {
		return Constants.METER_FOR_LAT * (meter/1000d);
	}

	public String getLocation() {
		return location;
	}
}
