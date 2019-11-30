package service.utility;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Responsible for managing relationship between actual time and logical time 
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class Time {
	/**
	 * Scaling time with this parameter
	 */
	//public static int scale=100;   
	
	public static AtomicInteger steps=new AtomicInteger(0);
}
