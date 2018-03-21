/* --------------------------------------------------------------------
 * ImportSumoODMatrix.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 02.04.2016
 * 
 * Function:
 *           1.从OD XML文件里获取TAZ名列表
 *           2.从OD XML文件里获取在不同TAZ里的源和目标点
 *           3.从OD Demand TXT文件里获取源目标点所需Demand值
 *           4.将源目标点所需Demand值存入矩阵中
 *           
 */
package network.importnetwork;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.io.SAXReader;
import org.dom4j.Document;
import org.dom4j.Element;

import org.ujmp.core.Matrix;
import org.ujmp.core.objectmatrix.impl.DefaultDenseObjectMatrix2D;

public class ImportSumoODMatrix
{
	//~Variables
	//--------------------------------------------------------------------------
	private Document document = null;      //读取源目标区域XML并将其存入document中以便分析
	
	private LinkedHashSet<String> originList      = new LinkedHashSet<String>(); //从XML文件获得的源点列表
	private LinkedHashSet<String> destinationList = new LinkedHashSet<String>(); //从XML文件获得的目标点列表
	private LinkedHashSet<String> odListInXMLFile = new LinkedHashSet<String>(); //XML文件中的TAZ名
	private LinkedHashSet<String> odListInTxtFile = new LinkedHashSet<String>(); //TXT文件中的TAZ名
	
	private Matrix odDemandMatrix = null;  //源目标点之间的Demand
	
