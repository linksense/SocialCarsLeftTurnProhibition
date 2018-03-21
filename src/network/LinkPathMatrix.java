/* --------------------------------------------------------------------
 * LinkPathMatrix.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and all Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 14.04.2016
 * 
 * Function:
 *           1.LinkPath矩阵
 *           
 */
package network;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import network.exportlocalnetwork.ExportLinkPathMatrix;
import network.graph.Vertex;

import org.ujmp.core.Matrix;

public class LinkPathMatrix 
{
	/**
	 * 生成LinkPath矩阵
	 * @param links
	 * @param paths
	 * @return 
	 */
	public void linkPathMatrix(List<Link> links, List<ArrayList<Vertex>> paths, Matrix linkPaths,
										List<ArrayList<Double>> lengthTimes,ExportLinkPathMatrix export)
	{	
		//List<ArrayList<Link>> linkInPaths = new ArrayList<ArrayList<Link>>();
		//int count = 1;
		for(int i=0; i<paths.size();i++)
		{
			@SuppressWarnings("unused")
			LinkedHashSet<Vertex> path = new LinkedHashSet<Vertex>(paths.get(i));
			//ArrayList<Link> linkInPath = new ArrayList<Link>();
			ArrayList<Double> lengthTime = new ArrayList<Double>();
			
			double lengthSum = 0.0f;
			double travelTimeSum = 0.0f;
			//StringBuilder linkPathS = new StringBuilder("");
			for(int k=0;k<paths.get(i).size()-1;k++)
			{
				//Vertex linkStartNode = paths.get(i).get(0);
				for(int j=0; j<links.size(); j++)
				{
					Vertex linkStartNode = links.get(j).getBegin(); //连接的起点
					Vertex linkEndNode = links.get(j).getEnd();     //连接的终点
					//int sum=0;
					if(paths.get(i).get(k).equals(linkStartNode) && paths.get(i).get(k+1).equals(linkEndNode))
					{
						linkPaths.setAsInt(1, j, i);
						//linkPathS.append("1");         //该Link存在于某Path当中
						//linkInPath.add(links.get(j));
						lengthSum += links.get(j).getLength();
						travelTimeSum += links.get(j).getTravelTime();
						//sum=sum+1;
					}
					else
					{
						//linkPathS.append("0");        //该Link不存在某Path当中
					}
					//System.out.println(sum+" paths are on link "+j);
				}
			}
			
			path = null;
			//linkInPaths.add(linkInPath);
			lengthTime.add(lengthSum);
			lengthTime.add(travelTimeSum);
			lengthTimes.add(lengthTime);
			
//			double expUtility= Math.exp(-travelTimeSum);
//			//System.out.println("exp utility function: "+i+"\t"+expUtility);
//			if(expUtility != 0.0)
//			{
//				StringBuilder lengthTimeS = new StringBuilder("");
//				lengthTimeS.append(lengthSum);
//				lengthTimeS.append(" ");
//				lengthTimeS.append(travelTimeSum);
//				
//				//export.addLinkPathInfo(Integer.toString(count), linkPathS.toString(), lengthTimeS.toString());
//				//count++;
//			}
		}
		
//		System.out.println("link path matrix");
//		System.out.print(linkPaths);
		
		return ;
	}
}
