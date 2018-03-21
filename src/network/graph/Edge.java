/* --------------------------------------------------------------------
 * Edge.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and all Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 18.04.2016
 * 
 * Function:
 *           1.Edge视图
 *           
 */
package network.graph;

import com.trolltech.qt.core.QPointF;
import com.trolltech.qt.core.QRectF;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QGraphicsItem;
import com.trolltech.qt.gui.QLineF;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QPen;
import com.trolltech.qt.gui.QPolygonF;
import com.trolltech.qt.gui.QStyleOptionGraphicsItem;
import com.trolltech.qt.gui.QWidget;

public class Edge extends QGraphicsItem implements Cloneable
{
	//~Variables
	//--------------------------------------------------------------------------
    private String name  = null;    //The name associated with this Edge       
	private Vertex begin = null;    //The begin vertex associated with this Edge	
	private Vertex end   = null;    //The end vertex associated with this Edge
	
	private QPointF sourcePoint = new QPointF();
    private QPointF destPoint   = new QPointF();
    
    protected static final double ARROWSIZE = 0.8;
    protected static final double PENWIDTH  = 0.6;
    protected static final double EXTRA = (PENWIDTH + ARROWSIZE)/2.0;
    
    private QColor qColor = QColor.black; 
    private QPen qPenEdge = new QPen(qColor, PENWIDTH, Qt.PenStyle.SolidLine, 
													   Qt.PenCapStyle.RoundCap, 
													   Qt.PenJoinStyle.RoundJoin);

    private QRectF boundingRect = new QRectF();
    QPointF destArrowP1 = new QPointF();
    QPointF destArrowP2 = new QPointF();
    QPolygonF pol = new QPolygonF();
	
    //~Methods
	//--------------------------------------------------------------------------
	/**
	 * Construct a new Edge object.
	 * @param 
	 * 		Set the of this Edge.
	 */
	public Edge(final Vertex begin, final Vertex end)
    {
    	this.setBegin(begin);
		this.setEnd(end);
		adjust();
    }
	
	/**
	 * Construct a new Edge object.
	 * @param the_name
	 * 		Set the name of this Edge.
	 */
    public Edge(final String the_name, final Vertex begin, final Vertex end)
    {
    	this.setName(the_name);
    	this.setBegin(begin);
		this.setEnd(end);
		adjust();
		
		this.setToolTip(this.getName() + 
				" ["+this.getBegin().getName()+","
						+ " "+this.getEnd().getName()+"]");
    }
	
	private void adjust()
    {
    	sourcePoint.setX(begin.getX_Coordinate());
		sourcePoint.setY(begin.getY_Coordinate());	
		destPoint.setX(end.getX_Coordinate());
		destPoint.setY(end.getY_Coordinate());

		boundingRect.setBottomLeft(sourcePoint);
        boundingRect.setTopRight(destPoint);
        boundingRect = boundingRect.normalized();
        boundingRect.adjust(-EXTRA, -EXTRA, EXTRA, EXTRA);	
    }
    
    @Override
    public QRectF boundingRect() 
    {
        return boundingRect;
    }

    @Override
    public void paint(QPainter painter, QStyleOptionGraphicsItem option, QWidget widget) 
    {
        if (begin == null || end == null)
            return;

        //Draw the line itself
        QLineF line = new QLineF(sourcePoint, destPoint);
        painter.setRenderHint(QPainter.RenderHint.Antialiasing, true); //Anti aliasing
        painter.setPen(qPenEdge);
        painter.drawLine(line);

        //Draw the arrows if there's enough room
        double angle;
        if (line.length() > 0)
            angle = Math.acos(line.dx() / line.length());
        else
            angle = 0;

        if (line.dy() >= 0)
            angle = (Math.PI * 2) - angle;

        destArrowP1.setX(destPoint.x() + Math.sin(angle - Math.PI / 3) * ARROWSIZE);
        destArrowP1.setY(destPoint.y() + Math.cos(angle - Math.PI / 3) * ARROWSIZE);
        destArrowP2.setX(destPoint.x() + Math.sin(angle - Math.PI + Math.PI / 3) * ARROWSIZE);
        destArrowP2.setY(destPoint.y() + Math.cos(angle - Math.PI + Math.PI / 3) * ARROWSIZE);
        
        pol.clear();
        pol.append(line.p2());
        pol.append(destArrowP1);
        pol.append(destArrowP2);

        painter.setBrush(qColor);
        painter.drawPolygon(pol);
    }
	
    /* （非 Javadoc）
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone()
	{  
		Edge cloneEdge = null;
        try
		{
        	cloneEdge = (Edge)super.clone();
		} 
        catch (CloneNotSupportedException e)
		{
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		return cloneEdge;  
    }  
    
	/****************************************************************
	 *******************Getter and Setter**************************
	 ****************************************************************/
	/**
	 * @return name
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * @param name 
	 *		要设置的 name
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	
	/**
	 * @return begin
	 */
	public Vertex getBegin()
	{
		return begin;
	}
	
	/**
	 * @param begin 
	 *		要设置的 begin
	 */
	public void setBegin(Vertex begin)
	{
		this.begin = begin;
	}
	
	/**
	 * @return end
	 */
	public Vertex getEnd()
	{
		return end;
	}
	
	/**
	 * @param end 
	 *		要设置的 end
	 */
	public void setEnd(Vertex end)
	{
		this.end = end;
	}
	
	public QColor getqColor()
	{
		return qColor;
	}

	public void setqColor(QColor qColor)
	{
		this.qColor = qColor;
	}	
	
	
	/**
	 * @return the qPenEdge
	 */
	public QPen getqPenEdge()
	{
		return qPenEdge;
	}

	/**
	 * @param qPenEdge the qPenEdge to set
	 */
	public void setqPenEdge(QPen qPenEdge)
	{
		this.qPenEdge = qPenEdge;
	}
	
	/***************************************************************
	 *******************hashCode and equals*************************
	 ***************************************************************/
	/* （非 Javadoc）
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (!(obj instanceof Edge))
		{
			return false;
		}
		Edge other = (Edge) obj;
		if (begin == null)
		{
			if (other.begin != null)
			{
				return false;
			}
		}
		else if (!begin.equals(other.begin))
		{
			return false;
		}
		if (end == null)
		{
			if (other.end != null)
			{
				return false;
			}
		}
		else if (!end.equals(other.end))
		{
			return false;
		}
		return true;
	}
    
	/* （非 Javadoc）
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((begin == null) ? 0 : begin.hashCode());
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		return result;
	}
	
    public String toString()
    {
    	StringBuilder sb = new StringBuilder("");
	    sb.append("Name=");
	    sb.append(name);
	    sb.append(" {From:<");
	    sb.append(begin);
	    sb.append("> , To:<");
	    sb.append(end);
	    sb.append(">} ");
	    return sb.toString();
	}
}
