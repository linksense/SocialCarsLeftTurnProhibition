/* --------------------------------------------------------------------
 * OD.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and all Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 11.04.2016
 * 
 * Function:
 *           1.源目标点对类
 *           
 */
package network;

import network.graph.Vertex;

public class OD
{
	//~Variables
	//--------------------------------------------------------------------------
	private Vertex origin;		//源节点
	private Vertex destination;	//目标节点
	private double demand;		//需求量

	//~Methods
	//--------------------------------------------------------------------------
	/**
	 * 构造函数
	 * @param origin
	 * @param destination
	 * @param demand
	 */
	public OD(final Vertex origin, final Vertex destination, final double demand)
	{
		this.origin = origin;
		this.destination = destination;
		this.demand = demand;
	}

	/**
	 * 获取源节点
	 * @return origin
	 */
	public Vertex getOrigin()
	{
		return origin;
	}

	/**
	 * 获取目标节点
	 * @return destination
	 */
	public Vertex getDestination()
	{
		return destination;
	}

	/**
	 * 获取需求量
	 * @return demand
	 */
	public double getDemand()
	{
		return demand;
	}
	
	/* （非 Javadoc）
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(demand);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((destination == null) ? 0 : destination.hashCode());
		result = prime * result + ((origin == null) ? 0 : origin.hashCode());
		return result;
	}

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
		if (!(obj instanceof OD))
		{
			return false;
		}
		OD other = (OD) obj;
		if (Double.doubleToLongBits(demand) != Double.doubleToLongBits(other.demand))
		{
			return false;
		}
		if (destination == null)
		{
			if (other.destination != null)
			{
				return false;
			}
		}
		else if (!destination.equals(other.destination))
		{
			return false;
		}
		if (origin == null)
		{
			if (other.origin != null)
			{
				return false;
			}
		}
		else if (!origin.equals(other.origin))
		{
			return false;
		}
		return true;
	}

	/* （非 Javadoc）
	 * 输出OD为String
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "OD {Origin=" + origin 
				             + ", Destination=" 
				             + destination
				             + ", Demand=" 
				             + demand + "}";
	}
}
