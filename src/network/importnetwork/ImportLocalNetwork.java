/* --------------------------------------------------------------------
 * ImportLocalNetwork.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and all Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 04.04.2016
 * 
 * Function:
 *           1.导入本软件指定格式的XML路网文件
 *           2.从载入的XML文件中提取信息以构建路网
 *           3.添加路口及其内部通路相关信息到路网网络
 *           4.添加(类型为External的)道路到路网网络中
 *           
 */
package network.importnetwork;

import java.io.File;
import java.util.List;

import network.Junction;
import network.Link;
import network.Network;
import network.graph.Vertex;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class ImportLocalNetwork
{
	//~Variables
	//--------------------------------------------------------------------------
	private Document document = null;          //XML被载入后存入Document
	private Network network   = new Network(); //建立新的网络，其中存储了路网的信息

	//~Methods
	//--------------------------------------------------------------------------
	/**
	 * 构造函数
	 * @param filename
	 */
	public ImportLocalNetwork(final String filename)
	{
		loadXML(filename); //载入本软件指定格式的XML路网文件
	}
	
	/**
	 * Load XML File, Read it into Document
	 * @param filename
	 * 		The Name of the file which will be analysis
	 */
	public Document loadXML(final String filename)
	{
		//Document document = null;
		try 
		{
			final SAXReader saxReader = new SAXReader();
			document = saxReader.read(new File(filename)); //将路网读入Document中
		}
		catch (final Exception ex)
		{
			ex.printStackTrace();
		}
		return document;
	}
	
	/**
	 * 从载入的XML文件中提取信息以构建路网
	 * @return network
	 * 			路网网络
	 */
	@SuppressWarnings("unchecked")
	public Network networkStorage()
	{
		final Element root = document.getRootElement();  //获取XML文件的根节点
		
		//获取根节点下的路口节点列表，再通过这些路口节点获取路口内部通路信息
		final List<Element> junctionList = root.selectNodes(LocalXmlAttributeMatch.JUNCTION); 
		
		//获取根节点下的道路节点列表，再通过这些道路节点获取更多道路相关的信息信息
		final List<Element> extLinkList = root.selectNodes(LocalXmlAttributeMatch.EXTLINK);
		
		addJunctionToNetwork(junctionList); //添加路口到路网网络
		addExtLinkToNetwork(extLinkList);   //添加道路到路网网络
		
		return network;
	}
	
	/**
	 * 添加路口及其内部通路相关信息到路网网络
	 * @param junctionList
	 * 			路口列表
	 */
	@SuppressWarnings("unchecked")
	private void addJunctionToNetwork(List<Element> junctionList)
	{
		final int junctionListSize = junctionList.size();  //路口的个数
		System.out.println("Junction's count: " + junctionListSize); 
		
		for(int i = 0; i<junctionListSize; i++)     //逐个分析路口信息及其内部通路信息并将它们存入路网
		{
			Element element = junctionList.get(i);  //Get a Junction from the Junction List
			List<Attribute> listJunctionAttribute = element.attributes();  //获取当前路口节点的属性列表
			
			//生成一个新的路口
			//---------------------------------------------------
			String id      = listJunctionAttribute.get(0).getValue();  //当前路口的ID
			String type    = listJunctionAttribute.get(1).getValue();  //当前路口的类型
			String x       = listJunctionAttribute.get(2).getValue();  //当前路口的X坐标
			String y       = listJunctionAttribute.get(3).getValue();  //当前路口的Y坐标
			String intNode = listJunctionAttribute.get(4).getValue();  //当前路口的内部节点
			
			Junction junction = new Junction(id);             //设置路口名字
			junction.setType(type);                           //设置路口类型
			junction.setX_Coordinate(Double.parseDouble(x));  //设置路口X坐标
			junction.setY_Coordinate(Double.parseDouble(y));  //设置路口Y坐标
			
			//添加内部节点到路口
			//---------------------------------------------------
			//step 1.  处理路口内部节点列表字符串[x x x, x x x, x x x]
			intNode = intNode.replace("[", "");      //把内部节点列表字符串中的‘[’去掉
			intNode = intNode.replace("]", "");      //把内部节点列表字符串中的‘]’去掉
			String[]intNodes = intNode.split(", ");  //把内部节点列表字符串中的剩下的字符串用‘, ’分割获取节点数组
			
			//step 2.  向路口中添加内部节点
			for(int p=0; p<intNodes.length; p++)
			{
				String[] node = intNodes[p].split(" ");  			     //分割内部节点及其坐标信息
				
				 Vertex interNode = new Vertex(node[0],"Internal",       //节点名和类型
										  Double.parseDouble(node[1]),   //节点X坐标
										  Double.parseDouble(node[2]));  //节点Y坐标
				
				junction.addInternalNode(interNode);    			     //为路口添加一个内部节点
			}

			//添加内部通路到路口和路网网络
			//-----------------------------------------------------
			List<Element> intLinkList = element.selectNodes(LocalXmlAttributeMatch.INTCONNECTION); //获取路口内部通路
			
			for(int j = 0; j<intLinkList.size(); j++)
			{
				//step 1.  获取内部通路属性
				List<Attribute> listIntLinkAttribute = intLinkList.get(j).attributes(); //获取内部通路属性列表
				
				String intLinkID       = listIntLinkAttribute.get(0).getValue(); //内部通路ID
				String intLinkNumLanes = listIntLinkAttribute.get(1).getValue(); //内部通路车道数
				String intLinkFrom     = listIntLinkAttribute.get(2).getValue(); //内部通路起点
				String intLinkFromX    = listIntLinkAttribute.get(3).getValue(); //内部通路起点X坐标
				String intLinkFromY    = listIntLinkAttribute.get(4).getValue(); //内部通路起点Y坐标
				String intLinkTo       = listIntLinkAttribute.get(5).getValue(); //内部通路终点
				String intLinkToX      = listIntLinkAttribute.get(6).getValue(); //内部通路终点X坐标
				String intLinkToY      = listIntLinkAttribute.get(7).getValue(); //内部通路终点Y坐标
				String dir             = listIntLinkAttribute.get(8).getValue(); //内部通路转向信息
				
				//step 2.  生成内部通路的起止点
				Vertex interFromNode = new Vertex(intLinkFrom,"Internal",        //内部通路起点及其类型
											  Double.parseDouble(intLinkFromX),  //内部通路起点X坐标
											  Double.parseDouble(intLinkFromY)); //内部通路起点Y坐标
				 
				Vertex interToNode   = new Vertex(intLinkTo,"Internal",          //内部通路终点及其类型        
										      Double.parseDouble(intLinkToX),    //内部通路终点X坐标
										      Double.parseDouble(intLinkToY));   //内部通路终点Y坐标
				
				//step 3.  添加内部通路到路口
//				junction.addInternalEdge(interFromNode, interToNode);            //添加内部通路到路口
				
				//step 4.  将内部通路作为(类型为Internal的)道路并将其添加到路网网络
				Link link = new Link(interFromNode,interToNode);       //生成一条道路
				link.setName(intLinkID);                               //设置道路的ID
				link.setLinkType("Internal");                          //设置道路类型
				link.setLaneCount(Integer.parseInt(intLinkNumLanes));  //设置道路车道
				link.setDirection(dir);                                //设置转向信息
				
				network.addLink(link);                                 //添加道路到路网网络
			}	
			
			network.addJunction(junction);                             //添加路口到网络
		}
	}
	
	
	/**
	 * 添加(类型为External的)道路到路网网络中
	 * @param extLinkList
	 *        (类型为External的)道路列表
	 */
	@SuppressWarnings("unchecked")
	private void addExtLinkToNetwork(List<Element> extLinkList)
	{
		final int extLinkListSize = extLinkList.size();  //(类型为External的)道路列表的大小
		System.out.println("ExtLink's count: " + extLinkListSize); 
		
		for(int i = 0; i<extLinkListSize; i++)  //逐个分析(类型为External的)道路列表并将其信息存入路网网络
		{
			Element element = extLinkList.get(i);                           //获取一个(类型为External的)道路
			
			List<Attribute> listExtLinkAttribute = element.attributes();    //道路的属性列表
			
			String id = listExtLinkAttribute.get(0).getValue();             //道路的名称
			//String fromJunction = listExtLinkAttribute.get(1).getValue();
			//String toJunction = listExtLinkAttribute.get(2).getValue();
			String numLanes = listExtLinkAttribute.get(3).getValue();       //道路的车道数
			String speed    = listExtLinkAttribute.get(4).getValue();       //道路的速度
			String length   = listExtLinkAttribute.get(5).getValue();       //道路的长度
			
			List<Element> extConnectionList = element.selectNodes(LocalXmlAttributeMatch.EXTCONNECTION); //外部通路信息
			//int numExtConnection = extConnectionList.size();	
			
			List<Attribute> listExtConnectionAttribute = extConnectionList.get(0).attributes();
			String fromNode = listExtConnectionAttribute.get(0).getValue(); //外部道路内部起点
			String from_x   = listExtConnectionAttribute.get(1).getValue(); //起点X坐标
			String from_y   = listExtConnectionAttribute.get(2).getValue(); //起点Y坐标
			String toNode   = listExtConnectionAttribute.get(3).getValue(); //外部道路内部终点
			String to_x     = listExtConnectionAttribute.get(4).getValue();	//终点X坐标
			String to_y     = listExtConnectionAttribute.get(5).getValue(); //终点Y坐标
			String dir      = listExtConnectionAttribute.get(6).getValue();	//转向信息

			Vertex linkStartNodeVertex = new Vertex(fromNode,"Internal",    //外部道路起点
											   Double.parseDouble(from_x),  //外部道路起点X坐标
											   Double.parseDouble(from_y)); //外部道路起点Y坐标
			Vertex linkEndNodeVertex   = new Vertex(toNode,"Internal",      //外部道路终点
											   Double.parseDouble(to_x),    //外部道路终点X坐标
											   Double.parseDouble(to_y));   //外部道路终点Y坐标
			
			//添加外部道路到路网
			//-----------------------------------------------------------
			Link link = new Link(linkStartNodeVertex, linkEndNodeVertex); //生成一条道路
			link.setName(id);                                             //道路ID
			link.setLaneCount(Integer.parseInt(numLanes));                //道路车道数
			link.setLinkType("External");                                 //道路类型
			link.setSpeed(Double.parseDouble(speed));                     //道路速度
			link.setLength(Double.parseDouble(length));                   //道路长度
			link.setDirection(dir);                                       //转向信息
			
			network.addLink(link);                                        //添加(类型为External的)道路到路网
		}
	}


	/****************************************************************
	 *******************Getter and Setter****************************
	 ****************************************************************/
	/**
	 * @return network
	 */
	public Network getNetwork()
	{
		return network;
	}
	
}
