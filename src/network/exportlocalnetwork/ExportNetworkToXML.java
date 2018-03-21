/* --------------------------------------------------------------------
 * ExportNetworkToXML.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and all Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 08.04.2016
 * 
 * Function:
 *           1.导出路网 XML文件
 *           
 */
package network.exportlocalnetwork;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class ExportNetworkToXML
{
	// ~Variables
	// --------------------------------------------------------------------------
	private Document document    = null; //要导出的文件
	private Element root         = null; //文件的根节点
	private Element junction     = null; //Junction 节点
	private Element intconnction = null; //Junction 的子节点 Intconnction
	private Element extlink      = null; //Extlink 节点
	private Element extconnction = null; //Extlink 节点的子节点 Extconnction
	private Element phase        = null; //交通信号
	private Element request      = null; //交通冲突
	private Element origin       = null; //Origin 节点
	private Element destination  = null; //Origin 节点子节点 Destination
	
	// ~Methods
	// --------------------------------------------------------------------------
	/**
	 * 构造函数
	 */
	public ExportNetworkToXML()
	{
		document = DocumentHelper.createDocument(); 		     //创建文档的根节点
		root     = DocumentHelper.createElement("localnetwork"); //创建文档的 根元素节点
		document.setRootElement(root);                           //设置文档根节点
		
		//给根节点添加属性
		//----------------------------------------------------------------
		Date dateNow = new Date();                                         //日期时间
		SimpleDateFormat ft = new SimpleDateFormat("dd/MM/yyyy kk:mm:ss"); //日期时间格式
		root.addAttribute("time", ft.format(dateNow));                     //设置创建文件的日期
	}
	
	/**
	 * 向文件中添加路口Junction节点
	 */
	public void addJunction()
	{
		//给根节点添加子节点Junction
		//----------------------------------------------------------------
		root.addComment("Junction");            
		junction = root.addElement("junction"); //添加路口Junction节点
	}
	
	/**
	 * 设置路口参数
	 * @param id
	 * @param type
	 * @param x
	 * @param y
	 * @param intNode
	 */
	public void setJunctionAttribute(final String id, final String type, 
									 final String x, final String y, final String intNode)
	{
		junction.addAttribute("id", id);           //路口ID
		junction.addAttribute("type", type);       //路口的Type
		junction.addAttribute("x", x);             //路口的X坐标
		junction.addAttribute("y", y);             //路口的Y坐标
		junction.addAttribute("intNode", intNode); //路口的内部节点
	}

	/**
	 * 添加路口内部通路
	 * @param id
	 * @param fromNode
	 * @param from_x
	 * @param from_y
	 * @param fromSpeed
	 * @param toNode
	 * @param to_x
	 * @param to_y
	 * @param toSpeed
	 * @param dir
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean addJunctionConnection(final String id, 
										 final String fromLaneIndex, final String toLaneIndex,
										 final String fromNode, 
										 final String from_x, final String from_y, 
										 final String fromSpeed, final String fromLane,
										 final String toNode, 
										 final String to_x, final String to_y, 
										 final String toSpeed, final String toLane,
										 final String dir)
	{
		String s = "intconnction[@fromNode='"+fromNode+"' and @toNode='"+toNode+"']"; 
		List<Element> list = junction.selectNodes(s); //看是否存在相同的Intconnction
		if(list.isEmpty()) //若不存在，添加之
		{
			intconnction = junction.addElement("intconnction"); //向当前Junction节点下添加Intconnction子节点
			intconnction.addAttribute("id", id);                //IntconnctionID
			intconnction.addAttribute("numLanes", "1");         //Intconnction车道数
			intconnction.addAttribute("fromLaneIndex", fromLaneIndex);//Intconnction来源车道
			intconnction.addAttribute("toLaneIndex", toLaneIndex);     //Intconnction终止车道
			
			
			intconnction.addAttribute("fromNode", fromNode);    //Intconnction起点
			intconnction.addAttribute("from_x", from_x);        //Intconnction起点X坐标
			intconnction.addAttribute("from_y", from_y);        //Intconnction起点Y坐标
			intconnction.addAttribute("from_speed", fromSpeed); //Intconnction驶入速度	
			intconnction.addAttribute("from_lane", fromLane);   //Intconnction驶入车道数
			
			intconnction.addAttribute("toNode", toNode);        //Intconnction终点
			intconnction.addAttribute("to_x", to_x);            //Intconnction终点X坐标
			intconnction.addAttribute("to_y", to_y);            //Intconnction终点Y坐标
			intconnction.addAttribute("to_speed", toSpeed);		//Intconnction驶出速度
			intconnction.addAttribute("to_lane", toLane);       //Intconnction驶出车道数
			
			intconnction.addAttribute("dir", dir);              //Intconnction转向信息
			return true;
		}
		else //若存在，车道数加一
		{
			Iterator<Element> iter = list.iterator();
			//System.out.println(list.size());
			while (iter.hasNext())
			{
				Element ele = iter.next();
				Attribute attr = ele.attribute("numLanes"); 
				Integer numLanes = Integer.parseInt(attr.getValue())+1; //车道数加一
				attr.setValue(numLanes.toString());
				
				Attribute attrFromLane = ele.attribute("fromLaneIndex"); 
				String fromLI = attrFromLane.getValue() + "," + fromLaneIndex;
				attrFromLane.setValue(fromLI);
				
				Attribute attrToLane = ele.attribute("toLaneIndex"); 
				String toLI = attrToLane.getValue() + "," + toLaneIndex;
				attrToLane.setValue(toLI);
			}
			//System.out.println("有重复的lane");
			return false;
		}
	}

	/**
	 * @param id
	 * @param duration
	 * @param state
	 */
	public void addJunctionPhase(final Integer id, final String duration, final String state)
	{
		phase = junction.addElement("phase"); //向当前Junction节点下添加Pahse子节点
		phase.addAttribute("id", id.toString());
		phase.addAttribute("duration", duration);
		phase.addAttribute("state", state);
	}
	
	
	public void addJunctionRequest(final Integer index, final String foes)
	{
		request = junction.addElement("request"); //向当前Junction节点下添加Request子节点
		request.addAttribute("index", index.toString());
		request.addAttribute("foes", foes);
	}
	
	/**
	 * 向文档中添加外部道路
	 */
	public void addExtLink()
	{
		//给根节点添加子节点Edge
		//----------------------------------------------------------------
		root.addComment("External Link");     
		extlink = root.addElement("extlink"); //向文档添加道路Extlink
	}
	
	/**
	 * 设置外部道路参数
	 * @param id
	 * @param fromJunction
	 * @param toJunction
	 * @param numLanes
	 * @param speed
	 * @param length
	 */
	public void setExtLinkAttribute(final String id, final String fromJunction, 
									final String toJunction, final String numLanes, 
									final String speed, final String length)
	{	
		extlink.addAttribute("id", id);                     //Extlink道路ID
		extlink.addAttribute("fromJunction", fromJunction); //Extlink道路起始路口
		extlink.addAttribute("toJunction", toJunction);     //Extlink道路终止路口
		extlink.addAttribute("numLanes", numLanes);         //Extlink道路车道数
		extlink.addAttribute("speed", speed);               //Extlink道路速度
		extlink.addAttribute("length", length);             //Extlink道路长度
	}

	/**
	 * 向文件当前Extlink节点下其详细信息子节点
	 * @param fromNode
	 * @param from_x
	 * @param from_y
	 * @param toNode
	 * @param to_x
	 * @param to_y
	 */
	public void addExtLinkConnection(final String fromNode, 
									 final String from_x, final String from_y, 
									 final String toNode, final String to_x, 
									 final String to_y)
	{
		extconnction = extlink.addElement("extconnction"); //当前Extlink节点添加子节点Extconnction
		extconnction.addAttribute("fromNode", fromNode);   //当前Extlink的起始路口内部节点
		extconnction.addAttribute("from_x", from_x);       //当前Extlink的起始路口内部节点X坐标
		extconnction.addAttribute("from_y", from_y);       //当前Extlink的起始路口内部节点Y坐标
		extconnction.addAttribute("toNode", toNode);       //当前Extlink的终止路口内部节点
		extconnction.addAttribute("to_x", to_x);           //当前Extlink的终止路口内部节点X坐标
		extconnction.addAttribute("to_y", to_y);           //当前Extlink的终止路口内部节点Y坐标
		extconnction.addAttribute("dir", "s");             //当前Extlink的转向信息
	}
	
	/**
	 * 修正Junction参数的值
	 * @param node
	 * @param id
	 * @param attribute
	 * @param text
	 */
	@SuppressWarnings("unchecked")
	public void modifyXMLAttribute(final String node, final String id, 
								   final String attribute, final String text)
	{
		Element r = document.getRootElement();
		List<Element> list = r.selectNodes(node);
		Iterator<Element> iter = list.iterator();

		while(iter.hasNext())
		{
			Element ele = iter.next();
			Attribute attrID = ele.attribute("id");
			if(attrID.getValue().equals(id))
			{//Other_Junction
				Attribute attr = ele.attribute(attribute);
				if(attr.getValue().equals("[]"))
				{
					String s = "[" + text + "]";
					attr.setValue(s);
				}
				else
				{
					String s = attr.getValue();
					if(!s.contains(text))
					{
						s = s.replace("]", (", "+text+"]"));
						attr.setValue(s);
					}
				}
			}
		} 	
	}
	
	public void addOrigin()
	{
		//给根节点添加子节点Junction
		//----------------------------------------------------------------
		root.addComment("Origin");
		origin = root.addElement("origin");
	}
	
	@SuppressWarnings("unchecked")
	public void setOriginAttribute(final String id, final String x, final String y, final String lane)
	{	
		String s = "junction[@type='Other_Junction' and contains(@intNode,'"+ id +"')]";
		//System.out.println("add O: " + id);
		Element r = document.getRootElement();
		List<Element> list = r.selectNodes(s);
		String junctemp = "";
		if(!list.isEmpty())
		{
			junctemp = list.get(0).attribute("id").getValue();
		}
		
		origin.addAttribute("id", id);
		origin.addAttribute("x", x);
		origin.addAttribute("y", y);
		origin.addAttribute("lane", lane);
		origin.addAttribute("junction", junctemp);
	}
	
	@SuppressWarnings("unchecked")
	public void addOriginDestination(final String id, final String x, final String y, 
													  final String lane, final int demand)
	{
		destination = origin.addElement("destination");
		String s = "junction[@type='Other_Junction' and contains(@intNode,'"+ id +"')]";
		//System.out.println("add D: " + id);
		Element r = document.getRootElement();
		List<Element> list = r.selectNodes(s);
		String junctemp = "";
		if(!list.isEmpty())
		{
			junctemp = list.get(0).attribute("id").getValue();
		}
		
//		int demand = 500;
//		String junc = origin.attribute("junction").getValue();
//		if(junc.equals(junctemp))
//		{
//			demand = 0;
//		}
		
		destination.addAttribute("id", id);
		destination.addAttribute("x", x);
		destination.addAttribute("y", y);
		destination.addAttribute("lane", lane);
		destination.addAttribute("junction", junctemp);
		destination.addAttribute("demand", Integer.toString(demand));
	}
	
	public void createXMLFile(final String fileName) throws IOException
	{
		// 把生成的XML文档存放在硬盘上 true代表是否换行
		// ----------------------------------------------------
		OutputFormat format = new OutputFormat("    ", true);
		format.setEncoding("UTF-8");			// 设置编码格式
		
		FileOutputStream outputStream = new FileOutputStream(fileName);
		XMLWriter xmlWriter = new XMLWriter(outputStream, format);
		xmlWriter.write(document);
		xmlWriter.close();
	}
}
