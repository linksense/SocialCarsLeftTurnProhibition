/* --------------------------------------------------------------------
 * ImportLinkPathMatrix.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and all Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 07.04.2016
 * 
 * Function:
 *           1.导入Link-Path XML文件
 *           2.构建Link-Path矩阵
 *           
 */
package network.importnetwork;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.ujmp.core.Matrix;

public class ImportLinkPathMatrix
{
	//~Variables
	//--------------------------------------------------------------------------
	private Document document = null; //XML被载入后存入Document 
	
	//~Methods
	//--------------------------------------------------------------------------
	/**
	 * 构造函数
	 * @param filename
	 */
	public ImportLinkPathMatrix(final String filename)
	{
		loadXML(filename);  //载入本软件指定格式的XML路网文件
	}
	
	/**
	 * Load XML File, Read it into Document
	 * @param filename
	 * 		The Name of the file which will be analysis
	 */
	public Document loadXML(final String filename)
	{
		try 
		{
			final SAXReader saxReader = new SAXReader();
			document = saxReader.read(new File(filename)); //将Link-Path XML载入
		}
		catch (final Exception ex)
		{
			ex.printStackTrace();
		}
		return document;
	}
	
	/**
	 * 创建Link-Path Matrix
	 * @param origin
	 * @param destination
	 * @param linkPathMatrix
	 * @param lengthTimes
	 */
	@SuppressWarnings("unchecked")
	public void createLinkPathMatrix(final String origin, final String destination, 
									 Matrix linkPathMatrix, List<ArrayList<Double>> lengthTimes)
	{
		final Element root = document.getRootElement(); //获取根节点
		final String odNode = "ODPair[@origin='"+origin+"' and @destination='"+destination+"']"; 
		final List<Element> odList = root.selectNodes(odNode); //获取指定OD的LinkPath
		
		if(!odList.isEmpty())
		{
			Element odElement = odList.get(0); //获取OD
			final Iterator<Element> it = odElement.elementIterator(); //该OD的子节点属性
			int pathCount = 0;
			while (it.hasNext())
			{
				final Element linkPathElelment = it.next(); //取一个LinkPathInfo
				final String linkPathS = linkPathElelment.attribute("linkpath").getValue();     //获取LinkPath
				final String lengthtimeS = linkPathElelment.attribute("lengthtime").getValue(); //获取LengthTime
				String[] linkPath = linkPathS.split("");    //分割获取的LinkPath String
				for(int i=0; i<linkPath.length; i++)
				{
					linkPathMatrix.setAsInt(Integer.parseInt(linkPath[i]),i,pathCount); //将LinkPath存入矩阵
				}
				
				String[] lengthtimes = lengthtimeS.split(" ");          //分割获取的LengthTime String
				ArrayList<Double> lengthTime = new ArrayList<Double>(); //LengthTime的列表
				for(int i=0; i<lengthtimes.length; i++)
				{
					lengthTime.add(Double.parseDouble(lengthtimes[i])); //存入LengthTime列表
				}
				lengthTimes.add(lengthTime); //存入LengthTime列表
				pathCount++;
			}
		}
	}
}
