/* --------------------------------------------------------------------
 * ReadGraph.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and all Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 15.06.2016
 * 
 * Function:
 *           1.整体测试代码
 *           
 */
package network.test;

//import leftTurnProhibition.LeftTurnToProhibit;
import ilog.concert.*;
import ilog.cplex.*;
import leftTurnProhibition.LeftTurnToProhibit;
import network.Link;
import network.Network;
import network.ODMatrix;
import network.importnetwork.ImportLocalNetImplement;

import java.io.IOException;
import java.util.ArrayList;

import org.jgap.InvalidConfigurationException;



import trafficAssignment.StochasticUserEquilibirum;
//import trafficAssignment.StochasticUserEquilibirum;
//import leftTurnProhibition.GeneticAlgorithm;

public class ReadGraphAll
{
	//~Variables
	//--------------------------------------------------------------------------
	// This network.graph is directional
	Network network = new Network();
	ODMatrix odMatrix = new ODMatrix();
	Thread runner = null;
	// public static IloCplex cplex = null;
	
	//~Methods
	//--------------------------------------------------------------------------
	@SuppressWarnings("unused")
	public ReadGraphAll() throws IOException, IloException, InvalidConfigurationException 
	{	
//		final String xmlSumoNetwork =  "Test_network\\5Intersection6ODTest3.net.xml";
//		final String xmlOD = "Test_network\\5Intersection6OD.districts.xml";
//		final String odDemandMatrix = "Test_network\\5Intersection6OD_OD_Matrix.txt";
//		final String xmlLocalNet = "localnetwork\\5Intersection6ODTest3.localnet.xml";
		
//		final String xmlSumoNetwork =  "Test_network\\fiveIntersectionsFiveOD.net.xml";
//		final String xmlOD = "Test_network\\fiveIntersectionsFiveOD.districts.xml";
//		final String odDemandMatrix = "Test_network\\fiveIntersectionsFiveOD_OD_Matrix.txt";
//		final String xmlLocalNet = "localnetwork\\fiveIntersectionsFiveOD.localnet.xml";
////		//导出本地XML路网
//		if(xmlSumoNetwork!=null && xmlOD!=null && odDemandMatrix!=null && xmlLocalNet!=null)
//		{
//			try{
//		ExportLocalNetworkImplement.ExportImplementer(xmlSumoNetwork, xmlOD, odDemandMatrix, xmlLocalNet);
//			}
//			catch(IOException e){e.printStackTrace();}
//		}
		
		//导入本地XML路网
		long storageStartTime=System.currentTimeMillis();   //获取开始时间

		final String xmlNetwork = "localnetwork\\fiveIntersectionsFiveOD.localnet.xml";
//		final String xmlNetwork = "localnetwork\\5Intersection6ODTest3.localnet.xml";
		
		ImportLocalNetImplement localNetwork = new ImportLocalNetImplement(xmlNetwork,network,odMatrix);
		localNetwork.networkStorage();
		localNetwork = null;
		
		
		LeftTurnToProhibit leftTurnProh = new LeftTurnToProhibit();
		System.out.println("左转个数:"+network.getLeftTurnLinks().size());
//		System.out.println("左转:"+network.getLeftTurnLinks());
		ArrayList<ArrayList<Link>> result = new ArrayList<ArrayList<Link>>(leftTurnProh.subsets(network.getLeftTurnLinks()));
		
		//System.out.println("禁止情况个数:"+result.size());
		//leftTurnProh = null;
		
		IloCplex cplex = null;
		cplex = new IloCplex();
		
		//测试用BPR更新travel time的SUE
		StochasticUserEquilibirum sueBPR = new StochasticUserEquilibirum(network, odMatrix,cplex, "BPR"); // , "StageBased"
		sueBPR.algorithmMSA();
//		System.out.println("SUE with BPR: ");
		int[] SUELinkFlow = new int[sueBPR.getLinkFlowAllOD().length];
		for(int j=0;j<sueBPR.getLinkFlowAllOD().length;j++)
		{
			SUELinkFlow[j] = (int)Math.round(sueBPR.getLinkFlowAllOD()[j]);
//			System.out.println(Math.round(sueBPR.getLinkFlowAllOD()[j]));
		}
//		for(int j=0;j<sueBPR.getLinkFlowAllOD().length;j++)
 //		{
//			System.out.println(1.0*Math.round(sueBPR.getTravelTime()[j]*100)/100);
//		}	
		
		// 用stagebased 更新的SUE
		StochasticUserEquilibirum sueStagebased = new StochasticUserEquilibirum(network, odMatrix,cplex, SUELinkFlow, "LaneBased");
		sueStagebased.algorithmMSA();
		System.out.println("SUE with lane-based: ");
		double sumTT=0;
		for(int j=0;j<sueBPR.getLinkFlowAllOD().length;j++)
		{
			//System.out.println(Math.round(sueStagebased.getLinkFlowAllOD()[j]));
			//System.out.println(sueStagebased.getTravelTime()[j]);
			sumTT+=Math.round(sueStagebased.getLinkFlowAllOD()[j])*(sueStagebased.getTravelTime()[j]);
			//sumTT+=Math.round(sueStagebased.getLinkFlowAllOD()[j])*(1.0*Math.round(sueStagebased.getTravelTime()[j]*100)/100);
		}
		System.out.println("Total travel time: "+sumTT);

		
		
		
		//long storageEndTime=System.currentTimeMillis(); //获取结束时间
		//System.out.println("路网读取时间： " + (storageEndTime-storageStartTime) +"ms");
		
//		System.out.println("ODMatrix: "+odMatrix.getODMatrix());
		
//		List<ArrayList<String>> tlLogicList = network.getTlLogicList();
//		System.out.println("LogicList:"+tlLogicList);
		
		
		//Step1. 获取路网的所有禁止情况 Leftturn Prohibition
		///////////////////////////////////////////////////////////////////
//循环禁止情况, 对于每一种禁止情况进行 SUE
		///////////////////////////////////////////////////////////////////
//		double minTotalEmission=1E10;
//		ArrayList<Link> minLTPsolution = null;
//		for (int i = 0; i < result.size(); i++) 
//		//for (int i = 31; i < 32; i++) 
//		{
//			//if(i==0)break;
//			
//			//System.out.println("禁止情况"+i);
//if(i!=7218) continue; //用来测试error
////if(i!=16383) continue; //用来测试error
//			
//			System.out.println("禁止情况"+i+":\n"+result.get(i));
//			//为每一种禁止情况建立一个新的临时路网
//			//并在此基础上进行相关的运算
//			Network networktemp = new Network();
//			ODMatrix odMatrixTemp = new ODMatrix();
//			ImportLocalNetImplement localNetworktemp = new ImportLocalNetImplement(xmlNetwork,networktemp,odMatrixTemp);
//			
//			localNetworktemp.networkStorage();
////			odMatrixTemp.setOdMatrix(null);
////			odMatrixTemp = null;
//			localNetworktemp = null;
////			System.gc();
//			//1. 从路网中移除某种禁止情况的所有的要禁止的边
//			//   但是路口内部的边并没有被删除
//			//   所以还需要对路口内部对应的边进行删除
//			//-----------------------------------------------------
//			for(int j=0; j<result.get(i).size(); j++)
//			{
//				//System.out.println("prohibited result "+j+": "+result.get(i).get(j).getName());
//				
//				//1. 在路网里删除要禁止的边
//				networktemp.removeLink(result.get(i).get(j));
//				
//				//2. 在路口里删除要禁止的边
//				//   并对冲突矩阵进行相应的修改
//				List<Junction> junctions = networktemp.getJunctions();
//				for(int k=0; k<junctions.size(); k++) //遍历所有的路口
//				{
//					Junction junc = junctions.get(k);
//					boolean removeResult = junc.removeLeftTurnEdge(result.get(i).get(j));
//					//System.out.println("markerList: "+junc.getMarkerList());
//					if(removeResult == true)
//					{
//						//System.out.println("在"+junc.getName()+"删除一条左转边成功!");
//						break;
//					}
//					
//					junc = null;
//				}
//				junctions = null;
//			}
//			//System.out.println("左转剩余个数:"+networktemp.getLeftTurnLinks().size());
//			//System.gc();
//		
//			//2. 计算SUE
//			//------------------------------------------------------
//			StochasticUserEquilibirum_withBPR sueBPR = new StochasticUserEquilibirum_withBPR(networktemp, odMatrixTemp,cplex);
//			sueBPR.algorithmMSA();
////			System.out.println("SUE with BPR: ");
//			int[] SUELinkFlow = new int[sueBPR.getLinkFlowAllOD().length];
//			int temp=0;
//			for(int j=0;j<sueBPR.getLinkFlowAllOD().length;j++)
//			{
//				SUELinkFlow[j] = (int)Math.round(sueBPR.getLinkFlowAllOD()[j]);
////				System.out.println(Math.round(sueBPR.getLinkFlowAllOD()[j]));
//				temp+=SUELinkFlow[j];
//			}
//			if(temp!=0)
//			{
//				StochasticUserEquilibirum_withStageBased sueStagebased = new StochasticUserEquilibirum_withStageBased(networktemp, odMatrixTemp,cplex, SUELinkFlow);
//				sueStagebased.algorithmMSA();
//				System.out.println("SUE with stage-based: ");
//				double sumTT=0;
//				for(int j=0;j<sueBPR.getLinkFlowAllOD().length;j++)
//				{
//					//System.out.println(Math.round(sueStagebased.getLinkFlowAllOD()[j]));
//	//				System.out.println(sueStagebased.getTravelTime()[j]);
//					sumTT+=Math.round(sueStagebased.getLinkFlowAllOD()[j])*(sueStagebased.getTravelTime()[j]);
//				}
//				System.out.println("Total travel time: "+sumTT);
//if(i==7218) System.exit(0); //用来检测error
////if(i==16383) System.exit(0); //用来检测error
//if(sumTT==0) System.exit(0); //用来检测error
////if(sumTT==3728.151084730462) System.exit(0); //用来检测error
//	//			for(int j=0;j<sueStagebased.getLinkFlowAllOD().length;j++)
//	//			{
//	//				System.out.println(1.0*Math.round(sueBPR.getTravelTime()[j]*100)/100);
//	//			}		
//				
//				if(sumTT<minTotalEmission)
//				{
//					minTotalEmission=sumTT;
//					//minLTPsolution[0]=result.get(i).get(0).getName();
//						//	System.out.println("## LTP "+minLTPsolution[0]);
//					minLTPsolution = new ArrayList<Link>(result.get(i));	
//					//System.out.println("## LTP "+minLTPsolution);
//				}
//				sueStagebased.setNetwork(null);
//				sueStagebased.setOdMatrix(null);
//				sueStagebased.setCplex(null);
//				sueStagebased = null;
//			}
//			// Output result by SUE
//			sueBPR.setNetwork(null);
//			sueBPR.setOdMatrix(null);
//			sueBPR.setCplex(null);
//			sueBPR = null;
//			
//			odMatrixTemp.setOdMatrix(null);
//			odMatrixTemp = null;
//			//System.gc();
//			networktemp.setNetworkName(null);
//			networktemp.setAdjacencyMatrix(null);
//			networktemp.setJunctions(null);
//			networktemp.setLeftTurnLinks(null);
//			networktemp.setLinks(null);
//			networktemp.setGraph(null);
//			networktemp.setLinks(null);
//			networktemp = null;
//			
//			//System.gc();
//			//System.gc();
////			try
////			{
////				Thread.sleep(50);
////			} catch (InterruptedException e)
////			{
////				// TODO 自动生成的 catch 块
////				e.printStackTrace();
////			}
//			
//			
//		}
//		System.out.println("## The minimum total emission: "+minTotalEmission);
//		System.out.println("## Left turn prohibition solution: "+minLTPsolution);
//		//最优解情况下的平均degree of saturation
//		long storageEndTime=System.currentTimeMillis(); //获取结束时间
//		System.out.println("Total running time: " + (storageEndTime-storageStartTime)/60000 +"min");
//		result = null;
//		
//循环禁止情况, 对于每一种禁止情况进行 SUE， 结束				
	}
	
	public static void main(String[] args)
	{
		try
		{
			try
			{
				try
				{
					@SuppressWarnings("unused")
					ReadGraphAll test = new ReadGraphAll();
				} catch (InvalidConfigurationException e)
				{
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			} catch (IloException e)
			{
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}