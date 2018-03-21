/* --------------------------------------------------------------------
 * SignalTiming.java
 * -------------------------------------------------------------------- 
 * Copyright (C) 2015-2017, by Huijun Liu and all Contributors.
 *
 * Original Author: Huijun Liu 
 * Contributor(s):  Qinrui Tang
 *
 * Last Change Date: 05.05.2017
 * 
 * Function: 
 *           1.计算phase的flow factor 
 *           2.计算周期长度 
 *           3.计算绿灯时间
 * 
 */
package signalOptimization;

import java.util.ArrayList;
import java.util.List;
import org.ujmp.core.Matrix;
import ilog.cplex.IloCplex;
import network.Link;

public class StagebasedSignalTiming extends SignalTiming
{
	// ~Variables
	// --------------------------------------------------------------------------
    // private int [][][] permittedLanes = null;      //已继承，注释掉
	// private int [][][] assignedFlowQ = null;       //已继承，注释掉
	// private int [][] startOfGreenMovement = null;  //已继承，注释掉
    // private int [][] durationMovement = null;      //已继承，注释掉
	// private int [][] durationLane = null;          //已继承，注释掉
	// private int cycleLength = 0;                   //已继承，注释掉
	// durationLane需要算！！！
	
	// private int[][] laneAssignedFlow   = null;
	// private int[][] laneSaturationFlow = null;
	
	// private static final int CYCLETIMEMIN  = 60;       // 最小周期时间  //共有，放父类
	// private static final int CYCLETIMEMAX  = 100;      // 最大周期时间  //共有，放父类
	// private static final int GREENDURATIONMIN = 6;     // 最小绿灯时间  //共有，放父类
	// private static final int CLEARANCETIME = 4;        // 清空时间      //共有，放父类
	
	// used in different functions, set as a private attribute 
	// private int armNumber = 0;                       // Arm的个数     //共有，放父类
	// private List<Integer> armLanesNum    = null;     // Arm里的车道数 //共有，放父类
	// private List<Integer> armDirNum      = null;     // Arm里的转向数 //共有，放父类
	// private List<ArrayList<Link>> armDir = null;     // Arm里的转向   //共有，放父类
	// private double[][] demand;                       // Demand        //共有，放父类
	
	// 本类的私有变量
	//=============================================================
	private List<ArrayList<Integer>> combinedPhaseGroup     = null;
	private List<ArrayList<Integer>> combinedPhaseList      = null;
	private List<ArrayList<Integer>> sharedLaneBasedDirList = null;
	private List<ArrayList<String>> fixedPhaseMatrix        = null; // 经过共享绿灯时间判断和整理过的PhaseMatrix
	
	private double B = 0;
	private double[] b = null;
	private double[] bCombined       = null;
	private int[] phaseGreen         = null;
	private int[] combinedPhaseGreen = null;
	private int sumClearanceTime = 0;
	
	private IloCplex cplex = null;
	
	// ~Methods
	// --------------------------------------------------------------------------
	public StagebasedSignalTiming(IloCplex cplex)
	{
		this.cplex = cplex;
	}

