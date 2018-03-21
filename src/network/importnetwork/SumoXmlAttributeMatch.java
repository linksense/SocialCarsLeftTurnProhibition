/* --------------------------------------------------------------------
 * SumoXmlAttributeMatch.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 01.04.2016
 * 					 30.05.2016
 * 
 * Function:
 *           1.定义搜索SUMO定义路网 XML文件节点之过滤条件常量
 *           2.定义搜索到的节点所需获取之节点属性列表
 */
package network.importnetwork;

import java.util.List;
import java.util.ArrayList;

public class SumoXmlAttributeMatch
{
	//~Variables
	//--------------------------------------------------------------------------
	
	/*常量-指定要在XML文件中查找的带某个特定属性的节点*/
	public static final String JUNCTION   = "junction[@type!='internal']";          //定位type属性是非internal的junction节点
	public static final String EDGE       = "edge[@from]";                          //定位type具有from属性的edge节点
	public static final String TLLOGIC    = "tlLogic";                              //定位tlLogic节点
	public static final String CONNECTION = "connection[starts-with(@from,':')=0]"; //定位from属性值以：开始的connection节点

	private List<String> junctionAttributeList   = null; //junction节点所要匹配的属性列表
	private List<String> edgeAttributeList       = null; //edge节点所要匹配的属性列表
	private List<String> tlLogicAttributeList    = null; //tlLogic节点所要匹配的属性列表
	private List<String> connectionAttributeList = null; //connection节点所要匹配的属性列表

	//~Methods
  	//--------------------------------------------------------------------------
	/**
	 * 初始化节点所需匹配的属性列表
	 * @param nodeToAnalysis
	 */
	public SumoXmlAttributeMatch(final String nodeToAnalysis)
	{
		//Build the attribute match list of junctions
		//------------------------------------------------------
		if(nodeToAnalysis.equals(JUNCTION))
		{
			junctionAttributeList = new ArrayList<String>();
			junctionAttributeList.add("id"); //Junction ID
			junctionAttributeList.add("x");  //Junction X Coordinate
			junctionAttributeList.add("y");  //Junction Y Coordinate
			//junctionAttributeList.add("foes");//Conflict Matrix
		}
		
		//Build the attribute match list of edges
		//------------------------------------------------------
		if(nodeToAnalysis.equals(EDGE))
		{
			edgeAttributeList = new ArrayList<String>();
			edgeAttributeList.add("id");		//0.Edge ID
			edgeAttributeList.add("from");		//1.Edge from Junction
			edgeAttributeList.add("to");		//2.Edge to Junction
			
			edgeAttributeList.add("numLanes");	//3.Lanes numbers
			edgeAttributeList.add("speed");		//4.Speed
			edgeAttributeList.add("length");	//5.Length
			
			edgeAttributeList.add("start_node");//6.Start Internal Node
			edgeAttributeList.add("start_x");	//7.Start Internal Node X Coordinate
			edgeAttributeList.add("start_y");	//8.Start Internal Node Y Coordinate
			
			edgeAttributeList.add("end_node");	//9.End Internal Node
			edgeAttributeList.add("end_x");		//10.End Internal Node X Coordinate
			edgeAttributeList.add("end_y");		//11.End Internal Node Y Coordinate
		}
		
		//Build the attribute match list of tllogic
		//------------------------------------------------------
		if(nodeToAnalysis.equals(TLLOGIC))
		{
			tlLogicAttributeList = new ArrayList<String>();
			tlLogicAttributeList.add("id");
			tlLogicAttributeList.add("duration");
			tlLogicAttributeList.add("state");
		}
		
		//Build the attribute match list of connections
		//------------------------------------------------------
		if(nodeToAnalysis.equals(CONNECTION))
		{
			connectionAttributeList = new ArrayList<String>();
			connectionAttributeList.add("from");		  //0.From which internal node
			connectionAttributeList.add("from_x");		  //1.Internal node x coordinate
			connectionAttributeList.add("from_y");		  //2.Internal node y coordinate
			connectionAttributeList.add("from_speed");    //3.From Speed
			connectionAttributeList.add("from_laneCount");//4.From Lane Count
			
			connectionAttributeList.add("to");			 //5.To which internal node
			connectionAttributeList.add("to_x");		 //6.Internal node x coordinate
			connectionAttributeList.add("to_y");		 //7.Internal node y coordinate
			connectionAttributeList.add("to_speed");	 //8.To Speed
			connectionAttributeList.add("to_laneCount"); //9.To Lane Count
			
			connectionAttributeList.add("junction");     //10.Junction
			connectionAttributeList.add("dir");			 //11.The direction of the connection
			connectionAttributeList.add("linkIndex");    //12.The Link Index	
			
			connectionAttributeList.add("fromLane");     //13.From Lane Index
			connectionAttributeList.add("toLane");       //14.To   Lane Index
		}
	}

	/****************************************************************
	 *******************Getter and Setter****************************
	 ****************************************************************/
	
	/**
	 * @return edgeAttributeList
	 */
	public List<String> getEdgeAttributeList()
	{
		return edgeAttributeList;
	}

	/**
	 * @param edgeAttributeList 
	 * 		要设置的 edgeAttributeList
	 */
	public void setEdgeAttributeList(List<String> edgeAttributeList)
	{
		this.edgeAttributeList = edgeAttributeList;
	}

	/**
	 * @return connectionAttributeList
	 */
	public List<String> getConnectionAttributeList()
	{
		return connectionAttributeList;
	}

	/**
	 * @param connectionAttributeList 
	 * 		要设置的 connectionAttributeList
	 */
	public void setConnectionAttributeList(List<String> connectionAttributeList)
	{
		this.connectionAttributeList = connectionAttributeList;
	}

	/**
	 * @return junctionAttributeList
	 */
	public List<String> getJunctionAttributeList()
	{
		return junctionAttributeList;
	}

	/**
	 * @param junctionAttributeList 
	 *		要设置的 junctionAttributeList
	 */
	public void setJunctionAttributeList(List<String> junctionAttributeList)
	{
		this.junctionAttributeList = junctionAttributeList;
	}

	/**
	 * @return tlLogicAttributeList
	 */
	public List<String> getTlLogicAttributeList()
	{
		return tlLogicAttributeList;
	}

	/**
	 * @param tlLogicAttributeList 
	 *		要设置的 tlLogicAttributeList
	 */
	public void setTlLogicAttributeList(List<String> tlLogicAttributeList)
	{
		this.tlLogicAttributeList = tlLogicAttributeList;
	}
	
}
