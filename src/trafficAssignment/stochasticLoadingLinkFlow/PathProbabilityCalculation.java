/* --------------------------------------------------------------------
 * PathProbabilityCalculation.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and all Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 25.04.2016
 * 
 * Function:
 *           1.计算每条道路被选择的概率
 *           
 */
package trafficAssignment.stochasticLoadingLinkFlow;

import java.util.List;

import org.ujmp.core.Matrix;

public class PathProbabilityCalculation
{
	//~Variables
	//--------------------------------------------------------------------------
	private double[] pathProbability;
	private int pathSize;
	//~Methods
	//--------------------------------------------------------------------------
	public PathProbabilityCalculation(int pathSize)
	{
		//Probability
		this.pathSize = pathSize;
		pathProbability = new double[pathSize];
	}
	/**
	 * 计算每条道路被选择的概率
	 * @param pathSize
	 * @param theta
	 * @param travelTimes
	 * @return
	 */
	public void LogitModel(double theta, double[] pathTravelTimes)
	{
		//Exponential values 
		double sum=0;
		double[] expUtility = new double[pathSize];
		//double[] positiveExpUtility = new double[pathSize];
		for(int i=0;i<pathSize;i++)
		{
			expUtility[i]= Math.exp(-theta*(pathTravelTimes[i]));// get path travel time in hour
			//positiveExpUtility[i]= Math.exp(theta*pathTravelTimes[i]);
			sum+=expUtility[i];
//			System.out.println("pathTravelTimes[i]: "+i+"\t"+pathTravelTimes[i]);
//			System.out.println("exp utility function: "+i+"\t"+expUtility[i]);
		}
//		System.out.println("sum of exp utility function: "+sum);
		
		for(int i=0;i<pathSize;i++)
		{
			pathProbability[i]=0;
			if(sum!=0)
			pathProbability[i]=(expUtility[i])/(sum);
			//System.out.println("sum  of exp utility function: "+sum);
			//System.out.println("probability of paths: "+i+"\t"+pathProbability[i]);
		}
	}
	
	/**
	 * 计算每条路径被选择的概率
	 * @param network
	 * @param odDemand
	 * @param paths
	 * @param linkPathM
	 * @param lengthTimes
	 * @return
	 */
	public static double[] PathSizeLogitModel(Matrix linkPathM,int linkSize, int pathSize,List<Double> travelTimes)
	{
		//Step 1: find all paths - in the package "network.algorithms"
		
		//Step 2: build link-path incidence matrix
		// the value Delta_aj = 1 if  link a in on path j 
		// -> Could be done in the package "network.algorithms"
		
		//for each path
		//Step 3: Calculate the path length, L_j 
		// -> in the lengthTime[0]
		
		//for each link
		//Step 4.1: how many paths the link a is on? ->  N_a = sum (Delta_aj)
		//Step 4.2 the length of link a : l_j 
		int N_a[]=new int[linkSize];
		for(int j=0;j<linkSize;j++)
		{
			N_a[j]=0;
		}
		for(int i=0;i<pathSize;i++)
		{
			for(int j=0;j<linkSize;j++)
			{
				N_a[j] += linkPathM.getAsInt(j,i);
			}	
		}
	
		// Step 5: probability calculation.  
		//V_i = path utility function = travel time = free flow travel time + delay
		// 5.1 PS_j = sum (l_j/ L_j / N_a)
		//5.2 P(i|C) = e^(V_i + ln PS_i)/sum_j in C (e^(V_j + ln PS_j))
		double[] pathProbability = new double[pathSize];
		
		return pathProbability;
	}

	/****************************************************************
	 *******************Getter and Setter****************************
	 ****************************************************************/
	/**
	 * @return pathProbability
	 */
	public double[] getPathProbability()
	{
		return pathProbability;
	}
}
