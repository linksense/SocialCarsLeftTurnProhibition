/* --------------------------------------------------------------------
 * ImportSumoNetwork.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 02.04.2016
 * 
 * Function:
 *           1.获取SUMO路网路口道路和路口内部通路的属性值
 *           2.存储路口属性值到路口属性矩阵
 *           3.存储道路属性值到道路属性矩阵
 *           4.存储路口内部通路属性值到路口内部通路属性矩阵
 *           
 */
package network.importnetwork;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;

import org.dom4j.Element;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.dom4j.DocumentException;
import org.ujmp.core.Matrix;
import org.ujmp.core.objectmatrix.impl.DefaultDenseObjectMatrix2D;

public class ImportSumoNetwork
{
	//~Variables
	//--------------------------------------------------------------------------
	private Document document; //载入的路网将被存入Document中
	private ArrayList<ArrayList<String>> conflictList = new ArrayList<ArrayList<String>>();
	
	//~Methods
	//--------------------------------------------------------------------------
	/**
	 * 构造函数
	 * @param filename 
	 * 	          要进行信息提取的SUMO路网XML文件
	 */
	public ImportSumoNetwork(final String filename)
	{
		loadXML(filename); //载入SUMO路网文件
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
			document = saxReader.read(new File(filename)); //载入的路网被存入Document中
		}
		catch (final Exception ex)
		{
			ex.printStackTrace();
		}
		return document;
	}
	
	/****************************************************************
	 *******************遍历节点的几种方法****************************
	 ****************************************************************/	
	/**
	 * Read all Node in the XML Fil
	 * @param node
	 * 		Node to Manage
	 * 
	 * 备注：此方法并未被使用
	 */
	@SuppressWarnings("unchecked")
	public void listNodes(final Element node) throws DocumentException
	{
		manageAttributeInfo(node, false); //获取当前结点的属性值
		
		//start next node if has
		final Iterator<Element> it = node.elementIterator(); //获取当前结点的子节点列表
		while (it.hasNext())
		{
			final Element e = it.next();
			listNodes(e); //递归调用，如果存在子节点则获取该子节点的属性并输出
		}
	}
	
	/**
	 * Read all Father Node under Root Node
	 * @param null
	 * 
	 * 备注：此方法并未被使用
	 */
	@SuppressWarnings("unchecked")
	public void listNodes() throws DocumentException
	{
		final Element node = document.getRootElement();      //从document中获取XML的根节点
		
		final Iterator<Element> it = node.elementIterator(); //获得根节点下的节点迭代器
		for(Element element = it.next();it.hasNext(); element = it.next())
		{
			manageAttributeInfo(element, false);             //提取输出保存对应节点的属性值
			
		}
	}
	
	/**
	 * Read all Father Node under Root Node and all Sub Node under Father Node
	 * @param null
	 * 
	 * 备注：此方法并未被使用		
	 */
	@SuppressWarnings("unchecked")
	public void listNodesSub() throws DocumentException
	{
		final Element node = document.getRootElement();     //从document中获取XML的根节点
		
		final Iterator<Element> it=node.elementIterator();  //根节点的所有子节点迭代器
		for(Element element = it.next();it.hasNext(); element = it.next())
		{
			manageAttributeInfo(element, false);                     //输出管理当前根节点的子节点的属性
			
			final Iterator<Element> subIt=element.elementIterator(); //当前根节点的子节点的所有子节点迭代器
			while (subIt.hasNext())
			{
				final Element e = subIt.next();
				manageAttributeInfo(e, true);  //输出管理当前根节点的子节点的当前子节点的属性
			}
			//Element element = it.next();
		}
	}
	
	/**
	 * Manage all Attribute in a Node 
	 * @param node
	 * 		Node to Manage
	 * @param subNode   
	 *      Sub Node or not
	 *      
	 * 备注：此方法并未被使用
	 */
	@SuppressWarnings("unchecked")
	private void manageAttributeInfo(final Element node, final boolean subNode)
	{
		/*控制台输出控制*/
		final String startVorString =  (subNode == false) ? "! Start Node --  ": "! Start SubNode --  "; 
		final String startHinString =  (subNode == false) ? "  -- Start Node ! ": "  -- Start SubNode !"; 
		final String endVorString   =  (subNode == false) ? "! End Node --  ": "! End SubNode --  "; 
		final String endHinString   =  (subNode == false) ? "  -- End Node !": "  -- End SubNode !"; 
		
		System.out.println("! ----------------------------------------------- !");
		System.out.println(startVorString + node.getName() + startHinString);
		
		final List<Attribute> list = node.attributes();  //获取当前或指定节点的属性列表
		for (final Attribute attr : list)
		{
			//System.out.println(attr.getText() + "\n" );
			System.out.println("  = > " + attr.getName() + " = " + attr.getValue() + ";" ); //在控制台输出当前节点属性及其值
			//Here Do something to match related information in Node
			
		}
  
		if (!(node.getTextTrim().equals("")))
		{
			System.out.println("Text: " + node.getText());
		}
		System.out.println(endVorString + node.getName() + endHinString);
		System.out.println("! ----------------------------------------------- !\n");
	}
	
	/**
	 * Element Analysis
	 * @param nodeToAnalysis
	 * 		Node To Analysis
	 * 
	 * 分析获取节点的信息
	 */
	@SuppressWarnings("unchecked")
	public Matrix elementAnalyser(final String nodeToAnalysis) throws DocumentException
	{
		final Element root = document.getRootElement();  //获取XML文档的根节点
		//System.out.println("Root Element: " + root.getName());
		
		//For example: nodeToAnalysis = edge[@function='internal']
		final List<Element> nodeList = root.selectNodes(nodeToAnalysis); //搜索带有指定搜索条件的根节点的子节点
		final int nodeListSize = nodeList.size();  //Matrix size based
		
		Matrix matrix = null; //用于存储节点的指定属性的属性值 
		List<String> matchAttributeList = null;  //指定要被存储的属性
		
		if(nodeToAnalysis.equals(SumoXmlAttributeMatch.JUNCTION))  //处理路口所需的属性值
		{
			//build the list of nodes
			SumoXmlAttributeMatch nodeAttributeList = new SumoXmlAttributeMatch(SumoXmlAttributeMatch.JUNCTION);
			matchAttributeList = nodeAttributeList.getJunctionAttributeList(); //要获取的属性的列表
			
			final int attributeListSize = matchAttributeList.size();  //Matrix size based
			//Create a Matrix to save Information get from XML
			matrix = new DefaultDenseObjectMatrix2D(nodeListSize, attributeListSize);          //初始化属性矩阵
			//Analysis Information in a Node
			saveJunctionAttributeValues(matrix, nodeToAnalysis, nodeList, matchAttributeList); //存储路口属性
		}
		else if(nodeToAnalysis.equals(SumoXmlAttributeMatch.EDGE))  //处理道路所对应的属性值
		{
			//build the list of edges
			SumoXmlAttributeMatch edgeAttributeList = new SumoXmlAttributeMatch(SumoXmlAttributeMatch.EDGE);
			matchAttributeList = edgeAttributeList.getEdgeAttributeList();  //要获取的属性的列表
			
			final int attributeListSize = matchAttributeList.size();  //Matrix size based
			//Create a Matrix to save Information get from XML
			matrix = new DefaultDenseObjectMatrix2D(nodeListSize, attributeListSize);
			//Analysis Information in a Node
			saveEdgeAttributeValues(matrix, nodeToAnalysis, nodeList, matchAttributeList);  //存储道路属性
		}
		else if(nodeToAnalysis.equals(SumoXmlAttributeMatch.CONNECTION))  //处理路口内部通路属性值
		{
			//build the list of connections
			SumoXmlAttributeMatch connectionAttributeList = new SumoXmlAttributeMatch(SumoXmlAttributeMatch.CONNECTION);
			matchAttributeList = connectionAttributeList.getConnectionAttributeList();  //要获取的属性的列表
			
			final int attributeListSize = matchAttributeList.size();  //Matrix size based
			//Create a Matrix to save Information get from XML
			matrix = new DefaultDenseObjectMatrix2D(nodeListSize, attributeListSize);
			//Analysis Information in a Node
			saveConnectionAttributeValues(matrix, nodeToAnalysis, nodeList, matchAttributeList);  //存储路口内部通路属性
		}
		
		return matrix;
	}
	
	/**
	 * 分析交通灯节点信息
	 * @return
	 * @throws DocumentException
	 */
	@SuppressWarnings("unchecked")
	public List<ArrayList<String>> logicElementAnalyser() throws DocumentException
	{
		final Element root = document.getRootElement();  //获取XML文档的根节点
		//System.out.println("Root Element: " + root.getName());
		
		//For example: nodeToAnalysis = edge[@function='internal']
		final List<Element> nodeList = root.selectNodes(SumoXmlAttributeMatch.TLLOGIC); //搜索带有指定搜索条件的根节点的子节点
		final int nodeListSize = nodeList.size();  //Matrix size based
		
		List<ArrayList<String>> tlLogicList = new ArrayList<ArrayList<String>>(); //存储交通灯节点的信息，不适用矩阵因为大小是变化的
		
		System.out.println(SumoXmlAttributeMatch.TLLOGIC +" \'s count: " + nodeListSize); 
		for(int i = 0; i<nodeListSize; i++) //开始获取交通灯属性列表里指定的属性的值
		{
			final Element element = nodeList.get(i);  //Get a node from the node list
			ArrayList<String> attrList = new ArrayList<String>();
			
			attrList.add(element.attribute("id").getValue());
			
			//获取Phase信息
			final List<Element> phaseList = element.selectNodes("phase"); //获取道路的车道信息
			for(int j=0; j<phaseList.size(); j++)
			{
				attrList.add(phaseList.get(j).attribute("duration").getValue());
				attrList.add(phaseList.get(j).attribute("state").getValue());
			}
			tlLogicList.add(attrList);
		}
		return tlLogicList;
	}
	
	/**
	 * 获取路口的属性并存储于矩阵中
	 * @param matrix
	 * 		Information will be stored in the Matrix
	 * @param nodeToAnalysis
	 * 		Node To Analysis
	 * @param nodeList
	 * 		Node List
	 * @param matchAttributeList
	 */
	@SuppressWarnings("unchecked")
	public void saveJunctionAttributeValues(Matrix matrix, final String nodeToAnalysis, 
											  			   final List<Element> nodeList, 
											  			   final List<String> matchAttributeList)
	{
		final int nodeListSize = nodeList.size();
		
		System.out.println(nodeToAnalysis +" \'s count: " + nodeListSize); 
		for(int i = 0; i<nodeListSize; i++) //开始获取路口属性列表里指定的属性的值
		{
			final Element element = nodeList.get(i);  //Get a node from the node list
			
			//match attribute information
			//System.out.println("! ----------------------------------------------- !");
			//System.out.println("! Start Node --  " + element.getName() + "  -- Start Node !");
			
			final List<Attribute> listAttribute = element.attributes(); //当前节点的属性
			Map<String, String> mapCurrently = new HashMap<String, String>();
			for (final Attribute attr : listAttribute)
			{
				//Save all Attribute and it's Value in a Key-Value Map
				mapCurrently.put(attr.getName(),attr.getValue());
			}
			
			
			//获取Request信息
			final List<Element> requestList = element.selectNodes("request"); //获取道路的车道信息
			if(!requestList.isEmpty())
			{
				ArrayList<String> attrList = new ArrayList<String>();
				attrList.add(element.attribute("id").getValue());    //将当前路口名加入列表
				for(int j=0; j<requestList.size(); j++)
				{
					attrList.add(requestList.get(j).attribute("foes").getValue()); //依次加入冲突序列
				}
				conflictList.add(attrList);
			}
			
			if(nodeToAnalysis.equals(SumoXmlAttributeMatch.JUNCTION))
			{
				String matchAttribute = "";
				//Match all demanded Attributes		
				for(int j = 0; j<matchAttributeList.size(); j++)
				{
					matchAttribute = matchAttributeList.get(j);
					if(mapCurrently.containsKey(matchAttribute))
					{
						final String attributeValue = mapCurrently.get(matchAttribute);
						//System.out.println("  = > " + matchAttribute + " = " + attributeValue + ";" );
						matrix.setAsObject(attributeValue, i,j); //存储路口属性值到矩阵中
					}
				}
			}
			//System.out.println("! End Node --  " + element.getName() + "  -- End Node !");
			//System.out.println("! ----------------------------------------------- !\n");		
		}
	}
	/**
	 * 获取道路的属性并存储于矩阵中
	 * @param matrix
	 * 		Information will be stored in the Matrix
	 * @param nodeToAnalysis
	 * 		Node To Analysis
	 * @param nodeList
	 * 		Node List
	 * @param matchAttributeList
	 */
	@SuppressWarnings("unchecked")
	public void saveEdgeAttributeValues(Matrix matrix, final String nodeToAnalysis, 
										  			   final List<Element> nodeList, 
										  			   final List<String> matchAttributeList)
	{
		final int nodeListSize = nodeList.size();
		
		System.out.println(nodeToAnalysis +" \'s count: " + nodeListSize); 
		for(int i = 0; i<nodeListSize; i++)
		{
			//Get a node from the node list
			final Element element = nodeList.get(i);
			
			//match attribute information
			//System.out.println("! ----------------------------------------------- !");
			//System.out.println("! Start Node --  " + element.getName() + "  -- Start Node !");
			
			final List<Attribute> listAttribute = element.attributes();
			Map<String, String> mapCurrently = new HashMap<String, String>();
			for (final Attribute attr : listAttribute)
			{
				//Save all Attribute and it's Value in a Key-Value Map
				mapCurrently.put(attr.getName(),attr.getValue());
			}
			
			if(nodeToAnalysis.equals(SumoXmlAttributeMatch.EDGE))
			{
				//为处于路口内部的虚拟道路起止点命名，命名规则是该点所在Junction@Edge当前边
				String start_node = mapCurrently.get("from") + "@" + mapCurrently.get("id");
				String end_node = mapCurrently.get("to") + "@" + mapCurrently.get("id");

				//获取多车道信息并将多车道取平均处理为单一车道
				final List<Element> subNodeList = element.selectNodes("lane"); //获取道路的车道信息
				double speed = 0, length = 0, start_x = 0, start_y = 0, end_x = 0, end_y = 0;
				int numLanes = subNodeList.size();	 //车道数量
				for(int p = 0; p<numLanes; p++)
				{
					speed += Double.parseDouble(subNodeList.get(p).attribute("speed").getValue());
					length += Double.parseDouble(subNodeList.get(p).attribute("length").getValue());

					final String[] shape = subNodeList.get(p).attribute("shape").getValue().split(",| ");
					start_x += Double.parseDouble(shape[0]);
					start_y += Double.parseDouble(shape[1]);
					end_x   += Double.parseDouble(shape[shape.length-2]);
					end_y   += Double.parseDouble(shape[shape.length-1]);
				}
				speed   /= numLanes;  //车道的平均速度
				length  /= numLanes;  //车道的平均长度 
				start_x /= numLanes;  //车道的平均起点X坐标
				start_y /= numLanes;  //车道的平均起点Y坐标
				end_x   /= numLanes;  //车道的平均终点X坐标
				end_y   /= numLanes;  //车道的平均终点Y坐标

				matrix.setAsObject(mapCurrently.get("id"), i,0);     //将道路名称存入矩阵
				matrix.setAsObject(mapCurrently.get("from"), i,1);   //将道路的起始路口存入矩阵
				matrix.setAsObject(mapCurrently.get("to"), i,2);     //将道路的终止路口存入矩阵
				matrix.setAsObject(Integer.toString(numLanes), i,3); //将道路的车道数存入矩阵
				matrix.setAsObject(Double.toString(speed), i,4);     //将道路的限速输入矩阵
				matrix.setAsObject(Double.toString(length), i,5);    //将道路的长度输入矩阵
				matrix.setAsObject(start_node, i,6);                 //将道路的起始点存入矩阵
				matrix.setAsObject(Double.toString(start_x), i,7);   //将道路的起始点X坐标存入矩阵
				matrix.setAsObject(Double.toString(start_y), i,8);   //将道路的起始点Y坐标存入矩阵
				matrix.setAsObject(end_node, i,9);                   //将道路的终止点存入矩阵
				matrix.setAsObject(Double.toString(end_x), i,10);    //将道路的终止点X坐标存入矩阵
				matrix.setAsObject(Double.toString(end_y), i,11);    //将道路的终止点Y坐标存入矩阵
			}
			//System.out.println("! End Node --  " + element.getName() + "  -- End Node !");
			//System.out.println("! ----------------------------------------------- !\n");		
		}
	}
	
	/**
	 * 获取路口内部通路的属性并存储于矩阵中
	 * @param matrix
	 * 		Information will be stored in the Matrix
	 * @param nodeToAnalysis
	 * 		Node To Analysis
	 * @param nodeList
	 * 		Node List
	 * @param matchAttributeList
	 */
	@SuppressWarnings("unchecked")
	public void saveConnectionAttributeValues(Matrix matrix, final String nodeToAnalysis, 
											  				 final List<Element> nodeList, 
											  				 final List<String> matchAttributeList)
	{
		final int nodeListSize = nodeList.size();
		
		System.out.println(nodeToAnalysis +" \'s count: " + nodeListSize); 
		for(int i = 0; i<nodeListSize; i++)
		{
			//Get a node from the node list
			final Element element = nodeList.get(i);
			
			//match attribute information
			//System.out.println("! ----------------------------------------------- !");
			//System.out.println("! Start Node --  " + element.getName() + "  -- Start Node !");
			
			final List<Attribute> listAttribute = element.attributes();
			Map<String, String> mapCurrently = new HashMap<String, String>();
			for (final Attribute attr : listAttribute)
			{
				//Save all Attribute and it's Value in a Key-Value Map
				mapCurrently.put(attr.getName(),attr.getValue());
			}
			
			if(nodeToAnalysis.equals(SumoXmlAttributeMatch.CONNECTION))
			{
				String matchAttribute = "";
				//Match all demanded Attributes		
				for(int j = 0; j<matchAttributeList.size(); j++)
				{
					matchAttribute = matchAttributeList.get(j);
					if(mapCurrently.containsKey(matchAttribute))
					{
						final String attributeValue = mapCurrently.get(matchAttribute);
						//System.out.println("  = > " + matchAttribute + " = " + attributeValue + ";" );
						matrix.setAsObject(attributeValue, i,j);  //存储路口内部通路属性值
					}
				}
			}
			//System.out.println("! End Node --  " + element.getName() + "  -- End Node !");
			//System.out.println("! ----------------------------------------------- !\n");		
		}
	}

	/**
	 * @return conflictList
	 */
	public ArrayList<ArrayList<String>> getConflictList()
	{
		return conflictList;
	}
}
