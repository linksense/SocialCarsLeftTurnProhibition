/* --------------------------------------------------------------------
 * Network.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and all Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 13.04.2016
 * 
 * Function:
 *           1.路网视图
 *           
 */
package network;

import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import network.graph.Graph;
import network.graph.Vertex;

import com.trolltech.qt.gui.QGraphicsScene;

public class Network extends QGraphicsScene implements Cloneable
{
	//~Variables
	//--------------------------------------------------------------------------
	private String networkName = null;                            //路网名称
	private List<Junction> junctions = new ArrayList<Junction>(); //所有的路口
	private List<Link> links         = new ArrayList<Link>();     //所有的边
	private List<Link> leftTurnLinks = new ArrayList<Link>();     //左转的内部边
//	private List<ArrayList<String>> tlLogicList = new ArrayList<ArrayList<String>>(); //存储交通灯节点的信息
//	private List<ArrayList<String>> foesList = new ArrayList<ArrayList<String>>(); //存储交通灯节点的信息
	
	//图,邻接表来展示内部节点的连接关系
	private Graph graph = new Graph();
	private Map<Vertex, LinkedHashSet<Vertex>> adjacencyMatrix = new HashMap<Vertex, LinkedHashSet<Vertex>>();

	public Signal0 linkRemoved = new Signal0();         //边移除信号
	public Signal0 linkLeftTurnRemoved = new Signal0(); //左转边移除信号
	//~Methods
	//--------------------------------------------------------------------------
	/**
	 * 添加路口到路网
	 * @param junction
	 */
	public void addJunction(final Junction junction)
	{
		if (junction == null)
		{
			throw new IllegalArgumentException("Junction can not be null!");
		}
		//Don't allow duplicate vertices
		else if (junctions.contains(junction))
		{
			throw new IllegalArgumentException("Duplicate Junction!");
		}
		else
		{
			junctions.add(junction);
			
			//向Scene添加内部节点
			List<Vertex> vertexsList = getInternalNodes(junction);
			for(int i=0; i<vertexsList.size(); i++)
			{
				Vertex v = vertexsList.get(i);
				v.setPos(v.getX_Coordinate(), v.getY_Coordinate()); //设置节点在Scene中的位置
				this.addItem(v);
			}
			junction.setPos(junction.getX_Coordinate(), junction.getY_Coordinate());
			this.addItem(junction);
		}	
	}
	
	/**
	 * 添加连接到路网
	 * @param link
	 */
	public void addLink(final Link link)
	{
		if(link == null)
		{
			throw new IllegalArgumentException("Link can not be null!");
		}
		else if(links.contains(link))
		{
			throw new IllegalArgumentException("Duplicate Link!");
		}
		else
		{
			//添加连接到连接列表中
			//----------------------------------------------
			links.add(link);
			//如果是左转边,则添加到左转边列表
			if(link.getDirection().equals("l") || link.getDirection().equals("L"))
			{
				leftTurnLinks.add(link);
			}
			
			//添加连接到邻接表中
			//----------------------------------------------
			LinkedHashSet<Vertex> adjacent = adjacencyMatrix.get(link.getBegin());
			if (adjacent == null)
			{
				adjacent = new LinkedHashSet<Vertex>();
				adjacencyMatrix.put(link.getBegin(), adjacent);
			}
			adjacent.add(link.getEnd());
			
			//添加连接到图中
			//----------------------------------------------
			graph.addEdge(link.getBegin().getName(), link.getEnd().getName());
			
			//添加到Scence
			//----------------------------------------------
			this.addItem(link);
		}
	}
	
	public void removeLink(final Link link)
	{
		//1. 从左转列表删除此边
		int indexLeftT = leftTurnLinks.indexOf(link);
		if(indexLeftT != -1)
		{
			leftTurnLinks.remove(indexLeftT);
			//发送移除左转连接信号
			//linkLeftTurnRemoved.emit();
		}
		
		//2. 从道路列表里删除此边
		int index = links.indexOf(link);
		if(index != -1)
		{
			links.remove(index);
			
			//3. 连接从邻接表中移除
			//----------------------------------------------
			LinkedHashSet<Vertex> adjacent = adjacencyMatrix.get(link.getBegin());
			if (adjacent != null)
			{
				if(adjacent.contains(link.getEnd()))
				{
					adjacent.remove(link.getEnd());
					adjacencyMatrix.replace(link.getBegin(), adjacent);
				}
			}
			
			//发送移除连接信号
			linkRemoved.emit();
		}
	}
	
	/**
	 * @param logicList
	 */
//	public void addTlLogic(ArrayList<String> logicList)
//	{
//		ArrayList<String> logic = new ArrayList<String>(logicList);
//		tlLogicList.add(logic);
//	}
//	
//	public void addFoes(ArrayList<String> juncFoesList)
//	{
//		ArrayList<String> foes = new ArrayList<String>(juncFoesList);
//		foesList.add(foes);
//	}
	
