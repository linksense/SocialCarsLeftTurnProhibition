/* --------------------------------------------------------------------
 * LinkFlowCalculation.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 28.04.2015
 *
 * Function:
 * 			1.计算Link Flow
 * 
 */
package trafficAssignment.stochasticLoadingLinkFlow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import network.LinkPathMatrix;
import network.Network;
import network.OD;
import network.ODMatrix;
import network.algorithms.GetAllPathDFS;
import network.graph.Vertex;

import org.ujmp.core.DenseMatrix;
import org.ujmp.core.Matrix;

public class LinkFlowCalculation
{
	//~Variables
	//--------------------------------------------------------------------------
	private Network network = null;     //路网
	private ODMatrix odMatrix = null;
	private Boolean havePaths = true;
	private double[] linkFlowAllOD  = null;
	//private double[] probabilitySum  = null;
	private int[] pathNumberALL = null; // 记录一条link会被几条path通过
	//~Methods
	//--------------------------------------------------------------------------
	public LinkFlowCalculation(Network network, ODMatrix odMatrix)
	{
		this.network = network;
		this.odMatrix = odMatrix;
		
		this.linkFlowAllOD = new double[network.getLinks().size()];
	}

	public void linkFlowAllOD() throws IOException
	{
		//probabilitySum = new double[network.getLinks().size()];
		pathNumberALL = new int[network.getLinks().size()];
		for(int i=0;i<network.getLinks().size();i++)
		{
			//累积Link Flow
			linkFlowAllOD[i] = 0;
			//累积probability
			//probabilitySum[i]=0;
			pathNumberALL[i]=0;
			
		}
		havePaths = true;
		//ExportLinkPathMatrix exportLinkPath = new ExportLinkPathMatrix();
		
		List<OD> odM = odMatrix.getODMatrix();        //OD Matrix提取出OD
		//System.out.println("OD Matrix Size:"+odM.size());    //OD Matrix大小
		for(int i=0; i<odM.size(); i++)               //逐个OD对进行计算
		{
			Vertex org = odM.get(i).getOrigin();      //获取当前源点
			Vertex des = odM.get(i).getDestination(); //获取当前目标点
			
			double demand = odM.get(i).getDemand();   //当前源目标之间的Demand
			if(demand != 0)
			{
				//System.out.println(org);  //输出当前的源点
				//System.out.println(des);  //输出当前的目标点
				
				//Step 1: 深度优先搜索寻找两点之间的所有路径
				//---------------------------------------------------------------------
				List<ArrayList<Vertex>> paths = new ArrayList<ArrayList<Vertex>>();
				List<Vertex> visited = new ArrayList<Vertex>();
				
				//long pathStartTime=System.currentTimeMillis();   //获取开始时间	
				GetAllPathDFS allPath = new GetAllPathDFS();
				allPath.DepthFirstRecursive(network, visited, paths, org, des);
				visited = null;
				allPath = null;
				//System.out.println("DepthFirstRecursive' Size: " + paths.size());	
				//long pathEndTime=System.currentTimeMillis();    //获取结束时间
				//System.out.println("搜索路径时间： " + (pathEndTime-pathStartTime) +"ms");
				//System.out.println("origin: "+org+" destination: "+des);
				
				if(paths.size() != 0)
				{
					//Step 2: 生成Link Path矩阵
					//---------------------------------------------------------------------
					//long matrixStartTime=System.currentTimeMillis();   //获取开始时间
					
					//exportLinkPath.addOD();
					//exportLinkPath.setODAttribute(org.getName(), des.getName());
					
					
					List<ArrayList<Double>> lengthTimes = new ArrayList<ArrayList<Double>>();	
					Matrix linkPathM = DenseMatrix.Factory.zeros(network.getLinks().size(), paths.size());
					
					LinkPathMatrix linkPathMatrix = new LinkPathMatrix();
					linkPathMatrix.linkPathMatrix(network.getLinks(),paths,linkPathM, lengthTimes,null);//可以计算出来lengthTimes
					
					linkPathMatrix = null;
					
					int pathSize = paths.size();
					double[] pathTravelTime = new double[pathSize];
					for(int n=0;n<pathSize;n++)
					{
						//System.out.println("paths: "+paths.get(n).toString());
						pathTravelTime[n] = lengthTimes.get(n).get(1);
						//System.out.println("pathTravelTime[n]: "+pathTravelTime[n]);
					}
					paths = null; lengthTimes = null; 
					//long matrixEndTime=System.currentTimeMillis();     //获取结束时间
					//System.out.println("连接路径矩阵计算时间: " + (matrixEndTime-matrixStartTime)+"ms");
					
					//if(paths.size() != 0)
					//{
					//	System.out.println("LinkPathMatrix: \n"+linkPathM.toString()+"\n");
						//System.out.println("LengthTimeList: \n"+lengthTimes.toString()+"\n");
					//}
					
					//System.out.println("OD demand:"+demand);
					
					//step 3. 计算OD的linkFlow
					//----------------------------------------------------------------------------
					linkFlow(network.getLinks().size(),pathSize,demand,linkPathM,pathTravelTime);
					linkPathM = null;
				}
				else
				{
//					for(int j=0;j<network.getLinks().size();j++)
//					{
//						//累积Link Flow
//						linkFlowAllOD[j] = 0;
//					}
					havePaths = false;
					break;
				}
			}
		}
		odM = null;
		//System.gc();
		//exportLinkPath.createXMLFile(network.getNetworkName());
	}
	
