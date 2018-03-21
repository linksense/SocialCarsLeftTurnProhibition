/* --------------------------------------------------------------------
 * LanebasedSinalTiming.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and all Contributors.
 *
 * Original Author:  1. Qinrui Tang  2. Huijun Liu
 * Contributor(s):   Huijun Liu
 *
 * Last Change Date: 23.06.2016
 * 
 * Function:
 *           1.信号灯时间
 *           
 */
package signalOptimization;

import java.util.ArrayList;
import java.util.List;

import org.ujmp.core.Matrix;

import network.Link;
import ilog.concert.*;
import ilog.cplex.*;

public class LanebasedSinalTiming extends SignalTiming
{
	//~Variables
	//--------------------------------------------------------------------------	
	// private int [][][] permittedLanes     = null;  //已继承，注释掉
	// private int [][][] assignedFlowQ      = null;  //已继承，注释掉
	// private int [][] startOfGreenMovement = null;  //已继承，注释掉
	// private int [][] durationMovement     = null;  //已继承，注释掉
	// private int [][] durationLane         = null;  //已继承，注释掉
	// private int cycleLength = 0;                   //已继承，注释掉
	
	// private int[][] laneAssignedFlow   = null;     //共有，放父类
	// private int[][] laneSaturationFlow = null;     //共有，放父类
	
	// private static final int CYCLETIMEMIN = 60;    // 最小周期时间  //共有，放父类
	// private static final int CYCLETIMEMAX = 100;   // 最大周期时间  //共有，放父类
	// private static final int GREENDURATIONMIN = 5; // 最小绿灯时间  //共有，放父类
	// private static final int CLEARANCETIME=4;      // 清空时间            //共有，放父类
	
	// private int armNumber = 0;                     // Arm的个数           //共有，放父类
	// private List<Integer> armLanesNum    = null;   // Arm里的车道数 //共有，放父类
	// private List<Integer> armDirNum      = null;   // Arm里的转向数 //共有，放父类
	// private List<ArrayList<Link>> armDir = null;   // Arm里的转向      //共有，放父类
	// private double[][] demand = null;              // Demand       //共有，放父类
	
	// 本类的私有变量
	//=============================================================
	private IloCplex cplex = null;

	//~Methods
	//--------------------------------------------------------------------------
	public LanebasedSinalTiming(IloCplex cplex)
	{	
		this.cplex = cplex;
	}
	
