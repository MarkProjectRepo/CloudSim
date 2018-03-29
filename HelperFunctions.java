package org.cloudbus.cloudsim.examples;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.Random;

public class HelperFunctions {
	
	private NormalDistribution nd;
	private float lowBound;
	private float highBound;
	
	public HelperFunctions(float lowBound, float highBound){
		this.lowBound = lowBound;
		this.highBound = highBound;
		float mean = (lowBound + highBound)/2;
		//standard deviation is half of the range
		float sd = (highBound - lowBound)/2;
		nd = new NormalDistribution(mean, sd);
	}
	
	/**
	 * Returns a random number, which is normally distributed
	 * @param lowerBound The lower bound for the numbers being generated
	 * @param highBound The upper bound for the numbers being generated
	 * @return The next normally distributed random number.
	 */
	public float NextPseudoRandomND(){
		float ret = (float)nd.sample();
		
		if(ret < lowBound)
			return lowBound;
		if(ret > highBound)
			return highBound;
		
		return ret;
	}
	
	/**
	 * Generates a random number within a range.
	 * @param lower the lower bound.
	 * @param higher the higher bound.
	 * @return a random number in between the bounds.
	 */
	public static float Random(float lower, float higher){
		float range = higher - lower;
		float rand = (float) (Math.random() * range) + lower;
		return rand;
	}

}
