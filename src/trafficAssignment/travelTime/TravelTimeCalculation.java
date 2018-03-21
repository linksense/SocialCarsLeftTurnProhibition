/* --------------------------------------------------------------------
 * TravelTimeCalculation.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and all Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 29.04.2016
 * 
 * Function:
 *           1.计算TravelTime
 *           
 */
package trafficAssignment.travelTime;

import java.util.ArrayList;
import java.util.List;

import org.ujmp.core.Matrix;

import signalOptimization.SignalTiming;
import signalOptimization.LanebasedSinalTiming;
import signalOptimization.StagebasedSignalTiming;
import network.Junction;
import network.Link;
import network.Network;
import network.graph.Vertex;
import ilog.cplex.*;

public class TravelTimeCalculation 
{
	//~Variables
	//--------------------------------------------------------------------------
	private Network network = null;;
	private double[] travelTime = null;  //存储Travel Time
	private double[] degreeOfSaturation = null; //存储饱和流率
	Matrix modFoesMatrix = null;
	
	private IloCplex cplex = null;
	//private LanebasedSinalTiming signalTiming=null;
	
	private SignalTiming signalTiming = null;
	
	private List<Vertex> arms = new ArrayList<Vertex>();                      //所有的arms
	private List<Vertex> outs = new ArrayList<Vertex>();                      //所有的outs
	private List<Integer> outLanesNum = new ArrayList<Integer>();             //驶出点的车道数
	private int [][] dirOutIndex;
	private int armNumber; //Arm的个数
	private List<Integer> armDirNum = new ArrayList<Integer>();               //arm的转向数
	private List<ArrayList<Link>> armDir = new ArrayList<ArrayList<Link>>();  //每个ARM的转向内部通路
	private List<Integer> armLanesNum = new ArrayList<Integer>();             //arm的车道数
	
	private List<Link> extLinks = new ArrayList<Link>();
	private List<Link> intLinks = new ArrayList<Link>();
	
	private double[][] demand;
	private double[][] fixedDemandForSignal;
	private List<Junction> junctions;
	private List<Link> links;
	private Junction junc;
	
	private double[][] delayPerMovement; 
	private int cycleLength;
	public int timeUnit =60; // it it is 3600, the time unit is hour, if it is 60, the time unit is minite
	private double observationTime =0.25;
	
	private int[][] laneAssignedFlow;
	private int[][] laneSaturationFlow;
	
	private String methodeType = null;
	
	
	//~Methods
	//--------------------------------------------------------------------------
	public TravelTimeCalculation(Network network,IloCplex cplex, String methodeType)
	{
		this.network = network;
		
		//this.signalTiming = signalTiming;
		this.cplex = cplex;
		travelTime = new double[network.getLinks().size()];
		degreeOfSaturation = new double[network.getLinks().size()];
		
		this.methodeType = methodeType;
	}
	
	
	/**
	 * 通过Link Flow,FreeFlowTravelTime和Saturation Flow
	 * 来计算Travel Time
	 * @param network
	 * @param linkFlow
	 * @param a
	 * @param b
	 */
	public void byBPRFunction(double[] linkFlowAllOD,double a,double b)
	{
		for(int i=0;i<network.getLinks().size();i++)        //依次计算Travel Time
		{
			//travelTime[i] = 0.0;
			travelTime[i] = (network.getLinks().get(i).getFreeFlowTravelTime()/timeUnit)*
					(1+a*Math.pow(linkFlowAllOD[i]/((double)network.getLinks().get(i).getSaturationFlow()*network.getLinks().get(i).getLaneCount()),b));//计算TravelTime
			//System.out.println("BPR travel time: "+travelTime[i]);
		}
	}

	
	public void byLBSTFunctionPreparation()
	{
		//1. 初始化Travel Time
		///////////////////////////////////////////////////////
		for(int i=0;i<network.getLinks().size();i++)//依次计算Travel Time
		{
			travelTime[i] = 0.0;
			double freeFlowTravelTime=network.getLinks().get(i).getFreeFlowTravelTime()/timeUnit;	
			travelTime[i] = freeFlowTravelTime;//计算TravelTime
			degreeOfSaturation[i]=0.0;
			//System.out.println("travel time 初始化： "+travelTime[i]);
		}

		//2. 以Junc为单位进行solveMe
		//////////////////////////////////////////////////////
		armNumber = 0; //Arm的个数
		//int outNumber = 0; //Out的个数

		junctions = network.getJunctions();	
		links = network.getLinks();
//		System.out.println("junctions.size(): "+junctions.size());

	}
	