	@Override
	public void solveMe(Matrix foesMatrix, List<ArrayList<String>> markerList,
						double[][] demand, int armNumber, List<ArrayList<Link>> armDir,
						List<Integer> armLanesNum, List<Integer> armDirNum)
	{
		this.armNumber   = armNumber;      // Arm 的个数
		this.armLanesNum = armLanesNum;    // Arm 的 lane 的个数
		this.armDirNum   = armDirNum;      // Arm 的 Dir  的个数
		this.armDir = armDir;              // Arm 的 Dir  的List
		this.demand = demand;              // Demand 
		
		
		// ------------------------------------------------- //
		//        fromLaneID    toLaneId    Direction
		// Arm 1      0             0           r
 		//            0             0           s
		//            1             1           s
		//            1             1           l
		// Arm 2      .             .           .
		// Arm 3      .             .           .
		// Arm 4      .             .           .
		// ------------------------------------------------- //
		List<ArrayList<String>> markerListTemp = new ArrayList<ArrayList<String>>(markerList);   // 
		
		// ------------------------------------------------- //
		// 1. 存储 junc 里面 所有转向的 saturation flow 
		// ------------------------------------------------- //
		// List<ArrayList<Double>> juncSaturFlows = new ArrayList<ArrayList<Double>>();
//		List<Double> juncSaturFlowsDirLane = new ArrayList<Double>();
		
		// ------------------------------------------------- //
		// 2. 存储 share line 的 dir 的 Index ->对整个路口有效
		// ------------------------------------------------- //
		List<ArrayList<Integer>> sharedLineDirIndex = new ArrayList<ArrayList<Integer>>();
		
		// ------------------------------------------------- //
		// 3. 存储 junc 里面 所有转向的 saturation flow 
		// ------------------------------------------------- //		
		permittedLanes = new int[armNumber][][];
		assignedFlowQ = new int[armNumber][][];
		
		// ********************************************************* //
		// 1. 填充：
		//    -- permittedLanes[i][j][k]
		//    -- assignedFlowQ[i][j][k]
		//    -- laneSaturationOfFlow[i][j]
		//    -- List<Double> juncSaturFlowsDirLane
		//    -- List<ArrayList<Integer>> sharedLineDirIndex
		// ********************************************************* //
		
		int MarkerListIndex = -1;              // 转向的指针
		int junDirIndex = 0;                   // 转向的指针
		for (int i = 0; i < armNumber; i++)
		{
			int armDirC  = armDirNum.get(i);   // 当前 Arm 的转向个数
			int armLaneC = armLanesNum.get(i); // 当前 Arm 的车道个数
			//System.out.println("armLaneC: "+armLaneC);
			ArrayList<Link> armDirLink = armDir.get(i);
			if(armDirLink.size() != armDirC)
			{
				System.out.println("## 程序有错误：转向个数与转向边个数不一致！");
				System.exit(0);
			}
			
			permittedLanes[i] = new int[armDirC][armLaneC];
			//System.out.println("permittedLanes[i][0][0]: "+permittedLanes[i][0][0]);
			
			assignedFlowQ[i]  = new int[armDirC][armLaneC];
			
			// ================================================= //
			// 1.1  处理shared lane的情况
			// ================================================= //
			@SuppressWarnings("unused")
			int armDirCSharedLane = 0;                                   // 转向的个数，按lane算
			ArrayList<Integer> armDirLaneC = new ArrayList<Integer>();   // 每个转向的lane个数
			@SuppressWarnings("unused")
			ArrayList<Double> armSaturFlows = new ArrayList<Double>();   // 转向对应的saturation flow
			for(int j=0; j<armDirC; j++)
			{
				int currDirLaneCount = armDirLink.get(j).getLaneCount(); // 当前dir的车道数
				armDirCSharedLane += currDirLaneCount;   // 总转向的个数 r l l s r l l s r l l s r l l s
				armDirLaneC.add(currDirLaneCount);       // 记录当前的dir的车道数
				
			} // dir j
			// juncSaturFlows.add(armSaturFlows); // 
			
			// ================================================= //
			// 1.2  填充 permittedLanes[i][j][k]
			// ================================================= //
//			System.out.println("before getting commom cycle markerListTemp: "+markerListTemp.toString());
			
			for(int j=0; j<armDirC; j++)      // dir 转向循环
			{
				int dirLaneC = armDirLaneC.get(j);     // 当前 dir 的  lane 个数
				//System.out.println("当前 dir 的  lane 个数: "+dirLaneC);
				for(int dirL=0; dirL<dirLaneC; dirL++)
				{
					// 当前的from Lane
					MarkerListIndex++;
					//System.out.println("MarkerListIndex "+MarkerListIndex);
					//System.out.println("markerListTemp.get(index).get(0): "+markerListTemp.get(index).get(0));
					if(!markerListTemp.isEmpty())
					{
						int fromLane = Integer.parseInt(markerListTemp.get(MarkerListIndex).get(0));
						for(int k=0; k<armLaneC; k++) //车道循环
						{
							if(fromLane == k)      //转向在车道上,设为1
							{
								permittedLanes[i][j][k] = 1;
							}
						}
					}
				}
			}//Dir j
			
// markerListTemp.clear();   // 删除当前Arm的marker
			
//			System.out.println(" permittedLane in StagebasedSignalTiming.java: ");
//			for(int j=0; j<armDirC; j++)      // dir 转向循环
//			{
//				for(int k=0; k<armLaneC; k++) //车道循环
//				{
//					System.out.println(i+" j "+j+" k "+k +" permittedLane: "+permittedLanes[i][j][k]);
//				}
//			}

			// =============================================================== //
			// 1.3  Shared Lane 可以通过 permittedLanes 计算  Arm i
			// ============================================================== //
			//  permittedLanes[i]：
			//       lane0 lane1 lane2  sum
			//  dir0   1     0     0     1
			//  dir1   0     1     1     2 
			//  dir2   0     0     1     1
			//  sum    1     1     2
			//  sharedLineDirIndex : 存的是共享车道的dir的累积index
			//         -     -    1,2
			// ------------------------------------------------------------- // 
//			ArrayList<Integer> sharedLaneValue = new ArrayList<Integer>();
					
			for(int k=0; k<armLaneC; k++)   // lane 车道循环
			{
				// Lane k 的转向
				int sum = 0;
				ArrayList<Integer> sharedLineDir = new ArrayList<Integer>();  // 
				
				for(int j=0; j<armDirC; j++)     // dir 转向循环
				{
					//junDirIndex ++; // dir index +1
					if(permittedLanes[i][j][k] == 1)
					{
						sum += 1;					
						sharedLineDir.add(junDirIndex+j);
						//sharedLineDir.add(j);
					}
				} // dir j
//				sharedLaneValue.add(sum);
				
				if(sum > 1) //有共享车道
				{
					sharedLineDirIndex.add(sharedLineDir); //记录lane k上的共享车道的dir的index
				}
			} // Lane k
			
			for(int j=0; j<armDirC; j++)     // dir 转向循环
			{
				junDirIndex++;
			}
			
//			for(int k=0; k<sharedLaneValue.size(); k++)   // 共享dir的 list 内循环
//			{
//				System.out.println("sharedLane 的 dir数量: "+sharedLaneValue.get(k));
//			}
			
//			for(int k=0; k<sharedLineDirIndex.size(); k++)   // 共享dir的 list 内循环
//			{
//				System.out.println(" lane上的共享车道的dir的index "+sharedLineDirIndex.get(k).toString());
//			}
			// ============================================================== //
			// 1.4  计算 assignedFlowQ                  Arm i
			// ============================================================== //
			assignedFlowCalculation(i,demand);
		
		} // Arm i
		
		laneAssignedFlowCalculation();
		laneSaturationFlow();
//		System.out.println("markerListTemp: ");
//		System.out.println(markerListTemp.toString());
		markerListTemp.clear();   // 删除当的marker
		// ********************************************************* //
		// 2. 使用最大团和CPLEX生成Phase Matrix
		// ********************************************************* //
		// -------------------------------------------------
		// | 1 | 1 | 0 | 0 | 0 | 0 | 1 | 1 | 0 | 0 | 0 | 0 |  phase0
		// -------------------------------------------------
		PhaseGeneration phaseGen = new PhaseGeneration(cplex);         // 产生 PhaseMatrix 的类
		phaseGen.phaseGeneration(sharedLineDirIndex, foesMatrix); // 产生 PhaseMatrix
		List<ArrayList<String>> phaseMatrix = null;               // 存储PhaseMatrix
		phaseMatrix = new ArrayList<ArrayList<String>>(phaseGen.getPhaseMatrix()); 
		int[][] adjacentMatrix = phaseGen.getAdjacentMatrix();
//		System.out.println("original phase:");
//		for(int i=0;i<phaseMatrix.size();i++)
//			System.out.println(phaseMatrix.get(i).toString());
		
		
		// ********************************************************* //
		// 3. 根据生成的Phase Matrix计算sumClearanceTime
		// ********************************************************* //
		//clearanceTime      = 4;		// 定义清空时间为4
		int clearanceTimeCount = phaseMatrix.size(); 							  // 清空时间数量	
		sumClearanceTime = CLEARANCETIME*clearanceTimeCount;  // 清空时间和
		
//		System.out.println("clearanceTimeCount: "+clearanceTimeCount);
//		System.exit(0);
		
		// ********************************************************* //
		// 4. 找combinedPhase 并建立fixedPhaseMatrix
		// ********************************************************* //
		// ## combinedPhaseGroup 存combinedPhaseList的index. combinedPhaseGroup的index 为A,B 
		//   ------------      ----------      
		// A | I   | II |      |   I    |      ---------
		// B | III | IV |  or  |   II   |  or  | null  |
		//   ------------      ----------      ---------
		// ## combinedPhaseList 存combinedPhase的index。 combinedPhaseList为 I, II， III, IV 等； combinedPhase的index为1， 2， 3 等
		//     ---------
		// I   | 1 | 2 |
		// II  | 2 | 3 |         ---------      
		// III | 4 | 5 |  or  I  | 1 | 2 |  or  ---------
		// IV  | 5 | 6 |      II | 3 | 4 |      | null  |
		//     ---------         ---------      ---------
		// ## sharedLaneBasedDirList 存combinedPhase的共享绿灯的位置(direction 以lane为单位)
		// ---------
		// |   5   |
		// | 3 | 6 |      ---------
		// |   7   |  or  | 2 | 5 |  or  ---------
		// | 2 | 7 |      | 3 | 6 |      | null  |
		// ---------      ---------      ---------
		// ## sharedLaneBasedDirIndex 存 共享的绿灯所在phase的以lane based dir的位置
		// ---------      ---------
		// | 3 | 6 |  or  | null  |
		// ---------      ---------
		// ## combinedPhasae 存共享绿灯的phase在phaseMatrix里的index
		// ---------      ---------
		// | 1 | 2 |  or  | null  |
		// ---------      ---------
//		List<ArrayList<Integer>> combinedPhaseGroup     = new ArrayList<ArrayList<Integer>>();
//		List<ArrayList<Integer>> combinedPhaseList      = new ArrayList<ArrayList<Integer>>();
//		List<ArrayList<Integer>> sharedLaneBasedDirList = new ArrayList<ArrayList<Integer>>();	
//		// 经过共享绿灯时间判断和整理过的PhaseMatrix
//		List<ArrayList<String>> fixedPhaseMatrix = new ArrayList<ArrayList<String>>();
		generateCombinePhaseDataStructure(adjacentMatrix,phaseMatrix);
		adjacentMatrix =null;
//		System.out.println("fixed phase matrix before common cycle:");
//		for(int temp=0; temp<fixedPhaseMatrix.size();temp++)
//		System.out.println(fixedPhaseMatrix.get(temp));		
//		System.exit(0);
		
		// ********************************************************* //
		// 5. 求 b[] 和  bCombined[]
		calculateRelavantRatio();		
		// 5.3 比较 b[] 与bCombined[] 的值，并计算B
		calculateSumOfRelevantRatio();
				
		// ********************************************************* //
		// 6. 求cycle 和 phase phase 
		// ********************************************************* //
		//6.1 计算common cycle length
		double originalB = B;		
		calculateCycleLength();	
		B =originalB;
		//6.2 计算duration
		
		// 释放内存
		combinedPhaseGroup=null;
		combinedPhaseList =null;
		sharedLaneBasedDirList =null;
		b=null;
		bCombined=null;
		phaseGreen=null;
		fixedPhaseMatrix = null;
		combinedPhaseGreen=null;	
//		laneAssignedFlow=null;
//		laneSaturationFlow=null;
//		permittedLanes = null;
//		assignedFlowQ =null;

	}
	
