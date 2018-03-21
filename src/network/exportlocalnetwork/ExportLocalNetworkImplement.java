/* --------------------------------------------------------------------
 * ExportLocalNetworkImplement.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and all Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 10.04.2016
 * 
 * Function:
 *           1.导出路网 XML文件执行类
 *           
 */
package network.exportlocalnetwork;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import network.importnetwork.ImportSumoNetImplement;

import org.ujmp.core.Matrix;

public class ExportLocalNetworkImplement
{
	//~Variables
	//--------------------------------------------------------------------------
	private static ExportNetworkToXML exportXML = null;
	
	//~Methods
	//--------------------------------------------------------------------------
	public static void ExportImplementer(final String netXML, 
										 final String odXML,
										 final String odMatrix, 
										 final String localNetXML) throws IOException
	{
		/////////////////////////////////////////////////////////////////
		//存储从XML文件中读取的信息到矩阵
		ImportSumoNetImplement importSumo = new ImportSumoNetImplement(netXML, odXML, odMatrix);
		Matrix matrixJunction = importSumo.getMatrixJunction();
		Matrix matrixEdge = importSumo.getMatrixEdge();
		Matrix matrixConnection = importSumo.getMatrixConnection();
		List<ArrayList<String>> tlLogicList = importSumo.getTlLogicList();  //交通灯信息
		List<ArrayList<String>> conflictList =importSumo.getConflictList(); //路口交通冲突信息
		
		List<String> originList = importSumo.getOriginList();           //获取源点列表
		List<String> destinationList = importSumo.getDestinationList(); //获取目标点列表
		List<String> odXMLList = importSumo.getOdXMLList();             //获取XML中定义的TAZ，用于检查和匹配
		List<String> odTXTList = importSumo.getOdTXTList();             //获取TXT中定义的TAZ，用于检查和匹配
		Matrix odDemandMatrix = importSumo.getOdDemandMatrix();         //获取OD Demand矩阵
		//存储网络信息到本地Network当中
		//------------------------------------------------
		long exportXMLStartTime=System.currentTimeMillis();     //获取开始时间
		exportXML = new ExportNetworkToXML();
		
		addJunctionToNetwork(matrixJunction, matrixConnection,tlLogicList,conflictList);//添加路口到网络中
		addExternalLinkToNetwork(matrixEdge);                              //添加外部连接到路网中
		addODMatrix(originList, destinationList,
					odXMLList,odTXTList,odDemandMatrix);	               //添加ODList到路网中
		exportXML.createXMLFile(localNetXML);  
		long exportXMLEndTime=System.currentTimeMillis(); 	               //获取结束时间
		System.out.println("本地XML路网导出时间： " + (exportXMLEndTime-exportXMLStartTime) +"ms");
		
		//导出本地路网信息到本地EXCEL文件
		//-----------------------------------------------
		//long exportStartTime=System.currentTimeMillis();   	//获取开始时间
		//ExportNetworkToExcel.exportToXLSXFile(matrixJunction, "localnetwork\\Junction.xlsx", "Junction");
		//ExportNetworkToExcel.exportToXLSXFile(matrixEdge, "localnetwork\\Edge.xlsx", "Edge");
		//ExportNetworkToExcel.exportToXLSXFile(matrixConnection, "localnetwork\\Connection.xlsx", "Connection");
		//long exportEndTime=System.currentTimeMillis(); 		//获取结束时间
		//System.out.println("本地EXCEL路网导出时间： " + (exportEndTime-exportStartTime) +"ms");
	}
	
