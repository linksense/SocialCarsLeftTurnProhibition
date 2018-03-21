/* --------------------------------------------------------------------
 * GetAllPathBFS.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and all Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 18.04.2016
 * 
 * Function:
 *           1.使用广度优先搜索获取图中两点之间的所有无环路径
 *           
 */
package network.algorithms;

import java.util.List;
import java.util.Queue;
import java.util.Vector;
import java.util.LinkedList;
import java.util.LinkedHashSet;

import network.Network;
import network.graph.Vertex;

public class GetAllPathBFS
{
	/**
	 * 回溯的广度优先搜索寻找两点之间的所有路径
	 * @param network
	 * @param paths
	 * @param start
	 * @param endNode
	 */
	public static void BreathFirstBacktrack(Network network, List<LinkedHashSet<Vertex>> paths, 
															      Vertex start, Vertex endNode)
	{
		Queue<Pair<Vertex, Vector<Vertex>>> queue = new LinkedList<Pair<Vertex, Vector<Vertex>>>();

		Vector<Vertex> startPath = new Vector<Vertex>();
		startPath.addElement(start);
		Pair<Vertex, Vector<Vertex>> pair = null;
		pair = new Pair<Vertex, Vector<Vertex>>(start, startPath);

		queue.offer(pair);

		while (!queue.isEmpty())
		{
			Pair<Vertex, Vector<Vertex>> current = queue.poll();
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
					// System.out.print("path:" + temppath1 + "\n");
					LinkedHashSet<Vertex> onePath = new LinkedHashSet<Vertex>(tempPath);
					paths.add(onePath);
				}
				else
				{
					Vector<Vertex> tempPath = new Vector<Vertex>();
					tempPath.addAll(path);
					tempPath.add(next);
					Pair<Vertex, Vector<Vertex>> tempPair = null;
					tempPair = new Pair<Vertex, Vector<Vertex>>(next, tempPath);

					queue.offer(tempPair);
				}
			}
		}
	}
}