	public void byLBSTFunctionPreparationOneJunction(int[] linkFlowAllOD, int junctionIdex)
	{	
		// i denotes the junction index 
		
		//1. 获取当前的Junction
		/////////////////////////////////////////////////
		//System.out.println("junctions.size() in TravelTime: "+junctions.size());
		junc = junctions.get(junctionIdex);
//		String juncName = junc.getName();
//		System.out.println("当前路口:"+ juncName);

		intLinks    = junc.getInternalLinks(); //内部通路
		extLinks    = junc.getExternalLinks(); //外部道路
		//Set<Vertex> intVertexs = junc.getInternalNode();  //内部点

		//Matrix foesMatrix      = junc.getFoesMatrix();    //路口内部冲突矩阵
		//System.out.println("路口"+juncName+"的冲突矩阵：");
		//System.out.println(foesMatrix);
		//System.out.println("intVertexs" + intVertexs);
		//2. 要提取的运算参数
		//////////////////////////////////////////////////
		//List<Link> intLeftTurnLinks = new ArrayList<Link>();              //路口的左转边

		//List<Integer> outDirNum = new ArrayList<Integer>();               //以某点为驶出点的所有转向

		arms = new ArrayList<Vertex>();                      //所有的arms
		armDir = new ArrayList<ArrayList<Link>>();  //每个ARM的转向内部通路
		armLanesNum = new ArrayList<Integer>();             //arm的车道数
		armDirNum = new ArrayList<Integer>();               //arm的转向数

		outs = new ArrayList<Vertex>();                      //所有的outs
		//List<Integer> outDirNum = new ArrayList<Integer>();               //以某点为驶出点的所有转向
		outLanesNum = new ArrayList<Integer>();             //驶出点的车道数
		
		//if(intLinks.size()!=0)System.out.println("当前路口:"+ juncName);
		//3. 归类提取信息
		/////////////////////////////////////////////////
		for(int j=0; j<intLinks.size(); j++) //找出所有的起始点 ARM
		{
			Link link    = intLinks.get(j);
			Vertex begin = link.getBegin();
			Vertex end   = link.getEnd();
			link = null;
			
			//arm,即为起始点, 则提取其转向内部通路
			// if(link.getDirection().equals("l") || link.getDirection().equals("L"))
			// {
			// 	    intLeftTurnLinks.add(link); //若为左转，记录之
			// }
			//System.out.println(arms);
			
			if(arms.indexOf(begin) == -1)//若该点在arm里不存在，添加之
			{
				arms.add(begin);
				armLanesNum.add(begin.getLaneNum()); //记录此arm的Lane数目
			}

			if(outs.indexOf(end) == -1)//若该点在outs里不存在，添加之
			{
				outs.add(end);
				outLanesNum.add(end.getLaneNum()); //记录此outs的Lane数目
			}
		}


		for(int v=0; v<arms.size(); v++) //找出所有的起始点 ARM
		{
			ArrayList<Link> dir = new ArrayList<Link>();//存储当前内部点的转向内部边
			Vertex vertex = arms.get(v);

			for(int j=0; j<intLinks.size(); j++) 
			{
				Link link = intLinks.get(j);
				Vertex begin = link.getBegin();

				if(begin.equals(vertex))
				{
					dir.add(link); //该ARM的转向
				}
			}

			if(!dir.isEmpty()) //该点为arm,
			{
				armDir.add(dir); //添加其转向
			}
		}
		//System.out.println(arms.isEmpty() );
		//System.out.println(outs.isEmpty() );
		//System.out.println(junc.getPhaseList().isEmpty() );
		
		//4. 若为有转向信息的路口，进行计算
		//////////////////////////////////////////////////////////////////
		if(!arms.isEmpty() && !outs.isEmpty() && !junc.getPhaseList().isEmpty()) //有arm和驶出点
		{
			//1. 获取demand矩阵
			//   是基于新的SUE结果的, 用SUE的内部边flow结果填充
			//------------------------------------------------------
			armNumber = arms.size(); //当前路口ARM的数量
			//outNumber = outs.size(); //当前路口OUT的数量
			//System.out.println("outs.size() "+outs.size());

			demand  = new double[armNumber][];
			dirOutIndex = new int[armNumber][];
			fixedDemandForSignal = new double[armNumber][];
			for(int k=0; k<arms.size(); k++) //遍历每个arm
			{
//				if(armDir.get(k).size()<3)
//				System.out.println("ARM "+k+":<"+arms.get(k)+ 
//						"> ArmLaneNum:"+armLanesNum.get(k)+
//						" ArmDirNum:"+armDir.get(k).size());

				//demand和armDirNum
				////////////////////////////////////////////////////////////
				demand[k] = new double[armDir.get(k).size()];
				fixedDemandForSignal[k] = new double[armDir.get(k).size()];
				armDirNum.add(armDir.get(k).size()); //某arm的转向个数
				dirOutIndex[k] = new int[armDir.get(k).size()];
				for(int m=0; m<armDir.get(k).size(); m++) //每个arm对应的转向信息
				{
					//System.out.print(armDir.get(k).get(m));
					//demand
					////////////////////////////////////////////////
					int index = links.indexOf(armDir.get(k).get(m)); //某个arm的某个方向		某个arm的某个转向的link的index										
					double value = linkFlowAllOD[index];             //该转向的Flow
					demand[k][m] = value;                            //demand赋值
					fixedDemandForSignal[k][m] = value; 
					//System.out.println("demand "+k+" "+m+" "+demand[k][m]);
					//转向
					////////////////////////////////////////////////
					int indexOut = outs.indexOf(armDir.get(k).get(m).getEnd()); //
					dirOutIndex[k][m] = indexOut;	
				}

			}
			if(modFoesMatrix == null)
			{
				junc.initModFoesMatrix();
				modFoesMatrix = junc.getModFoesMatrix();
			}
//			System.out.println("修改前路口冲突矩阵:");
//			System.out.println(modFoesMatrix);
	        
			// left turn type determination
			leftTurnTypeDetermination();
			
	        ////冲突矩阵
			// 此处应该修改冲突矩阵！！
            // modFoesMatrix = junc.getModFoesMatrix();
					
			modifyConflictMatrix();
//			System.out.println("修改后路口冲突矩阵:");
//			System.out.println(modFoesMatrix);
			
			//Matrix 
			
			////////////////////////////////////////////////////////
			//                计算绿灯时间等                                                       //
			////////////////////////////////////////////////////////
				
//			signalTiming = new LanebasedSinalTiming(cplex);
//			signalTiming.solveMe(armNumber,demand,dirOutIndex,modFoesMatrix,armDir,
//					armLanesNum,armDirNum,outLanesNum,extLinks);
			
		
			//System.out.println("modFoesMatrix in TravelTimeCalculation.java: "+modFoesMatrix);
			
			if(methodeType.equals("StageBased"))
			{
				signalTiming = new StagebasedSignalTiming(cplex);
				signalTiming.solveMe(modFoesMatrix, junc.getMarkerList(), 
									 fixedDemandForSignal, armNumber, armDir,
									 armLanesNum, armDirNum);
			}
			else if(methodeType.equals("LaneBased"))
			{
				signalTiming = new LanebasedSinalTiming(cplex);		
				signalTiming.solveMe(armNumber,fixedDemandForSignal, 
									 dirOutIndex,modFoesMatrix,armDir,
		                             armLanesNum,armDirNum,outLanesNum,extLinks);	
			}
			else
			{
				
			}
			
			cycleLength = signalTiming.getCycleLengthEpslon();
			//System.out.println("Cycle length in TravelTimeCalculation.java: "+signalTiming.getCycleLengthEpslon());
		}	

	}
	
	
	public void byLBSTFunctionPreparationOneJunction(int[] linkFlowAllOD, int junctionIdex, int commonCycle)
	{	
		// i denotes the junction index 
		
		//1. 获取当前的Junction
		/////////////////////////////////////////////////
		//System.out.println("junctions.size() in TravelTime: "+junctions.size());
		junc = junctions.get(junctionIdex);
//		String juncName = junc.getName();
//		System.out.println("当前路口:"+ juncName);

		intLinks    = junc.getInternalLinks(); //内部通路
		extLinks    = junc.getExternalLinks(); //外部道路
		//Set<Vertex> intVertexs = junc.getInternalNode();  //内部点

		//Matrix foesMatrix      = junc.getFoesMatrix();    //路口内部冲突矩阵
		//System.out.println("路口"+juncName+"的冲突矩阵：");
		//System.out.println(foesMatrix);
		//System.out.println("intVertexs" + intVertexs);
		//2. 要提取的运算参数
		//////////////////////////////////////////////////
		//List<Link> intLeftTurnLinks = new ArrayList<Link>();              //路口的左转边

		//List<Integer> outDirNum = new ArrayList<Integer>();               //以某点为驶出点的所有转向

		arms = new ArrayList<Vertex>();                      //所有的arms
		armDir = new ArrayList<ArrayList<Link>>();  //每个ARM的转向内部通路
		armLanesNum = new ArrayList<Integer>();             //arm的车道数
		armDirNum = new ArrayList<Integer>();               //arm的转向数

		outs = new ArrayList<Vertex>();                      //所有的outs
		//List<Integer> outDirNum = new ArrayList<Integer>();               //以某点为驶出点的所有转向
		outLanesNum = new ArrayList<Integer>();             //驶出点的车道数
		
		//if(intLinks.size()!=0)System.out.println("当前路口:"+ juncName);
		//3. 归类提取信息
		/////////////////////////////////////////////////
		for(int j=0; j<intLinks.size(); j++) //找出所有的起始点 ARM
		{
			Link link    = intLinks.get(j);
			Vertex begin = link.getBegin();
			Vertex end   = link.getEnd();
			link = null;
			//arm,即为起始点, 则提取其转向内部通路
			// if(link.getDirection().equals("l") || link.getDirection().equals("L"))
			// {
			//	    intLeftTurnLinks.add(link); //若为左转，记录之
			// }
			//System.out.println(arms);
			if(arms.indexOf(begin) == -1)//若该点在arm里不存在，添加之
			{
				arms.add(begin);
				armLanesNum.add(begin.getLaneNum()); //记录此arm的Lane数目
			}

			if(outs.indexOf(end) == -1)//若该点在outs里不存在，添加之
			{
				outs.add(end);
				outLanesNum.add(end.getLaneNum()); //记录此outs的Lane数目
			}
		}


		for(int v=0; v<arms.size(); v++) //找出所有的起始点 ARM
		{
			ArrayList<Link> dir = new ArrayList<Link>();//存储当前内部点的转向内部边
			Vertex vertex = arms.get(v);

			for(int j=0; j<intLinks.size(); j++) 
			{
				Link link = intLinks.get(j);
				Vertex begin = link.getBegin();

				if(begin.equals(vertex))
				{
					dir.add(link); //该ARM的转向
				}
			}

			if(!dir.isEmpty()) //该点为arm,
			{
				armDir.add(dir); //添加其转向
			}
		}
		//System.out.println(arms.isEmpty() );
		//System.out.println(outs.isEmpty() );
		//System.out.println(junc.getPhaseList().isEmpty() );
		
		//4. 若为有转向信息的路口，进行计算
		//////////////////////////////////////////////////////////////////
		if(!arms.isEmpty() && !outs.isEmpty() && !junc.getPhaseList().isEmpty()) //有arm和驶出点
		{
			//1. 获取demand矩阵
			//   是基于新的SUE结果的, 用SUE的内部边flow结果填充
			//------------------------------------------------------
			armNumber = arms.size(); //当前路口ARM的数量
			//outNumber = outs.size(); //当前路口OUT的数量
			//System.out.println("outs.size() "+outs.size());

			demand  = new double[armNumber][];
			dirOutIndex = new int[armNumber][];
			fixedDemandForSignal = new double[armNumber][];
			for(int k=0; k<arms.size(); k++) //遍历每个arm
			{
//				if(armDir.get(k).size()<3)
//				System.out.println("ARM "+k+":<"+arms.get(k)+ 
//						"> ArmLaneNum:"+armLanesNum.get(k)+
//						" ArmDirNum:"+armDir.get(k).size());

				//demand和armDirNum
				////////////////////////////////////////////////////////////
				demand[k] = new double[armDir.get(k).size()];
				fixedDemandForSignal[k] = new double[armDir.get(k).size()];
				armDirNum.add(armDir.get(k).size()); //某arm的转向个数
				dirOutIndex[k] = new int[armDir.get(k).size()];
				for(int m=0; m<armDir.get(k).size(); m++) //每个arm对应的转向信息
				{
					//System.out.print(armDir.get(k).get(m));
					//demand
					////////////////////////////////////////////////
					int index = links.indexOf(armDir.get(k).get(m)); //某个arm的某个方向		某个arm的某个转向的link的index										
					double value = linkFlowAllOD[index];             //该转向的Flow
					demand[k][m] = value;                            //demand赋值
					fixedDemandForSignal[k][m] = value; 
					//System.out.println("demand "+k+" "+m+" "+demand[k][m]);
					//转向
					////////////////////////////////////////////////
					int indexOut = outs.indexOf(armDir.get(k).get(m).getEnd()); //
					dirOutIndex[k][m] = indexOut;	
				}

			}
			if(modFoesMatrix == null)
			{
				junc.initModFoesMatrix();
				modFoesMatrix = junc.getModFoesMatrix();
			}
//			System.out.println("修改前路口冲突矩阵:");
//			System.out.println(modFoesMatrix);
	//// left turn type determination
			leftTurnTypeDetermination();
			
	////冲突矩阵

			// 此处应该修改冲突矩阵！！
//			modFoesMatrix = junc.getModFoesMatrix();
					
			modifyConflictMatrix();
//			System.out.println("修改后路口冲突矩阵:");
//			System.out.println(modFoesMatrix);
			
			//Matrix 
			
		////////////////////////////////////////////////////////
		//                计算绿灯时间等                                                       //
		////////////////////////////////////////////////////////
				
//			signalTiming = new LanebasedSinalTiming(cplex);
//			signalTiming.solveMe(armNumber,demand,dirOutIndex,modFoesMatrix,armDir,
//					armLanesNum,armDirNum,outLanesNum,extLinks);
			
		
			//System.out.println("modFoesMatrix in TravelTimeCalculation.java: "+modFoesMatrix);
			
			if(methodeType.equals("StageBased"))
			{
				signalTiming = new StagebasedSignalTiming(cplex);
				signalTiming.afterGettingCommonCycle(modFoesMatrix, junc.getMarkerList(), fixedDemandForSignal, armNumber, armDir,
						armLanesNum, armDirNum,commonCycle);
			}
			else
			{
				System.out.println("应指定methodeType为 StageBased !");
				System.exit(0);
			}
			//cycleLength = signalTiming.getCycleLengthEpslon();
			//System.out.println("Cycle length in TravelTimeCalculation.java: "+signalTiming.getCycleLengthEpslon());
		}	

	}
	
//	public void byLBSTFunctionPreparationOneJunctionCycleLength(int[] linkFlowAllOD, int junctionIdex)
//	{
//		signalTiming = new StagebasedSignalTiming();
//		signalTiming.solveMe(modFoesMatrix, junc.getMarkerList(), demand, armNumber, armDir,
//				armLanesNum, armDirNum);
//	}
	
