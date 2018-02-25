package com.mag.test.trafficdrone.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;

public class CSVUtil {
	
	static Logger logger = LoggerFactory.getLogger(CSVUtil.class.getSimpleName());
	
	public static RTree<String,Point> loadLocations(Path csv) throws Exception{
		RTree<String,Point> tree = RTree.create();
		
		List<String> lines = Files.readAllLines(csv);
		
		for (String line : lines) {
			String[] lineSplitted = line.split(",");
			String name = lineSplitted[0];
			Double lat = Double.parseDouble(lineSplitted[1]);
			Double lng = Double.parseDouble(lineSplitted[2]);
			tree = tree.add(name, Geometries.point(lat, lng));
			logger.debug(String.format("Added tube station %s [%.2f%.2f]",name, Double.valueOf(lat), Double.valueOf(lng) ));
		}
		
		if(Constants.DEBUG) {
			// print out the diagram of the RTree
			tree.visualize(600,600).save("target\" + File.separator + \"mytree.png");
		}
		
		return tree;
	}
	
	public static TreeMap<Date, Point> loadDrone(Path csv) throws Exception{
		TreeMap<Date, Point> treeMap = new TreeMap<>();
		
		Files.readAllLines(csv).forEach( line -> {
			String[] lineSplitted = line.split(",");
			Double lat = Double.parseDouble(lineSplitted[1].replaceAll("\"", ""));
			Double lng = Double.parseDouble(lineSplitted[2].replaceAll("\"", ""));
			Date time = null;
			try {
				time = Constants.formatter.parse(lineSplitted[3].replaceAll("\"", ""));
			} catch (ParseException e) {
				// do nothing
			}
			treeMap.put(time, Geometries.point(lat, lng));
			logger.debug(String.format("Added route for drone id %s [%.2f%.2f] at %s", lineSplitted[0], Double.valueOf(lat), Double.valueOf(lng), Constants.formatter.format(time)));
		});
		return treeMap;
	}
}