	@Override
	public void afterGettingCommonCycle(Matrix foesMatrix, List<ArrayList<String>> markerList,
//			Matrix phaseMatrixLaneBased, Matrix phaseDurationMatrix,
			double[][] demand, int armNumber, List<ArrayList<Link>> armDir,
			List<Integer> armLanesNum, List<Integer> armDirNum, int commonCycle)
	{
	
		this.armNumber   = armNumber;      // Arm 的个数
		this.armLanesNum = armLanesNum;    // Arm 的 lane 的个数
		this.armDirNum   = armDirNum;      // Arm 的 Dir  的个数
		this.armDir = armDir;              // Arm 的 Dir  的List
		this.demand = demand;              // Demand 
		
		
		// ------------------------------------------------- //
		//        fromLaneID    toLaneId    Direction
		// Arm 1      0             0           r
 		//            0             0           s
		//            1             1           s
		//            1             1           l
		// Arm 2      .             .           .
		// Arm 3      .             .           .
		// Arm 4      .             .           .
		// ------------------------------------------------- //
		List<ArrayList<String>> markerListTemp = new ArrayList<ArrayList<String>>(markerList);   // 
		
		// ------------------------------------------------- //
		// 2. 存储 share line 的 dir 的 Index ->对整个路口有效
		// ------------------------------------------------- //
		List<ArrayList<Integer>> sharedLineDirIndex = new ArrayList<ArrayList<Integer>>();
		
		// ------------------------------------------------- //
		// 3. 存储 junc 里面 所有转向的 saturation flow 
		// ------------------------------------------------- //
		
		permittedLanes = new int[armNumber][][];
		assignedFlowQ = new int[armNumber][][];
		
		// ********************************************************* //
		// 1. 填充：
		//    -- permittedLanes[i][j][k]
		//    -- assignedFlowQ[i][j][k]
		//    -- laneSaturationOfFlow[i][j]
		//    -- List<Double> juncSaturFlowsDirLane
		//    -- List<ArrayList<Integer>> sharedLineDirIndex
		// ********************************************************* //
		
		int MarkerListIndex = -1;                   // 转向的指针
		int junDirIndex = 0;           // 转向的指针
		for (int i = 0; i < armNumber; i++)
		{
			int armDirC  = armDirNum.get(i);   // 当前 Arm 的转向个数
			int armLaneC = armLanesNum.get(i); // 当前 Arm 的车道个数
			//System.out.println("armLaneC: "+armLaneC);
			ArrayList<Link> armDirLink = armDir.get(i);
			if(armDirLink.size() != armDirC)
			{
				System.out.println("## 程序有错误：转向个数与转向边个数不一致！");
				System.exit(0);
			}
			
			permittedLanes[i] = new int[armDirC][armLaneC];
			//System.out.println("permittedLanes[i][0][0]: "+permittedLanes[i][0][0]);
			
			assignedFlowQ[i]  = new int[armDirC][armLaneC];
			
			// ================================================= //
			// 1.1  处理shared lane的情况
			// ================================================= //
			@SuppressWarnings("unused")
			int armDirCSharedLane = 0;                                   // 转向的个数，按lane算
			ArrayList<Integer> armDirLaneC = new ArrayList<Integer>();   // 每个转向的lane个数
			@SuppressWarnings("unused")
			ArrayList<Double> armSaturFlows = new ArrayList<Double>();   // 转向对应的saturation flow
			for(int j=0; j<armDirC; j++)
			{
				int currDirLaneCount = armDirLink.get(j).getLaneCount(); // 当前dir的车道数
				armDirCSharedLane += currDirLaneCount;   // 总转向的个数 r l l s r l l s r l l s r l l s
				armDirLaneC.add(currDirLaneCount);       // 记录当前的dir的车道数
				
			} // dir j
			// juncSaturFlows.add(armSaturFlows); // 
			
			// ================================================= //
			// 1.2  填充 permittedLanes[i][j][k]
			// ================================================= //
//			System.out.println("before getting commom cycle markerListTemp: "+markerListTemp.toString());
			
			for(int j=0; j<armDirC; j++)      // dir 转向循环
			{
				int dirLaneC = armDirLaneC.get(j);     // 当前 dir 的  lane 个数
				//System.out.println("当前 dir 的  lane 个数: "+dirLaneC);
				for(int dirL=0; dirL<dirLaneC; dirL++)
				{
					// 当前的from Lane
					MarkerListIndex++;
					//System.out.println("MarkerListIndex "+MarkerListIndex);
					//System.out.println("markerListTemp.get(index).get(0): "+markerListTemp.get(index).get(0));
					if(!markerListTemp.isEmpty())
					{
						int fromLane = Integer.parseInt(markerListTemp.get(MarkerListIndex).get(0));
						for(int k=0; k<armLaneC; k++) //车道循环
						{
							if(fromLane == k)      //转向在车道上,设为1
							{
								permittedLanes[i][j][k] = 1;
							}
						}
					}
				}
			}//Dir j

			// =============================================================== //
			// 1.3  Shared Lane 可以通过 permittedLanes 计算  Arm i
			// ============================================================== //
			//  permittedLanes[i]：
			//       lane0 lane1 lane2  sum
			//  dir0   1     0     0     1
			//  dir1   0     1     1     2 
			//  dir2   0     0     1     1
			//  sum    1     1     2
			//  sharedLineDirIndex : 存的是共享车道的dir的累积index
			//         -     -    1,2
			// ------------------------------------------------------------- // 
//			ArrayList<Integer> sharedLaneValue = new ArrayList<Integer>();
					
			for(int k=0; k<armLaneC; k++)   // lane 车道循环
			{
				// Lane k 的转向
				int sum = 0;
				ArrayList<Integer> sharedLineDir = new ArrayList<Integer>();  // 
				
				for(int j=0; j<armDirC; j++)     // dir 转向循环
				{
					//junDirIndex ++; // dir index +1
					if(permittedLanes[i][j][k] == 1)
					{
						sum += 1;					
						sharedLineDir.add(junDirIndex+j);
						//sharedLineDir.add(j);
					}
				} // dir j
//				sharedLaneValue.add(sum);
				
				if(sum > 1) //有共享车道
				{
					sharedLineDirIndex.add(sharedLineDir); //记录lane k上的共享车道的dir的index
				}
			} // Lane k
			
			for(int j=0; j<armDirC; j++)     // dir 转向循环
			{
				junDirIndex++;
			}
			// ============================================================== //
			// 1.4  计算 assignedFlowQ                  Arm i
			// ============================================================== //
			assignedFlowCalculation(i,demand);
		
		} // Arm i
		
		laneAssignedFlowCalculation();
		laneSaturationFlow();
//		System.out.println("markerListTemp");
//		System.out.println(markerListTemp.toString());
		
		markerListTemp.clear();   // 删除当的marker
		// ********************************************************* //
		// 2. 使用最大团和CPLEX生成Phase Matrix
		// ********************************************************* //
		// -------------------------------------------------
		// | 1 | 1 | 0 | 0 | 0 | 0 | 1 | 1 | 0 | 0 | 0 | 0 |  phase0
		// -------------------------------------------------
		PhaseGeneration phaseGen = new PhaseGeneration(cplex);         // 产生 PhaseMatrix 的类
		phaseGen.phaseGeneration(sharedLineDirIndex, foesMatrix); // 产生 PhaseMatrix
		List<ArrayList<String>> phaseMatrix = null;               // 存储PhaseMatrix
		phaseMatrix = new ArrayList<ArrayList<String>>(phaseGen.getPhaseMatrix()); 
		int[][] adjacentMatrix = phaseGen.getAdjacentMatrix();
		// ********************************************************* //
		// 3. 根据生成的Phase Matrix计算sumClearanceTime
		// ********************************************************* //
		// clearanceTime      = 4;			// 定义清空时间为4
		int clearanceTimeCount = phaseMatrix.size(); 							  // 清空时间数量	
		sumClearanceTime = CLEARANCETIME*clearanceTimeCount;  // 清空时间和
		
		generateCombinePhaseDataStructure(adjacentMatrix,phaseMatrix);
		adjacentMatrix=null;
		// ********************************************************* //
		// 5. 求 b[] 和  bCombined[]
		calculateRelavantRatio();		
		// 5.3 比较 b[] 与bCombined[] 的值，并计算B
		calculateSumOfRelevantRatio();
				
		// ********************************************************* //
		// 6. 求cycle 和 phase phase 
		// ********************************************************* //
		//6.1 计算common cycle length
		double originalB = B;		
		calculateCycleLength();	
		B =originalB;
		
		//6.2 计算duration
		// 此处的 cycle length应该用common cycle length来替代
		durationWithCommonCycle(commonCycle);
		
		// 7. 计算 durationMovement和startOfGreenMovement	
		calculateDurationMovement(phaseGreen, fixedPhaseMatrix);
		calculateDurationLane();
		calculateStartOfGreenMovement(fixedPhaseMatrix,phaseGreen);
//		System.exit(0);
		
		
		//修改permissive left turn的饱和流
		//saturationFlowPermissiveLT(commonCycle);
//		System.out.println("fixed phase matrix:");
//		for(int temp=0; temp<fixedPhaseMatrix.size();temp++)
//		System.out.println(fixedPhaseMatrix.get(temp));	
//		System.out.println("phase duration: ");
//		for(int i=0;i<fixedPhaseMatrix.size();i++)
//		{
//			System.out.println(phaseGreen[i]);
//		}
				
		// 释放内存
		combinedPhaseGroup     = null;
		combinedPhaseList      = null;
		sharedLaneBasedDirList = null;
		
		b=null;
		bCombined  = null;
		phaseGreen = null;
		fixedPhaseMatrix   = null;
		combinedPhaseGreen = null;
	}
	
	
	public void generateCombinePhaseDataStructure(int[][] adjacentMatrix,List<ArrayList<String>> phaseMatrix)
	{
		combinedPhaseGroup     = new ArrayList<ArrayList<Integer>>();
		combinedPhaseList      = new ArrayList<ArrayList<Integer>>();
		sharedLaneBasedDirList = new ArrayList<ArrayList<Integer>>();
		fixedPhaseMatrix       = new ArrayList<ArrayList<String>>();
		
		@SuppressWarnings("unused")
		boolean isSharedPhase = false;
		
//		// 生成fixedPhaseMatrix
		if(phaseMatrix.size()>3)
		{
			int tempSize = adjacentMatrix.length;
			
	//		System.out.println("adjacent matrix:");
	//		for(int i=0;i<tempSize;i++)
	//		{
	//    		for(int j=0;j<tempSize;j++)
	//    		{
	//    			System.out.print(adjacentMatrix[i][j]+"  ");
	//    		}
	//    		System.out.println();
	//    	}
			int[][] tempAdjacentMatrix = new int[tempSize][tempSize];
					for(int i=0;i<tempSize;i++)
						for(int j=0;j<tempSize;j++)
						{
							
							if(adjacentMatrix[i][j]==1)
								tempAdjacentMatrix[i][j]=0;
							if(adjacentMatrix[i][j]==0)
								tempAdjacentMatrix[i][j]=CLEARANCETIME;
							if(i==j)
								tempAdjacentMatrix[i][j]=0;
						}

	//		    	System.out.println("temp adjacent matrix:");
	//				for(int i=0;i<tempSize;i++)
	//				{
	//		    		for(int j=0;j<tempSize;j++)
	//		    		{
	//		    			System.out.print(tempAdjacentMatrix[i][j]+"  ");
	//		    		}
	//		    		System.out.println();
	//		    	}
					
			int numPhase = phaseMatrix.size();
			int[][] weight = new int[numPhase][numPhase];
			for(int p=0; p<numPhase;p++)
				for(int i=0;i<phaseMatrix.get(0).size();i++)
				{
					if(phaseMatrix.get(p).get(i).equals("G"))
					{
						for(int q=p+1; q<numPhase;q++)
							for(int j=0;j<phaseMatrix.get(0).size();j++)
							{
								if(phaseMatrix.get(q).get(j).equals("G"))
								{
									weight[p][q]+=tempAdjacentMatrix[i][j];
									weight[q][p]+=tempAdjacentMatrix[i][j];
								}
							}
					}
				}
			
	//		System.out.println("weight matrix:");
	//		for(int i=0;i<numPhase;i++)
	//		{
	//    		for(int j=0;j<numPhase;j++)
	//    		{
	//    			System.out.print(weight[i][j]+"  ");
	//    		}
	//    		System.out.println();
	//    	}
			
			
			PhaseSequenceOptimization phaseSq = new PhaseSequenceOptimization();
			phaseSq.solve(weight, phaseMatrix);
			fixedPhaseMatrix = phaseSq.getFixedPhaseMatrix();
			
			tempAdjacentMatrix = null;
			weight = null;
		}
		else
		{
			fixedPhaseMatrix = phaseMatrix;
		}
		
		//由fixedPhaseMatrix 中的index来构建combinedphase		
		for(int p=0; p<fixedPhaseMatrix.size(); p++)
		{
			// ---------------------------------------------------
			// 1. 当前之主 phase
			// ---------------------------------------------------
			ArrayList<String> currPhase1 = new ArrayList<String>(fixedPhaseMatrix.get(p)); // 当前的phase		
			// ---------------------------------------------------
			// 2. 查找与主phase有共同绿灯时间的从phase
			// ---------------------------------------------------
			int laneBasedDirCount = phaseMatrix.get(0).size();
			// 如果combinedPhaseList的前一对combined phase组合中，包含当前的phase index，则这个combined phase 与当前phase组成三重combined
//			if(!combinedPhaseList.isEmpty())
//			if(combinedPhaseList.get(combinedPhaseList.size()-1).contains(p))
//				isSharedPhase = true;
//			else
//				isSharedPhase = false;
			
//			ArrayList<Integer> combinedPhaseIndex = new ArrayList<Integer>();
			for(int pp=p+1; pp<fixedPhaseMatrix.size(); pp++)
			{
				ArrayList<String> currPhase2 = new ArrayList<String>(fixedPhaseMatrix.get(pp)); // 之后的phase

				ArrayList<Integer> sharedLaneBasedDirIndex = new ArrayList<Integer>();     // 共享的绿灯所在phase的以lane based dir的位置
				for(int index=0; index<laneBasedDirCount; index++)
				{
					String currLights = currPhase2.get(index);

					if((currLights.equals("G") || currLights.equals("g")) && currLights.equals(currPhase1.get(index))) // 如果共享绿灯
					{
						sharedLaneBasedDirIndex.add(index);
					}
				} // index

				// 若有共享之绿灯					
				if(!sharedLaneBasedDirIndex.isEmpty()) 
				{
					// 1. 添加 combined Phase在phaseMatrix里的位置
					ArrayList<Integer> combinedPhase = new ArrayList<Integer>();
					combinedPhase.add(p);
					combinedPhase.add(pp);
					// 2. 添加 combinedPhase到combinedPhaseList
					combinedPhaseList.add(combinedPhase);

					// 3. 添加 共享的绿灯所在phase的以lane based dir的位置到其sharedLaneBasedDirList
					sharedLaneBasedDirList.add(sharedLaneBasedDirIndex);					
					break;
				} // combined
				//System.out.println("combinedPhaseIndex: "+combinedPhaseIndex.get(0));		
			} // pp
//			if(!combinedPhaseIndex.isEmpty())combinedPhaseGroup.add(combinedPhaseIndex);	
		} // phase  p
		
		if(combinedPhaseList.size()>1)
		{
			for(int i=0;i<combinedPhaseList.size()-1;i++)
			{
				ArrayList<Integer> combinedPhaseIndex = new ArrayList<Integer>();
				for(Integer i1:combinedPhaseList.get(i))
					for(Integer i2:combinedPhaseList.get(i+1))
						if(i2==i1)
						{
							combinedPhaseIndex.add(i);
							combinedPhaseIndex.add(i+1);
						}
				if(!combinedPhaseIndex.isEmpty())
				{
					combinedPhaseGroup.add(combinedPhaseIndex);
				}
			}
		}
		
//		System.out.println("fixed phase matrix:");
//		for(int temp=0; temp<fixedPhaseMatrix.size();temp++)
//		System.out.println(fixedPhaseMatrix.get(temp));

//		System.out.println("sharedLaneBasedDirList:");
////		System.out.println(sharedLaneBasedDirList.size());
//		for(int temp=0; temp<sharedLaneBasedDirList.size();temp++)
//			System.out.println(sharedLaneBasedDirList.get(temp).toString());
		
//		System.out.println("combinedPhaseList:");
//////		System.out.println(combinedPhaseList.size());
//		for(int temp=0; temp<combinedPhaseList.size();temp++)
//			System.out.println(combinedPhaseList.get(temp).toString());
		
		//System.exit(0);
//		System.out.println("combinedPhaseGroup:");
//////		System.out.println(combinedPhaseGroup.size());
//		System.out.println(combinedPhaseGroup.toString());
//		for(int temp=0; temp<combinedPhaseGroup.size();temp++)
//		{
//			//System.out.println("combinedPhaseGroup.get(temp).size() "+combinedPhaseGroup.get(temp).size());
//			for(int temp2=0; temp2<combinedPhaseGroup.get(temp).size();temp2++)
//			System.out.println(combinedPhaseGroup.get(temp).get(temp2));
//		}
//		
	}
	