	public void byLBSTFunctionOneJunction(int junctionIdex,int commonCycleLength,double[] oldLinkFlowAllOD)
	{
		// i denotes the junction index 
		//  System.out.println("common cycle length in TravelTimeCalculation.java: "+commonCycleLength);			
		Junction junc = junctions.get(junctionIdex);
//		List<Link> intLinks    = junc.getInternalLinks(); //内部通路
//		List<Link> extLinks    = junc.getExternalLinks(); //外部道路		
		
		if(!arms.isEmpty() && !outs.isEmpty() && !junc.getPhaseList().isEmpty()) //有arm和驶出点
		{
			armNumber = arms.size(); //当前路口ARM的数量
			//armDir
			
			demand = new double[armNumber][];
			//System.out.println("demand value in byLBSTFunctionOneJunction()");
			for(int k=0;k<arms.size();k++)
			{
				demand[k] =  new double[armDir.get(k).size()];
				for(int m=0;m<armDir.get(k).size();m++)
				{
					int index = links.indexOf(armDir.get(k).get(m));
					double value = oldLinkFlowAllOD[index];
					demand[k][m] = value;
					//System.out.println("demand["+k+"]["+m+"]: "+demand[k][m]);
				}
			}
			
			outLanesNum = null; //驶出点的车道数
			//foesMatrix  = null; //路口内部冲突矩阵
			dirOutIndex = null;	

		//////////////////////////////////////////////////////////
			//5. 在Junction里的每一个arm里计算每一条lane的Delay      //
			//////////////////////////////////////////////////////////
			for (int itemp = 0; itemp < armNumber; itemp++)
			{
				signalTiming.assignedFlowCalculation(itemp,demand);
			}
			signalTiming.laneAssignedFlowCalculation();
			signalTiming.laneSaturationFlow();
			saturationFlowForLane();
			
			for (int itemp = 0; itemp < armNumber; itemp++)
			{
				//signalTiming.assignedFlowCalculation(itemp,demand);
				delayPerMovementCalculation(armNumber,armDir,armLanesNum,armDirNum,arms,junc,commonCycleLength,demand);
				////////////////////////////////////////
				//计算link travel time
				////////////////////////////////////////
				for (int j = 0; j < armDirNum.get(itemp); j++)
				{
					Link currLink = armDir.get(itemp).get(j);
					int indexCurrLink = network.getLinks().indexOf(currLink);
					double freeFlowTravelTime = currLink.getFreeFlowTravelTime()/timeUnit; // unit in minutes
					double linkDelay = delayPerMovement[itemp][j]/timeUnit;
					
					//degree of saturation 还没算出来 2017.02.28
//					degreeOfSaturation[indexCurrLink]=signalTiming.laneFlowCalculation(itemp, indexLaneForDir, armDirNum)
//							/signalTiming.saturationFlowForLane(itemp, indexLaneForDir, armDir, armDirNum);
					double LinkTravelTime = freeFlowTravelTime + linkDelay;

					travelTime[indexCurrLink] = LinkTravelTime;
					//System.out.println("signalTiming.delayPerMovement in TravelTimeCalculation.java: "+linkDelay);
					//System.out.println("LinkTravelTime in TravelTimeCalculation.java: "+LinkTravelTime);
				}
			}
			
			signalTiming.setAssignedFlowQ(null);
			signalTiming.setDurationMovement(null);
			signalTiming.setDurationMovement(null);
			signalTiming.setPermittedLanes(null);
			signalTiming.setStartOfGreenMovement(null);
			signalTiming = null;
			demand       = null;
			delayPerMovement = null;
		}//有转向信息的Junction

		arms        = null; //所有的arms
		outs        = null; //所有的outs
		extLinks    = null; //外部道路
		intLinks    = null; //内部通路
		armDir      = null; //每个ARM的转向内部通路
		armLanesNum = null; //arm的车道数
		armDirNum   = null; //arm的转向数
		modFoesMatrix = null;
//		cplex = null;
//		System.out.println("cplex在TravelTimeCalculation.java 的最后被设为null ");
		//arms = new ArrayList<Vertex>();                      //所有的arms
		//outs = new ArrayList<Vertex>();                      //所有的outs
		//armDirNum = new ArrayList<Integer>();               //arm的转向数
		//armDir = new ArrayList<ArrayList<Link>>();  //每个ARM的转向内部通路
		//armLanesNum = new ArrayList<Integer>();             //arm的车道数
		
		junc = null;
	//System.gc();
	}
	
