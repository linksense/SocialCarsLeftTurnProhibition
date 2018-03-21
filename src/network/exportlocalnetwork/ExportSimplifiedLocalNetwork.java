/* --------------------------------------------------------------------
 * ExportSimplifiedLocalNetwork.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and all Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 09.04.2016
 * 
 * Function:
 *           1.导出简化的路网 XML文件
 *           
 */
package network.exportlocalnetwork;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class ExportSimplifiedLocalNetwork
{
	//~Variables
	//--------------------------------------------------------------------------
	private Document document = null;
	//~Methods
	//--------------------------------------------------------------------------
	public ExportSimplifiedLocalNetwork(final String originalXML, 
										final String simplifiedXML)
	{
		loadOriginalXML(originalXML);  //载入原始本地路网
		
		try
		{//导出简化后的路网
			createXMLFile(simplifiedXML);
		} 
		catch (IOException e)
		{
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
	
	/**
	 * Load XML File, Read it into Document
	 * @param filename
	 * 		The Name of the file which will be analysis
	 */
	public void loadOriginalXML(final String filename)
	{
		try 
		{
			final SAXReader saxReader = new SAXReader();
			document = saxReader.read(new File(filename));
		}
		catch (final Exception ex)
		{
			ex.printStackTrace();
		}
		Element root = document.getRootElement();
		simplifyLocalNetwork(root);
	}
	
	/**
	 * 本地路网简化
	 */
	public void simplifyLocalNetwork(Element root)
	{
		//存储被删除的路口中被删除的节点 
		List<ArrayList<String>> deletedVertexs = new ArrayList<ArrayList<String>>();
		/*******************************************************
		 *******************开始路网简化*************************
		 *******************************************************/
		simplifyJunction(root,deletedVertexs);  //简化路口	
		simplifyExtLink(root,deletedVertexs);   //简化外部连接
	}
	
	/**
	 * 简化路口
	 * @param root
	 * @param deletedVertexs
	 * @param simplifiedJunctions 
	 */
	@SuppressWarnings("unchecked")
	private void simplifyJunction(final Element root, List<ArrayList<String>> deletedVertexs)
	{
		final List<Element> junctionList = root.selectNodes("junction"); //获取Junction
		final int junctionListSize = junctionList.size();
		for(int i=0; i<junctionListSize; i++)
		{
			Element junction = junctionList.get(i);  //当前的Junction
			List<Element> intLinkList = junction.selectNodes("intconnction");
			
			List<Attribute> listJunctionAttribute = junction.attributes();
			String junctionID = listJunctionAttribute.get(0).getValue();
			String intNode = listJunctionAttribute.get(4).getValue();
			
			//只有一条内部边, 其将被删除
			if(intLinkList.size() == 1)
			{
				intNode = intNode.replace("[", "");	    //去掉[
				intNode = intNode.replace("]", "");	    //去掉]
				String[]intNodes = intNode.split(", "); //获取内部节点数组
				
				String[] nodeFrom = intNodes[0].split(" "); //获取起始内部节点的信息数组
				String fromNode = nodeFrom[0];  //起始内部节点名称
				String fromNodeX = nodeFrom[1];
				String fromNodeY = nodeFrom[2];
				
				String[] nodeTo = intNodes[1].split(" "); //获取起始内部节点的信息数组
				String toNode = nodeTo[0];  //起始内部节点名称
				String toNodeX = nodeTo[1];
				String toNodeY = nodeTo[2];
				
				double fx = Double.parseDouble(fromNodeX); //内部连接起始内部节点X坐标
				double fy = Double.parseDouble(fromNodeY); //内部连接起始内部节点Y坐标
				double tx = Double.parseDouble(toNodeX);   //内部连接终止内部节点X坐标
				double ty = Double.parseDouble(toNodeY);   //内部连接终止内部节点Y坐标
				Double length = Math.sqrt(((tx-fx)*(tx-fx) + (ty-fy)*(ty-fy)));//内部连接长度
				
				//记录删除的路口的信息
				ArrayList<String> vertexInJun = new ArrayList<String>();
				vertexInJun.add(junctionID); 		//要被删除的路口
				vertexInJun.add(fromNode); 			//内部节点：内部连接的起始点
				vertexInJun.add(toNode);   			//内部节点：内部连接的终止点
				vertexInJun.add(length.toString()); //内部连接的长度
				deletedVertexs.add(vertexInJun);
				
				//删除这个路口
				root.remove(junction);
			}
		}
	}
	
	/**
	 * 简化外部连接
	 * @param root
	 */
	@SuppressWarnings("unchecked")
	private void simplifyExtLink(final Element root,final List<ArrayList<String>> deletedVertexs)
	{		
		System.out.println(deletedVertexs.size());
		for(int i=0; i<deletedVertexs.size(); i++)
		{
			String interLinkInJunction = deletedVertexs.get(i).get(0); //被删除的路口
			//String interLinkFromNode = deletedVertexs.get(i).get(1); //被删除的内部连接的起点
			//String interLinkToNode = deletedVertexs.get(i).get(2);   //被删除的内部连接
			String interLinkLength = deletedVertexs.get(i).get(3);     //被删除内部边长度
			String fromJunction = "extlink[@toJunction='"+interLinkInJunction+"']";
			final List<Element> extLinkFromList = root.selectNodes(fromJunction);//获取Extlinks
			String toJunction = "extlink[@fromJunction='"+interLinkInJunction+"']";
			final List<Element> extLinkToList = root.selectNodes(toJunction);  //获取Extlinks
			System.out.println(i+ ": from "+extLinkFromList.size()+", to "+extLinkToList.size());
			//if(extLinkFromList.size() == 1 && extLinkToList.size() == 1)
			{
				/*********************************************
				 ***********获取From ExtLink的信息*************
				 *********************************************/
				Element fromExtLink = extLinkFromList.get(0);
				List<Attribute> listExtLinkAttributeF = fromExtLink.attributes(); //获取其参数
				String idF = listExtLinkAttributeF.get(0).getValue();             //连接名称
				//String fromJuncF = listExtLinkAttributeF.get(1).getValue();     //起始路口
				//String toJuncF = listExtLinkAttributeF.get(2).getValue();       //终止路口
				//String numLanesF = listExtLinkAttributeF.get(3).getValue();     //车道数量
				//String speedF = listExtLinkAttributeF.get(4).getValue();        //最大车速
				String lengthF = listExtLinkAttributeF.get(5).getValue();         //连接长度
				
				List<Element> extConnectionListF = fromExtLink.selectNodes("extconnction");//获取更详细的参数
				//List<Attribute> listExtConnectionAttributeF = extConnectionListF.get(0).attributes();
				//String linkFromNodeF = listExtConnectionAttributeF.get(0).getValue();//外部连接内部起点
				//String from_xF = listExtConnectionAttributeF.get(1).getValue();  	 //起点X坐标
				//String from_yF = listExtConnectionAttributeF.get(2).getValue();  	 //起点Y坐标
				//String linkToNodeF = listExtConnectionAttributeF.get(3).getValue();//外部连接内部终点
				//String to_xF = listExtConnectionAttributeF.get(4).getValue();	   	 //终点X坐标
				//String to_yF = listExtConnectionAttributeF.get(5).getValue();    	 //终点Y坐标
				//String dirF = listExtConnectionAttributeF.get(6).getValue();	     //转向信息
				/*********************************************
				 *************获取To ExtLink的信息*************
				 *********************************************/
				Element toExtLink = extLinkToList.get(0);
				List<Attribute> listExtLinkAttributeT = toExtLink.attributes();   //获取其参数
				String idT = listExtLinkAttributeT.get(0).getValue();             //连接名称
				//String fromJuncT = listExtLinkAttributeT.get(1).getValue();       //起始路口
				String toJuncT = listExtLinkAttributeT.get(2).getValue();         //终止路口
				String numLanesT = listExtLinkAttributeT.get(3).getValue();       //车道数量
				String speedT = listExtLinkAttributeT.get(4).getValue();          //最大车速
				String lengthT = listExtLinkAttributeT.get(5).getValue();         //连接长度
				
				List<Element> extConnectionListT = toExtLink.selectNodes("extconnction");//获取更详细的参数
				List<Attribute> listExtConnectionAttributeT = extConnectionListT.get(0).attributes();
				//String linkFromNodeT = listExtConnectionAttributeT.get(0).getValue();//外部连接内部起点
				//String from_xT = listExtConnectionAttributeT.get(1).getValue();  	 //起点X坐标
				//String from_yT = listExtConnectionAttributeT.get(2).getValue();  	 //起点Y坐标
				String linkToNodeT = listExtConnectionAttributeT.get(3).getValue();  //外部连接内部终点
				String to_xT = listExtConnectionAttributeT.get(4).getValue();	   	 //终点X坐标
				String to_yT = listExtConnectionAttributeT.get(5).getValue();    	 //终点Y坐标
				String dirT = listExtConnectionAttributeT.get(6).getValue();	     //转向信息
				
				/*******************************************************************
				 ***********合并边,重写To ExtLink的信息,并把From ExtLink删除*********
				 *******************************************************************/
				String newLinkID = idF+"+"+idT;
				String newToJunction = toJuncT;
				String newNumLanes = numLanesT;
				String newSpeed = speedT;
				Double newLengthD = Double.parseDouble(lengthF)+
									Double.parseDouble(interLinkLength)+
									Double.parseDouble(lengthT);
				String newLength = newLengthD.toString();
				String newLinkToNode = linkToNodeT;
				String newToX = to_xT;
				String newToY = to_yT;
				String newDir = dirT;
				
				fromExtLink.attribute("id").setValue(newLinkID);
				fromExtLink.attribute("toJunction").setValue(newToJunction);
				fromExtLink.attribute("numLanes").setValue(newNumLanes);
				fromExtLink.attribute("speed").setValue(newSpeed);
				fromExtLink.attribute("length").setValue(newLength);
				extConnectionListF.get(0).attribute("toNode").setValue(newLinkToNode);
				extConnectionListF.get(0).attribute("to_x").setValue(newToX);
				extConnectionListF.get(0).attribute("to_y").setValue(newToY);
				extConnectionListF.get(0).attribute("dir").setValue(newDir);
				
				root.remove(toExtLink);
			}
		}
		
		/*
		ArrayList<ArrayList<String>> deletedExtLinks = new ArrayList<ArrayList<String>>();
		final List<Element> extLinkList = root.selectNodes("extlink");      //获取Extlinks
		final int extLinkListSize = extLinkList.size();
		for(int i=0; i<extLinkListSize; i++)
		{
			Element extLink = extLinkList.get(i);                           //获取一个外部连接
			//获取关于该连接的所有信息
			List<Attribute> listExtLinkAttribute = extLink.attributes();    //获取其参数
			String id = listExtLinkAttribute.get(0).getValue();             //连接名称
			String fromJunction = listExtLinkAttribute.get(1).getValue();   //起始路口
			String toJunction = listExtLinkAttribute.get(2).getValue();     //终止路口
			String numLanes = listExtLinkAttribute.get(3).getValue();       //车道数量
			String speed = listExtLinkAttribute.get(4).getValue();          //最大车速
			String length = listExtLinkAttribute.get(5).getValue();         //连接长度
			
			List<Element> extConnectionList = extLink.selectNodes("extconnction");//获取更详细的参数
			List<Attribute> listExtConnectionAttribute = extConnectionList.get(0).attributes();
			String linkFromNode = listExtConnectionAttribute.get(0).getValue();	 //外部连接内部起点
			String from_x = listExtConnectionAttribute.get(1).getValue();  	     //起点X坐标
			String from_y = listExtConnectionAttribute.get(2).getValue();  	     //起点Y坐标
			String linkToNode = listExtConnectionAttribute.get(3).getValue();  	 //外部连接内部终点
			String to_x = listExtConnectionAttribute.get(4).getValue();	   	     //终点X坐标
			String to_y = listExtConnectionAttribute.get(5).getValue();    	     //终点Y坐标
			String dir = listExtConnectionAttribute.get(6).getValue();	         //转向信息

			for(int j=0; j<deletedVertexs.size(); j++)
			{
				String interLinkFromNode = deletedVertexs.get(j).get(1); //被删除的内部连接的起点
				String interLinkToNode = deletedVertexs.get(j).get(2);   //被删除的内部连接
				
				if(linkToNode.equals(interLinkFromNode))
				{//获得一条指向被删除的内部边的起始内部节点的要删除的边
					ArrayList<String> fromInfo = new ArrayList<String>();
					fromInfo.add("From");
					fromInfo.add(id);
					fromInfo.add(fromJunction);
					fromInfo.add(toJunction);
					fromInfo.add(numLanes);
					fromInfo.add(length);
					fromInfo.add(speed);
					fromInfo.add(dir);
					
					fromInfo.add(linkFromNode);
					fromInfo.add(from_x);
					fromInfo.add(from_y);

					deletedExtLinks.add(fromInfo);
					System.out.println("delete extlink");
					root.remove(extLink);
					//break;
				}
				if(linkFromNode.equals(interLinkToNode))
				{//获得一条从被删除的内部边的终止内部节点出发的要删除的边
					ArrayList<String> toInfo = new ArrayList<String>();
					toInfo.add("To");
					toInfo.add(id);
					toInfo.add(fromJunction);
					toInfo.add(toJunction);
					toInfo.add(numLanes);
					toInfo.add(length);
					toInfo.add(speed);
					toInfo.add(dir);
					
					toInfo.add(linkToNode);
					toInfo.add(to_x);
					toInfo.add(to_y);

					deletedExtLinks.add(toInfo);
					System.out.println("delete extlink");
					root.remove(extLink);
					//break;
				}
			}
		}
		*/
	}
	
	/**
	 * 创建XML
	 * @param fileName
	 * @throws IOException
	 */
	public void createXMLFile(final String fileName) throws IOException
	{
		// 把生成的XML文档存放在硬盘上 true代表是否换行
		// ----------------------------------------------------
		OutputFormat format = new OutputFormat("", false);
		format.setEncoding("UTF-8");		// 设置编码格式
		
		FileOutputStream outputStream = new FileOutputStream(fileName);
		XMLWriter xmlWriter = new XMLWriter(outputStream, format);
		xmlWriter.write(document);
		xmlWriter.close();
	}
}