	public void calculateSumOfRelevantRatio()
	{
		int phaseCount = fixedPhaseMatrix.size();                 // phase的个数
		@SuppressWarnings("unused")
		int combinedPhaseCount = combinedPhaseList.size();   // combinedPhase的个数
		
		if(!combinedPhaseGroup.isEmpty()) // 存在连续的phase
		{
			ArrayList<Integer> allCombinedPhaseIndexList =new ArrayList<Integer>(); //记录所有与其他phase 组合的phase的index
			if(sharedLaneBasedDirList.get(0).equals(sharedLaneBasedDirList.get(1)))//3个phase连续
			{
				double sumB = 0;
				for(int cpGroupIndex=0; cpGroupIndex<combinedPhaseGroup.size();cpGroupIndex++) // cpGroupIndex = A,B,  combinedPhaseGroup.get(A)则是combinedPhaseList  I 和 II
				{
						for(int i = 0; i < combinedPhaseGroup.get(cpGroupIndex).size();i++) //cpListIndex = I, II
						{
							int cpListIndex = combinedPhaseGroup.get(cpGroupIndex).get(i);
							for(int j=0;j<combinedPhaseList.get(cpListIndex).size();j++)
							{
							if(!allCombinedPhaseIndexList.contains(combinedPhaseList.get(cpListIndex).get(j)))
								allCombinedPhaseIndexList.add(combinedPhaseList.get(cpListIndex).get(j));
							}
						}
				}
				
				for(int i=0;i<allCombinedPhaseIndexList.size();i++)
				{
					sumB+=b[allCombinedPhaseIndexList.get(i)];
				}
				B = Math.max(sumB, bCombined[0]);
			}
			else
			for(int cpGroupIndex=0; cpGroupIndex<combinedPhaseGroup.size();cpGroupIndex++) // cpGroupIndex = A,B,  combinedPhaseGroup.get(A)则是combinedPhaseList  I 和 II
			{
				//检查一共存在几个连续 shared phase
				//combinedPhaseGroup.get(cpGroupIndex).size()： 在group A中，List的个数.
				//如果个数等于1，则只有两个phase combined，等于2则又三个phase combined
				double maxTemp = b[combinedPhaseList.get(combinedPhaseGroup.get(cpGroupIndex).get(0)).get(0)];
				double bLastPhase =0;
				for(int i = 0; i < combinedPhaseGroup.get(cpGroupIndex).size();i++) //cpListIndex = I, II
				{
					//int cpListIndex = combinedPhaseGroup.get(cpGroupIndex).get(i) 是combinedList 的第i个位置的值，值是combinedList的index I/II/III
					//combinedPhaseList.get(cpListIndex) 获得combinedPhases 所有的index
					// combinedPhaseList.get(cpListIndex).get(j) 获得 combinedPhases的第j个位置的值，该值为phase 的index
					int cpListIndex = combinedPhaseGroup.get(cpGroupIndex).get(i);
					maxTemp = Math.max(maxTemp+b[combinedPhaseList.get(cpListIndex).get(1)], bLastPhase+bCombined[cpListIndex]);

					bLastPhase+=b[combinedPhaseList.get(cpListIndex).get(0)];
					for(int j=0;j<combinedPhaseList.get(cpListIndex).size();j++)
					{
						if(!allCombinedPhaseIndexList.contains(combinedPhaseList.get(cpListIndex).get(j)))
						{
							allCombinedPhaseIndexList.add(combinedPhaseList.get(cpListIndex).get(j));
						}
					}
				}
				B+=maxTemp;
			}

			
			//计算不在combined phase中的其他 单独phase的 b
			for(int phaseIndex=0; phaseIndex<phaseCount; phaseIndex++)
			{
				if(allCombinedPhaseIndexList.contains(phaseIndex))
				{
					continue;
				}
				B+=b[phaseIndex];
			}

		}
		else //不存在连续的phase
		{
			for(int phaseIndex=0; phaseIndex<phaseCount; phaseIndex++)
			{
				B+=b[phaseIndex];
			}
			
		}
		B = 1.0*Math.round(B*10000)/10000;
//		System.out.println("the value of B: "+B);
//		System.exit(0);
	}
	