	public void byLBSTFunctionReset()
	{
		links     = null;
		junctions = null;		
	}
	
	public void leftTurnTypeDetermination()
	{
		for (int i = 0; i < armNumber; i++)
		{
			Link leftTurnLink = null;
			Link oppTHLink =null;
			int oppArmIndex = -1;//对面直行的arm信息
			double leftTurnFlow=0;
			double oppTHFlow=0;
			for (int j = 0; j < armDirNum.get(i); j++)
			{
				if((armDir.get(i).get(j).getDirection().equals("l")||armDir.get(i).get(j).getDirection().equals("L")))
				{
					// i = left turn arm index
					leftTurnLink = armDir.get(i).get(j);
					leftTurnFlow =demand[i][j];
					oppArmIndex = opposingThroughArmIndex(i);
					//System.out.println("left turn arm index: "+i+" type: "+armDir.get(i).get(j).getDirection());
//					System.out.println("left turn arm index: "+i+" oppArmIndex: "+oppArmIndex);
					if(oppArmIndex!=-1)
					{
						int oppTHDirIndex=0;
						for (int j2 = 0; j2 < armDirNum.get(oppArmIndex); j2++)
						{
							String dirS = armDir.get(oppArmIndex).get(j2).getDirection();//该arm的第j个转向
							//System.out.println("dirS "+dirS);
							if(dirS.equals("s") || dirS.equals("S")) //若为直行
							{
								oppTHDirIndex=j2;
								oppTHFlow =demand[oppArmIndex][oppTHDirIndex];
								oppTHLink = armDir.get(oppArmIndex).get(oppTHDirIndex); //对面直行的信息，link类型
								break;
							}
						}
						//1. 对于arm i j=S 对k求和  
						//System.out.println("leftTurnLink.getLaneCount(): "+leftTurnLink.getLaneCount());
//						System.out.println("oppTHLink.getLaneCount(): "+oppTHLink.getLaneCount());
						int opposingTHLane = oppTHLink.getLaneCount(); 
						
						if(leftTurnFlow > 240)
						{
							leftTurnLink.setDirection("L");
						}
						else if(opposingTHLane == 1 && (leftTurnFlow*oppTHFlow) > 50000)
						{
							leftTurnLink.setDirection("L");
						}
						else if(opposingTHLane == 2 && (leftTurnFlow*oppTHFlow) > 90000)
						{
							leftTurnLink.setDirection("L");
						}
						else if(opposingTHLane >= 3 && (leftTurnFlow*oppTHFlow) > 110000)
						{
							leftTurnLink.setDirection("L");
						}
						else //LT Permissive
						{
							leftTurnLink.setDirection("l");
						}
					}
				}
			}
		}
	}
	
