/* --------------------------------------------------------------------
 * ExportResults.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2017, by Huijun Liu and all Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 14.06.2017
 * 
 * Function:
 *           1.导出结果
 *           
 */
package result;

import java.io.IOException;
import java.util.List;

import network.Link;

public class ExportResults
{
	//~Variables
	//--------------------------------------------------------------------------
	private ExportResultToXML resultsXML = null;
	
	//~Methods
	//--------------------------------------------------------------------------
	public ExportResults()
	{
		// 存储结果信息到本地XML当中
		// ------------------------------------------------
		resultsXML = new ExportResultToXML();
	}
	
	public void addSignalGroup(final String junction, final int cycleLength, 
							   final int [][] startOfGreenMovement,
							   final int [][] durationMovement, 
							   final int [][][] permittedLanes)
	{
		resultsXML.addSignalGroup(junction);
		
		// 1. cycleLength
		// ----------------------------------------------------
		resultsXML.addCycleLength(String.valueOf(cycleLength));
		
		// 2. startOfGreenMovement
		// ----------------------------------------------------
		for(int armID=0; armID<startOfGreenMovement.length; armID++)
		{
			for(int dirID=0; dirID<startOfGreenMovement[armID].length; dirID++)
			{
				resultsXML.addStartOfGreenMovement(String.valueOf(armID), String.valueOf(dirID), 
												   String.valueOf(startOfGreenMovement[armID][dirID]));
			}
		}
		
		// 3. durationMovement
		// ----------------------------------------------------
		for(int armID=0; armID<durationMovement.length; armID++)
		{
			for(int dirID=0; dirID<durationMovement[armID].length; dirID++)
			{
				resultsXML.addDurationMovement(String.valueOf(armID), String.valueOf(dirID), 
						String.valueOf(durationMovement[armID][dirID]));
			}
		}
		
		// 4. permittedLanes
		// ----------------------------------------------------
		for(int armID=0; armID<permittedLanes.length; armID++)
		{
			resultsXML.addPermittedLanes(String.valueOf(armID));
			for(int dirID=0; dirID<permittedLanes[armID].length; dirID++)
			{
				for(int laneID=0; laneID<permittedLanes[armID][dirID].length; laneID++)
				{
					resultsXML.addSubPermittedLanes(String.valueOf(dirID), String.valueOf(laneID), 
							String.valueOf(permittedLanes[armID][dirID][laneID]));
				}
			}
		}
	}
	
	public void addBestProhiLeftTurns(final List<Link> leftTurns)
	{
		resultsXML.addProhibitedLeftTurn();
		
		for(int ltID=0; ltID<leftTurns.size(); ltID++)
		{
			String leftTurnName = leftTurns.get(ltID).getName();
			resultsXML.addLeftTurn(String.valueOf(ltID), leftTurnName);
		}
	}
	
	public void addSUEResult(final double totalTravelTime, final double[] linkFlow, final double[] travelTime)
	{
		resultsXML.addSUEResult(String.valueOf(totalTravelTime));
		
		for(int linkID=0; linkID<linkFlow.length; linkID++)
		{
			resultsXML.addFlowTravelTime(String.valueOf(linkID), 
									   	 String.valueOf(linkFlow[linkID]), 
										 String.valueOf(travelTime[linkID]));
		}
	}
	
	public void creatResultsXMLFile(final String resultsDir)
	{
		try
		{
			resultsXML.createXMLFile(resultsDir);
		}
		catch(IOException e)
		{
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} 
	}
}