	public void calculateRelavantRatio()
	{
		// 数据结构 List<ArrayList<Integer>> combinedPhaseGroup
		// 数据结构 List<ArrayList<Integer>> combinedPhaseList
		// 数据结构 ArrayList<Integer> combinedPhase
		// ********************************************************* //
		int phaseCount = fixedPhaseMatrix.size();                 // phase的个数
		int combinedPhaseCount = combinedPhaseList.size();   // combinedPhase的个数
		
		b         = new double[phaseCount];         // 
		bCombined = new double[combinedPhaseCount]; //
		B=0; 
		
		//5. 1记录每个phase所有的与其他phase共用的dir,主要是考虑一个phase与两个其他phase combine，该phase与其他phase共用的dir会多于一般的phase
		//初始化， 如果一个phase不与任何其他phase combine，则sharedLaneBasedDir为空
		ArrayList<ArrayList<Integer>> sharedDirEachPhaseList = new ArrayList<ArrayList<Integer>>(); 
		//大小与phase的数量一致，对每个phase都找到全部与其他phase共用的dir
		for(int i=0;i<fixedPhaseMatrix.size();i++)
		{
			ArrayList<Integer> sharedLaneBasedDir = new ArrayList<Integer>();
			sharedDirEachPhaseList.add(sharedLaneBasedDir);
		}
		
		//System.out.println("combinedPhaseGroup.size():  "+combinedPhaseGroup.size());
		if(!combinedPhaseGroup.isEmpty()) // 存在三个连续的phase
		{
			for(int cpGroupIndex=0; cpGroupIndex<combinedPhaseGroup.size();cpGroupIndex++) // cpGroupIndex = A,B,  combinedPhaseGroup.get(A)则是combinedPhaseList  I 和 II
			{
				//检查一共存在几个连续 shared phase
				//combinedPhaseGroup.get(cpGroupIndex).size()： 在group A中，List的个数.
				//如果个数等于1，则只有两个phase combined，等于2则又三个phase combined
				// visited[] 用来记录已经访问过的phaseIndex
				int[] visited=new int[2];
				visited[0]=-1;
				visited[1]=-1;
				for(int i = 0; i < combinedPhaseGroup.get(cpGroupIndex).size();i++) //cpListIndex = I, II
				{
					//int cpListIndex = combinedPhaseGroup.get(cpGroupIndex).get(i) 是combinedList 的第i个位置的值，值是combinedList的index I/II/III
					//combinedPhaseList.get(cpListIndex) 获得combinedPhases 所有的index
					// combinedPhaseList.get(cpListIndex).get(j) 获得 combinedPhases的第j个位置的值，该值为phase 的index
					int cpListIndex = combinedPhaseGroup.get(cpGroupIndex).get(i);					
//					System.out.println("calculate relevantRatio b - cpListIndex: "+cpListIndex);
					for(int j =0; j<combinedPhaseList.get(cpListIndex).size();j++)
					{
						int phaseIndex = combinedPhaseList.get(cpListIndex).get(j);
						//System.out.println("cp phaseIndex: "+phaseIndex);
						if(sharedDirEachPhaseList.get(phaseIndex).isEmpty())
						sharedDirEachPhaseList.get(phaseIndex).addAll(sharedLaneBasedDirList.get(cpListIndex));
						
						if(visited[j]==phaseIndex)
						{
							//去除重复的dir，并添加其他的dir
							sharedDirEachPhaseList.get(phaseIndex).removeAll(sharedLaneBasedDirList.get(cpListIndex));
							sharedDirEachPhaseList.get(phaseIndex).addAll(sharedLaneBasedDirList.get(cpListIndex));
						}
						visited[j]=phaseIndex;
						//sharedDirEachPhaseList.set(phaseIndex, sharedLaneBasedDir);
					}	
					visited[0]=visited[1];
					visited[1]=-1;	
				}
			}
		}
		// else 不对 sharedDirEachPhaseList.get(phaseIndex) 做修改，也就是对应的值为空
//		System.out.println("存放每个phase所有的与其他phase共用的dir - sharedDirEachPhaseList: ");
//		for(int i=0;i<fixedPhaseMatrix.size();i++)
//		{
//			System.out.println(sharedDirEachPhaseList.get(i).toString());
//		}
		
		// 5.2 计算所有 b[]的值 和bCombined[]的值
		dirFlowRate();
//		System.out.println("the values of b[]");
		for(int i=0; i<phaseCount; i++)
		{
			//b[i] = maxFlowRateNonSharedDir(sharedDirEachPhaseList.get(i),phaseMatrix.get(i),laneSaturationOfFlow); 
			//-> laneSaturationOfFlow 应该用laneSaturationFlow计算。尤其是计算b的时候，有的dir不考虑在内
			b[i] = maxFlowRateNonSharedDir(sharedDirEachPhaseList.get(i),fixedPhaseMatrix.get(i));
//			System.out.println(b[i]);
		}
//		System.out.println("the values of bCombined[]");
		for(int i=0; i<combinedPhaseCount; i++)
		{
			//bCombined[i] = maxFlowRateSharedDir(sharedLaneBasedDirList.get(i),laneSaturationOfFlow);
			bCombined[i] = maxFlowRateSharedDir(sharedLaneBasedDirList.get(i));
//			System.out.println(bCombined[i]);
		}
//		System.exit(0);
	}
	