	/**
	 * 添加路口到本地路网中
	 * @param matrixJunction
	 * @param matrixConnection
	 */
	private static void addJunctionToNetwork(final Matrix matrixJunction, 
			                                 final Matrix matrixConnection,
			                                 final List<ArrayList<String>> tlLogicList,
			                                 final List<ArrayList<String>> conflictList)
	{
		final long junctionMatrixRowCount = matrixJunction.getRowCount();
		final long connectionMatrixRowCount = matrixConnection.getRowCount();

		//从路口矩阵中获取信息存入路网中
		for(int i=0; i<junctionMatrixRowCount; i++)
		{
			//int count = 0;
			//当前要添加的路口
			//------------------------------------------------------------------------
			String junctionID = matrixJunction.getAsString(i,0);//路口名
			String junction_X = matrixJunction.getAsString(i,1);//路口X坐标
			String junction_Y = matrixJunction.getAsString(i,2);//路口Y坐标
			Set<String> intNodes = new HashSet<String>();
			
			Double juncY = -Double.parseDouble(junction_Y);     //转换坐标
			junction_Y = juncY.toString();  
			
			exportXML.addJunction();
			
			//添加内部通路到当前路口
			//----------------------------------------------------------
			Map<Integer,Integer> map = new HashMap<Integer,Integer>();
			for(int j=0; j<connectionMatrixRowCount; j++)
			{
				final String junctionIdInConn = matrixConnection.getAsString(j,10);
				if(junctionIdInConn.equals(junctionID))
				{
					map.put(matrixConnection.getAsInt(j,12),j);
				}
			}
			
			for(int j=0; j<map.size(); j++)
			{
				final String junctionIdInConn = matrixConnection.getAsString(map.get(j),10);
				
				String internalFromNode = matrixConnection.getAsString(map.get(j),0); //起点名
				String interFromNode_X = matrixConnection.getAsString(map.get(j),1);  //起点X坐标
				String interFromNode_Y = matrixConnection.getAsString(map.get(j),2);  //起点Y坐标
				String interFromSpeed = matrixConnection.getAsString(map.get(j),3);   //起点速度
				String interFromLane = matrixConnection.getAsString(map.get(j),4);    //起点车道数
				
				String internalToNode = matrixConnection.getAsString(map.get(j),5);   //终点名
				String interToNode_X = matrixConnection.getAsString(map.get(j),6);    //终点X坐标
				String interToNode_Y = matrixConnection.getAsString(map.get(j),7);    //终点Y坐标	
				String interToSpeed = matrixConnection.getAsString(map.get(j),8);     //终点速度
				String interToLane = matrixConnection.getAsString(map.get(j),9);      //终点车道数
				
				String direction = matrixConnection.getAsString(map.get(j),11);        //转向信息
				String linkIndex = matrixConnection.getAsString(map.get(j),12);        //内部通路索引
				
				String fromLane = matrixConnection.getAsString(map.get(j),13);         //内部通路索引
				String toLane = matrixConnection.getAsString(map.get(j),14);           //内部通路索引
				
				Double fromY = -Double.parseDouble(interFromNode_Y);
				Double toY = -Double.parseDouble(interToNode_Y);
				interFromNode_Y = fromY.toString();
				interToNode_Y = toY.toString();
				
				String intFrom = internalFromNode + " " + interFromNode_X + " " + 
								 interFromNode_Y + " " + interFromSpeed + " " + interFromLane;
				String intTo = internalToNode + " " + interToNode_X + " " + 
							   interToNode_Y + " " + interToSpeed + " " + interToLane;
				intNodes.add(intFrom);
				intNodes.add(intTo);
				
				//添加内部link到XML中
				//----------------------------------------------------------------
				String interLinkName = "InternalLink"+ linkIndex + "@" + junctionIdInConn;//连接名
				@SuppressWarnings("unused")
				boolean single = exportXML.addJunctionConnection(interLinkName, fromLane, toLane,
																 internalFromNode, 
																 interFromNode_X, interFromNode_Y,
																 interFromSpeed,interFromLane,
																 internalToNode, 
																 interToNode_X, interToNode_Y,
																 interToSpeed,interToLane,
																 direction);
			}
			/*
			for(int j=0; j<connectionMatrixRowCount; j++)
			{
				final String junctionIdInConn = matrixConnection.getAsString(j,8);
				if(junctionIdInConn.equals(junctionID))
				{//确定某条连接属于某个路口，即获取属于当前路口的内部连接
					String internalFromNode = matrixConnection.getAsString(j,0); //起点名
					String interFromNode_X = matrixConnection.getAsString(j,1);  //起点X坐标
					String interFromNode_Y = matrixConnection.getAsString(j,2);  //起点Y坐标
					String interFromSpeed = matrixConnection.getAsString(j,3);   //起点速度
					
					String internalToNode = matrixConnection.getAsString(j,4);   //终点名
					String interToNode_X = matrixConnection.getAsString(j,5);    //终点X坐标
					String interToNode_Y = matrixConnection.getAsString(j,6);    //终点Y坐标	
					String interToSpeed = matrixConnection.getAsString(j,7);     //终点速度
					
					String direction = matrixConnection.getAsString(j,9);        //转向信息
					String linkIndex = matrixConnection.getAsString(j,10);       //内部通路索引
					
					Double fromY = -Double.parseDouble(interFromNode_Y);
					Double toY = -Double.parseDouble(interToNode_Y);
					interFromNode_Y = fromY.toString();
					interToNode_Y = toY.toString();
					
					String intFrom = internalFromNode + " " + interFromNode_X + " " + 
									                          interFromNode_Y + " " + interFromSpeed;
					String intTo = internalToNode + " " + interToNode_X + " " + 
									                      interToNode_Y + " " + interToSpeed;
					intNodes.add(intFrom);
					intNodes.add(intTo);
					
					//添加内部link到XML中
					//----------------------------------------------------------------
					String interLinkName = "InternalLink"+ linkIndex + "@" + junctionIdInConn;//连接名
					@SuppressWarnings("unused")
					boolean single = exportXML.addJunctionConnection(interLinkName, 
																	 internalFromNode, 
																	 interFromNode_X, interFromNode_Y,
																	 interFromSpeed,
																	 internalToNode, 
																	 interToNode_X, interToNode_Y,
																	 interToSpeed,
																	 direction);
					//if(single == true)
					//{
					//	count++;
					//}
				}//找到一个当前路口的内部连接
			}//找到当前路口的所有内部连接，结束循环
			*/
			
			
			//添加Phase到当前路口
			//-------------------------------------------------------------
			for(int j=0; j<tlLogicList.size(); j++)
			{
				
				String tlLogicID = tlLogicList.get(j).get(0); //有红绿灯的路口的路口名称
				if(tlLogicID.equals(junctionID))
				{
					List<String> phaseInfo = new ArrayList<String>(tlLogicList.get(j));
					
					int count = 0;
					for(int n=1; n<phaseInfo.size(); n=n+2)
					{
						String duration = phaseInfo.get(n);
						String state = phaseInfo.get(n+1);
						count++;
						exportXML.addJunctionPhase(count, duration, state);
					}
				}
			}
			
			//添加Request到当前路口
			//-------------------------------------------------------------
			for(int j=0; j<conflictList.size(); j++)
			{
				
				String juncID = conflictList.get(j).get(0); //有红绿灯的路口的路口名称
				if(juncID.equals(junctionID))
				{
					List<String> foesInfo = new ArrayList<String>(conflictList.get(j));
					
					for(int n=1; n<foesInfo.size(); n++)
					{
						String foes = foesInfo.get(n);
						exportXML.addJunctionRequest(n-1, foes);
					}
				}
			}
			
			//设置Junction的属性
			//-------------------------------------------------------------
			if(intNodes.isEmpty())
			{//其内部节点信息将在下面方法addExternalLinkToNetwork()中进行修改和填充
				exportXML.setJunctionAttribute(junctionID, "Other_Junction", 
														   junction_X, junction_Y, 
														   intNodes.toString());
			}
			else
			{
				exportXML.setJunctionAttribute(junctionID, "Junction", 
														   junction_X, junction_Y, 
														   intNodes.toString());
			}		
		}//所有路口都已经处理完毕，循环结束
	}
	
