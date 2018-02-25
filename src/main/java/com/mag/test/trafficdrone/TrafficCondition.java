package com.mag.test.trafficdrone;

import java.util.Random;

public enum TrafficCondition{
	HEAVY,
	LIGHT,
	MODERATE;
	
	private final static Random random = new Random();
	public static TrafficCondition getRandomValue() {
		return values()[random.nextInt(3)];
	}
}