	public void modifyConflictMatrix()
	{
		junc.initModFoesMatrix();
		modFoesMatrix = junc.getModFoesMatrix();
		
		for (int i = 0; i < armNumber; i++)
		{
			Link leftTurnLink = null;
			int oppArmIndex = -1;//对面直行的arm信息
			Link oppTHLink = null;
			
			for (int j = 0; j < armDirNum.get(i); j++)
			{
				if((armDir.get(i).get(j).getDirection().equals("l")))
				{
					// i = left turn arm index
					leftTurnLink = armDir.get(i).get(j);
					oppArmIndex = opposingThroughArmIndex(i);
					if(oppArmIndex!=-1)
					{
						for (int j2 = 0; j2 < armDirNum.get(oppArmIndex); j2++)
						{
							String dirS = armDir.get(oppArmIndex).get(j2).getDirection();//该arm的第j个转向

							if(dirS.equals("s") || dirS.equals("S")) //若为直行
							{
								oppTHLink = armDir.get(oppArmIndex).get(j2); //对面直行的信息，link类型
								break;
							}
						}
						int row    = intLinks.indexOf(leftTurnLink);
						int column = intLinks.indexOf(oppTHLink);
						
						junc.modifyFoesMatrix(row, column, 0);
						junc.modifyFoesMatrix(column, row, 0);
					}
				}
				if((armDir.get(i).get(j).getDirection().equals("L")))
				{
					// i = left turn arm index
					leftTurnLink = armDir.get(i).get(j);
					oppArmIndex = opposingThroughArmIndex(i);
					if(oppArmIndex!=-1)
					{
						for (int j2 = 0; j2 < armDirNum.get(oppArmIndex); j2++)
						{
							String dirS = armDir.get(oppArmIndex).get(j2).getDirection();//该arm的第j个转向

							if(dirS.equals("s") || dirS.equals("S")) //若为直行
							{
								oppTHLink = armDir.get(oppArmIndex).get(j2); //对面直行的信息，link类型
								break;
							}
						}
						int row    = intLinks.indexOf(leftTurnLink);
						int column = intLinks.indexOf(oppTHLink);
						
						junc.modifyFoesMatrix(row, column, 1);
						junc.modifyFoesMatrix(column, row, 1);
					}
				}
			}
		}
	}
	
