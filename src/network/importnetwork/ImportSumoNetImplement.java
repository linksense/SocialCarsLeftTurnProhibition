/* --------------------------------------------------------------------
 * ImportSumoNetImplement.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and all Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 04.04.2016
 * 
 * Function:
 *           1.导入SUMO路网的执行类
 *           2.获得路口信息矩阵并对其信息进行整理
 *           3.获得道路信息矩阵并对其信息进行整理
 *           4.获得路口内部通路信息矩阵并对其信息进行整理
 *           5.获取源目标列表及其Demand矩阵
 *           
 */
package network.importnetwork;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.DocumentException;
import org.ujmp.core.Matrix;

public class ImportSumoNetImplement
{
	// ~Variables
	// --------------------------------------------------------------------------
	// 存储从SUMO XML文件中读取的信息到矩阵
	private Matrix matrixJunction   = null; //该矩阵用于存储路口的属性信息
	private Matrix matrixEdge       = null; //该矩阵用于存储道路的属性信息
	private Matrix matrixConnection = null; //该矩阵用于存储路口内部通路的属性信息
	private Matrix odDemandMatrix   = null; //该矩阵用于存储OD Demand值
	
	private List<ArrayList<String>> tlLogicList = null; //存储交通灯节点的信息
	private List<ArrayList<String>> conflictList = new ArrayList<ArrayList<String>>(); //存储路口交通冲突信息

	private List<String> originList      = new ArrayList<String>(); //源点列表，在此处存储的是路口内部的道路起点
	private List<String> destinationList = new ArrayList<String>(); //目标点列表，在此处存储的是路口内部的道路终点
	private List<String> odXMLList = new ArrayList<String>();       //OD XML文件中的TAZ名
	private List<String> odTXTList = new ArrayList<String>();       //OD TXT文件中的TAZ名
	