	public void calculateCycleLength()
	{
		int phaseCount = fixedPhaseMatrix.size();
		int combinedPhaseCount = combinedPhaseList.size();
		phaseGreen         = new int[phaseCount];         // 
		combinedPhaseGreen = new int[combinedPhaseCount]; //
		int sumMinGreen=0;
		int tempSumMinGreen =0;
		double Btemp = B;
		
		int flag=0;
		ArrayList<Integer> minGreenPhase = new ArrayList<Integer>();
		do{
			flag=0;
			if(B>=1) 
			{
				B=0.99;
			}
			cycleLength = (int)Math.round((1.5*(sumClearanceTime +sumMinGreen)+5)/(1-B));
			if(cycleLength<CYCLETIMEMIN)
			{
				cycleLength=CYCLETIMEMIN;
			}
			if(cycleLength>CYCLETIMEMAX)
			{
				cycleLength=CYCLETIMEMAX;
			}
			
			
			for(int phaseIndex=0; phaseIndex<phaseCount; phaseIndex++)
			{
				if(!minGreenPhase.isEmpty()&& minGreenPhase.contains(phaseIndex))
				{	
					continue;
				}
				phaseGreen[phaseIndex] = (int)Math.round((b[phaseIndex]/B)*(cycleLength - sumClearanceTime -sumMinGreen));
				if(phaseGreen[phaseIndex]<GREENDURATIONMIN)
				{
					phaseGreen[phaseIndex]=GREENDURATIONMIN;
					Btemp-=b[phaseIndex];
					tempSumMinGreen+=phaseGreen[phaseIndex];
					flag=1;
					minGreenPhase.add(phaseIndex);
				}
			}
			
			if(!combinedPhaseList.isEmpty())
			for(int cpListIndex=0; cpListIndex<combinedPhaseList.size();cpListIndex++)
			{
				combinedPhaseGreen[cpListIndex] = (int)Math.round((bCombined[cpListIndex]/B)*(cycleLength - sumClearanceTime -sumMinGreen));
			}
		
			B=Btemp;
			sumMinGreen = tempSumMinGreen;
		} while(flag==1);
		
//		System.out.println("minGreenPhase before common cycle: "+ minGreenPhase.toString());
//		System.out.println("sumMinGreen before common cycle: "+ sumMinGreen);
//		System.out.println("cycleLength: "+ cycleLength);
//		System.out.println("B in cycle length calculation: "+B);
//		System.exit(0);
	}
	
	public void durationWithCommonCycle(int commonCycleLength)
	{
		// 6.2 计算所有phase和green phase的绿灯
		int sumMinGreen=0;
		int tempSumMinGreen =0;
		
		//double B = originalB;
		double Btemp = B;
//		System.out.println("B: "+B);
//		System.out.println("common cycle : "+commonCycleLength);
		int phaseCount = fixedPhaseMatrix.size();
		int flag;
		ArrayList<Integer> minGreenPhase = new ArrayList<Integer>();
		do{
			flag=0;		
		
			for(int phaseIndex=0; phaseIndex<phaseCount; phaseIndex++)
			{
				if(!minGreenPhase.isEmpty() && minGreenPhase.contains(phaseIndex))
					continue;
				phaseGreen[phaseIndex] = (int)Math.round((b[phaseIndex]/B)*(commonCycleLength - sumClearanceTime -sumMinGreen));
				if(phaseGreen[phaseIndex]<GREENDURATIONMIN)
				{
					phaseGreen[phaseIndex]=GREENDURATIONMIN;
					Btemp-=b[phaseIndex];
					tempSumMinGreen+=phaseGreen[phaseIndex];
					flag=1;
					minGreenPhase.add(phaseIndex);
				}
			}
			
			if(!combinedPhaseList.isEmpty())
			for(int cpListIndex=0; cpListIndex<combinedPhaseList.size();cpListIndex++)
			{
				combinedPhaseGreen[cpListIndex] = (int)Math.round((bCombined[cpListIndex]/B)*(commonCycleLength - sumClearanceTime -sumMinGreen));
			}
		
			B=Btemp;
			sumMinGreen = tempSumMinGreen;
		} while(flag==1);
		

////		System.out.println("minGreenPhase after common cycle: "+ minGreenPhase.toString());
////		System.out.println("sumMinGreen: "+sumMinGreen);
//////		System.out.println("Btemp: "+Btemp);
//		System.out.println("common cycle: "+commonCycleLength);
//		System.out.println("phase greens with common cycle length are: ");
//		for(int temp=0;temp<phaseCount;temp++)
//			System.out.println(phaseGreen[temp]);
//		System.exit(0);
		
		//6.3 比较phase和combined phase的绿灯大小，确定绿灯的值		
		if(!combinedPhaseGroup.isEmpty()) // 存在连续的phase
		{
			for(int cpGroupIndex=0; cpGroupIndex<combinedPhaseGroup.size();cpGroupIndex++) // cpGroupIndex = A,B,  combinedPhaseGroup.get(A)则是combinedPhaseList  I 和 II
			{
				//检查一共存在几个连续 shared phase
				//combinedPhaseGroup.get(cpGroupIndex).size()： 在group A中，List的个数.
				//如果个数等于1，则只有两个phase combined，等于2则又三个phase combined
				int[] maxGreen=new int[2];
				maxGreen[0]=0;
				maxGreen[1]=0;
				for(int i = 0; i < combinedPhaseGroup.get(cpGroupIndex).size();i++) //cpListIndex = I, II
				{
					//int cpListIndex = combinedPhaseGroup.get(cpGroupIndex).get(i) 是combinedList 的第i个位置的值，值是combinedList的index I/II/III
					//combinedPhaseList.get(cpListIndex) 获得combinedPhases 所有的index
					// combinedPhaseList.get(cpListIndex).get(j) 获得 combinedPhases的第j个位置的值，该值为phase 的index
					int cpListIndex = combinedPhaseGroup.get(cpGroupIndex).get(i);
					// combinedPhaseGreen[cpListIndex] combined phase的绿灯
					
					int sumGreen=0;
					double bk=b[combinedPhaseList.get(cpListIndex).get(0)];
					double bl=b[combinedPhaseList.get(cpListIndex).get(1)];
					for(int j =0; j<combinedPhaseList.get(cpListIndex).size();j++)
					{
						sumGreen+=phaseGreen[combinedPhaseList.get(cpListIndex).get(j)];
//						if(maxGreen[j] > phaseGreen[combinedPhaseList.get(cpListIndex).get(j)])
//							continue;
//						maxGreen[j] = Math.max(phaseGreen[combinedPhaseList.get(cpListIndex).get(j)],maxGreen[j]);
					}	
//					System.out.println("g_l+g_k: "+sumGreen+" combined_l,k: " + combinedPhaseGreen[cpListIndex]);
					if(sumGreen<combinedPhaseGreen[cpListIndex])
					{
						for(int j =0; j<combinedPhaseList.get(cpListIndex).size();j++)
						{
							//maxGreen[j]=Math.max(combinedPhaseSingleGreen(bk, bl, combinedPhaseGreen[cpListIndex])[j], maxGreen[j]);
							phaseGreen[combinedPhaseList.get(cpListIndex).get(j)] = combinedPhaseSingleGreen(bk, bl, combinedPhaseGreen[cpListIndex])[j];
						}	
					}
					
					for(int j =0; j<combinedPhaseList.get(cpListIndex).size();j++)
					{
						maxGreen[j] = Math.max(phaseGreen[combinedPhaseList.get(cpListIndex).get(j)],maxGreen[j]);
					}	
					maxGreen[0]=maxGreen[1];
					maxGreen[1]=0;	
				}
			}
		}
		
//		System.out.println("phase duration: ");
//		for(int i=0;i<fixedPhaseMatrix.size();i++)
//		{
//			System.out.println(phaseGreen[i]);
//		}
//		System.exit(0);
	}
	
	public void calculateDurationMovement(int[] phaseGreen, List<ArrayList<String>> fixedPhaseMatrix)
	{
		durationMovement = new int[armNumber][];
		for(int i=0;i<armNumber;i++)
			durationMovement[i] = new int[armDirNum.get(i)];
		int phaseNumber = phaseGreen.length;
		
		for(int p=0;p<phaseNumber;p++)
		{
			int accumlativeDirIndex=-1;
			for(int i=0;i<armNumber;i++)
			{
				for(int j=0;j<armDirNum.get(i);j++)
				{
					accumlativeDirIndex++;
					//System.out.println("fixedPhaseMatrix.get(p).get(accumlativeDirIndex): "+fixedPhaseMatrix.get(p).get(accumlativeDirIndex).toString());
					if(fixedPhaseMatrix.get(p).get(accumlativeDirIndex).equals("G")||fixedPhaseMatrix.get(p).get(accumlativeDirIndex).equals("g"))
					{
						if(durationMovement[i][j]==0)
							durationMovement[i][j] =phaseGreen[p];
						else
							durationMovement[i][j]+=CLEARANCETIME + phaseGreen[p];
					}
				}
			}
		}
		
//		System.out.println("durationMovement in calculateDurationMovement(int[] phaseGreen, List<ArrayList<String>> fixedPhaseMatrix): ");
//		for(int i=0;i<armNumber;i++)
//		{
//			for(int j=0;j<armDirNum.get(i);j++)
//			{
//				System.out.print(durationMovement[i][j]+"\t");
//			}
//			System.out.println();
//		}
		
		//return durationMovement;
	}
	