	public void delayPerMovementCalculation(int armNumber,List<ArrayList<Link>> armDir,
            List<Integer> armLanesNum,List<Integer> armDirNum, List<Vertex> arms,Junction junc, int commonCycleLength, double[][] demand)
	{
		
		delayPerMovement = new double[armNumber][];
		//saturationFlowAdjustment satFlow = new saturationFlowAdjustment(junc, signalTiming, demand, armDir,arms,armDirNum);
		for (int i = 0; i < armNumber; i++)
		{
			delayPerMovement[i] = new double[armDirNum.get(i)];
		}
		for (int i = 0; i < armNumber; i++)
		{
			for (int j = 0; j < armDirNum.get(i); j++)
			{
				delayPerMovement[i][j]=0;
				
				//此处加入左转类型，如果是permissive的，需要对saturation进行更新
				if((armDir.get(i).get(j).getDirection().equals("l")))
				{
					
					double saturationPermLT = saturationFlowPermissiveLT(signalTiming.getDurationMovement()[i][j], i);			
					armDir.get(i).get(j).setSaturationFlow((int)saturationPermLT);
					//System.out.println(armHavingLeftTurn.get(i).get(j)+ " Saturation Flow of permissive LT in LanebasedSignalTiming.java: "+(int)saturationPermLT);
				}
				
				for(int k=0;k<armLanesNum.get(i);k++)
				{ 
					if(signalTiming.getPermittedDelta()[i][j][k] ==1)
					{				
						Link currLink = armDir.get(i).get(j);
						int indexCurrLink = network.getLinks().indexOf(currLink);
						
						double laneSaturation =laneSaturationFlow[i][k];
						//double laneFlow =laneAssignedFlow[i][k];
						double laneFlow =signalTiming.getLaneAssignedFlow()[i][k];
						
						//System.out.println("newFlowOfOldFlow[indexCurrLink] in delayPerMovement() in TravelTimeCalculation.java: "+newFlowOfOldFlow[indexCurrLink]);
						//System.out.println("laneFlow in delayPerMovement() in TravelTimeCalculation.java: "+laneFlow);
						//System.out.println("laneSaturationFlow in delayPerMovement() in TravelTimeCalculation.java: "+laneSaturation);
						//System.out.println("signalTiming.getCycleLengthEpslon() in delayPerMovement() in TravelTimeCalculation.java: "+signalTiming.getCycleLengthEpslon());
						//System.out.println("signalTiming.getDurationLane()[i][k] in delayPerMovement() in TravelTimeCalculation.java: "+signalTiming.getDurationLane()[i][k]);
						double delayPerLane = delayPerLane(laneFlow,laneSaturation,commonCycleLength,signalTiming.getDurationLane()[i][k],observationTime);
						delayPerMovement[i][j] = delayPerLane;
						//System.out.println("delayPerMovement[i][j] in LanebasedSignalTiming.java "+delayPerMovement[i][j]);				
						//System.out.println("laneSaturationFlow in TravelTimeCalculation.java: "+laneSaturationFlow);
						//System.out.println("signalTiming.getDurationLane()[i][k]: "+signalTiming.getDurationLane()[i][k]);
						double degreeOfS = degreeOfSaturationCalculation(laneFlow,laneSaturation,commonCycleLength,signalTiming.getDurationLane()[i][k]);
						//System.out.println("degreeOfS "+degreeOfS);
						degreeOfSaturation[indexCurrLink] = degreeOfS;
					}
//					else
//						delayPerMovement[i][j]=0;
				//System.out.println(permittedDelta[i][j]);
				}
				
				// 将permissive的饱和流还原，便于下次迭代使用
				if((armDir.get(i).get(j).getDirection().equals("l")))
				{		
					armDir.get(i).get(j).setSaturationFlow((int)(1900*0.95));
					//System.out.println(armHavingLeftTurn.get(i).get(j)+ " Saturation Flow of permissive LT in LanebasedSignalTiming.java: "+(int)saturationPermLT);
				}
			}
		}
	}
	
	
	/* units:
	 * laneFlow: veh/hour; laneFlow = sumAssignedFlow/reservedCapacityMu
	 * observationTime: hour
	 * saturation flow: veh/hour
	 * cycleLength: second
	 * greenDuration: second
	 * reference: Akcelik 1981
	 * */
	public double delayPerLane(double laneFlow,double laneSaturationFlow,int cycleLength,int greenDuration,double observationTime)
	{
		double delay =0;
		double u=1.0*greenDuration/cycleLength;
		double capacity = laneSaturationFlow*u;
		double x = laneFlow/capacity; //lane flow = assigned flow
		double y = laneFlow/laneSaturationFlow;
		double z = x-1;
		
		if(greenDuration>0 && laneSaturationFlow!=0)
		{
			double uniformDelay = 0;//unit is second
			if(x<1)
				uniformDelay = 0.5*cycleLength*(1-u)*(1-u)/(1-y);
			else
				uniformDelay = 0.5*(cycleLength-greenDuration);
		
			double incrementalDelay =0;
			double x0=0.67+laneSaturationFlow*greenDuration/(600*3600);
			if(x<x0)
				incrementalDelay=0;
			else
				incrementalDelay =900*observationTime*(z+Math.sqrt(z*z+12*(x-x0)/(capacity*observationTime)));
		
			delay = uniformDelay+incrementalDelay;
		//System.out.println("uniformDelay "+uniformDelay);
		//System.out.println("incrementalDelay "+incrementalDelay);
		}
		return delay;
	}
	
	public double degreeOfSaturationCalculation(double laneFlow,double laneSaturationFlow,int cycleLength,int greenDuration)
	{
		double degreeOfS=0;
		double u=1.0*greenDuration/cycleLength;
		double capacity = laneSaturationFlow*u;
		
		if(greenDuration>0 && laneSaturationFlow!=0)
		{
			degreeOfS = laneFlow/capacity;
		}
		
		return degreeOfS;
	}
	

//	public void laneFlowCalculation(double newflowOfOldflow)
//	{
//		laneAssignedFlow = signalTiming.getLaneAssignedFlow();
//		for(int i=0;i<laneAssignedFlow.length;i++)
//			for(int k=0;k<laneAssignedFlow[i].length;k++)
//			{
//				laneAssignedFlow[i][k] = (int)Math.round(newflowOfOldflow*laneAssignedFlow[i][k]);
//			}			
//	}

