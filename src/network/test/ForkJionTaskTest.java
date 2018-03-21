///* --------------------------------------------------------------------
// * 
// * --------------------------------------------------------------------
// * (C) Copyright 2015-2016, by Huijun Liu and Contributors.
// *
// * Original Author:  Huijun Liu
// * Contributor(s):   Qinrui Tang
// *
// * Last Change Date: 
// *					22.09.2016
// *
// */
//package network.test;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.concurrent.ForkJoinPool;
//import java.util.concurrent.RecursiveTask;
//
//import ilog.concert.IloException;
//import ilog.cplex.IloCplex;
//import leftTurnProhibition.LeftTurnToProhibit;
//import network.Junction;
//import network.Link;
//import network.Network;
//import network.ODMatrix;
//import network.algorithms.Pair;
//import network.importnetwork.ImportLocalNetImplement;
//import trafficAssignment.StochasticUserEquilibirum;
//
//public class ForkJionTaskTest
//{
//	//~Variables
//	// --------------------------------------------------------------------------
//	// This network.graph is directional
//	Network network = new Network();
//	ODMatrix odMatrix = new ODMatrix();
//
//	// ~Methods
//	// --------------------------------------------------------------------------
//	public ForkJionTaskTest() throws IOException, InterruptedException 
//	{
//		// final String xmlSumoNetwork = "Test_network\\suedstadt.net.xml";
//		// final String xmlOD = "Test_network\\suedstadt.districts.xml";
//		// final String odDemandMatrix = "Test_network\\suedstadt_OD_Matrix.txt";
//		// final String xmlLocalNet = "localnetwork\\suedstadt.localnet.xml";
//		// 导出本地XML路网
//		// ExportLocalNetworkImplement.ExportImplementer(xmlSumoNetwork, xmlOD,odDemandMatrix, xmlLocalNet);
//
//		// 导入本地XML路网
//		long storageStartTime = System.currentTimeMillis(); // 获取开始时间
//		// final String xmlNetwork = "localnetwork\\twoLanesFourIntersectionsEightODs.localnet.xml";
//		// final String xmlNetwork = "localnetwork\\FourIntersectionsEightODs.localnet.xml";
//		// final String xmlNetwork = "localnetwork\\NineIntersectionTwoODpair.localnet.xml";
//		final String xmlNetwork = "localnetwork\\fiveIntersectionsFiveOD.localnet.xml";
//		ImportLocalNetImplement localNetwork = new ImportLocalNetImplement(xmlNetwork, network, odMatrix);
//		localNetwork.networkStorage();
//		localNetwork = null;
//
//		// long storageEndTime=System.currentTimeMillis(); //获取结束时间
//		// System.out.println("路网读取时间： " + (storageEndTime-storageStartTime)+"ms");
//
//		// System.out.println("ODMatrix: "+odMatrix.getODMatrix());
//
//		// List<ArrayList<String>> tlLogicList = network.getTlLogicList();
//		// System.out.println("LogicList:"+tlLogicList);
//
//		// Step1. 获取路网的所有禁止情况 Leftturn Prohibition
//		///////////////////////////////////////////////////////////////////
//		LeftTurnToProhibit leftTurnProh = new LeftTurnToProhibit();
//		ArrayList<ArrayList<Link>> result = new ArrayList<ArrayList<Link>>(leftTurnProh.subsets(network.getLeftTurnLinks()));
//		System.out.println("左转个数:" + network.getLeftTurnLinks().size());
//		System.out.println("左转:" + network.getLeftTurnLinks());
//		System.out.println("禁止情况个数:" + result.size());
//		leftTurnProh = null;
//		
//		//并行执行任务开始
//		int coreNum = Runtime.getRuntime().availableProcessors();
//		System.out.println("CPU核心数目："+coreNum); 
//		
//		List<List<ArrayList<Link>>> list = new ArrayList<List<ArrayList<Link>>>();
//		List<Integer> fromList = new ArrayList<Integer>() ;
//		int blockSize = result.size()/coreNum;	
//		for(int i=0; i<coreNum; i++)
//		{
//			int fromIndex = i*blockSize;
//			int toIndex   = 0;
//			if(i != (coreNum-1))
//			{
//				toIndex   = (i+1)*blockSize-1;
//			}
//			else
//			{
//				toIndex   = result.size()-1;
//			}
//			
//			
//			System.out.println("fromIndex: " + fromIndex + "   toIndex: " + toIndex);
//			list.add(result.subList(fromIndex, toIndex));
//			fromList.add(fromIndex);
//		}
//		
//		ForkJoinPool forkJoinPool = new ForkJoinPool();
//		Pair<Double,ArrayList<Link>> finalResult = forkJoinPool.invoke(new LTPTask(list,fromList,xmlNetwork));
//		
//		
//		System.out.println("## Output The Final Result >>>>>>>>>>>>>>>>>>>>>>>>>");
//		System.out.println("## The minimum total emission: "+finalResult.getKey());
//		System.out.println("## Left turn prohibition solution: "+finalResult.getValue());
//		//最优解情况下的平均degree of saturation
//		long storageEndTime=System.currentTimeMillis(); //获取结束时间
//		System.out.println("Total running time: " + (storageEndTime-storageStartTime)/60000 +"min");
//		
//	}
//	
//	
//	
//	class LTPTask extends RecursiveTask<Pair<Double,ArrayList<Link>>>
//	{
//		private static final long serialVersionUID = 1L;
//		//~Variables
//		// --------------------------------------------------------------------------
//		private String xmlNetwork = null;
//		private List<List<ArrayList<Link>>> result = null;
//		private List<Integer> fromList = null;
//		
//		// ~Methods
//		// --------------------------------------------------------------------------
//		public LTPTask(List<List<ArrayList<Link>>> list,List<Integer> fromList,String xmlNetwork)
//		{
//			this.result = list;
//			this.fromList = fromList;
//			this.xmlNetwork = xmlNetwork;
//		}
//
//		@Override
//	    protected Pair<Double,ArrayList<Link>> compute() 
//		{
//			List<RecursiveTask<Pair<Double,ArrayList<Link>>>> forks = new LinkedList<>();
//			
//			//并行执行任务开始
//			int coreNum = Runtime.getRuntime().availableProcessors();
//			// System.out.println("CPU核心数目："+coreNum); 
//			
//			for(int i=0; i<coreNum; i++)
//			{
//				SUETask task = new SUETask(result.get(i),xmlNetwork,fromList.get(i));
//	            forks.add(task);
//	            task.fork();
//	       }
//			
//			double minTotalEmission = 1E10;
//			ArrayList<Link> minLTPsolution = null;
//	       for(RecursiveTask<Pair<Double,ArrayList<Link>>> task:forks) 
//	       {
//	    	   double totalEmission = task.join().getKey();
//	    	   if (totalEmission < minTotalEmission)
//				{
//					minTotalEmission = totalEmission;
//					// minLTPsolution[0]=result.get(i).get(0).getName();
//					// System.out.println("## LTP "+minLTPsolution[0]);
//					minLTPsolution = new ArrayList<Link>(task.join().getValue());
//					// System.out.println("## LTP "+minLTPsolution);
//				}  
//	       }
//	       
//	       Pair<Double,ArrayList<Link>> finalResult = new Pair<Double,ArrayList<Link>>(minTotalEmission,minLTPsolution);
//	       return finalResult;
//	    }
//	}
//	
//	
//	
//	class SUETask extends RecursiveTask<Pair<Double,ArrayList<Link>>>
//	{
//		private static final long serialVersionUID = 1L;
//		//~Variables
//		// --------------------------------------------------------------------------
//		private String xmlNetwork = null;
//		private List<ArrayList<Link>> result = null;
//		private int fromIndex = -1;
//		
//		// ~Methods
//		// --------------------------------------------------------------------------
//		public SUETask(List<ArrayList<Link>> list,String xmlNetwork,int fromIndex)
//		{
//			this.xmlNetwork = xmlNetwork;
//			this.result = list;
//			this.fromIndex = fromIndex;
//		}
//
//		@Override
//	    protected Pair<Double,ArrayList<Link>> compute() 
//		{
//			return doSUE(result,xmlNetwork,fromIndex);
//	    }
//	}
//	
//	public Pair<Double,ArrayList<Link>> doSUE(List<ArrayList<Link>> list,String xmlNetwork,int fromIndex)
//	{
//		IloCplex cplex = null;
//		double minTotalEmission=1E10;
//		ArrayList<Link> minLTPsolution = null;
//		try
//		{
//			cplex = new IloCplex();
//		} 
//		catch (IloException e)
//		{
//			// TODO 自动生成的 catch 块
//			e.printStackTrace();
//		}
//		
//		//Thread.currentThread().setName("");
//		// TODO 自动生成的方法存根
//		try
//		{
//			//long storageStartTime=System.currentTimeMillis();   //获取开始时间
//			System.out.println("## "+Thread.currentThread().getName() + " is running..");  
//			//循环禁止情况, 对于每一种禁止情况进行 SUE
//			///////////////////////////////////////////////////////////////////
//			
//			int resultIndex = fromIndex-1;
//			for (int i = 0; i < list.size(); i++) 
//			{
//				resultIndex++;
//				System.out.println("禁止情况"+resultIndex+":\n"+list.get(i));
//				
//				//为每一种禁止情况建立一个新的临时路网
//				//并在此基础上进行相关的运算
//				Network networktemp = new Network();
//				ODMatrix odMatrixTemp = new ODMatrix();
//				ImportLocalNetImplement localNetworktemp = new ImportLocalNetImplement(xmlNetwork,networktemp,odMatrixTemp);
//				
//				localNetworktemp.networkStorage();
//				localNetworktemp = null;
//				
//				//1. 从路网中移除某种禁止情况的所有的要禁止的边
//				//   但是路口内部的边并没有被删除
//				//   所以还需要对路口内部对应的边进行删除
//				//-----------------------------------------------------
//				for(int j=0; j<list.get(i).size(); j++)
//				{
//					//System.out.println(result.get(i).get(j).getName());
//					//1. 在路网里删除要禁止的边
//					networktemp.removeLink(list.get(i).get(j));
//					
//					//2. 在路口里删除要禁止的边
//					//   并对冲突矩阵进行相应的修改
//					List<Junction> junctions = networktemp.getJunctions();
//					for(int k=0; k<junctions.size(); k++) //遍历所有的路口
//					{
//						Junction junc = junctions.get(k);
//						boolean removeResult = junc.removeInternalEdge(list.get(i).get(j));
//						if(removeResult == true)
//						{
//							//System.out.println("在"+junc.getName()+"删除一条左转边成功!");
//							break;
//						}
//						
//						junc = null;
//					}
//					junctions = null;
//				}
//
//				//2. 计算SUE
//				//------------------------------------------------------
//				StochasticUserEquilibirum sue = new StochasticUserEquilibirum(networktemp, odMatrixTemp,cplex);
//				sue.algorithmMSA();
//				
//				double totalEmission=0;
//				for(int j=0;j<sue.getLinkFlowAllOD().length;j++)
//				{
//					//System.out.println("link flow: "+sue.getLinkFlowAllOD()[j]);
//					//System.out.println("travel time: "+sue.getTravelTime()[j]);
//					totalEmission+=sue.getLinkFlowAllOD()[j]*sue.getTravelTime()[j];
//					//System.out.println("totalEmission: "+totalEmission);
//				}
//				if(totalEmission==0)
//				{
//					continue;
//					//System.exit(0);
//				}
//				
//				if(totalEmission<minTotalEmission)
//				{
//					minTotalEmission=totalEmission;
//					//minLTPsolution[0]=result.get(i).get(0).getName();
//						//	System.out.println("## LTP "+minLTPsolution[0]);
//					minLTPsolution = new ArrayList<Link>(list.get(i));	
//					//System.out.println("## LTP "+minLTPsolution);
//				}
//				
//				// Output result by SUE
//				sue.setNetwork(null);
//				sue.setOdMatrix(null);
//				sue.setCplex(null);
//				sue = null;
//				//System.gc();
//				networktemp.setNetworkName(null);
//				networktemp.setAdjacencyMatrix(null);
//				networktemp.setJunctions(null);
//				networktemp.setLeftTurnLinks(null);
//				networktemp.setLinks(null);
//				networktemp.setGraph(null);
//				networktemp.setLinks(null);
//				networktemp = null;
//				
////				try
////				{
////					Thread.sleep((int)Math.random()*10);//不让当前线程霸占该进程所获CPU，留一定时间给其他线程执行
////				} 
////				catch (InterruptedException e)
////				{
////					// TODO 自动生成的 catch 块
////					e.printStackTrace();
////				}
//			}
//			
//			System.out.println("## The minimum total emission: "+minTotalEmission);
//			System.out.println("## Left turn prohibition solution: "+minLTPsolution);
//			//最优解情况下的平均degree of saturation
//			//long storageEndTime=System.currentTimeMillis(); //获取结束时间
//			//System.out.println("Total running time: " + (storageEndTime-storageStartTime)/60000 +"min");
//			list = null;
//			
//			cplex = null;
//			System.out.println("## "+Thread.currentThread().getName() + " will be ended..");
//		} 
//		catch (IOException e)
//		{
//			// TODO 自动生成的 catch 块
//			e.printStackTrace();
//		}
//		Pair<Double,ArrayList<Link>> result =  new Pair<Double,ArrayList<Link>>(minTotalEmission,minLTPsolution);
//		
//		return result;
//	}
//	
//	/**
//	 * @param args
//	 * @throws InterruptedException 
//	 * @throws IOException 
//	 */
//	public static void main(String[] args) throws IOException, InterruptedException
//	{
//		// TODO 自动生成的方法存根
//		@SuppressWarnings("unused")
//		ForkJionTaskTest test = new ForkJionTaskTest();
//	}
//
//}