	private void linkFlow(int linkSize, int pathSize,double odDemand,Matrix linkPathM,double[] pathTravelTime)
	{
		PathProbabilityCalculation probability = new PathProbabilityCalculation(pathSize);
		probability.LogitModel(1.0,pathTravelTime);
		
		double[] pathProbability = new double[pathSize];
		int[] pathNumber = new int[linkSize];
		double[] linkFlow = new double[linkSize];  //Link Flow 道路流量
		double[] probabilityTemp = new double[linkSize];//计算每条link被选取到的概率，测试用
		for(int i=0;i<pathSize;i++)
		{
			
			pathProbability[i] = probability.getPathProbability()[i];
			for(int j=0;j<linkSize;j++)
			{
				//计算Link Flow
				
				linkFlow[j] += (odDemand*linkPathM.getAsInt(j,i)*pathProbability[i]);
				//System.out.println("link flow: "+j+"\t"+linkFlow[j]);
				probabilityTemp[j]+=linkPathM.getAsInt(j,i)*pathProbability[i];//计算每条link被选取到的概率，测试用
				pathNumber[j]+=linkPathM.getAsInt(j,i);
			}
			
		}
		
		//for(int j=0;j<linkSize;j++){System.out.println("link "+j+" probabilityTemp "+probabilityTemp[j]);}
		
		for(int i=0;i<network.getLinks().size();i++)
		{
			//累积Link Flow
			linkFlowAllOD[i] += linkFlow[i];
			pathNumberALL[i]+= pathNumber[i];
			//probabilitySum[i] += probabilityTemp[i];
			//System.out.println("OD demand is: "+odDemand+" "+network.getLinks().get(i).getName()+" "+ probabilityTemp[i]);
		}
	}
	
	public void finalize()
	{           
        //super.finalize();  
        //System.out.println("####### LinkFlowCalculation finalize method was called! #######");
    }

	/****************************************************************
	 *******************Getter and Setter****************************
	 ****************************************************************/
	/**
	 * @return linkFowAllOD
	 */
	public double[] getLinkFlowAllOD()
	{
		return linkFlowAllOD;
	}
	
	/**
	 * @return pathNumberALL
	 */
	public int[] getPathNumberAllOD()
	{
		return pathNumberALL;
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
	 * @return havePaths
	 */
	public Boolean getHavePaths()
	{
		return havePaths;
	}

	/**
	 * @param havePaths 
	 *		要设置的 havePaths
	 */
	public void setHavePaths(Boolean havePaths)
	{
		this.havePaths = havePaths;
	}
}