	public void saturationFlowForLane()
	{
		//System.out.println("lane saturtaion flow test: "+signalTiming.getLaneSaturationFlow()[0][0]);
		laneSaturationFlow = signalTiming.getLaneSaturationFlow();
	}
	
	
	//判断左转是否为 leading-lagging LT
	@SuppressWarnings("unused")
	public double saturationFlowLeadingLaggingLT(int LTgreenDuration, int leftTurnArmIndex, double permSaturationFlow)
	{
		int saturationFlow=0;
		
		Link leftTurnLink = null;
		Link straihgtLink = null;

		int greenProtected=0;
		int leftTurnDirIndex=-1;
		int cycleLength =(int)signalTiming.getCycleLengthEpslon();

		double armTHFlow = 0.0f;
		int oppArmIndex = -1;//对面直行的arm信息
		int oppTHDirIndex = -1;
		Link oppTHLink = null;
		double opposingTHFlow=0;
		
		for (int j = 0; j < armDirNum.get(leftTurnArmIndex); j++)
		{
			String dirS = armDir.get(leftTurnArmIndex).get(j).getDirection();//第 i个arm的第j个转向

			if(dirS.equals("l")) //若为permissive左转
			{
				leftTurnLink = armDir.get(leftTurnArmIndex).get(j);
				leftTurnDirIndex=j;
			}
			if(dirS.equals("s") || dirS.equals("S")) //若为直行
			{
				straihgtLink = armDir.get(leftTurnArmIndex).get(j);
			}
		}

		//该对面arm存在直行边
		oppArmIndex = opposingThroughArmIndex(leftTurnArmIndex);
		//System.out.println("LanebasedSignalTiming.java leftTurnArmIndex: "+leftTurnArmIndex+" oppArmIndex: "+ oppArmIndex);
		if(oppArmIndex!=-1&& LTgreenDuration>0)
		{
			for (int j = 0; j < armDirNum.get(oppArmIndex); j++)
			{
				String dirS = armDir.get(oppArmIndex).get(j).getDirection();//该arm的第j个转向

				if(dirS.equals("s") || dirS.equals("S")) //若为直行
				{
					oppTHLink = armDir.get(oppArmIndex).get(j); //对面直行的信息，link类型
					oppTHDirIndex = j; //对面直行的 movement index
					opposingTHFlow = demand[oppArmIndex][j]; //对面直行的flow
					break;
				}
			}
			
		
			int startGreenOTH = signalTiming.getStartOfGreenMovement()[oppArmIndex][oppTHDirIndex];
			int durationGreenOTH = signalTiming.getDurationMovement()[oppArmIndex][oppTHDirIndex];
			int endGreenOTH = startGreenOTH+durationGreenOTH;

			int startGreenLT = signalTiming.getStartOfGreenMovement()[leftTurnArmIndex][leftTurnDirIndex];
			//int durationGreenLT = signalTiming.getDurationMovementPhi()[leftTurnArmIndex][leftTurnDirIndex];
			int endGreenLT = startGreenLT+LTgreenDuration;


			//Compound Phasing
			if(startGreenLT<endGreenOTH && startGreenOTH<startGreenLT && endGreenOTH<endGreenLT)      //Lagging
			{
				greenProtected = endGreenLT-endGreenOTH;
			}
			else if(startGreenOTH<endGreenLT && startGreenOTH>startGreenLT && endGreenOTH>endGreenLT)//Leading
			{
				greenProtected = startGreenOTH-startGreenLT;
			}
			else if(startGreenOTH>startGreenLT && endGreenOTH<endGreenLT)//Lagging&Leading
			{
				greenProtected = LTgreenDuration-durationGreenOTH;
			}

			// Have Leading/Lagging/Leading-Lagging
			if(greenProtected>0)
			{
				// 计算新的左转饱和流
				System.out.println("left turn is compound!");
				double greenPermissive = LTgreenDuration-greenProtected;
				double armTHSaturationFlow =oppTHLink.getSaturationFlow();
				double protSaturationFlow = 0.95*armTHSaturationFlow;
				saturationFlow = (int)Math.round((permSaturationFlow*greenPermissive+
						protSaturationFlow*greenProtected)/LTgreenDuration);

				leftTurnLink.setSaturationFlow((int)saturationFlow); //更新饱和流
				leftTurnLink = null;
			}
			else if(greenProtected == 0)
			{
				//Do Something
				saturationFlow = (int)permSaturationFlow;
				leftTurnLink.setSaturationFlow((int)permSaturationFlow);

			}
		}
		return saturationFlow;
	}
	