	public void calculateDurationLane()
	{
		durationLane = new int[armNumber][];
		for(int i=0;i<armNumber;i++)
			durationLane[i] = new int[armLanesNum.get(i)];
		for(int i=0;i<armNumber;i++)
		{
			for(int j=0;j<armDirNum.get(i);j++)
			{
				for(int k=0;k<armLanesNum.get(i);k++)
				{
					if(permittedLanes[i][j][k]==1)
					{
						durationLane[i][k]=durationMovement[i][j];
					}
				}
			}
		}
		
//		System.out.println("durationLane in calculateDurationLane(): ");
//		for(int i=0;i<armNumber;i++)
//		{
//			for(int k=0;k<armLanesNum.get(i);k++)
//			{
//				System.out.print(durationLane[i][k]+"\t");
//			}
//			System.out.println();
//		}
		//return durationLane;
	}
	
	public void calculateStartOfGreenMovement(List<ArrayList<String>> fixedPhaseMatrix,int[] phaseGreen)
	{
		startOfGreenMovement = new int[armNumber][];
		for(int i=0;i<armNumber;i++)
		{
			startOfGreenMovement[i] = new int[armDirNum.get(i)];
		}
		int phaseNumber = fixedPhaseMatrix.size();
		
		for(int p=0;p<phaseNumber;p++)
		{
			int accumlativeDirIndex=-1;
			for(int i=0;i<armNumber;i++)
			{
				for(int j=0;j<armDirNum.get(i);j++)
				{
					accumlativeDirIndex++;
					//System.out.println("fixedPhaseMatrix.get(p).get(accumlativeDirIndex): "+fixedPhaseMatrix.get(p).get(accumlativeDirIndex).toString());
					if(fixedPhaseMatrix.get(p).get(accumlativeDirIndex).equals("G"))
					{
						startOfGreenMovement[i][j] = startOfGreenPerPhase(p, phaseGreen);
					}
				}
			}
		}
//		System.out.println("startOfGreenMovement in calculateStartOfGreenMovement(: ");
//		for(int i=0;i<armNumber;i++)
//		{
//			for(int j=0;j<armDirNum.get(i);j++)
//			{
//				System.out.print(startOfGreenMovement[i][j]+"\t");
//			}
//			System.out.println();
//		}
	}
	
	public int startOfGreenPerPhase(int phaseIndex, int[] phaseGreen)
	{
		if( phaseIndex==0)
		{
			return 0;
		}
		else
			return startOfGreenPerPhase(phaseIndex-1, phaseGreen) + CLEARANCETIME + phaseGreen[phaseIndex];
	}
	

	
	public int[] combinedPhaseSingleGreen(double b_k, double b_l, int combinedPhaseGreenTime)
	{
		int[] phase = new int[2];
		
		phase[0] = (int)Math.round(combinedPhaseGreenTime*b_k/(b_k+b_l));
		if(phase[0]>GREENDURATIONMIN)
		{
			phase[1] = (int)Math.round(combinedPhaseGreenTime*b_l/(b_k+b_l));
		}
		else
		{
			phase[0] = GREENDURATIONMIN;
			phase[1] = combinedPhaseGreenTime - phase[0];
		}
		return phase;
	}
	
	public double maxFlowRateNonSharedDir(ArrayList<Integer> sharedLaneBasedDir, ArrayList<String> phase)
	{
		//sharedLaneBasedDir可以为空
		double maxB=0;
		//System.out.println("sharedLaneBasedDir in  maxFlowRateNonSharedDir()"+sharedLaneBasedDir.toString());
		for(int j=0; j<phase.size();j++)
		{
			//System.out.println("dirFlowRate()[j] "+dirFlowRate()[j]);
			if(phase.get(j).equals("G")||phase.get(j).equals("g")) // 只对有绿灯的部分进行取值
			{
			//System.out.println(j+" phase.get(j) "+phase.get(j));
			if(sharedLaneBasedDir.contains(j)) continue;
			if(dirFlowRate()[j]>maxB)
				maxB = dirFlowRate()[j];
			}

		}
		
		maxB = 1.0*Math.round(maxB*10000)/10000;
		//System.out.println("maxB: "+maxB);
		return maxB; 
	}
	
	public double maxFlowRateSharedDir(ArrayList<Integer> sharedLaneBasedDir)
	{
		//sharedLaneBasedDir 不能为空，否则会报错
		//double maxB=dirFlowRate()[sharedLaneBasedDir.get(0)];
//		System.out.println("sharedLaneBasedDir.toString() "+sharedLaneBasedDir.toString());
		double maxB=0;
		for(int i=0; i<sharedLaneBasedDir.size();i++)	
		{
//			System.out.println("sharedLaneBasedDir.get(i) "+sharedLaneBasedDir.get(i));
			if(dirFlowRate()[sharedLaneBasedDir.get(i)]>maxB)
				maxB = 1.0*Math.round(dirFlowRate()[sharedLaneBasedDir.get(i)]*10000.0)/10000;
		}
//		System.out.println("maxB "+maxB);
		return maxB; 
	}
	
	//对每个dir计算flow rate = dir 所在lane的flow rate = sum of assignment flow / saturation
	public double[] dirFlowRate()
	{
		@SuppressWarnings("unused")
		double[][] bTemp = new double[armNumber][]; //计算每个movement的 flow rate
		int numOfDir =0;
		
		double[] b = new double[fixedPhaseMatrix.get(0).size()];
		for(int i=0;i<armNumber;i++)
			for(int j=0;j<armDirNum.get(i);j++)
			{
				for(int k=0; k< armLanesNum.get(i); k++)
				{
					if(permittedLanes[i][j][k]==1)
					{
						//System.out.println("i= "+i+" k= "+k+" laneAssignedFlow(i)[k] in dirFlowRate(): "+laneAssignedFlow(i)[k]);
						if(laneAssignedFlow[i][k]!=0)
						{
							//System.out.println("i= "+i+" k= "+k+" laneAssignedFlow(i)[k] in dirFlowRate(): "+laneAssignedFlow[i][k]);
							//System.out.println("i= "+i+" k= "+k+" laneSaturationOfFlow[i][k] in dirFlowRate(): "+laneSaturationFlow[i][k]);
							b[numOfDir] = 1.0*laneAssignedFlow[i][k]/laneSaturationFlow[i][k];
							//System.out.println("i= "+i+" j= "+j+" bTemp[i][j] in dirFlowRate(): "+b[numOfDir]);
						}
						else
						{
							b[numOfDir]=0;
						}
					}
					
				}
				numOfDir++;
			}
		
//		for(int i=0;i<fixedPhaseMatrix.get(0).size();i++)
//		{
//			System.out.println(" b[] in dirFlowRate(): "+b[i]);
//		}
		
		return b;
	}
	
	@Override
	public void laneAssignedFlowCalculation()
	{
		laneAssignedFlow = new int[armNumber][];
		for(int armIndex=0;armIndex<armNumber;armIndex++)
		{
			laneAssignedFlow[armIndex]=  new int[armLanesNum.get(armIndex)];
		for(int k=0; k< armLanesNum.get(armIndex); k++)
		{
			for(int j=0;j<armDirNum.get(armIndex);j++)
			{
				laneAssignedFlow[armIndex][k]+=assignedFlowQ[armIndex][j][k];
			}
//			System.out.println("laneAssignedFlow in Stagebased.java: "+laneAssignedFlow[armIndex][k]);
		}
		}
	}
	
	@Override
	public void laneSaturationFlow()
	{
		laneSaturationFlow = new int[armNumber][];
				//[armLanesNum.get(armIndex)];
		
		for(int armIndex=0;armIndex<armNumber;armIndex++)
		{
			laneSaturationFlow[armIndex] =  new int[armLanesNum.get(armIndex)];
			for(int k=0; k< armLanesNum.get(armIndex); k++)
			{
				double sumDirFlow=0;
				for(int j=0;j<armDirNum.get(armIndex);j++)
				{
					sumDirFlow +=assignedFlowQ[armIndex][j][k];	
				}
				//System.out.println("sumDirFlow: "+sumDirFlow);
				double sumFlowRate=0;
				for(int j=0;j<armDirNum.get(armIndex);j++)
				{
					//System.out.print("assigned Flow in stagebased: "+assignedFlowQ[armIndex][j][k]+"  saturtaion flow in stagebased: ");
					//System.out.print(armDir.get(armIndex).get(j).getSaturationFlow()+"  ");
					sumFlowRate +=1.0*assignedFlowQ[armIndex][j][k]/(armDir.get(armIndex).get(j).getSaturationFlow()*sumDirFlow);		
				}
				laneSaturationFlow[armIndex][k] = (int)Math.round(1.0/sumFlowRate);
				//System.out.println();
				//System.out.println("lane saturation in Stagebased: "+ laneSaturationFlow[armIndex][k]);
			}
		}
	}
	