	public void solveMe(int armNumber,double[][] demand, int [][] dirOutIndex,
						       Matrix foesMatrix,List<ArrayList<Link>> armDir,
			                   List<Integer> armLanesNum,List<Integer> armDirNum,
							   List<Integer> outLanesNum,List<Link> extLinks)
	{
		this.armNumber   = armNumber;      // Arm 的个数
		this.armLanesNum = armLanesNum;    // Arm 的 lane 的个数
		this.armDirNum   = armDirNum;      // Arm 的 Dir  的个数
		this.armDir = armDir;              // Arm 的 Dir  的List
		this.demand = demand;              // Demand 
		
		// clearance time 用冲突矩阵内容替换
		//////////////////////////////////////////////////////////////////////////////////////
		
		int indexRow = -1;
		int indexRowTemp = -1;
		
		int indexColumn = -1;
		int indexColumnTemp = -1;
		int[][][][] clearanceTime = new int[armNumber][armNumber][][];
		for (int i = 0; i < armNumber; i++)
		{
			for (int l = 0; l < armNumber; l++)
			{
				indexRowTemp = indexRow;
				clearanceTime[i][l] = new int[armDirNum.get(i)][armDirNum.get(l)];
				for (int j = 0; j < armDirNum.get(i); j++)
				{
					indexColumnTemp = indexColumn;
					indexRowTemp += 1;
					
					for (int m = 0; m < armDirNum.get(l); m++)
					{
						indexColumnTemp += 1;
						//使用冲突矩阵填充
						clearanceTime[i][l][j][m] = foesMatrix.getAsInt(indexRowTemp,indexColumnTemp)*CLEARANCETIME;
						//System.out.print(clearanceTime[i][l][j][m] + " ");
					}
					//System.out.println("");
				}
				indexColumn = indexColumnTemp;
				//System.out.println("");
			}
			indexRow = indexRowTemp;
			indexColumn = -1;
		}

		

		// 计算demand, 此处用sue替换
		/////////////////////////////////////////////////////////
		// 0.2 New Flows QPrime.
		double Qprime[][] = new double[armNumber][];
		for (int l = 0; l < armNumber; l++)
		{
			Qprime[l] = new double[armDirNum.get(l)];
			for(int m=0; m<armDirNum.get(l); m++)
			{
				Qprime[l][m] = demand[l][m];
				//System.out.println("Qprime[l][m] in LanebasedSignalTiming.java: "+Qprime[l][m]);
			}
		}

		try
		{
			// define a new model
			//IloCplex cplex = new IloCplex();

			// permitted movements
			IloNumVar[][][] permittedDelta = new IloNumVar[armNumber][][];
			permittedLanes = new int[armNumber][][];
			
			for (int i = 0; i < armNumber; i++)
			{
				permittedDelta[i] = new IloNumVar[armDirNum.get(i)][armLanesNum.get(i)];
				permittedLanes[i] = new int[armDirNum.get(i)][armLanesNum.get(i)];
				for (int j = 0; j < armDirNum.get(i); j++)
				{
					permittedDelta[i][j] = cplex.boolVarArray(armLanesNum.get(i));
					//System.out.println(permittedDelta[i][j]);
				}
			}
			
			// Successor functions
			IloNumVar[][][][] successorOmega = new IloNumVar[armNumber][armNumber][][];
			for (int i = 0; i < armNumber; i++)
			{
				for (int l = 0; l < armNumber; l++)
				{
					successorOmega[i][l] = new IloNumVar[armDirNum.get(i)][armDirNum.get(l)];
					
					for (int j = 0; j < armDirNum.get(i); j++)
					{
						successorOmega[i][l][j] = cplex.boolVarArray(armDirNum.get(l));
					}
				}
			}
			// successorOmega[i][j][l][m] = cplex.boolVar();

			// Assigned flows q
			IloNumVar[][][] assignedFlow = new IloNumVar[armNumber][][];
			assignedFlowQ = new int [armNumber][][];
			for (int i = 0; i < armNumber; i++)
			{
				assignedFlow[i] = new IloNumVar[armDirNum.get(i)][armLanesNum.get(i)];
				assignedFlowQ[i] = new int[armDirNum.get(i)][armLanesNum.get(i)];
				
				for (int j = 0; j < armDirNum.get(i); j++)
				{
					assignedFlow[i][j] = cplex.numVarArray(armLanesNum.get(i), 0, Double.MAX_VALUE);
				}
			}
			
			// Common multiplier
			IloNumVar capacityMiu = cplex.numVar(0, Double.MAX_VALUE);
			// Cycle length
			IloNumVar cycleLengthEpslon = cplex.numVar(0, Double.MAX_VALUE);
			
			// Starts of green for movements
			IloNumVar[][] startOfGreenMovementTheta = new IloNumVar[armNumber][];
			startOfGreenMovement = new int[armNumber][];
			for (int i = 0; i < armNumber; i++)
			{
				startOfGreenMovement[i] = new int[armDirNum.get(i)];
				startOfGreenMovementTheta[i] = cplex.numVarArray(armDirNum.get(i), 0,1);
			}
			
			// Duration of green for movements
			IloNumVar[][] durationMovementPhi = new IloNumVar[armNumber][];
			durationMovement =new int[armNumber][];
			for (int i = 0; i < armNumber; i++)
			{
				durationMovement[i] = new int[armDirNum.get(i)];
				durationMovementPhi[i] = cplex.numVarArray(armDirNum.get(i), 0, 1);
			}
			
			// Starts of green for lanes
			IloNumVar[][] startOfGreenLaneTHETA = new IloNumVar[armNumber][];
			for (int i = 0; i < armNumber; i++)
			{
				startOfGreenLaneTHETA[i] = cplex.numVarArray(armLanesNum.get(i), 0, 1);
			}
			
			// Duration of green for lanes
			IloNumVar[][] durationLanePHI = new IloNumVar[armNumber][];
			durationLane = new int[armNumber][];
			for (int i = 0; i < armNumber; i++)
			{
				durationLane[i] = new int[armLanesNum.get(i)];
				durationLanePHI[i] = cplex.numVarArray(armLanesNum.get(i), 0, 1);
			}
			
			// /objective
			cplex.addMaximize(capacityMiu);
			// System.out.println("objective "+objective);
			
			//开始
			//////////////////////////////////////////////////////////////////////////////////
			
			// 1. Flow conservation(保护) --> 5)
			//////////////////////////////////////////////////////////////////////////////////
			IloLinearNumExpr[][] sumAssignedFlow = new IloLinearNumExpr[armNumber][];
			for (int i = 0; i < armNumber; i++)
			{
				sumAssignedFlow[i] = new IloLinearNumExpr[armDirNum.get(i)];
				
				for (int j = 0; j < armDirNum.get(i); j++)
				{
					sumAssignedFlow[i][j] = cplex.linearNumExpr();
					//assignedFlow q 求和, Equation (11) 
					for (int k = 0; k < armLanesNum.get(i); k++)
					{
						sumAssignedFlow[i][j].addTerm(1.0,assignedFlow[i][j][k]);
					}
				}
			}
			
			for (int i = 0; i < armNumber; i++)
			{
				for (int j = 0; j < armDirNum.get(i); j++)
				{
					//miu*Q = sum(q). Equation (11) 
					cplex.addEq(cplex.prod(capacityMiu, Qprime[i][j]),sumAssignedFlow[i][j]);
				    //System.out.println("capacity "+cplex.prod(capacityMiu,Qprime[i][j]));
				}
			}
			
			// 2. Minimum permitted movement on a lane --> 2)
			////////////////////////////////////////////////////////////////////////////////////////
			IloLinearNumExpr[][] permittedPerMovement = new IloLinearNumExpr[armNumber][];
			for (int i = 0; i < armNumber; i++)
			{
				permittedPerMovement[i] = new IloLinearNumExpr[armLanesNum.get(i)];
				
				for (int k = 0; k < armLanesNum.get(i); k++)
				{
					// Equation (7) sum(delta)>=1 
					permittedPerMovement[i][k] = cplex.linearNumExpr();
					
					for (int j = 0; j < armDirNum.get(i); j++)
					{
						permittedPerMovement[i][k].addTerm(1.0,permittedDelta[i][j][k]);
					}
					// System.out.println("permitted movement "+permittedPerMovement[i][k]);
					cplex.addGe(permittedPerMovement[i][k], 1.0);
				}
			}
			
			// 3. Maximum permitted (lanes) movements at the exit --> 3)
			////////////////////////////////////////////////////////////////////////////////////////
			IloLinearNumExpr[][] permittedPerDirection = new IloLinearNumExpr[armNumber][];
			for (int i = 0; i < armNumber; i++)
			{
				permittedPerDirection[i] = new IloLinearNumExpr[armDirNum.get(i)];
				
				for (int j = 0; j < armDirNum.get(i); j++)
				{
					// Equation (8) sum(delta)<=laneNumber
					// 这里laneNumber是出某个arm的车道数
					permittedPerDirection[i][j] = cplex.linearNumExpr();
					for (int k = 0; k < armLanesNum.get(i); k++)
					{
						permittedPerDirection[i][j].addTerm(1.0,permittedDelta[i][j][k]);
					}
					
					int outIndex = dirOutIndex[i][j];
					cplex.addGe(outLanesNum.get(outIndex), permittedPerDirection[i][j]);
					//System.out.println("************outLanesNum "+outLanesNum.get(outIndex));
				}
			}
			
			// 4. Assigned Flow of Prohibited (lane) movement -->6)
			////////////////////////////////////////////////////////
			double M = 1.0e15;   //arbitrary arge positive constant
			for (int i = 0; i < armNumber; i++)
			{
				for (int j = 0; j < armDirNum.get(i); j++)
				{
					for (int k = 0; k < armLanesNum.get(i); k++)
					{
						// Equation (12) 0=<q<=M*delta
						cplex.addGe(cplex.prod(M, permittedDelta[i][j][k]),assignedFlow[i][j][k]);
						// cplex.addGe(assignedFlow[i][j][k], 0.0);
					}
				}
			}
			
			// 5. Permitted movement across adjacent lanes -->4)
			//////////////////////////////////////////////////////////////////////
			for (int i = 0; i < armNumber; i++)
			{
				for (int j = 0; j < armDirNum.get(i); j++)
				{
					for (int k = 0; k < armLanesNum.get(i) - 1; k++)
					{
						for (int m = 0; m < j; m++)
						{
							// Equation (9) delta <= 1-delta(k+1)
							cplex.addGe(cplex.diff(1.0,permittedDelta[i][j][k]),permittedDelta[i][m][k+1]);
							
							// ?? delta >= delta(k+1)-1
							//cplex.addGe(permittedDelta[i][m][k], cplex.diff(permittedDelta[i][j][k + 1], 1.0));
							// System.out.println(j+ " "+cplex.diff(1,permittedDelta[i][j][k+1]));
						}
					}
				}
			}
			
			// 6. Cycle length -->9)
			/////////////////////////////////////////////////////////////////////
			// Equation (16) 1/c_min >= cycleLengthEpslon >= 1/c_max
			cplex.addGe(cycleLengthEpslon, 1.0 / CYCLETIMEMAX);
			cplex.addLe(cycleLengthEpslon, 1.0 / CYCLETIMEMIN);
			// System.out.println(1.0/minCycleTime);
			
			// 7. Identical signal setting of movements on shared lane -->10)
			/////////////////////////////////////////////////////////////////////
			for (int i = 0; i < armNumber; i++)
			{
				for (int j = 0; j < armDirNum.get(i); j++)
				{
					for (int k = 0; k < armLanesNum.get(i); k++)
					{
						// M(1-delta) >= startOfGreenLaneTHETA - startOfGreenMovementTheta
						cplex.addGe(cplex.prod(M,cplex.diff(1.0, permittedDelta[i][j][k])),
								    cplex.diff(startOfGreenLaneTHETA[i][k],startOfGreenMovementTheta[i][j]));
						
						// startOfGreenLaneTHETA-startOfGreenMovementTheta >= -M(1-permittedDelta)
						cplex.addGe(cplex.diff(startOfGreenLaneTHETA[i][k],startOfGreenMovementTheta[i][j]), 
								    cplex.prod(-M, cplex.diff(1.0, permittedDelta[i][j][k])));
						
						// M(1-delta) >= durationLanePHI - durationMovementPhi
						cplex.addGe(cplex.prod(M,cplex.diff(1.0, permittedDelta[i][j][k])),
								    cplex.diff(durationLanePHI[i][k],durationMovementPhi[i][j]));
						
						// durationLanePHI - durationMovementPhi >= -M(1-permittedDelta)
						cplex.addGe(cplex.diff(durationLanePHI[i][k],durationMovementPhi[i][j]), 
								    cplex.prod(-M,cplex.diff(1.0, permittedDelta[i][j][k])));
					}
				}
			}
			
			// 8. Start of green 9. Duration of green -->9) ??
			//////////////////////////////////////////////////////////////////
			for (int i = 0; i < armNumber; i++)
			{
				for (int j = 0; j < armDirNum.get(i); j++)
				{
					//Equation 18
					// cplex.addLe(durationMovementPhi[i][j], 1);
					cplex.addGe(durationMovementPhi[i][j],cplex.prod(GREENDURATIONMIN * 1.0, cycleLengthEpslon));
					//cplex.addGe(1.0,cplex.prod(cycleLengthEpslon,startOfGreenMovementTheta[i][j]));
					cplex.addGe(1.0,startOfGreenMovementTheta[i][j]);
				}
			}
			
			// 10. Signal sequence/Order of signal displays -->11)
			//////////////////////////////////////////////////////////////////
			for (int i = 0; i < armNumber; i++)
			{
				for (int l = 0; l < armNumber; l++)
				{
					for (int j = 0; j < armDirNum.get(i); j++)
					{
						for (int m = 0; m < armDirNum.get(l); m++)
						{
							// Equation (21 22)
							// System.out.println(clearanceTime[i][j][l][m]+" "+clearanceTime[l][m][i][j]);
							if (clearanceTime[i][l][j][m] != 0 || clearanceTime[l][i][m][j] != 0)//?????????
							{
								// System.out.println(i+" "+j+" "+l+" "+m+" "+clearanceTime[i][j][l][m]);
								cplex.addEq(cplex.sum(successorOmega[i][l][j][m],successorOmega[l][i][m][j]), 1.0);
							}
						}
					}
				}
			}
			
			// 11. Clearance time -->12)
			//////////////////////////////////////////////////////////////////
			for (int i = 0; i < armNumber; i++)
			{
				for (int l = 0; l < armNumber; l++)
				{
					for (int j = 0; j < armDirNum.get(i); j++)
					{
						for (int m = 0; m < armDirNum.get(l); m++)
						{
							//Equation 23
							if (clearanceTime[i][l][j][m] != 0 || clearanceTime[l][i][m][j] != 0)//???????
							{
								//Equation 23
								for (int k = 0; k < armLanesNum.get(i); k++)
								{
									for (int n = 0; n < armLanesNum.get(l); n++)
									{

										IloLinearNumExpr numExpr1 = cplex.linearNumExpr();
										numExpr1.addTerm(-1.0,permittedDelta[i][j][k]); //-delta
										numExpr1.addTerm(-1.0,permittedDelta[l][m][n]); //-delta
										
										IloLinearNumExpr numExpr3 = cplex.linearNumExpr();
										numExpr3.addTerm(1.0,startOfGreenMovementTheta[l][m]); //Theta(l m)
										numExpr3.addTerm(1.0,successorOmega[i][l][j][m]);      //Omega

										IloLinearNumExpr numExpr2 = cplex.linearNumExpr();
										numExpr2.addTerm(1.0,startOfGreenMovementTheta[i][j]); //Theta(i j)
										numExpr2.addTerm(1.0,durationMovementPhi[i][j]);       //Phi(i j)
										
										cplex.addGe(cplex.sum(numExpr3,cplex.prod(M,cplex.sum(2.0,numExpr1))),
												    cplex.sum(numExpr2,cplex.prod(clearanceTime[i][l][j][m] * 1.0,
																				  cycleLengthEpslon)));
									}
								}
							}
						}
					}
				}
			}
			
			// 12. Flow factor -->7)
			//////////////////////////////////////////////////////////////////
			IloLinearNumExpr[][] flowRatioY = new IloLinearNumExpr[armNumber][];
			
			for (int i = 0; i < armNumber; i++)
			{
				flowRatioY[i] = new IloLinearNumExpr[armLanesNum.get(i)];
				
				for (int k = 0; k < armLanesNum.get(i); k++)
				{
					flowRatioY[i][k] = cplex.linearNumExpr();
					
					for (int j = 0; j < armDirNum.get(i); j++)
					{
						// Equation (13) 
						flowRatioY[i][k].addTerm(1.0/(armDir.get(i).get(j).getSaturationFlow()),assignedFlow[i][j][k]);
					}
					// System.out.println(flowRatioY[i][k]);
				}
			}
			
			for (int i = 0; i < armNumber; i++)
			{
				for (int j = 0; j < armDirNum.get(i); j++)
				{
					for (int k = 0; k < armLanesNum.get(i) - 1; k++)
					{
						//Equation (14)
						IloLinearNumExpr num_expr1 = cplex.linearNumExpr();
						num_expr1.addTerm(1.0, permittedDelta[i][j][k]);
						num_expr1.addTerm(1.0, permittedDelta[i][j][k + 1]);
						
						cplex.addGe(cplex.prod(M, cplex.diff(2.0, num_expr1)),
								    cplex.diff(flowRatioY[i][k],flowRatioY[i][k + 1]));
						
						cplex.addLe(cplex.prod(-M, cplex.diff(2.0, num_expr1)),
								    cplex.diff(flowRatioY[i][k],flowRatioY[i][k + 1]));
					}
				}
			}
			
			// 13. Maximum acceptance degree of saturation -->8)
			/////////////////////////////////////////////////////////////////
			double maxDegreeOfSaturation = 0.9; //
		    int differenceEffectiveGreen = 1;   //

			for (int i = 0; i < armNumber; i++)
			{
				for (int k = 0; k < armLanesNum.get(i); k++)
				{
					IloLinearNumExpr num_expr1 = cplex.linearNumExpr();
					num_expr1.addTerm(maxDegreeOfSaturation,durationLanePHI[i][k]);
					num_expr1.addTerm(differenceEffectiveGreen*maxDegreeOfSaturation, cycleLengthEpslon);
					
					cplex.addGe(num_expr1, flowRatioY[i][k]);
				}
			}

			cplex.setOut(null);
//			cplex.setParam(IloCplex.Param.Simplex.Display, 0);
			cplex.exportModel("cplex.lp");
			
			//输出结果
			//////////////////////////////////////////////////////////////
			// solve model
			if (cplex.solve())
			{
				//System.out.println("obj = " + Math.round(cplex.getObjValue()*10000)*1.0/10000);
				//System.out.println("obj = " + cplex.getObjValue());
//				System.out.println("Permitted Delta");
				for (int i = 0; i < armNumber; i++)
				{
					for (int j = 0; j < armDirNum.get(i); j++)
					{
						for (int k = 0; k < armLanesNum.get(i); k++)
						{
//							System.out.println(i+" "+j+" "+k);
//							System.out.print(cplex.getValue(permittedDelta[i][j][k]) + " ");
							permittedLanes[i][j][k] = (int)cplex.getValue(permittedDelta[i][j][k]);
						}
//						System.out.println();
					}
			    }
//				System.out.println("Shared lanes");
//				for (int i = 0; i < armNumber; i++)
//				{
//					for (int k = 0; k < armLanesNum.get(i); k++)
//					{
//						int sum=0;
//						for (int j = 0; j < armDirNum.get(i); j++)
//						{
//							sum+=permittedLanes[i][j][k];
//						}
//						System.out.println("lane "+k+ " has "+sum+" movements");
//					}
//				}
				
				
				permittedDelta = null;
//				System.out.println("Assigned Flow");
				for (int i = 0; i < armNumber; i++)
				{
					for (int j = 0; j < armDirNum.get(i); j++)
					{
						for (int k = 0; k < armLanesNum.get(i); k++)
						{
//							System.out.println(i+" "+j+" "+k);
//						System.out.println(cplex.getValue(assignedFlow[i][j][k])/cplex.getObjValue() + " ");
							assignedFlowQ[i][j][k] = (int)Math.round(cplex.getValue(assignedFlow[i][j][k])/cplex.getObjValue());
						}
						//System.out.println();
					}
				}
				assignedFlow = null;

				cycleLength = (int)Math.round(1.0/cplex.getValue(cycleLengthEpslon));
//				System.out.println("cycle length = " + cycleLength);
				
//				System.out.println("Start of green");
				for (int i = 0; i < armNumber; i++)
				{
					for (int j = 0; j < armDirNum.get(i); j++)
					{
//						System.out.println(i + " " + j + " \t"
//											 + Math.round(cplex.getValue(startOfGreenMovementTheta[i][j])
//													     /cplex.getValue(cycleLengthEpslon)));
     					// System.out.println(i+" "+k+" "+cplex.getValue(startOfGreenLaneTHETA[i][k]));
						startOfGreenMovement[i][j] = (int)Math.round(cycleLength*cplex.getValue(startOfGreenMovementTheta[i][j]));
//						System.out.println("startOfGreenMovement "+startOfGreenMovement[i][j]);
					}
    			}
				startOfGreenMovementTheta = null;
//				System.out.println("green duration");
				for (int i = 0; i < armNumber; i++)
				{
					for (int j = 0; j < armDirNum.get(i); j++)
					{
//						System.out.println(i+" "+j+" \t"+ Math.round(cplex.getValue(durationMovementPhi[i][j]));
//										                                 /cplex.getValue(cycleLengthEpslon)));
						// System.out.println(i+" "+k+" "+(cplex.getValue(durationLanePHI[i][k])/cplex.getValue(cycleLengthEpslon)));
						durationMovement[i][j] = (int)Math.round(cycleLength*cplex.getValue(durationMovementPhi[i][j]));
//						System.out.println("durationMovement "+durationMovement[i][j]);
					}
				}
				durationMovementPhi = null;
				//System.out.println("green duration lane");
				for (int i = 0; i < armNumber; i++)
				{
					for (int k = 0; k < armLanesNum.get(i); k++)
					{
						durationLane[i][k] = (int)Math.round(cycleLength*cplex.getValue(durationLanePHI[i][k]));
						//System.out.println("durationMovementLane "+durationMovementLane[i][k]);
					}
					//System.out.println();
				}
				durationLanePHI = null;
				
				clearanceTime = null;
				Qprime = null;
			}
			else
			{
				System.out.println("***************Problem  NOT  Solved*****************");
				
				cplex.getStatus();
				System.exit(0);
			}
//			cplex.clearModel();
//			cplex.end();
			cplex.clearLazyConstraints();
			cplex.clearUserCuts();
			cplex.clearCuts();
			cplex.clearModel();
			
			//System.gc();
		}
		catch (IloException e)
		{
			e.printStackTrace();
		}
	}
	
