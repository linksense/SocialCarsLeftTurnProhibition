/* --------------------------------------------------------------------
 * ODMatrix.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and all Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 12.04.2016
 * 
 * Function:
 *           1.OD Demand矩阵类
 *           
 */
package network;

import java.util.ArrayList;
import java.util.List;

public class ODMatrix
{
	//~Variables
	//--------------------------------------------------------------------------
	private List<OD> odMatrix;

	//~Methods
	//--------------------------------------------------------------------------
	/**
	 * 构造函数
	 */
	public ODMatrix()
	{
		odMatrix = new ArrayList<OD>();
	}

	/**
	 * 添加源与目标节点
	 * @param od
	 */
	public void addOD(OD od)
	{
		odMatrix.add(od);
	}

	/**
	 * 获取源目标节点列表
	 * @return odTable
	 */
	public List<OD> getODMatrix()
	{
		return odMatrix;
	}

	/**
	 * @param odMatrix 
	 *		要设置的 odMatrix
	 */
	public void setOdMatrix(List<OD> odMatrix)
	{
		this.odMatrix = odMatrix;
	}

	/* （非 Javadoc）
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("ODMatrix \n{\n");
		for(int i=0; i<odMatrix.size(); i++)
		{
			sb.append(">>>>>> " + odMatrix.get(i) + "\n");
		}
		sb.append( "}");
		return sb.toString();
	}
	
}
