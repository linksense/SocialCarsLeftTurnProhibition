/* --------------------------------------------------------------------
 * StochasticUserEquilibirum.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and all Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 30.04.2016
 * 
 * Function:
 *           1.用户均衡
 *           
 */
package trafficAssignment;

import java.io.IOException;

import ilog.cplex.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import trafficAssignment.stochasticLoadingLinkFlow.LinkFlowCalculation;
import trafficAssignment.travelTime.TravelTimeCalculation;

import com.trolltech.qt.QSignalEmitter;

import network.Junction;
import network.Network;
import network.ODMatrix;

public class StochasticUserEquilibirum extends QSignalEmitter implements Runnable
{
	//~Variables
	//--------------------------------------------------------------------------
	private Network network = null;     //路网
	@SuppressWarnings("unused")
	private ODMatrix odMatrix  = null;
	
//	private int[][] greenDurationLane;
//	private int[][][] assignedFlow;
//	private int cycleLength;
	
	//private LanebasedSinalTiming signalTiming =null;
	
	private double[] linkFlowAllOD  = null;  //Link Flow
	private double[] travelTime  = null; //Travel Time
	private double[] degreeOfSaturation  = null;
	
	private Queue<double[]> queue = new LinkedList<double[]>(); 
	private LinkFlowCalculation linkFlowC;
	private TravelTimeCalculation travelTimeC;
	private double[] oldLinkFlowAllOD;
	private double[] newlinkFowAllOD;
	private double[] newTravelTime; //新的Travel Time
	private double[] degreeOfSaturationTemp;
	private double[] newLinkFlow;
	
//	private double[] initialFlows;
	private static final int QUEUE_SIZE = 5;//队列大小
	private static final int MAX_ITERATIONS = 200; //最大迭代次数
	private static final double EPSILON = 0.0001;    //收敛条件
	
	private int timeUnit;
	private boolean shutdownRequested = false;
	
	private double[] newFlowOfOldFlow;
	private int[] BPRLinkFlow;
	private double[] initialFlows;
	
	private String methodeType;
	
	//~Methods
	//--------------------------------------------------------------------------
	/**
	 * 构造函数
	 * @param network
	 */
	public StochasticUserEquilibirum(Network network,ODMatrix odMatrix,IloCplex cplex, String methodeType)
	{
		this.setMethodeType(methodeType);
		this.network = network;
		this.odMatrix = odMatrix;
		
		this.cplex = cplex;
		
		this.linkFlowAllOD = new double[network.getLinks().size()]; 
		this.travelTime = new double[network.getLinks().size()];
		this.degreeOfSaturation = new double[network.getLinks().size()];
		this.newTravelTime   = new double[network.getLinks().size()]; //新的Travel Time
		this.degreeOfSaturationTemp = new double[network.getLinks().size()];
		this.newLinkFlow     = new double[network.getLinks().size()]; //新的Link Flow
		
		// added to be able to use it in different functions 
		this.linkFlowC = new LinkFlowCalculation(network, odMatrix);	//用于计算Link Flow
		this.travelTimeC = new TravelTimeCalculation(network,cplex, methodeType);     //用于计算Travel Time
		
		this.newlinkFowAllOD = new double[network.getLinks().size()]; //下一次迭代的Link Flow
		this.oldLinkFlowAllOD = new double[network.getLinks().size()]; //当前的Link Flow
		timeUnit = travelTimeC.timeUnit;
	}
	
	public StochasticUserEquilibirum(Network network,ODMatrix odMatrix,IloCplex cplex, int[] BPRLinkFlow, String methodeType)
	{
		this.setMethodeType(methodeType);
		this.network = network;
		this.odMatrix = odMatrix;
		
		this.cplex = cplex;
		this.BPRLinkFlow = BPRLinkFlow;
		
		this.linkFlowAllOD = new double[network.getLinks().size()]; 
		this.travelTime = new double[network.getLinks().size()];
		this.degreeOfSaturation = new double[network.getLinks().size()];
		this.newTravelTime   = new double[network.getLinks().size()]; //新的Travel Time
		this.degreeOfSaturationTemp = new double[network.getLinks().size()];
		this.newLinkFlow     = new double[network.getLinks().size()]; //新的Link Flow
		
		// added to be able to use it in different functions 
		this.linkFlowC = new LinkFlowCalculation(network, odMatrix);	//用于计算Link Flow
		this.travelTimeC = new TravelTimeCalculation(network,cplex, methodeType);     //用于计算Travel Time
		
		this.newlinkFowAllOD = new double[network.getLinks().size()]; //下一次迭代的Link Flow
		this.oldLinkFlowAllOD = new double[network.getLinks().size()]; //当前的Link Flow
		
		this.newFlowOfOldFlow = new double[network.getLinks().size()];
		timeUnit = travelTimeC.timeUnit;
	}

