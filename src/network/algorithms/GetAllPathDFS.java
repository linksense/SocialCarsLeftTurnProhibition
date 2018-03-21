/* --------------------------------------------------------------------
 * GetAllPathDFS.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and all Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 17.04.2016
 * 
 * Function:
 *           1.使用深度优先搜索获取图中两点之间的所有无环路径
 *           2.递归的深度优先搜索寻找两点之间的所有路径
 *           3.回溯的深度优先搜索寻找两点之间的所有路径
 *           
 */
package network.algorithms;

import network.Network;
import network.graph.Graph;
import network.graph.Vertex;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import com.trolltech.qt.QSignalEmitter;

public class GetAllPathDFS extends QSignalEmitter
{
	int index = 0;
	/**
	 * 递归的深度优先搜索寻找两点之间的所有路径
	 * @param network
	 * @param visited
	 * @param paths
	 * @param currentNode
	 * @param endNode
	 */
	public void DepthFirstRecursive(Network network, List<Vertex> visited,
									        List<ArrayList<Vertex>> paths,
											Vertex currentNode, Vertex endNode)
	{
		this.index++;
		//System.out.println("路径搜索迭代次数：" + this.index);
		visited.add(currentNode);
		if (currentNode.equals(endNode))
		{
			ArrayList<Vertex> path = new ArrayList<Vertex>(visited);
			paths.add(path);
			return;
		}
		else
		{
			LinkedHashSet<Vertex> nodes = network.adjacentNodes(currentNode);
			//nodes.removeAll(visited);
			for (Vertex node : nodes)
			{
				if (visited.contains(node))
				{
					continue;
				}
				List<Vertex> temp = new ArrayList<Vertex>();
				temp.addAll(visited);
				DepthFirstRecursive(network, temp, paths, node, endNode);
			}
		}
		//System.gc();
	}
	
	/**
	 *  递归的深度优先搜索寻找两点之间的所有路径
	 * @param network.graph
	 * @param visited
	 * @param paths
	 * @param currentNode
	 * @param endNode
	 */
	public void DepthFirstRecursive(Graph graph, List<String> visited, 
												 List<ArrayList<String>> paths,
												 String currentNode, String endNode)
	{
		this.index++;
		System.out.println("路径搜索迭代次数：" + this.index);
		visited.add(currentNode);
		if (currentNode.equals(endNode))
		{
			ArrayList<String> path = new ArrayList<String>(visited);
			paths.add(path);
			return;
		}
		else
		{
			LinkedList<String> nodes = graph.adjacentNodes(currentNode);
			for (String node : nodes)
			{
				if (visited.contains(node))
				{
					continue;
				}
				List<String> temp = new ArrayList<String>();
				temp.addAll(visited);
				DepthFirstRecursive(graph, temp, paths, node, endNode);
			}
		}
	}
	
	/**
	 * 回溯的深度优先搜索寻找两点之间的所有路径
	 * @param network
	 * @param paths
	 * @param start
	 * @param endNode
	 */
	public void DepthFirstBacktrack(Network network, List<ArrayList<Vertex>> paths, 
													      Vertex start, Vertex endNode)
	{
		Stack<Pair<Vertex, Vector<Vertex>>> stack = new Stack<Pair<Vertex, Vector<Vertex>>>();

		Vector<Vertex> startPath = new Vector<Vertex>();
		startPath.addElement(start);
		Pair<Vertex, Vector<Vertex>> pair = null;
		pair = new Pair<Vertex, Vector<Vertex>>(start, startPath);

		stack.push(pair);

		while (!stack.isEmpty())
		{
			this.index++;
			System.out.println("路径搜索迭代次数：" + this.index);
			
			Pair<Vertex, Vector<Vertex>> current = stack.pop();
			Vertex vertex = current.getKey();
			Vector<Vertex> path = current.getValue();

			LinkedHashSet<Vertex> neighbours = network.adjacentNodes(vertex);
			neighbours.removeAll(new LinkedList<Vertex>(path));
			for (Vertex next : neighbours)
			{
				if (next.equals(endNode))
				{
					Vector<Vertex> tempPath = new Vector<Vertex>();
					tempPath.addAll(path);
					tempPath.add(next);
					ArrayList<Vertex> onePath = new ArrayList<Vertex>(tempPath);
					paths.add(onePath);
				}
				else
				{
					Vector<Vertex> tempPath = new Vector<Vertex>();
					tempPath.addAll(path);
					tempPath.add(next);
					Pair<Vertex, Vector<Vertex>> tempPair = null;
					tempPair = new Pair<Vertex, Vector<Vertex>>(next, tempPath);

					stack.push(tempPair);
				}
			}
		}
	}

	/**
	 * 回溯的深度优先搜索寻找两点之间的所有路径
	 * @param network
	 * @param paths
	 * @param start
	 * @param endNode
	 */
	public void DepthFirstBacktrack(Graph graph,List<LinkedHashSet<String>> paths, 
													 String start, String endNode)
	{
		Stack<Pair<String, Vector<String>>> stack = new Stack<Pair<String, Vector<String>>>();
	
		Vector<String> startPath = new Vector<String>();
		startPath.addElement(start);
		Pair<String, Vector<String>> pair = null;
		pair = new Pair<String, Vector<String>>(start, startPath);
	
		stack.push(pair);
	
		while (!stack.isEmpty())
		{
			Pair<String, Vector<String>> current = stack.pop();
			String vertex = current.getKey();
			Vector<String> path = current.getValue();
	
			LinkedList<String> neighbours = graph.adjacentNodes(vertex);
			neighbours.removeAll(new LinkedList<String>(path));
			for (String next : neighbours)
			{
				if (next.equals(endNode))
				{
					Vector<String> tempPath = new Vector<String>();
					tempPath.addAll(path);
					tempPath.add(next);
					LinkedHashSet<String> onePath = new LinkedHashSet<String>(tempPath);
					paths.add(onePath);
				}
				else
				{
					Vector<String> tempPath = new Vector<String>();
					tempPath.addAll(path);
					tempPath.add(next);
					Pair<String, Vector<String>> tempPair = null;
					tempPair = new Pair<String, Vector<String>>(next, tempPath);
	
					stack.push(tempPair);
				}
			}
		}
	}
}