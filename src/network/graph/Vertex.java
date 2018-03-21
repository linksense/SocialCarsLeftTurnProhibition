package network.graph;

import com.trolltech.qt.core.QRectF;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QGraphicsItem;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QPainterPath;
import com.trolltech.qt.gui.QPen;
import com.trolltech.qt.gui.QStyleOptionGraphicsItem;
import com.trolltech.qt.gui.QWidget;

public class Vertex extends QGraphicsItem implements Cloneable
{
	//~Variables
	//--------------------------------------------------------------------------	
    private String name = null;			//The name associated with this Vertex 
    private String type = null;			//The type associated with this Vertex
    private double xCoordinate = 0.0f;	//The x Axis Coordinate this Vertex
    private double yCoordinate = 0.0f;	//The y Axis Coordinate this Vertex
    private double speed = 0.0f;        //The speed of in or out Link's
    private int laneNum = 0;            //The lane nummber of in or out Link's
    
	private static final QPen QPEN_BLACK = new QPen(QColor.black, 0.01);
    private static QPainterPath NODE_SHAPE;
    static{
	      		NODE_SHAPE = new QPainterPath();
	      		NODE_SHAPE.addEllipse(-1, -1, 2, 2);
    	  }
    //~Methods
  	//--------------------------------------------------------------------------
	/**
	 * Construct a new Vertex object.
	 * @param myName
	 *          Set the name of this vertex.
	 */
    public Vertex(final String myName)
    {
    	this.setName(myName);
		this.setToolTip(name);
    }

	/**
	 * Construct a new Vertex object.
	 * @param the_data
	 *          Set the data of this vertex.
	 * @param the_name
	 *          Set the name of this vertex.
	 */
    public Vertex(final String myName, final String myType)
    {
    	this.setName(myName);
    	this.setType(myType);
		this.setToolTip(name);
    }
    
    /**
	 * Construct a new Vertex object.
	 * @param myName
	 *          Set the name of this vertex.
	 * @param x_coordinate
	 *          Set the x Axis Coordinate of this vertex.
	 * @param y_coordinate
	 *          Set the x Axis Coordinate of this vertex.
	 */
    public Vertex(final String myName, final double x_coordinate,final double y_coordinate)
    {
    	this.setName(myName);
    	this.setX_Coordinate(x_coordinate);
    	this.setY_Coordinate(y_coordinate);
		this.setToolTip(name+" ["+x_coordinate+", "+y_coordinate+"]");
    }
    
    /**
	 * Construct a new Vertex object.
	 * @param the_data
	 *          Set the data of this vertex.
	 * @param the_name
	 *          Set the name of this vertex.
	 *@param x_coordinate
	 *          Set the x Axis Coordinate of this vertex.
	 *@param y_coordinate
	 *          Set the x Axis Coordinate of this vertex.
	 */
    public Vertex(final String myName, final String myType,  final double x_coordinate,final double y_coordinate)
    {
    	this.setName(myName);
    	this.setType(myType);
    	this.setX_Coordinate(x_coordinate);
    	this.setY_Coordinate(y_coordinate);
		this.setToolTip(name+" ["+x_coordinate+", "+y_coordinate+"]");
    }
	
	 /* (non-Javadoc)
     * @see com.trolltech.qt.gui.QGraphicsItem#boundingRect()
     */
    @Override
	public QRectF boundingRect()
	{
		//TODO Auto-generated method stub
    	double adjust = 2;
    	QRectF boundingRect = new QRectF(-10-adjust, -10-adjust, 23+adjust, 23+adjust);
		return boundingRect;
	}
    
    /* (non-Javadoc)
     * @see com.trolltech.qt.gui.QGraphicsItem#shape()
     */
    @Override
    public QPainterPath shape() 
    {
        return NODE_SHAPE;
    }

	/* (non-Javadoc)
	 * @see com.trolltech.qt.gui.QGraphicsItem#paint(com.trolltech.qt.gui.QPainter, 
	 * 												 com.trolltech.qt.gui.QStyleOptionGraphicsItem, 
	 * 												 com.trolltech.qt.gui.QWidget)
	 */
	@Override
	public void paint(QPainter painter, QStyleOptionGraphicsItem option, QWidget widget)
	{
		//TODO Auto-generated method stub
		painter.setRenderHint(QPainter.RenderHint.Antialiasing, true); //Anti aliasing
		painter.setPen(Qt.PenStyle.NoPen);
        painter.setBrush(QColor.fromRgba(QColor.yellow.rgb() & 0x7fffffff));
        painter.drawEllipse(-1, -1, 2, 2);

        painter.setPen(QPEN_BLACK);
        painter.drawEllipse(-1, -1, 2, 2);
	}
    
	/****************************************************************
	 *******************Getter and Setter****************************
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
	 * @return type
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @param type 
	 *		要设置的 type
	 */
	public void setType(String type)
	{
		this.type = type;
	}

	/**
	 * @return xCoordinate
	 */
	public double getX_Coordinate()
	{
		return xCoordinate;
	}

	/**
	 * @param xCoordinate 
	 *		要设置的 xCoordinate
	 */
	public void setX_Coordinate(double xCoordinate)
	{
		this.xCoordinate = xCoordinate;
	}

	/**
	 * @return yCoordinate
	 */
	public double getY_Coordinate()
	{
		return yCoordinate;
	}

	/**
	 * @param yCoordinate 
	 *		要设置的 yCoordinate
	 */
	public void setY_Coordinate(double yCoordinate)
	{
		this.yCoordinate = yCoordinate;
	}
	
	/**
	 * @return the speed
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
	 * @return laneNum
	 */
	public int getLaneNum()
	{
		return laneNum;
	}

	/**
	 * @param laneNum 
	 *		要设置的 laneNum
	 */
	public void setLaneNum(int laneNum)
	{
		this.laneNum = laneNum;
	}
	
	/* （非 Javadoc）
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone()
	{  
		Vertex cloneVertex = null;
		try
		{
			cloneVertex = (Vertex)super.clone();
		} 
		catch (CloneNotSupportedException e)
		{
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		return cloneVertex;  
	}  
	/***************************************************************
	 *******************hashCode and equals*************************
	 ***************************************************************/
	/* （非 Javadoc）
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		long temp;
		temp = Double.doubleToLongBits(xCoordinate);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(yCoordinate);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/* （非 Javadoc）
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
		{
			return true;
		}
		if(obj == null)
		{
			return false;
		}
		if(!(obj instanceof Vertex))
		{
			return false;
		}
		Vertex other = (Vertex) obj;
		if(name == null)
		{
			if(other.name != null)
			{
				return false;
			}
		}
		else if(!name.equals(other.name))
		{
			return false;
		}
		if(type == null)
		{
			if(other.type != null)
			{
				return false;
			}
		}
		else if(!type.equals(other.type))
		{
			return false;
		}
		if(Double.doubleToLongBits(xCoordinate) != Double.doubleToLongBits(other.xCoordinate))
		{
			return false;
		}
		if(Double.doubleToLongBits(yCoordinate) != Double.doubleToLongBits(other.yCoordinate))
		{
			return false;
		}
		return true;
	}

	public String toString()
    {
    	StringBuilder sb = new StringBuilder("Vertex");
	    sb.append(": ");
	    sb.append(name);
	    sb.append(" (");
	    sb.append(type);
	    sb.append(")");
	    sb.append(" [");
	    sb.append(xCoordinate);
	    sb.append(" , ");
	    sb.append(yCoordinate);
	    sb.append("] ");
	    sb.append(laneNum);
	    return sb.toString();
	}
}