	// ~Methods
	// --------------------------------------------------------------------------
	/**
	 * 构造函数
	 */
	public ImportSumoNetImplement(final String netXML, final String odXML, final String odMatrix)
	{
		//////////////////////////////////////////////////////////////////
		//Import XML to get information in the Network
		final ImportSumoNetwork importXMLNetwork = new ImportSumoNetwork(netXML);          //获取SUMO XML文件的路网信息
		final ImportSumoODMatrix importODList    = new ImportSumoODMatrix(odXML,odMatrix); //获取源目标点之间Demand信息

		originList.addAll(importODList.getOriginList());           //获取源点列表
		destinationList.addAll(importODList.getDestinationList()); //获取目标点列表
		odXMLList.addAll(importODList.getOdListInXMLFile());       //获取XML中定义的TAZ，用于检查和匹配
		odTXTList.addAll(importODList.getOdListInTxtFile());       //获取TXT中定义的TAZ，用于检查和匹配
		odDemandMatrix = importODList.getOdDemandMatrix();         //获取OD Demand矩阵
		//System.out.println("originList>"+originList.size()+"<"+originList);
		//System.out.println("destinationList>"+destinationList.size()+"<"+destinationList);
		
		// 导入SUMO路网到矩阵中并进行相关运算与整合
		// --------------------------------------------------------------
		long importStartTime = System.currentTimeMillis(); // 获取开始时间
		try
		{
			matrixJunction = importXMLNetwork.elementAnalyser(SumoXmlAttributeMatch.JUNCTION);    //获取路口矩阵
			System.out.println(matrixJunction);
			
			matrixEdge = importXMLNetwork.elementAnalyser(SumoXmlAttributeMatch.EDGE);            //获取道路矩阵
			System.out.println(matrixEdge);
			
			matrixConnection = importXMLNetwork.elementAnalyser(SumoXmlAttributeMatch.CONNECTION);//获取路口内部通路矩阵
			//System.out.println(matrixConnection); //matrixConnection矩阵待整理
			
			tlLogicList = importXMLNetwork.logicElementAnalyser();
			conflictList.addAll(importXMLNetwork.getConflictList());
		} 
		catch (DocumentException e)
		{
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		
		connectionMatrixManage(matrixEdge, matrixConnection); // 整理连接矩阵
		System.out.println(matrixConnection);
		long importEndTime = System.currentTimeMillis();      // 获取结束时间
		System.out.println("SUMO路网导入时间： " + (importEndTime - importStartTime) + "ms");
		
		System.out.println("originList>"+originList.size()+"<"+originList);
		System.out.println("destinationList>"+destinationList.size()+"<"+destinationList);
	}
	
	/**
	 * Connection Matrix Management
	 * @param null
	 */
	private void connectionMatrixManage(final Matrix matrixEdge, final Matrix matrixConnection)
	{
		final long edgeMatrixRowCount       = matrixEdge.getRowCount();        //获取道路矩阵的行数
		final long connectionMatrixRowCount = matrixConnection.getRowCount();  //获取内部通路的行数
		
		int countOrg = 1;
		int countDes = 1;
		
		//将连接矩阵中边到边的信息改为点到点的信息
		for(int i=0; i<edgeMatrixRowCount; i++)
		{
			String edgeID = matrixEdge.getAsString(i,0);			 //边名
			String edgeLaneCount = matrixEdge.getAsString(i,3);     //车道数
			String edgeSpeed = matrixEdge.getAsString(i,4);			 //边的速度
			
			String fromJunctionID = matrixEdge.getAsString(i,1);     //边的起点
			String internalFromNode = matrixEdge.getAsString(i,6);   //边的内部起点
			String internalFromNode_x = matrixEdge.getAsString(i,7); //边的内部起点X坐标
			String internalFromNode_y = matrixEdge.getAsString(i,8); //边的内部起点Y坐标
			
			//String toJunctionID = matrixEdge.getAsString(i,2);	 //边的终点
			String internalToNode = matrixEdge.getAsString(i,9);	 //边的内部终点
			String internalToNode_x = matrixEdge.getAsString(i,10);  //边的内部终点X坐标
			String internalToNode_y = matrixEdge.getAsString(i,11);  //边的内部终点Y坐标
			
			//Step1. 处理ODList内容为内部点
			int orgindex = originList.indexOf(edgeID);
			if(orgindex != -1)
			{
				String org = internalFromNode+" "+internalFromNode_x+" "
							+internalFromNode_y+" "+edgeLaneCount; //路口内部起点名及其坐标
				originList.set(orgindex, org);                                               //路口内部点替换掉原道路名
				System.out.println("originList"+countOrg +":      "+internalFromNode);
				countOrg++;
			}
			
			int desindex = destinationList.indexOf(edgeID);
			if(desindex != -1)
			{
				String des = internalToNode+" "+internalToNode_x+" "
							+internalToNode_y+" "+edgeLaneCount; //路口内部终点名及其坐标
				destinationList.set(desindex, des);                                    //替换掉原来的TAZ名
				System.out.println("destinationList"+countDes+": "+internalToNode);
				countDes++;
			}
			
			//Step2. 处理connectionMatrix
			for(int j=0; j<connectionMatrixRowCount; j++)
			{
				String connFrom = matrixConnection.getAsString(j,0);        //路口内部通路的起点
				String connTo = matrixConnection.getAsString(j,5);          //路口内部通路的终点
				
				if(connFrom.equals(matrixEdge.getAsString(i,0)))
				{//替换起始边为起始点	
					matrixConnection.setAsObject(internalToNode, j,0);      //内部通路起点
					matrixConnection.setAsObject(internalToNode_x, j,1);    //内部通路起点X坐标
					matrixConnection.setAsObject(internalToNode_y, j,2);    //内部通路起点Y坐标
					matrixConnection.setAsObject(edgeSpeed, j,3);           //道路速度
					matrixConnection.setAsObject(edgeLaneCount, j,4);       //道路车道数
				}
				if(connTo.equals(matrixEdge.getAsString(i,0)))
				{//替换终止边为终止点			
					matrixConnection.setAsObject(internalFromNode, j,5);    //内部通路终点
					matrixConnection.setAsObject(internalFromNode_x, j,6);  //内部通路终点X坐标
					matrixConnection.setAsObject(internalFromNode_y, j,7);  //内部通路终点Y
					matrixConnection.setAsObject(edgeSpeed, j,8);           //道路速度
					matrixConnection.setAsObject(edgeLaneCount, j,9);       //道路车道数
					matrixConnection.setAsObject(fromJunctionID, j,10);      //内部通路所属路口
				}
			}
		}
	}
	
	/****************************************************************
	 *******************Getter and Setter****************************
	 ****************************************************************/
	/**
	 * @return matrixJunction
	 */
	public Matrix getMatrixJunction()
	{
		return matrixJunction;
	}

	/**
	 * @return matrixEdge
	 */
	public Matrix getMatrixEdge()
	{
		return matrixEdge;
	}

	/**
	 * @return matrixConnection
	 */
	public Matrix getMatrixConnection()
	{
		return matrixConnection;
	}

	/**
	 * @return originList
	 */
	public List<String> getOriginList()
	{
		return originList;
	}

	/**
	 * @return destinationList
	 */
	public List<String> getDestinationList()
	{
		return destinationList;
	}

	/**
	 * @return odXMLList
	 */
	public List<String> getOdXMLList()
	{
		return odXMLList;
	}

	/**
	 * @return odTXTList
	 */
	public List<String> getOdTXTList()
	{
		return odTXTList;
	}

	/**
	 * @return odDemandMatrix
	 */
	public Matrix getOdDemandMatrix()
	{
		return odDemandMatrix;
	}

	/**
	 * @return tlLogicList
	 */
	public List<ArrayList<String>> getTlLogicList()
	{
		return tlLogicList;
	}

	/**
	 * @return conflictList
	 */
	public List<ArrayList<String>> getConflictList()
	{
		return conflictList;
	}
}