	public void finalize()
	{           
        //super.finalize();  
        //System.out.println("####### LanebasedSinalTiming finalize method was called! #######");
    }
	
	
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
			//System.out.println("laneAssignedFlow in Lanebased.java: "+laneAssignedFlow[armIndex][k]);
		}
		}
	}
	
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
				//System.out.println("lane saturation in LanebasedSignalTiming.java: "+ laneSaturationFlow[armIndex][k]);
			}
		}
	}
	
	/**
	 * 使用 permittedLanes 计算  assignedFlowQ 的值
	 * @param armIndex
	 */
	public void assignedFlowCalculation(int armIndex, double demand[][])
	{

		int numOfVariables =numOfAssignedFlow(armIndex);
//		System.out.println("numOfVariables "+numOfVariables);
		double[][] matrixAinJava = new double[numOfVariables][numOfVariables];
		double[][] matrixBinJava = new double[numOfVariables][1];
		
		int variableRow=0;
		int variableColumn=0;

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
	
	
	
	//////////////////////////////////////////////////
	//             getter and setter                //
	//////////////////////////////////////////////////
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
	 * @return durationMovementPhi
	 */
	public int[][] getDurationMovement()
	{
		return durationMovement;
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
	public void setDurationLane(int[][] durationLane)
	{
		this.durationLane = durationLane;
	}

	/**
	 * @return durationMovementLane
	 */
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