	/**
	 * 获取内部节点列表
	 * @return
	 */
	public List<Vertex> getInternalNodes()
	{
		List<Vertex> vertexList = new ArrayList<Vertex>();
		final Iterator<Junction> it = junctions.iterator();	
		while(it.hasNext())
		{
			Junction junction = it.next();
			vertexList.addAll(junction.getInternalNode());
		}
		return vertexList;
	}
	
	/**
	 * 获取内部节点列表
	 * @return
	 */
	private List<Vertex> getInternalNodes(Junction juncton)
	{
		List<Vertex> vertexList = new ArrayList<Vertex>();
		final Iterator<Vertex> it = juncton.getInternalNode().iterator();	
		while(it.hasNext())
		{
			Vertex vertex = it.next();
			vertexList.add(vertex);
		}
		return vertexList;
	}
	
	/**
	 * 检查两点之间的连通性
	 * @param from
	 * @param to
	 * @return
	 */
	public boolean isConnected(Vertex from, Vertex to)
	{
		Set<Vertex> adjacent = adjacencyMatrix.get(from);
		if (adjacent == null)
		{
			return false;
		}
		return adjacent.contains(to);
	}
	
	/**
	 * 获取一个点的邻居
	 * @param currentNode
	 * @return
	 */
	public LinkedHashSet<Vertex> adjacentNodes(Vertex currentNode)
	{
		LinkedHashSet<Vertex> adjacent = adjacencyMatrix.get(currentNode);
		if (adjacent == null)
		{
			return new LinkedHashSet<Vertex>();
		}
		return new LinkedHashSet<Vertex>(adjacent);
	}
	
	
	/* （非 Javadoc）
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone()
	{  
		Network cloneNet = null;
        try
		{
        	cloneNet = (Network)super.clone();
        	cloneNet.graph = (Graph) graph.clone();
        	for(int i=0; i<cloneNet.links.size(); i++)
        	{
        		cloneNet.links.set(i, (Link) links.get(i).clone());
        	}
        	for(int i=0; i<cloneNet.leftTurnLinks.size(); i++)
        	{
        		cloneNet.leftTurnLinks.set(i, (Link) leftTurnLinks.get(i).clone());
        	}
        	
        	
		} 
        catch (CloneNotSupportedException e)
		{
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		return cloneNet;  
    }  
	
	/****************************************************************
	 *******************Getter and Setter****************************
	 ****************************************************************/
	/**
	 * @return junctions
	 */
	public List<Junction> getJunctions()
	{
		return junctions;
	}
	
	/**
	 * @param junctions 
	 *		要设置的 junctions
	 */
	public void setJunctions(List<Junction> junctions)
	{
		this.junctions = junctions;
	}
	
	/**
	 * @return adjacencyMatrix
	 */
	public Map<Vertex, LinkedHashSet<Vertex>> getAdjacencyMatrix()
	{
		return adjacencyMatrix;
	}
	
	/**
	 * @param adjacencyMatrix 
	 *		要设置的 adjacencyMatrix
	 */
	public void setAdjacencyMatrix(Map<Vertex, LinkedHashSet<Vertex>> adjacencyMatrix)
	{
		this.adjacencyMatrix = adjacencyMatrix;
	}
	
	/**
	 * @return links
	 */
	public List<Link> getLinks()
	{
		return links;
	}
	
	/**
	 * @param links 
	 *		要设置的 links
	 */
	public void setLinks(ArrayList<Link> links)
	{
		this.links = links;
	}

	/**
	 * @return
	 */
	public Graph getGraph()
	{
		return graph;
	}

	/**
	 * @param links 
	 *		要设置的 links
	 */
	public void setLinks(List<Link> links)
	{
		this.links = links;
	}

	/**
	 * @param graph 
	 *		要设置的 graph
	 */
	public void setGraph(Graph graph)
	{
		this.graph = graph;
	}

	/**
	 * @return the leftTurnLinks
	 */
	public List<Link> getLeftTurnLinks()
	{
		return leftTurnLinks;
	}

	/**
	 * @param leftTurnLinks 
	 *		要设置的 leftTurnLinks
	 */
	public void setLeftTurnLinks(List<Link> leftTurnLinks)
	{
		this.leftTurnLinks = leftTurnLinks;
	}

	/**
	 * @return networkName
	 */
	public String getNetworkName()
	{
		return networkName;
	}

	/**
	 * @param networkName 
	 *		要设置的 networkName
	 */
	public void setNetworkName(String networkName)
	{
		this.networkName = networkName;
	}

	/**
	 * @return tlLogicList
	 */
//	public List<ArrayList<String>> getTlLogicList()
//	{
//		return tlLogicList;
//	}

	/**
	 * @return foesList
	 */
//	public List<ArrayList<String>> getFoesList()
//	{
//		return foesList;
//	}
}