	/**
	 * 添加外部连接到本地路网
	 * @param matrixEdge
	 */
	private static void addExternalLinkToNetwork(final Matrix matrixEdge)
	{
		final long edgeMatrixRowCount = matrixEdge.getRowCount();
		for(int i=0; i<edgeMatrixRowCount; i++)
		{			
			String linkName = matrixEdge.getAsString(i,0);       //连接名
			String linkFrom = matrixEdge.getAsString(i,1);       //起始路口
			String linkTo = matrixEdge.getAsString(i,2);         //终止路口
			String linkLaneCount = matrixEdge.getAsString(i,3);  //车道数量
			String linkSpeed = matrixEdge.getAsString(i,4);      //速度
			String linklength = matrixEdge.getAsString(i,5);     //连接长度
			
			String linkStartNode = matrixEdge.getAsString(i,6);  //起始点
			String linkStartNode_X = matrixEdge.getAsString(i,7);//起始点X坐标
			String linkStartNode_Y = matrixEdge.getAsString(i,8);//起始点Y坐标
			
			String linkEndNode = matrixEdge.getAsString(i,9);    //终止点
			String linkEndNode_X = matrixEdge.getAsString(i,10); //终止点X坐标
			String linkEndNode_Y = matrixEdge.getAsString(i,11); //终止点Y坐标
			
			Double fromY = -Double.parseDouble(linkStartNode_Y);
			Double toY = -Double.parseDouble(linkEndNode_Y);
			linkStartNode_Y = fromY.toString();
			linkEndNode_Y = toY.toString();
			
			//修复没有内部连接的路口的内部节点信息
			String intFrom = linkStartNode + " " + linkStartNode_X + " " + 
							 linkStartNode_Y + " " + linkSpeed + " " + linkLaneCount;
			String intTo = linkEndNode + " " + linkEndNode_X + " " + 
						   linkEndNode_Y + " " + linkSpeed + " " + linkLaneCount;
			exportXML.modifyXMLAttribute("junction[@type='Other_Junction']", linkFrom, "intNode", intFrom);
			exportXML.modifyXMLAttribute("junction[@type='Other_Junction']", linkTo, "intNode", intTo);
			
			exportXML.addExtLink();
			exportXML.addExtLinkConnection(linkStartNode, linkStartNode_X, linkStartNode_Y, 
										   				  linkEndNode, linkEndNode_X, linkEndNode_Y);
			exportXML.setExtLinkAttribute(linkName, linkFrom, linkTo, linkLaneCount, linkSpeed, linklength);
		}
	}
	
