/* --------------------------------------------------------------------
 * SignalTiming.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 
 *
 *
 */
package signalOptimization;

import java.util.ArrayList;
import java.util.List;

import org.ujmp.core.Matrix;

import network.Link;

public class SignalTiming
{
	//~Variables
	//--------------------------------------------------------------------------	
	protected int [][][] permittedLanes     = null;
	protected int [][][] assignedFlowQ      = null;  //
	protected int [][] startOfGreenMovement = null;  //绿灯开始时间
	protected int [][] durationMovement     = null;  //Movement持续时间
	protected int [][] durationMovementLane = null;  //lanebased Movement持续时间
	protected int [][] durationLane         = null;
	protected int cycleLength = 0;                   //信号灯周期
	
	protected int[][] laneAssignedFlow   = null;     //共有，放父类
	protected int[][] laneSaturationFlow = null;     //共有，放父类
	
	protected static final int CYCLETIMEMIN = 60;    // 最小周期时间  //共有，放父类
	protected static final int CYCLETIMEMAX = 100;   // 最大周期时间  //共有，放父类
	protected static final int GREENDURATIONMIN = 5; // 最小绿灯时间  //共有，放父类
	protected static final int CLEARANCETIME=4;      // 清空时间            //共有，放父类
	
	protected int armNumber = 0;                     // Arm的个数           //共有，放父类
	protected List<Integer> armLanesNum    = null;   // Arm里的车道数 //共有，放父类
	protected List<Integer> armDirNum      = null;   // Arm里的转向数 //共有，放父类
	protected List<ArrayList<Link>> armDir = null;   // Arm里的转向      //共有，放父类
	protected double[][] demand = null;              // Demand       //共有，放父类
	
	//~Methods
	//--------------------------------------------------------------------------
	public SignalTiming()
	{
		
	}

	public void solveMe(Matrix foesMatrix, List<ArrayList<String>> markerList,
			double[][] demand, int armNumber, List<ArrayList<Link>> armDir,
			List<Integer> armLanesNum, List<Integer> armDirNum)
	{

	}
	
	public void solveMe(int armNumber,double[][] demand, int [][] dirOutIndex,
						Matrix foesMatrix,List<ArrayList<Link>> armDir,
						List<Integer> armLanesNum,List<Integer> armDirNum,
						List<Integer> outLanesNum,List<Link> extLinks)
	{

	}
	
	public void assignedFlowCalculation(int armIndex, double demand[][])
	{
		
	}
	
	public void laneAssignedFlowCalculation()
	{
		
	}
	
	public void laneSaturationFlow()
	{
		
	}

	public void afterGettingCommonCycle(Matrix foesMatrix, List<ArrayList<String>> markerList,
			double[][] demand, int armNumber, List<ArrayList<Link>> armDir,
			List<Integer> armLanesNum, List<Integer> armDirNum, int commonCycle)
	{
		
	}
	
	/****************************************************************
	 *******************Getter and Setter****************************
	 ****************************************************************/
	/**
	 * @return permittedDelta
	 */
	public int[][][] getPermittedDelta()
	{
		return permittedLanes;
	}

	/**
	 * @return assignedFlow
	 */
	public int [][][] getAssignedFlow()
	{
		return assignedFlowQ;
	}

	/**
	 * @return cycleLength
	 */
	public int getCycleLengthEpslon()
	{
		return cycleLength;
	}

	/**
	 * @return startOfGreenMovement
	 */
	public int[][] getStartOfGreenMovement()
	{
		return startOfGreenMovement;
	}

	/**
	 * @return durationMovement
	 */
	public int[][] getDurationMovement()
	{
		return durationMovement;
	}

	public int[][] getDurationLane()
	{
		return durationLane;
	}
	/**
	 * @param permittedLanes 
	 *		要设置的 permittedLanes
	 */
	public void setPermittedLanes(int[][][] permittedLanes)
	{
		this.permittedLanes = permittedLanes;
	}

