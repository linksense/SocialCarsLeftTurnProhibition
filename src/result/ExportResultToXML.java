/* --------------------------------------------------------------------
 * ExportResultToXML.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2017, by Huijun Liu and all Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 07.06.2017
 * 
 * Function:
 *           1.导出结果XML文件
 *           
 */
package result;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class ExportResultToXML
{
	// ~Variables
	// --------------------------------------------------------------------------
	private Document document           = null; // 要导出的文件
	private Element  root               = null; // 文件的根节点
	
	private Element  prohibitedLeftTurn = null; // prohibitedLeftTurn 节点
	private Element  leftTurn           = null; // prohibitedLeftTurn 的子节点 leftTurn
	
	private Element  sueResult          = null; // sueResult
	private Element  flowTravelTime     = null; // sueResult 的子节点 flowTravelTime
	
	private Element  signalGroup          = null; // signalGroup
	private Element  cycleLength          = null; 
	private Element  startOfGreenMovement = null;
	private Element  durationMovement     = null;
	private Element  permittedLanes       = null;
	private Element  subPermittedLanes    = null;
	
	// ~Methods
	// --------------------------------------------------------------------------
	/**
	 * 构造函数
	 */
	public ExportResultToXML()
	{
		document = DocumentHelper.createDocument(); 		     //创建文档的根节点
		root     = DocumentHelper.createElement("bestResult"); //创建文档的 根元素节点
		document.setRootElement(root);                           //设置文档根节点
		
		// 给根节点添加属性
		// ----------------------------------------------------------------
		Date dateNow = new Date();                                         //日期时间
		SimpleDateFormat ft = new SimpleDateFormat("dd/MM/yyyy kk:mm:ss"); //日期时间格式
		root.addAttribute("time", ft.format(dateNow));                     //设置创建文件的日期
	}
	
	public void addProhibitedLeftTurn()
	{
		//给根节点添加子节点prohibitedLeftTurn
		//----------------------------------------------------------------
		root.addComment("ProhibitedLeftTurn");            
		prohibitedLeftTurn = root.addElement("prohibitedLeftTurn"); //添加路口Junction节点
	}
	
	public void addLeftTurn(final String id, final String name)
	{
		leftTurn = prohibitedLeftTurn.addElement("leftTurn");
		
		leftTurn.addAttribute("id", id);
		leftTurn.addAttribute("name", name);
		// leftTurn.addAttribute("junction", junction);
	}
	
	public void addSUEResult(final String totalTravelTime)
	{
		//给根节点添加子节点prohibitedLeftTurn
		//----------------------------------------------------------------
		root.addComment("SUEResult");            
		sueResult = root.addElement("sueResult"); //添加sueResult节点
		sueResult.addAttribute("totalTravelTime", totalTravelTime);
	}
	
	public void addFlowTravelTime(final String id, final String linkFlow, final String linkTravelTime)
	{
		flowTravelTime = sueResult.addElement("flowTravelTime");
		
		flowTravelTime.addAttribute("id", id);
		flowTravelTime.addAttribute("linkFlow", linkFlow);
		flowTravelTime.addAttribute("linkTravelTime", linkTravelTime);
	}
	
	public void addSignalGroup(final String junction)
	{
		root.addComment("SignalGroup");
		signalGroup = root.addElement("signalGroup");
		signalGroup.addAttribute("junction", junction);
	}
	
	public void addCycleLength(final String value)
	{
		cycleLength = signalGroup.addElement("cycleLength");
		
		cycleLength.addAttribute("value", value);
	}
	
	public void addStartOfGreenMovement(final String armID, final String dirID, final String value)
	{
		startOfGreenMovement = signalGroup.addElement("startOfGreenMovement");
		
		startOfGreenMovement.addAttribute("armID", armID);
		startOfGreenMovement.addAttribute("dirID", dirID);
		startOfGreenMovement.addAttribute("value", value);
	}
	
	public void addDurationMovement(final String armID, final String dirID, final String value)
	{
		durationMovement = signalGroup.addElement("durationMovement");
		
		durationMovement.addAttribute("armID", armID);
		durationMovement.addAttribute("dirID", dirID);
		durationMovement.addAttribute("value", value);
	}
	
	public void addPermittedLanes(final String armID)
	{
		permittedLanes = signalGroup.addElement("permittedLanes");
		
		permittedLanes.addAttribute("armID", armID);
	}
	
	public void addSubPermittedLanes(final String dirID, final String laneID, final String value)
	{
		subPermittedLanes = permittedLanes.addElement("subPermittedLanes");
		
		subPermittedLanes.addAttribute("dirID", dirID);
		subPermittedLanes.addAttribute("laneID", laneID);
		subPermittedLanes.addAttribute("value", value);
	}
	
	/**
	 * @param fileName
	 * @throws IOException
	 */
	public void createXMLFile(final String fileName) throws IOException
	{
		// 把生成的XML文档存放在硬盘上 true代表是否换行
		// ----------------------------------------------------
		OutputFormat format = new OutputFormat("    ", true);
		format.setEncoding("UTF-8");		 // 设置编码格式
		
		FileOutputStream outputStream = new FileOutputStream(fileName);
		XMLWriter xmlWriter = new XMLWriter(outputStream, format);
		xmlWriter.write(document);
		xmlWriter.close();
	}
	
	/****************************************************************
	 *******************Getter and Setter****************************
	 ****************************************************************/
	/**
	 * @return leftTurn
	 */
	public Element getLeftTurn()
	{
		return leftTurn;
	}

	/**
	 * @param leftTurn 要设置的 leftTurn
	 */
	public void setLeftTurn(Element leftTurn)
	{
		this.leftTurn = leftTurn;
	}

	/**
	 * @return flowTravelTime
	 */
	public Element getFlowTravelTime()
	{
		return flowTravelTime;
	}

	/**
	 * @param flowTravelTime 要设置的 flowTravelTime
	 */
	public void setFlowTravelTime(Element flowTravelTime)
	{
		this.flowTravelTime = flowTravelTime;
	}

	/**
	 * @return signalGroup
	 */
	public Element getSignalGroup()
	{
		return signalGroup;
	}

	/**
	 * @param signalGroup 要设置的 signalGroup
	 */
	public void setSignalGroup(Element signalGroup)
	{
		this.signalGroup = signalGroup;
	}

	/**
	 * @return cycleLength
	 */
	public Element getCycleLength()
	{
		return cycleLength;
	}

	/**
	 * @param cycleLength 要设置的 cycleLength
	 */
	public void setCycleLength(Element cycleLength)
	{
		this.cycleLength = cycleLength;
	}

	/**
	 * @return startOfGreenMovement
	 */
	public Element getStartOfGreenMovement()
	{
		return startOfGreenMovement;
	}

	/**
	 * @param startOfGreenMovement 要设置的 startOfGreenMovement
	 */
	public void setStartOfGreenMovement(Element startOfGreenMovement)
	{
		this.startOfGreenMovement = startOfGreenMovement;
	}

	/**
	 * @return durationMovement
	 */
	public Element getDurationMovement()
	{
		return durationMovement;
	}

	/**
	 * @param durationMovement 要设置的 durationMovement
	 */
	public void setDurationMovement(Element durationMovement)
	{
		this.durationMovement = durationMovement;
	}

	/**
	 * @return permittedLanes
	 */
	public Element getPermittedLanes()
	{
		return permittedLanes;
	}

	/**
	 * @param permittedLanes 要设置的 permittedLanes
	 */
	public void setPermittedLanes(Element permittedLanes)
	{
		this.permittedLanes = permittedLanes;
	}

	/**
	 * @return subPermittedLanes
	 */
	public Element getSubPermittedLanes()
	{
		return subPermittedLanes;
	}

	/**
	 * @param subPermittedLanes 要设置的 subPermittedLanes
	 */
	public void setSubPermittedLanes(Element subPermittedLanes)
	{
		this.subPermittedLanes = subPermittedLanes;
	}
}