	/**
	 * 添加OD
	 * @param origin
	 * @param destination
	 * @param odDemandMatrix 
	 * @param odTXTList 
	 * @param odXMLList 
	 */
	private static void addODMatrix(final List<String> origin, final List<String> destination, 
									final List<String> odXMLList, final List<String> odTXTList, 
																	final Matrix odDemandMatrix)
	{
		for(int i=0; i<origin.size(); i++)
		{
			String org = origin.get(i);
			final String tazOrigin = odXMLList.get(i);
			final int indexRow = odTXTList.indexOf(tazOrigin);
			
			if(!org.contains("#org"))
			{
				String[] orginfo = org.split(" ");
				
				exportXML.addOrigin();
				exportXML.setOriginAttribute(orginfo[0],orginfo[1],
						                     new Double(-Double.parseDouble(orginfo[2])).toString(),
						                     orginfo[3]);
				
				for(int j=0; j<destination.size(); j++)
				{
					String des = destination.get(j);
					final String tazDestination = odTXTList.get(j);
					final int indexColumn = odTXTList.indexOf(tazDestination);
					
					if(!des.contains("#des"))
					{
						String[] desinfo = des.split(" ");
						
						int demand = odDemandMatrix.getAsInt(indexRow,indexColumn);
		                //System.out.println(demand);
						exportXML.addOriginDestination(desinfo[0],desinfo[1],
													   new Double(-Double.parseDouble(desinfo[2])).toString(),
													   desinfo[3],demand);
					}
				}
			}
		}
	}
	
}
