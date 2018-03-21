/* --------------------------------------------------------------------
 * GetShortestPath.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 
 *
 *
 */
package network.algorithms;

import java.util.ArrayList;
import java.util.List;

import network.Link;
import network.Network;
import network.graph.Vertex;

public class GetShortestPath
{
	private double[][] adjacencyMatrix;
	private Network network = null;
	
	private static double INF = Double.POSITIVE_INFINITY; // dist[i][j]=INF<==>顶点i和j之间没有边
	private double[][] dist; // 顶点i 到 j的最短路径长度，初值是i到j的边的权重
	private int[][] path;
	List<Integer> result = new ArrayList<Integer>();
	
	public GetShortestPath(Network network)
	{
		this.setNetwork(network);
		
		//System.out.println("初始化Travel Time：");
//		double[] initialTravelTime = new double[network.getLinks().size()]; //Initial Travel Time T0
//		for (int i=0;i<initialTravelTime.length;i++) 
//		{
//			double freeFlowTravelTime=network.getLinks().get(i).getFreeFlowTravelTime()/60;
//			double linkLength=network.getLinks().get(i).getLength()/1000;
//			initialTravelTime[i] = 0.2038*freeFlowTravelTime *Math.exp(0.7962*freeFlowTravelTime/linkLength) ;
//			System.out.println(i+" initial travel time: "+initialTravelTime[i]);
//
//			network.getLinks().get(i).setTravelTime(initialTravelTime[i]);
//		}
//		initialTravelTime = null;
		
		List<Vertex> nodes = network.getInternalNodes();
		this.path = new int[nodes.size()][nodes.size()];
		this.dist = new double[nodes.size()][nodes.size()];
		adjacencyMatrix = new double[nodes.size()][nodes.size()];
		
		creatAdjacencyMatrix();
		
		//floydWarshallAlgorithm(0, 43);
	}
	
	public void creatAdjacencyMatrix()
	{
		List<Vertex> nodes = network.getInternalNodes();
		List<Link> links = network.getLinks();
		
		for(int i=0; i<nodes.size(); i++)
		{
			Vertex nodeRow = nodes.get(i);
			for(int j=0; j<nodes.size(); j++)
			{
				Vertex nodeColumn = nodes.get(j);
				if(i == j)
				{
					adjacencyMatrix[i][j] = Double.POSITIVE_INFINITY;
				}
				else
				{
					// 1. 无通路
					adjacencyMatrix[i][j] = Double.POSITIVE_INFINITY;
					// 2. 有通路
					for(int k=0; k<links.size(); k++)
					{
						Link currLink = links.get(k);
						
						Vertex begin = currLink.getBegin();
						Vertex end   = currLink.getEnd();
						double weight = currLink.getTravelTime();
						
						if(begin.equals(nodeRow) && end.equals(nodeColumn))
						{
							adjacencyMatrix[i][j] = weight;
							break;
						}
					}
				}
			}
		}
	}
	
	/**
	 * @param paths
	 * @param startNode
	 * @param endNode
	 */
	public void floydWarshallAlgorithm(ArrayList<Vertex> shortesPaths, Vertex startNode, Vertex endNode)
	{
		List<Vertex> nodes = network.getInternalNodes();
		
		int beginID = nodes.indexOf(startNode);
		int endID = nodes.indexOf(endNode);
		
		floyd(adjacencyMatrix);
		result.add(beginID);
		findPath(beginID, endID);
		result.add(endID);
		
//		System.out.println(beginID + " to " + endID + ",the cheapest path is:");
//		System.out.println(result.toString());
//		System.out.println(dist[beginID][endID]);
		
		for(int i=0; i<result.size(); i++)
		{
			shortesPaths.add(nodes.get(result.get(i)));
		}
	}
	
	/**
	 * @param paths
	 * @param startNode
	 * @param endNode
	 */
	public void floydWarshallAlgorithm(int begin, int end)
	{
		floyd(adjacencyMatrix);
		result.add(begin);
		findPath(begin, end);
		result.add(end);
		
		System.out.println(begin + " to " + end + ",the cheapest path is:");
		System.out.println(result.toString());
		System.out.println(dist[begin][end]);
	}
	
	/**
	 * @param i
	 * @param j
	 */
	public void findPath(int i, int j)
	{
		int k = path[i][j];
		if (k == -1)
		{
			return;
		}
		findPath(i, k); // 递归
		result.add(k);
		findPath(k, j);
	}

	/**
	 * @param adjacencyMatrix
	 */
	public void floyd(double[][] adjacencyMatrix)
	{
		int size = network.getInternalNodes().size();
		// initialize dist and path
		for (int i = 0; i < size; i++)
		{
			for (int j = 0; j < size; j++)
			{
				path[i][j] = -1;
				dist[i][j] = adjacencyMatrix[i][j];
			}
		}
		for (int k = 0; k < size; k++)
		{
			for (int i = 0; i < size; i++)
			{
				for (int j = 0; j < size; j++)
				{
					if (dist[i][k] != INF && dist[k][j] != INF
							&& dist[i][k] + dist[k][j] < dist[i][j])
					{
						dist[i][j] = dist[i][k] + dist[k][j];
						path[i][j] = k;
					}
				}
			}
		}

	}

	/**
	 * @return adjacencyMatrix
	 */
	public double[][] getAdjacencyMatrix()
	{
		return adjacencyMatrix;
	}

	/**
	 * @param adjacencyMatrix 
	 *		要设置的 adjacencyMatrix
	 */
	public void setAdjacencyMatrix(double[][] adjacencyMatrix)
	{
		this.adjacencyMatrix = adjacencyMatrix;
	}

	/**
	 * @return network
	 */
	public Network getNetwork()
	{
		return network;
	}

	/**
	 * @param network 
	 *		要设置的 network
	 */
	public void setNetwork(Network network)
	{
		this.network = network;
	}
	
//	public static void main(String[] args)
//	{
//		Network network = new Network();
//		final String xmlNetwork = "localnetwork\\fiveIntersectionsFiveOD.localnet.xml";
//		ODMatrix odMatrix = new ODMatrix();
//		
//		ImportLocalNetImplement localNetwork = new ImportLocalNetImplement(xmlNetwork,network,odMatrix);
//		localNetwork.networkStorage();
//		localNetwork = null;
//		
//		GetShortestPath shortPath = new GetShortestPath(network);
//		
//	}
}