	@SuppressWarnings("unused")
	public double saturationFlowPermissiveLT(int LTgreenDuration,int leftTurnArmIndex)///
	{
		double saturationFlow=0;
		Link leftTurnLink = null;
		Link straihgtLink = null;

		int cycleLength =(int)signalTiming.getCycleLengthEpslon();

		double armTHFlow = 0.0f;
		int oppArmIndex = -1;//对面直行的arm信息
		int oppTHDirIndex = -1;
		Link oppTHLink = null;
		double opposingTHFlow=0;
		
		for (int j = 0; j < armDirNum.get(leftTurnArmIndex); j++)
		{
			String dirS = armDir.get(leftTurnArmIndex).get(j).getDirection();//第 i个arm的第j个转向

			if(dirS.equals("l")) //若为permissive左转
			{
				leftTurnLink = armDir.get(leftTurnArmIndex).get(j);
			}
			if(dirS.equals("s") || dirS.equals("S")) //若为直行
			{
				straihgtLink = armDir.get(leftTurnArmIndex).get(j);
			}
		}

		//该对面arm存在直行边
		oppArmIndex = opposingThroughArmIndex(leftTurnArmIndex);
		//System.out.println("LanebasedSignalTiming.java leftTurnArmIndex: "+leftTurnArmIndex+" oppArmIndex: "+ oppArmIndex);
		if(oppArmIndex!=-1&& LTgreenDuration>0)
		{
			for (int j = 0; j < armDirNum.get(oppArmIndex); j++)
			{
				String dirS = armDir.get(oppArmIndex).get(j).getDirection();//该arm的第j个转向

				if(dirS.equals("s") || dirS.equals("S")) //若为直行
				{
					oppTHLink = armDir.get(oppArmIndex).get(j); //对面直行的信息，link类型
					oppTHDirIndex = j; //对面直行的 movement index
					opposingTHFlow = demand[oppArmIndex][j]; //对面直行的flow
					break;
				}
			}
			
		
		double l_c=4.5;
		double l_f=2.5;
		double n_f = 1.5;
		double filteredSaturation=(opposingTHFlow/3600)*Math.exp(-opposingTHFlow*l_c/3600)
				/(1-Math.exp(-opposingTHFlow*l_f/3600)); //unit:veh/s
		double gu =Math.max(0, ((1.0*oppTHLink.getSaturationFlow()/3600)*LTgreenDuration 
				- (opposingTHFlow/3600)*cycleLength)/((1.0*oppTHLink.getSaturationFlow()/3600)- (opposingTHFlow/3600)));
		saturationFlow =(1.0*straihgtLink.getSaturationFlow()/3600)*(filteredSaturation*gu+n_f)/(LTgreenDuration);//unit: veh/s
		saturationFlow =3600*saturationFlow;//unit:veh/h
//		System.out.println("opposingTHFlow "+opposingTHFlow);
//		System.out.println("filteredSaturation "+filteredSaturation);
//		System.out.println("gu "+gu);
//		System.out.println("LTgreenDuration "+LTgreenDuration);
//		System.out.println("straihgtLink.getSaturationFlow() "+straihgtLink.getSaturationFlow());
//		
//		System.out.println("saturationFlow "+saturationFlow);
		}
		else
			saturationFlow=1900*0.95;//否则赋值为protected的饱和流
//		leftTurnLink.setSaturationFlow((int)saturationFlow);
		
		//释放内存
		leftTurnLink = null;
		straihgtLink = null;
		return saturationFlow;
	}
	
	
	// 左转对面直行的arm的index
	@SuppressWarnings("unused")
	public int opposingThroughArmIndex(int leftTurnArmIndex)///
	{
//		List<Link> extLinks    = junc.getExternalLinks(); //外部道路
		boolean Straight = false;
		boolean oppStraight=false;

		Link leftTurnLink = null;
		Link straihgtLink = null;

		int oppArmIndex = -1;//对面直行的arm信息


		for (int j = 0; j < armDirNum.get(leftTurnArmIndex); j++)
		{
			String dirS = armDir.get(leftTurnArmIndex).get(j).getDirection();//第 i个arm的第j个转向

			if(dirS.equals("l")||dirS.equals("L")) //若为permissive左转
			{
				leftTurnLink = armDir.get(leftTurnArmIndex).get(j);
				//System.out.println(leftTurnArmIndex+ " left turn "+leftTurnLink);
			}
			if(dirS.equals("s") || dirS.equals("S")) //若为直行
			{
				straihgtLink = armDir.get(leftTurnArmIndex).get(j);
				//System.out.println(leftTurnArmIndex+ " strainght "+straihgtLink);
				Straight=true;
			}
		}

		if(Straight == true)//arm i 有LT 和 TH(S)
		{
			Vertex straightOut = straihgtLink.getEnd();
			Vertex straightOutExtEnd = null;
			Vertex opposingTHArm = null; //对面直行的Arm
			for(int p=0; p<extLinks.size(); p++)
			{
				if(extLinks.get(p).getBegin().equals(straightOut))
				{
					straightOutExtEnd = extLinks.get(p).getEnd();
					break;
				}
			}

			for(int p=0; p<extLinks.size(); p++)
			{
				String juncSOEE = straightOutExtEnd.getName();
//				System.out.println("juncSOEE "+juncSOEE);
				int index = juncSOEE.indexOf("@");
				String juncS = juncSOEE.substring(0,index);
				//System.out.println("juncSOEE.substring(0,index) "+juncSOEE.substring(0,index));
				
				Vertex opposingTHBegin = extLinks.get(p).getBegin();
				String juncOpp = opposingTHBegin.getName();
//				System.out.println("juncOpp "+juncOpp);
				index = juncOpp.indexOf("@");
				String juncO = juncOpp.substring(0,index);

				if(juncS.equals(juncO))
				{
					opposingTHArm = extLinks.get(p).getEnd(); //找到对面直行的Arm
					break;
				}
			}

			//该对面arm存在直行边		
			oppArmIndex = arms.indexOf(opposingTHArm);//对面直行的arm信息
//			System.out.println("opposing arm index: "+ oppArmIndex);
			if(oppArmIndex!=-1)
			for (int j = 0; j < armDirNum.get(oppArmIndex); j++)
			{
				String dirS = armDir.get(oppArmIndex).get(j).getDirection();//该arm的第j个转向

				if(dirS.equals("s") || dirS.equals("S")) //若为直行
				{
					oppStraight = true; //左转存在对面直行
					break;
				}
			}
		}
		//System.out.println("oppStraight = true: "+ oppStraight);
		if(oppStraight == true)
			return oppArmIndex;
		else
			return -1;
	}
	
	public void finalize()
	{           
        //super.finalize();  
        //System.out.println("####### TravelTimeCalculation finalize method was called! #######");
    }
	
	/****************************************************************
	 *******************Getter and Setter****************************
	 ****************************************************************/
	/**
	 * @return travelTime
	 */
	public double[] getTravelTime()
	{
		return travelTime;
	}
	
	/**
	 * @return degreeOfSaturation
	 */
	public double[] degreeOfSaturation()
	{
		return degreeOfSaturation;
	}
	
	public int getCycleLength()
	{
		return cycleLength;
	}

	/**
	 * @param network 
	 *		要设置的 network
	 */
	public void setNetwork(Network network)
	{
		this.network = network;
	}

	/**
	 * @param travelTime 
	 *		要设置的 travelTime
	 */
	public void setTravelTime(double[] travelTime)
	{
		this.travelTime = travelTime;
	}
	
	/**
	 * @param degreeOfSaturation 
	 *		要设置的 degreeOfSaturation
	 */
	public void setdegreeOfSaturation(double[] degreeOfSaturation)
	{
		this.degreeOfSaturation = degreeOfSaturation;
	}
	

	public double getArmNumber()
	{
		return armNumber;
	}

	public List<ArrayList<Link>> getArmDir()
	{
		return armDir;
	}
		
	public List<Integer> getArmLanesNum()
	{
		return armLanesNum;
	}
	
	public List<Integer> getArmDirNum()
	{
		return armDirNum;
	}

	public List<Integer> getOutLanesNum()
	{
		return outLanesNum;
	}	
	
	public List<Junction> getJunctions()
	{
		return junctions;
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

	/**
	 * @return methodeType
	 */
	public String getMethodeType()
	{
		return methodeType;
	}

	/**
	 * @param methodeType 要设置的 methodeType
	 */
	public void setMethodeType(String methodeType)
	{
		this.methodeType = methodeType;
	}
	
}