	/**
	 * @param assignedFlowQ 
	 *		要设置的 assignedFlowQ
	 */
	public void setAssignedFlowQ(int[][][] assignedFlowQ)
	{
		this.assignedFlowQ = assignedFlowQ;
	}

	/**
	 * @param cycleLength 
	 *		要设置的 cycleLength
	 */
	public void setCycleLength(int cycleLength)
	{
		this.cycleLength = cycleLength;
	}

	/**
	 * @param startOfGreenMovement 
	 *		要设置的 startOfGreenMovement
	 */
	public void setStartOfGreenMovement(int[][] startOfGreenMovement)
	{
		this.startOfGreenMovement = startOfGreenMovement;
	}

	/**
	 * @param durationMovement 
	 *		要设置的 durationMovement
	 */
	public void setDurationMovement(int[][] durationMovement)
	{
		this.durationMovement = durationMovement;
	}

	/**
	 * @param durationMovementLane 
	 *		要设置的 durationMovementLane
	 */
	public void setDurationLane(int[][] durationMovementLane)
	{
		this.durationMovementLane = durationMovementLane;
	}


	/**
	 * @return laneAssignedFlow
	 */
	public int[][] getLaneAssignedFlow()
	{
		return laneAssignedFlow;
	}


	/**
	 * @param laneAssignedFlow 
	 *		要设置的 laneAssignedFlow
	 */
	public void setLaneAssignedFlow(int[][] laneAssignedFlow)
	{
		this.laneAssignedFlow = laneAssignedFlow;
	}


	/**
	 * @return laneSaturationFlow
	 */
	public int[][] getLaneSaturationFlow()
	{
		return laneSaturationFlow;
	}


	/**
	 * @param laneSaturationFlow 
	 *		要设置的 laneSaturationFlow
	 */
	public void setLaneSaturationFlow(int[][] laneSaturationFlow)
	{
		this.laneSaturationFlow = laneSaturationFlow;
	}


	/**
	 * @return cycletimemin
	 */
	public static int getCycletimemin()
	{
		return CYCLETIMEMIN;
	}


	/**
	 * @return cycletimemax
	 */
	public static int getCycletimemax()
	{
		return CYCLETIMEMAX;
	}


	/**
	 * @return greendurationmin
	 */
	public static int getGreendurationmin()
	{
		return GREENDURATIONMIN;
	}


	/**
	 * @return clearancetime
	 */
	public static int getClearancetime()
	{
		return CLEARANCETIME;
	}


	/**
	 * @return armNumber
	 */
	public int getArmNumber()
	{
		return armNumber;
	}


	/**
	 * @param armNumber 
	 *		要设置的 armNumber
	 */
	public void setArmNumber(int armNumber)
	{
		this.armNumber = armNumber;
	}


	/**
	 * @return armLanesNum
	 */
	public List<Integer> getArmLanesNum()
	{
		return armLanesNum;
	}


	/**
	 * @param armLanesNum 
	 *		要设置的 armLanesNum
	 */
	public void setArmLanesNum(List<Integer> armLanesNum)
	{
		this.armLanesNum = armLanesNum;
	}


	/**
	 * @return armDirNum
	 */
	public List<Integer> getArmDirNum()
	{
		return armDirNum;
	}


	/**
	 * @param armDirNum 
	 *		要设置的 armDirNum
	 */
	public void setArmDirNum(List<Integer> armDirNum)
	{
		this.armDirNum = armDirNum;
	}


	/**
	 * @return armDir
	 */
	public List<ArrayList<Link>> getArmDir()
	{
		return armDir;
	}


	/**
	 * @param armDir 
	 *		要设置的 armDir
	 */
	public void setArmDir(List<ArrayList<Link>> armDir)
	{
		this.armDir = armDir;
	}


	/**
	 * @return demand
	 */
	public double[][] getDemand()
	{
		return demand;
	}


	/**
	 * @param demand 
	 *		要设置的 demand
	 */
	public void setDemand(double[][] demand)
	{
		this.demand = demand;
	}

	/**
	 * @return durationMovementLane
	 */
//	public int [][] getDurationMovement()
//	{
//		return durationMovementLane;
//	}
}