	/* （非 Javadoc）
	 * 处理SUE的线程
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
		// TODO 自动生成的方法存根
		while(!shutdownRequested)
		{
			System.out.println(Thread.currentThread().getName() + " is running..");  
			try
			{
				algorithmMSA(); //调用MSA算法
			} 
			catch (IOException e)
			{
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			shutdownRequested = true;
		}
		shutdownRequested = false;
	}
	
	public void algorithmMSA() throws IOException
	{	
		initializationLinkFlow(); //初始化travel time 和 link flow
		
		for(int numberOfIteration=1;numberOfIteration<MAX_ITERATIONS;numberOfIteration++)
		{
			algorithmMSAPreparation(numberOfIteration); // 初始化queue
			
			if(linkFlowC.getHavePaths() != false)
			{
				if(methodeType.equals("BPR"))
				{
					algorithmMSATravelTimeBPR();
				}
				else
				{
					List<Junction> junctions = network.getJunctions();
					
					if(methodeType.equals("StageBased"))
					{
						int commonCycleLength = 0;
						for(int i=0; i<junctions.size(); i++) //遍历所有的路口
						{
							algorithmMSAPreparationOneJunction(i); 
							// algorithmMSAPreparationOneJunctionCycleLength(i); 
							// 运行signal，计算 cycle time
							if(commonCycleLength< travelTimeC.getCycleLength())
							{
								commonCycleLength= travelTimeC.getCycleLength();
							}
						}
	
						for(int i=0; i<junctions.size(); i++) //遍历所有的路口
						{
							algorithmMSAOneJunctionStage(i,commonCycleLength,oldLinkFlowAllOD); //用common cycle length计算greens，再 计算travel time
						}
					}
					else if(methodeType.equals("LaneBased"))
					{
						for(int i=0; i<junctions.size(); i++) //遍历所有的路口
						{
							algorithmMSAPreparationOneJunction(i); // 运行signal，计算 cycle time
							algorithmMSAOneJunctionLane(i,travelTimeC.getCycleLength(),oldLinkFlowAllOD); 
							// System.out.println("cycle in SUE: "+travelTimeC.getCycleLength());
						}
					}
					else
					{
						
					}
					
				}
				
				algorithmMSAOneIteration(numberOfIteration);
				
				boolean convergence = algorithmMSAOneIterationCheckConvergence(numberOfIteration);
				if(convergence)
				{
					break;
				}
				//System.exit(0);
			}
		}
		
		algorithmMSAIterationReset();

		algorithmMSAReset();
	}
	
	public void initializationLinkFlow() throws IOException
	{
		System.out.println("---------------" + Thread.currentThread().getName() + " " + 0 + "-Iteration---------------");
		
		//System.out.println("初始化Travel Time：");
		double[] initialTravelTime = new double[network.getLinks().size()]; //Initial Travel Time T0
		for (int i=0;i<initialTravelTime.length;i++) 
		{
			double freeFlowTravelTime=network.getLinks().get(i).getFreeFlowTravelTime()/timeUnit;
			initialTravelTime[i] = freeFlowTravelTime ;
			//System.out.println(i+" initial travel time: "+initialTravelTime[i]);
			//System.out.println(i+" free flow travel time: "+freeFlowTravelTime);
			
			network.getLinks().get(i).setTravelTime(initialTravelTime[i]);
        }
		initialTravelTime = null;
		
		linkFlowC.linkFlowAllOD();                  //利用初始Travel Time计算当前的Link Flow
		
		for (int i=0;i<network.getLinks().size();i++)            
		{
			oldLinkFlowAllOD[i] = linkFlowC.getLinkFlowAllOD()[i];
		}
		
		if(linkFlowC.getHavePaths() != false)
		{
			queue = new LinkedList<double[]>();
			for(int i=0;i<QUEUE_SIZE;i++)
			{
				queue.offer(new double[network.getLinks().size()]);  //初始化队列
			}
		}
		
		if(linkFlowC.getHavePaths() == false)
		{
			System.out.println("********初始化Travel Time所得Link Flow为空，也许是无"
			          + "可用路径选择，无法继续进行运算 ***************");
		}
	}
	
	public void algorithmMSAPreparation(int numberOfIteration)
	{	
		if(linkFlowC.getHavePaths() != false)
		{
			queue.poll();                  //取出一个元素
			double[] addnew = new double[network.getLinks().size()];
			for(int i=0;i<network.getLinks().size();i++)
			{
				addnew[i] = oldLinkFlowAllOD[i];    //存储当前的Link Flow
				//System.out.println("queue addnew[i]: "+addnew[i]);
			}
			queue.offer(addnew); //添加一个元素
			
			//System.out.println("Queue Initialized:" + queue);
			//Step1.Update更新Travel Time
			///////////////////////////////////////////////////
			if(methodeType.equals("LaneBased") || methodeType.equals("StageBased"))
			{
				travelTimeC.byLBSTFunctionPreparation();
			}
		}

	}
	
	public void algorithmMSATravelTimeBPR() 
	{
		if(linkFlowC.getHavePaths() != false)
		{
			travelTimeC.byBPRFunction(oldLinkFlowAllOD, 0.15, 4); // 此处使用初始的link flow作为输入
		}
	}
	
	public void algorithmMSAPreparationOneJunction(int junctionIndex) 
	{
		// 此处使用BPR收敛后的的link flow作为输入,但是只有一个路口的周期时间，之后需要对所有路口取公共周期才行！
		travelTimeC.byLBSTFunctionPreparationOneJunction(BPRLinkFlow, junctionIndex); 
		//travelTimeC.byLBSTFunctionPreparationOneJunction(oldLinkFlowAllOD, junctionIndex);
	}
		
	public void algorithmMSAOneJunctionStage(int junctionIndex,int commonCycleLength, double[] oldLinkFlowAllOD)
	{
		//调用每个junction里 internal links的 link travel time
		//1. 用BPR flow 和 common cycle计算 durations
		travelTimeC.byLBSTFunctionPreparationOneJunction(BPRLinkFlow, junctionIndex,commonCycleLength); 
		// 2. 用duration 和 当前迭代的flow 计算delay，并计算travel time
		travelTimeC.byLBSTFunctionOneJunction(junctionIndex,commonCycleLength,oldLinkFlowAllOD);
	}
	
	public void algorithmMSAOneJunctionLane(int junctionIndex,int cycleLength, double[] oldLinkFlowAllOD)
	{
		//调用每个junction里 internal links的 link travel time
		travelTimeC.byLBSTFunctionOneJunction(junctionIndex,cycleLength,oldLinkFlowAllOD);
	}
		
	public void algorithmMSAOneIteration(int numberOfIteration) throws IOException 
	{	
		if(linkFlowC.getHavePaths() != false)
		{
			if(methodeType.equals("LaneBased") || methodeType.equals("StageBased"))
			{
				 // 对于所有junctions 和 links 设为null
				 travelTimeC.byLBSTFunctionReset();
			}
			
			for(int i=0;i<network.getLinks().size();i++)
			{	
				//System.out.println("NewTravelTime" + newTravelTime);
				//System.out.println("travel time" + travelTimeC.getTravelTime()[i]);
				
				newTravelTime[i] = travelTimeC.getTravelTime()[i];         //存储新的Travel Time
				degreeOfSaturationTemp[i] = travelTimeC.degreeOfSaturation()[i];
	//			double diff = network.getLinks().get(i).getTravelTime()-newTravelTime[i];
				network.getLinks().get(i).setTravelTime(newTravelTime[i]);
				//System.out.println(i+" 新 Travel Time："+ network.getLinks().get(i).getTravelTime()+" ");//输出新的Travel Time
			}
			
	        //Step2.Direction finding
	        /////////////////////////////////////////////////////
			linkFlowC.linkFlowAllOD();   //利用新的Travel Time计算新的Link Flow
			for(int i=0;i<network.getLinks().size();i++)
			{
				newLinkFlow[i] = linkFlowC.getLinkFlowAllOD()[i];    //存储新的Link Flow
			}
			
//	        System.out.println("Stochastic link Flow：");
//			for (int i=0;i<network.getLinks().size();i++)            
//			{
//				System.out.println(i+" "+newLinkFlow[i]);  
//	        }
	       		
	        //Step3.Move更新Link Flow
	        /////////////////////////////////////////////////////
	        for(int i=0;i<network.getLinks().size();i++)               //计算下一次迭代的Link Flow
	        {
	        	newlinkFowAllOD[i] = oldLinkFlowAllOD[i]+((newLinkFlow[i]-oldLinkFlowAllOD[i])/numberOfIteration);
	        }
	        
//	        System.out.println("更新所得下一次迭代的Link Flow：");
//			for (int i=0;i<network.getLinks().size();i++)            
//			{
//				System.out.println(newlinkFowAllOD[i]+"   "+(oldLinkFlowAllOD[i])+" ");  //输出下一次迭代的Link Flow
//	        }
//			
//			 System.out.println("更新所得下一次迭代的Link travel time：");
//			for (int i=0;i<network.getLinks().size();i++)            
//			{
//				System.out.println(network.getLinks().get(i).getTravelTime()); 
//		    }
	        
	        for(int i=0;i<network.getLinks().size();i++)
			{
	        	oldLinkFlowAllOD[i] = newlinkFowAllOD[i]; //为下一次迭代
			}
		}
	} 
	
	public boolean algorithmMSAOneIterationCheckConvergence(int numberOfIteration) throws IOException
	{
	//Step4.Check for Convergence
        /////////////////////////////////////////////////////
        boolean converge = convergenceCriteriaForTravelTime(newlinkFowAllOD,queue,numberOfIteration); //收敛判断
	
        if(converge)//收敛成功
        { 
        	System.out.println("converge success!");
        	for(int i=0;i<network.getLinks().size();i++)
    		{
        		linkFlowAllOD[i] = newlinkFowAllOD[i];//最终的Link Flow
        		travelTime[i] = newTravelTime[i];    //最终的Travel Time
        		degreeOfSaturation[i]=degreeOfSaturationTemp[i];
        		//System.out.println(network.getLinks().get(i).getName()+" has "+linkFlowC.getPathNumberAllOD()[i]+" paths");
        		//System.out.println("degree of saturation: "+i+" "+degreeOfSaturation[i]);
        		//newlinkFowAllOD = null;
        		//newTravelTime = null;
    			//System.out.println(network.getLinks().get(i).getName()+" "+linkFlowAllOD[i] +" "+ travelTime[i]); //输出最终结果
    		}
        }
        return converge;
	}
	
	public void algorithmMSAIterationReset() throws IOException
	{
		//travelTimeC.setdegreeOfSaturation(null);//如果收敛了，则不需要设饱和流率为null了，于是可以获取饱和流率
		newTravelTime   = null; //新的Travel Time
		degreeOfSaturationTemp = null;
		newLinkFlow     = null; //新的Link Flow
		newlinkFowAllOD = null;
		//System.out.println("Queue before reset: " + queue);
		queue = null;
	}
	
	public void algorithmMSAReset() throws IOException
	{
		linkFlowC.setNetwork(null);
		linkFlowC.setOdMatrix(null);
		linkFlowC.setLinkFlowAllOD(null);
		linkFlowC = null;
		
		travelTimeC.setNetwork(null);
		travelTimeC.setTravelTime(null);
		travelTimeC.setdegreeOfSaturation(null);
		travelTimeC = null;
		oldLinkFlowAllOD = null;
		
		this.network = null;
		this.odMatrix = null;
		//System.gc();
	}
	
	private boolean convergenceCriteriaForTravelTime(double[] currentLinkFlows, Queue<double[]> queue,int numberOfItration)
	{
		//Step1. 求平均Link Flow
		//-----------------------------------------------------------------------------------
		LinkedList<double[]> queueList = new LinkedList<double[]>(queue);       //获取队列的当前所有元素
//		for(int i=0;i<queueList.size();i++)
//		{
//			for(int j=0;j<network.getLinks().size();j++)
//			{
//				System.out.print("Queue:"+queueList.get(i)[j]+" ");
//			}
//			System.out.println(" ");
//		}
//		for(int j=0;j<network.getLinks().size();j++)
//		{// 在第一次迭代中， current link flows = infinite
//				System.out.print("currentLinkFlows:"+currentLinkFlows[j]);
//		}
		Queue<double[]> tempQueue = new LinkedList<double[]>(queue);
		tempQueue.poll();
		tempQueue.offer(currentLinkFlows);
		LinkedList<double[]> newQueueList = new LinkedList<double[]>(tempQueue);//获取临时队列的当前所有元素
		
		double[] averageFlows = new double[network.getLinks().size()];    //当前的averageFlows
		double[] newAverageFlows = new double[network.getLinks().size()]; //下一次迭代的averageFlows
		//System.out.println("queueList.size() "+queueList.size());
		for(int i=0;i<QUEUE_SIZE;i++)
		{
			for(int j=0;j<network.getLinks().size();j++)
			{
				averageFlows[j] += queueList.get(i)[j];
				newAverageFlows[j] += newQueueList.get(i)[j];
			}
		}
		
//		for(int j=0;j<network.getLinks().size();j++)
//		{
//			System.out.println("previous flow vs current flow: "+averageFlows[j]+" "+newAverageFlows[j]);
//		}
		
		for(int i=0;i<network.getLinks().size();i++)
		{
			if(numberOfItration<QUEUE_SIZE)
			{
				averageFlows[i] /= (numberOfItration);    //当前的averageFlows
				newAverageFlows[i] /= (numberOfItration+1); //下一次迭代的averageFlows
			}
			else 
			{
				averageFlows[i] /= QUEUE_SIZE;    //当前的averageFlows
				newAverageFlows[i] /= QUEUE_SIZE; //下一次迭代的averageFlows
			}
		}
		
		//Step2. 求终止条件
		//---------------------------------------------------------------
		double sumFlows = 0.0;
		double sumRootMeanDiffFlows = 0.0;
		for (int i=0;i<network.getLinks().size();i++)
		{
			sumRootMeanDiffFlows += Math.pow((newAverageFlows[i]-averageFlows[i]),2);
			sumFlows += averageFlows[i];
		}
		newAverageFlows = null;
		averageFlows = null;
		//System.out.println("sumRootMeanDiffFlows:"+sumRootMeanDiffFlows);
		//System.out.println("sumFlows:"+sumFlows);
		
		double result = Math.sqrt(sumRootMeanDiffFlows)/sumFlows;
		System.out.println("## SUE当前迭代收敛结果: "+result);
		if (result <= EPSILON)
		{
			newQueueList = null;
			tempQueue = null;
			tempQueue = null;
			return true;
		}
		else
		{
			newQueueList = null;
			tempQueue = null;
			tempQueue = null;
			return false;
		}		
	}
	
	public void finalize()
	{           
        //super.finalize();  
        //System.out.println("####### StochasticUserEquilibirum finalize method was called! #######");
    }
	
	/**
	 * @return linkFowAllOD
	 */
	public double[] getLinkFlowAllOD()
	{
		return linkFlowAllOD;
	}
	
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
	public double[] getdegreeOfSaturation()
	{
		return degreeOfSaturation;
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
	 * @param odMatrix 
	 *		要设置的 odMatrix
	 */
	public void setOdMatrix(ODMatrix odMatrix)
	{
		this.odMatrix = odMatrix;
	}

	/**
	 * @param linkFlowAllOD 
	 *		要设置的 linkFlowAllOD
	 */
	public void setLinkFlowAllOD(double[] linkFlowAllOD)
	{
		this.linkFlowAllOD = linkFlowAllOD;
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
	
	private IloCplex cplex = null;
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
	 * @return newFlowOfOldFlow
	 */
	public double[] getNewFlowOfOldFlow()
	{
		return newFlowOfOldFlow;
	}

	/**
	 * @param newFlowOfOldFlow 要设置的 newFlowOfOldFlow
	 */
	public void setNewFlowOfOldFlow(double[] newFlowOfOldFlow)
	{
		this.newFlowOfOldFlow = newFlowOfOldFlow;
	}

	/**
	 * @return bPRLinkFlow
	 */
	public int[] getBPRLinkFlow()
	{
		return BPRLinkFlow;
	}

	/**
	 * @param bPRLinkFlow 要设置的 bPRLinkFlow
	 */
	public void setBPRLinkFlow(int[] bPRLinkFlow)
	{
		BPRLinkFlow = bPRLinkFlow;
	}

	/**
	 * @return initialFlows
	 */
	public double[] getInitialFlows()
	{
		return initialFlows;
	}

	/**
	 * @param initialFlows 要设置的 initialFlows
	 */
	public void setInitialFlows(double[] initialFlows)
	{
		this.initialFlows = initialFlows;
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