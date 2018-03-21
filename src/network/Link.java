/* --------------------------------------------------------------------
 * Link.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and all Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 18.04.2016
 * 
 * Function:
 *           1.Link类
 *           2.扩展自Edge视图类
 *           
 */
package network;

import network.graph.Edge;
import network.graph.Vertex;

import com.trolltech.qt.core.QRectF;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QPen;
import com.trolltech.qt.gui.QStyleOptionGraphicsItem;
import com.trolltech.qt.gui.QWidget;

public class Link extends Edge implements Cloneable
{
	// ~Variables
	// --------------------------------------------------------------------------
	private int saturationFlow = 800;      //饱和流量
	private double freeFlowTravelTime = 0;  //Free Flow Travel Time
	private double travelTime = 0;
	private double degreeOfSaturation = 0;
	
	private int laneCount = 0;              //车道数
	private String linkType = "Internal";   //道路类型
	private double speed  = 0.0f;           //道路速度，单位 m/s
	private double length = 0.0f;           //道路长度，单位 m
	private String direction = "s";         //转向信息
	
	// ~Methods
	// --------------------------------------------------------------------------
	/**
	 * 构造函数
	 * @param begin
	 * @param end
	 */
	public Link(final Vertex begin, final Vertex end)
	{
		super(begin, end);
	}

	/**
	 * 构造函数
	 * @param myName
	 * @param begin
	 * @param end
	 */
	public Link(final String myName, final Vertex begin, final Vertex end)
	{
		super(myName, begin, end);
	}
	
	@Override
	public QRectF boundingRect()
	{
		// TODO Auto-generated method stub
		return super.boundingRect();
	}

	@Override
	public void paint(QPainter painter, QStyleOptionGraphicsItem option, QWidget widget)
	{
		// TODO Auto-generated method stub
		if(linkType.equals("Internal"))
		{
			QColor qColor = null;
			QPen penEdge = null;
			if(direction.equals("l")|| direction.equals("L"))
			{
				qColor = QColor.red;
				penEdge = new QPen(qColor, PENWIDTH, 
									Qt.PenStyle.SolidLine, 
									Qt.PenCapStyle.RoundCap, 
									Qt.PenJoinStyle.RoundJoin);
			}
			else if(direction.equals("r")|| direction.equals("R"))
			{
				qColor = QColor.darkGreen;
				penEdge = new QPen(qColor, PENWIDTH, 
									Qt.PenStyle.SolidLine, 
									Qt.PenCapStyle.RoundCap, 
									Qt.PenJoinStyle.RoundJoin);
			}
			else if(direction.equals("t"))
			{
				qColor = QColor.darkRed;
				penEdge = new QPen(qColor, PENWIDTH, 
									Qt.PenStyle.SolidLine, 
									Qt.PenCapStyle.RoundCap, 
									Qt.PenJoinStyle.RoundJoin);
			}
			else
			{
				qColor = QColor.blue;
				penEdge = new QPen(qColor, PENWIDTH, 
									Qt.PenStyle.SolidLine, 
									Qt.PenCapStyle.RoundCap, 
									Qt.PenJoinStyle.RoundJoin);
			}
			super.setqColor(qColor);
			super.setqPenEdge(penEdge);
		}
		super.paint(painter, option, widget);
		
	}

	/* （非 Javadoc）
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone()
	{  
		Link cloneLink = null;
		cloneLink = (Link)super.clone();
		return cloneLink;  
	} 
	/****************************************************************
	 *******************Getter and Setter****************************
	 ****************************************************************/
	/**
	 * @return saturationFlow
	 */
	public int getSaturationFlow()
	{
		return saturationFlow;
	}

	/**
	 * @param saturationFlow 
	 *		要设置的 saturationFlow
	 */
	public void setSaturationFlow(int saturationFlow)
	{
		this.saturationFlow = saturationFlow;
	}

	/**
	 * @return freeFlowTravelTime
	 */
	public double getFreeFlowTravelTime()
	{
		freeFlowTravelTime = length/speed;
		//System.out.println("Link.java length "+length+",  speed "+speed);
		return freeFlowTravelTime;
	}

	/**
	 * @param freeFlowTravelTime 
	 *		要设置的 freeFlowTravelTime
	 */
	public void setFreeFlowTravelTime(double freeFlowTravelTime)
	{
		this.freeFlowTravelTime = freeFlowTravelTime;
	}

	/**
	 * @return travelTime
	 */
	public double getTravelTime()
	{
		return travelTime;
	}

	/**
	 * @param travelTime 
	 *		要设置的 travelTime
	 */
	public void setTravelTime(double travelTime)
	{
		this.travelTime = travelTime;
	}
	
	/**
	 * @return degreeOfSaturation
	 */
	public double getdegreeOfSaturation()
	{
		return degreeOfSaturation;
	}

	/**
	 * @param travelTime 
	 *		要设置的 travelTime
	 */
	public void setDegreeOfSaturation(double degreeOfSaturation)
	{
		this.degreeOfSaturation = degreeOfSaturation;
	}

	/**
	 * @return laneCount
	 */
	public int getLaneCount()
	{
		return laneCount;
	}

	/**
	 * @param laneCount 
	 *		要设置的 laneCount
	 */
	public void setLaneCount(int laneCount)
	{
		this.laneCount = laneCount;
	}
	
	/**
	 * @return linkType
	 */
	public String getLinkType()
	{
		return linkType;
	}

	/**
	 * @param linkType 
	 *		要设置的 linkType
	 */
	public void setLinkType(String linkType)
	{
		this.linkType = linkType;
	}
	
	/**
	 * @return speed
	 */
	public double getSpeed()
	{
		return speed;
	}

	/**
	 * @param speed 
	 *		要设置的 speed
	 */
	public void setSpeed(double speed)
	{
		this.speed = speed;
	}

	/**
	 * @return length
	 */
	public double getLength()
	{
		return length;
	}

	/**
	 * @param length 
	 *		要设置的 length
	 */
	public void setLength(double length)
	{
		this.length = length;
	}

	/**
	 * @return direction
	 */
	public String getDirection()
	{
		return direction;
	}

	/**
	 * @param direction 
	 *		要设置的 direction
	 */
	public void setDirection(String direction)
	{
		this.direction = direction;
		this.setToolTip(super.getName() + " ["+super.getBegin().getName()
										+", "+super.getEnd().getName()+"] "+ direction);
	}
	
	/* （非 Javadoc）
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		return result;
	}
	
	/* （非 Javadoc）
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "Link{" + super.toString()
					   + "LaneCount= "   + laneCount 
					   + ", EdgeType= "  + linkType
					   + ", Direction= " + direction + "}\n";
	}
}