	//~Methods
	//--------------------------------------------------------------------------
	/**
	 * 构造函数
	 * @param filename
	 */
	public ImportSumoODMatrix(final String districts,final String odMatrix)
	{
		loadXML(districts);    //载入定义源和目标点的XML文件
		createODNodeList();    //从载入的XML文件中提取源和目标点列表
		readTXTFile(odMatrix); //从TXT文件中读取源目标节点的Demand并将其存入矩阵中
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
			document = saxReader.read(new File(filename));
		}
		catch (final Exception ex)
		{
			ex.printStackTrace();
		}
		return document;
	}
	
	/**
	 * 从被载入的XML文件中获取源和目标点并将它们分别存入源与目标点列表
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void createODNodeList()
	{
		final Element root = document.getRootElement();
		final List<Element> tazList = root.selectNodes("taz"); //在tay节点里描述了源或目标区域
		
		final int tazListSize = tazList.size();
		for(int i = 0; i<tazListSize; i++)
		{
			//Get a Taz from the Taz List
			Element element = tazList.get(i);
			odListInXMLFile.add(element.attribute("id").getValue());        //获取当前TAZ的名称
			
			List<Element> tazSourceList = element.selectNodes("tazSource"); //在当前TAZ下的源点
			if(!tazSourceList.isEmpty())
			{
				String origin = tazSourceList.get(0).attribute("id").getValue();
				originList.add(origin);             //添加该源点到源点列表
			}
			else
			{
				String origin = "#org"+i;
				originList.add(origin);             //添加该源点到源点列表
			}
			
			List<Element> tazSinkList = element.selectNodes("tazSink");     //在当前TAZ下的目标点
			if(!tazSinkList.isEmpty())
			{
				String destination = tazSinkList.get(0).attribute("id").getValue();
				destinationList.add(destination);  //添加该目标点到目标点列表
			}
			else
			{
				String destination = "#des"+i;
				destinationList.add(destination);  //添加该目标点到目标点列表
			}
		}
	}
	
	/**
	 * 功能：用JAVA读取TXT文件的内容
	 * 步骤：
	 * 		1：先获得文件句柄 
	 * 		2：获得文件句柄当做是输入一个字节码流，需要对这个输入流进行读取
	 * 		3：读取到输入流后，需要读取生成字节流 
	 * 		4：一行一行的输出。readline()。
	 * 备注：需要考虑的是异常情况
	 * 
	 * @param filePath
	 * 
	 */
	public void readTXTFile(String filePath)
	{
		List<String> originListTemp = new ArrayList<String>();
		try
		{
			String encoding = "UTF-8";
			File file = new File(filePath);
			if (file.isFile() && file.exists())//判断文件是否存在
			{ 
				InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				
				String lineTxt = null;
				boolean columnName = false;
				boolean rowName = false;
				String rowTazName = "";
				
				while((lineTxt = bufferedReader.readLine()) != null)
				{
					if(lineTxt.contains("* names:"))//找到要提取列名的位置，其下一行将是存储列名的行
					{
						columnName = true;
						continue;
					}
					if(lineTxt.contains("* District ")&&lineTxt.contains("Sum ="))//找到要提取行名的位置，其下一行将是存储demand的行
					{
						String[] tazAnalysis = lineTxt.split(" "); //分割读取的整行信息
						String tazName = tazAnalysis[2];           //从分割得到的信息中获得源点所在TAZ名
						//System.out.println(tazName);
						//odListInTxtFile.add(tazName);
						rowTazName = tazName;                      //获得源点所在TAZ名
						
						rowName = true;
						continue;
					}
					
					if(columnName == true)//列名存储与分析
					{
						//System.out.println(lineTxt);
						String[] tazAnalysis = lineTxt.split("\\s+"); //分割读取的整行信息获取目标点所在TAZ名
						for(int i=0; i<tazAnalysis.length; i++)
						{
							if(!tazAnalysis[i].isEmpty())
							{
								//System.out.println(i+ " " +tazAnalysis[i]);
								odListInTxtFile.add(tazAnalysis[i]);  //将目标点所在TAZ的名字存入ODListInTxtFile
							}
						}
						
						originListTemp.addAll(odListInTxtFile);
						odDemandMatrix = new DefaultDenseObjectMatrix2D(odListInTxtFile.size(), 
																		odListInTxtFile.size()); //创建矩阵存储Demand
						columnName = false;
					}
					
					if(rowName == true)//demand存储与分析
					{
//						System.out.println(lineTxt);
						int indexRow = originListTemp.indexOf(rowTazName);
						
						String[] tazAnalysis = lineTxt.split("\\s+"); //获取Demand值
						int indexColumn = 0;
						for(int i=0; i<tazAnalysis.length; i++)
						{
							if(!tazAnalysis[i].isEmpty())
							{
//								System.out.println(i+ " " +tazAnalysis[i]);
//								System.out.println("indexRow " +indexRow);
//								System.out.println("indexColumn " +indexColumn);
								odDemandMatrix.setAsInt(Integer.parseInt(tazAnalysis[i]), 
																  indexRow, indexColumn);//存储Demand值
								indexColumn++;
							}
						}
						rowName = false;
					}	
				}
				read.close();
			}
			else
			{
				System.out.println("找不到指定的文件");
			}
		} catch (Exception e)
		{
			System.out.println("读取文件内容出错");
			e.printStackTrace();
		}
		System.out.println("odDemandMatrix:\n"+odListInTxtFile.size()+"\n"+odDemandMatrix);
	}
	
	/****************************************************************
	 *******************Getter and Setter****************************
	 ****************************************************************/
	/**
	 * @return originList
	 */
	public LinkedHashSet<String> getOriginList()
	{
		return originList;
	}

	/**
	 * @return destinationList
	 */
	public LinkedHashSet<String> getDestinationList()
	{
		return destinationList;
	}

	/**
	 * @return odListInTxtFile
	 */
	public LinkedHashSet<String> getOdListInTxtFile()
	{
		return odListInTxtFile;
	}

	/**
	 * @return odDemandMatrix
	 */
	public Matrix getOdDemandMatrix()
	{
		return odDemandMatrix;
	}

	/**
	 * @return odListInXMLFile
	 */
	public LinkedHashSet<String> getOdListInXMLFile()
	{
		return odListInXMLFile;
	}
}