	/**
	 * 使用 permittedLanes 计算  assignedFlowQ 的值
	 * @param armIndex
	 */
	@Override
	public void assignedFlowCalculation(int armIndex, double demand[][])
	{

		int numOfVariables =numOfAssignedFlow(armIndex);
//		System.out.println("numOfVariables "+numOfVariables);
		double[][] matrixAinJava = new double[numOfVariables][numOfVariables];
		double[][] matrixBinJava = new double[numOfVariables][1];
		
		int variableRow=0;
		int variableColumn=0;
		//System.out.println("numOfVariables "+numOfVariables);
//		System.out.println("demand value in assignedFlowCalculation(): ");
//		for(int j=0;j<armDirNum.get(armIndex);j++)
//		{
//			System.out.println("demand["+armIndex+"]["+j+"]: "+demand[armIndex][j]);
//		}
		//construct A matrix and B vector
		for(int j=0;j<armDirNum.get(armIndex);j++)
		{
			int count = 0;
			for(int k=0; k< armLanesNum.get(armIndex); k++)
			{
				if(permittedLanes[armIndex][j][k]==1)
				{
					//System.out.println("variableRow "+variableRow+" variableColumn "+variableColumn);
					matrixAinJava[variableRow][variableColumn++]=1;
					//System.out.println("demand[armIndex][j] in assignedFlowCalculation(): "+demand[armIndex][j]);
					matrixBinJava[variableRow][0]=demand[armIndex][j];
					
					//variableRow++;
					count++;
				}	
			}
			if(count>0) variableRow++;
		}
		
		for(int j=0;j<armDirNum.get(armIndex);j++)
		{
			for(int k=0; k< armLanesNum.get(armIndex)-1; k++)
			{
				if(permittedLanes[armIndex][j][k]==1 && permittedLanes[armIndex][j][k+1]==1)
				{
					variableColumn=0;
					for(int m=0;m<armDirNum.get(armIndex);m++)
					{
						int count = 0;
						for(int n=0; n< armLanesNum.get(armIndex); n++)
						{
							if(permittedLanes[armIndex][m][n]==1)
							{
//								System.out.println(armDir.get(armIndex).get(m).getDirection());
//								System.out.println(armDir.get(armIndex).get(m).getSaturationFlow());
								if(n==k)
									matrixAinJava[variableRow][variableColumn]=1.0/armDir.get(armIndex).get(m).getSaturationFlow();
								if(n==k+1)
									matrixAinJava[variableRow][variableColumn]=-1.0/armDir.get(armIndex).get(m).getSaturationFlow();
								count++;
							}
							
							if(count>0)
							{
								variableColumn++;
								count = 0;
							}
						}
					}
					variableRow++;
				}	
			}
		}
		
//		for(int p=0;p<numOfVariables;p++)
//		{
//			System.out.println("B "+matrixBinJava[p][0]);
//		}
//		System.out.println("A ");
//		for(int p=0;p<numOfVariables;p++)
//		{
//			for(int q=0;q<numOfVariables;q++)
//			{
////				if(matrixAinJava[p][q]==0)
//			System.out.print(matrixAinJava[p][q]+" \t");
////				else
////					System.out.print(1.0/matrixAinJava[p][q]+" \t");
//			}
//			System.out.println();
//		}
		
		 Jama.Matrix matrixAinJama = new Jama.Matrix(matrixAinJava);	
		 Jama.Matrix matrixBinJama = new Jama.Matrix(matrixBinJava);	
		 Jama.Matrix x = matrixAinJama.solve(matrixBinJama);
		 
		 int variableIndex=0;
		 @SuppressWarnings("unused")
		 int tempFlow=0;
		 for(int j=0;j<armDirNum.get(armIndex);j++)
			{
				for(int k=0; k< armLanesNum.get(armIndex); k++)
				{
					if(permittedLanes[armIndex][j][k]==1)
					{
//						assignedFlowQ[armIndex][j][k] = tempFlow + (int)Math.round(x.get(variableIndex,0));
//						if(assignedFlowQ[armIndex][j][k]<0)
//						{
//							assignedFlowQ[armIndex][j][k]=0;
//							tempFlow=(int)Math.round(x.get(variableIndex,0));
//						}
						assignedFlowQ[armIndex][j][k] = (int)Math.round(x.get(variableIndex,0));
						variableIndex++;
					}
					else
						assignedFlowQ[armIndex][j][k] = 0;
				}
			}
		 
//		 System.out.println("calculate assigned flow in assignedFlowCalculation(int armIndex):");
//		 for(int j=0;j<armDirNum.get(armIndex);j++)
//			{
//				for(int k=0; k< armLanesNum.get(armIndex); k++)
//				{
//					System.out.println("j= "+armDir.get(armIndex).get(j).getDirection()+" k= "+k +" saturation=: "+armDir.get(armIndex).get(j).getSaturationFlow()+ " assignedFlowQ[armIndex][j][k]= "+assignedFlowQ[armIndex][j][k]);
//				}
//			}

	}
	
	// return the number of assignedFlow variables in an arm
	public int numOfAssignedFlow(int armIndex) 
	{
		int num =0;
		for(int j=0;j<armDirNum.get(armIndex);j++)
		{
			for(int k=0; k< armLanesNum.get(armIndex); k++)
			{
//				System.out.println(armIndex+" "+j+ " "+ k+ " permittedLanes "+ permittedLanes[armIndex][j][k]);
				if(permittedLanes[armIndex][j][k]==1)
				num++;		
			}
		}
		return num;
	}
	

//	
	/****************************************************************
	 ******************* Getter and Setter****************************
	 ****************************************************************/
	
//	public int[][][] getPermittedDelta()
//	{
//		return permittedLanes;
//	}
//
//	/**
//	 * @return assignedFlow
//	 */
	public int [][][] getAssignedFlow()
	{
		return assignedFlowQ;
	}
//	
//	/**
//	 * @return cycleLength
//	 */
	public int getCycleLengthEpslon()
	{
		return cycleLength;
	}
//
//	/**
//	 * @return startOfGreenMovement
//	 */
//	public int[][] getStartOfGreenMovementTheta()
//	{
//		return startOfGreenMovement;
//	}
//
//	/**
//	 * @return durationMovementPhi
//	 */
//	public int[][] getDurationMovementPhi()
//	{
//		return durationMovement;
//	}
//
//	/**
//	 * @param permittedLanes 
//	 *		要设置的 permittedLanes
//	 */
//	public void setPermittedLanes(int[][][] permittedLanes)
//	{
//		this.permittedLanes = permittedLanes;
//	}
//
//	/**
//	 * @param assignedFlowQ 
//	 *		要设置的 assignedFlowQ
//	 */
	public void setAssignedFlowQ(int[][][] assignedFlowQ)
	{
		this.assignedFlowQ = assignedFlowQ;
	}
//
//	/**
//	 * @param cycleLength 
//	 *		要设置的 cycleLength
//	 */
	public void setCycleLength(int cycleLength)
	{
		this.cycleLength = cycleLength;
	}
//
//	/**
//	 * @param startOfGreenMovement 
//	 *		要设置的 startOfGreenMovement
//	 */
//	public void setStartOfGreenMovement(int[][] startOfGreenMovement)
//	{
//		this.startOfGreenMovement = startOfGreenMovement;
//	}
//
//	/**
//	 * @param durationMovement 
//	 *		要设置的 durationMovement
//	 */
//	public void setDurationMovement(int[][] durationMovement)
//	{
//		this.durationMovement = durationMovement;
//	}
//
//	/**
//	 * @param durationMovementLane 
//	 *		要设置的 durationMovementLane
//	 */
	public void setDurationLane(int[][] durationLane)
	{
		this.durationLane = durationLane;
	}
//
//	/**
//	 * @return durationMovementLane
//	 */
	public int [][] getDurationLane()
	{
		return durationLane;
	}
	
	public int[][] getLaneAssignedFlow()
	{
		return laneAssignedFlow;
	}
	
	public int[][] getLaneSaturationFlow()
	{
		return laneSaturationFlow;
	}

	/**
	 * @return cplex
	 */
	public IloCplex getCplex()
	{
		return cplex;
	}

	/**
	 * @param cplex 
	 *		要设置的 cplex
	 */
	public void setCplex(IloCplex cplex)
	{
		this.cplex = cplex;
	}

}
