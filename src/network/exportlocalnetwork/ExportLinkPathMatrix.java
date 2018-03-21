/* --------------------------------------------------------------------
 * ExportLinkPathMatrix.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and all Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 08.04.2016
 * 
 * Function:
 *           1.导出Link-Path XML文件
 *           
 */
package network.exportlocalnetwork;

import java.io.FileOutputStream;
import java.io.IOException;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class ExportLinkPathMatrix
{
	//~Variables
	//--------------------------------------------------------------------------
	private Document document = null;             //要导出的文件
	private Element root = null;                  //根节点LinkPathMatrix
	private Element odPair = null;                //ODPair
	private Element pathLinkAndLengthTime = null; //LinkPathInfo
	//~Methods
	//--------------------------------------------------------------------------
	/**
	 * 构造函数
	 */
	public ExportLinkPathMatrix()
	{
		document = DocumentHelper.createDocument(); 		   //创建文档的根节点
		root = DocumentHelper.createElement("LinkPathMatrix"); //创建文档的 根元素节点
		document.setRootElement(root);                         //设置文档根节点
	}
	
	/**
	 * 给根节点添加子节点OD Pair
	 */
	public void addOD()
	{
		//给根节点添加子节点OD Pair
		//----------------------------------------------------------------
		root.addComment("OD Pair");
		odPair = root.addElement("ODPair");
	}
	
	/**
	 * 设置OD Pair 的属性
	 * @param origin
	 * @param destination
	 */
	public void setODAttribute(final String origin, final String destination)
	{
		odPair.addAttribute("origin", origin);           //设置源点
		odPair.addAttribute("destination", destination); //设置终点
	}
	
	/**
	 * 设置OD Pair的子节点LinkPathInfo
	 * @param id
	 * @param linkpath
	 * @param lengthtime
	 */
	public void addLinkPathInfo(final String id,final String linkpath, final String lengthtime)
	{
		pathLinkAndLengthTime = odPair.addElement("LinkPathInfo");    //添加子节点LinkPathInfo
		pathLinkAndLengthTime.addAttribute("id", id);                 //设置子节点的ID属性
		pathLinkAndLengthTime.addAttribute("linkpath", linkpath);     //设置子节点的linkpath属性
		pathLinkAndLengthTime.addAttribute("lengthtime", lengthtime); //设置子节点的lengthtime属性
	}
	
	/**
	 * 创建LinkPath XML
	 * @param fileName
	 * @throws IOException
	 */
	public void createXMLFile(final String fileName) throws IOException
	{
		// 把生成的XML文档存放在硬盘上 true代表是否换行
		// ----------------------------------------------------
		OutputFormat format = new OutputFormat("    ", true);
		format.setEncoding("UTF-8");			// 设置编码格式

		FileOutputStream outputStream = new FileOutputStream("localnetwork\\"+fileName+".linkPath.xml");
		XMLWriter xmlWriter = new XMLWriter(outputStream, format);
		xmlWriter.write(document); //导出XML文件
		xmlWriter.close();
	}
}
