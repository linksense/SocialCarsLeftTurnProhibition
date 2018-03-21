/* --------------------------------------------------------------------
 * GetAllPath.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and all Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 17.04.2016
 * 
 * Function:
 *           1.路径搜索获取图中两点之间的所有无环路径
 *           2.此类并不使用
 *           
 */
package network.algorithms;

import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.ArrayList;

import network.Link;
import network.graph.Vertex;

public class GetAllPath
{
	//~Variables
	//--------------------------------------------------------------------------
	private List<Link> edgeList = null;                    //All Edges in the List	
	private List<Vertex> backList = null;                  //Visited Vertex Storage	
	private Set<String> resultSet = new HashSet<String>(); //Result LinkPathMatrix	
	private Set<Link> cirList = new HashSet<Link>();       //Cycle Storage
	
	//~Methods
	//--------------------------------------------------------------------------
	public GetAllPath(List<Link> hybridGraphEdges)
	{
		this.edgeList = hybridGraphEdges;
		backList = new ArrayList<Vertex>();
	}
	
	/**
	 * OD_Vertex All LinkPathMatrix Traverse
	 * @param start
	 * 		Start Vertex
	 * @param destination
	 * 		Destined Vertex
	 */
	public void getAllPath(final Vertex start, final Vertex destination) 
	{
		backList.add(start);
		for(int i = 0; i < edgeList.size(); i++)
		{
			//Find path begin with "start"
			if(edgeList.get(i).getBegin().equals(start))
			{
				//If a edge end with "destination", it is a wanted path
				if(edgeList.get(i).getEnd().equals(destination))
				{ 
					resultSet.add(backList.toString().substring(0, backList.toString().lastIndexOf("]")) 
																		      + "," + destination + "]");
					continue;
				}
				
				//If a Vertex is not traversed yet
				if(!backList.contains(edgeList.get(i).getEnd()))
				{
					getAllPath(edgeList.get(i).getEnd(), destination);
				}
				else
				{
					//Has Cycle
					cirList.add(edgeList.get(i));
				}
			}
		}
		backList.remove(start);
	}

	/**
	 * @return edgeList
	 */
	public List<Link> getEdgeList()
	{
		return edgeList;
	}

	/**
	 * @param edgeList 
	 *		要设置的 edgeList
	 */
	public void setEdgeList(List<Link> edgeList)
	{
		this.edgeList = edgeList;
	}

	/**
	 * @return backList
	 */
	public List<Vertex> getBackList()
	{
		return backList;
	}

	/**
	 * @param backList 
	 *		要设置的 backList
	 */
	public void setBackList(List<Vertex> backList)
	{
		this.backList = backList;
	}

	/**
	 * @return resultSet
	 */
	public Set<String> getResultSet()
	{
		return resultSet;
	}

	/**
	 * @param resultSet 
	 *		要设置的 resultSet
	 */
	public void setResultSet(Set<String> resultSet)
	{
		this.resultSet = resultSet;
	}

	/**
	 * @return cirList
	 */
	public Set<Link> getCirList()
	{
		return cirList;
	}

	/**
	 * @param cirList 
	 *		要设置的 cirList
	 */
	public void setCirList(Set<Link> cirList)
	{
		this.cirList = cirList;
	}
}
