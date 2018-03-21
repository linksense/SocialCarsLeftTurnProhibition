/*
 * --------------------------------------------------------------------
 * 
 * -------------------------------------------------------------------- 
 * Copyright (C) 2015-2016, by Huijun Liu and Contributors.
 * 
 * Original Author: Huijun Liu 
 * Contributor(s) : Qinrui Tang
 * 
 * Last Change Date:
 */
package leftTurnProhibition;

import java.io.IOException;
import java.util.List;

import org.jgap.FitnessFunction;
import org.jgap.IChromosome;

import ilog.cplex.IloCplex;
import network.Junction;
import network.Network;
import network.ODMatrix;
import trafficAssignment.StochasticUserEquilibirum;

public class SUEFitness extends FitnessFunction
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Network network = null; // 路网
	private Network networkTemp = null; // 路网
	private ODMatrix odMatrix = null;
	private IloCplex cplex = null;
	
	private String methodType;
	//private double target = 0.0f;

	public SUEFitness(Network network,ODMatrix odMatrix, String methodType)
	{
		this.setMethodType(methodType);
		this.network = network;
		this.odMatrix = odMatrix;
		//.target = target;
	}

	protected double evaluate(IChromosome chromosome)
	{
		double sumTravelCost = 0;
	    //boolean defaultComparation = chromosome.getConfiguration().getFitnessEvaluator().isFitter(2, 1);
	    // ------------------------------------------------------------------
		// TODO Auto-generated method stub
		prohibitLeftTurnLinks(this.networkTemp, chromosome);

		//StochasticUserEquilibirum sue = new StochasticUserEquilibirum(this.networkTemp,this.odMatrix,this.cplex);
		
		StochasticUserEquilibirum sueBPR = new StochasticUserEquilibirum(this.networkTemp,this.odMatrix,this.cplex, "BPR");
		try
		{
			sueBPR.algorithmMSA();
		} 
		catch (IOException e)
		{
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		
//		System.out.println("SUE with BPR: ");
		int[] SUELinkFlow = new int[sueBPR.getLinkFlowAllOD().length];
		for(int j=0;j<sueBPR.getLinkFlowAllOD().length;j++)
		{
			SUELinkFlow[j] = (int)Math.round(sueBPR.getLinkFlowAllOD()[j]);
//			System.out.println(Math.round(sueBPR.getLinkFlowAllOD()[j]));
			sumTravelCost+=SUELinkFlow[j];
		}
		if (sumTravelCost == 0)
		{
			sumTravelCost = 1.0E10;
			// System.exit(0);
		}
		else
		{
			sumTravelCost=0;
			StochasticUserEquilibirum sueStagebased = new StochasticUserEquilibirum(this.networkTemp,this.odMatrix,cplex, SUELinkFlow, this.methodType);
			
			try
			{
				sueStagebased.algorithmMSA();
			} 
			catch (IOException e)
			{
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
						
			for(int j=0;j<sueBPR.getLinkFlowAllOD().length;j++)
			{
				sumTravelCost+=Math.round(sueStagebased.getLinkFlowAllOD()[j])*(sueStagebased.getTravelTime()[j]);
			}
			
			sueStagebased.setNetwork(null);
			sueStagebased.setOdMatrix(null);
			sueStagebased.setCplex(null);
			sueStagebased = null;
		}
		
		sueBPR.setNetwork(null);
		sueBPR.setOdMatrix(null);
		sueBPR.setCplex(null);
		sueBPR = null;
		
		
		//double fitnessValue = (1.0E10-sumTravelCost);
		double fitnessValue = (sumTravelCost);
		System.out.println("######--------------------------------- #####" );
		System.out.println("###### 当前的行驶耗时: " + sumTravelCost);
		//System.out.println("###### 当前的行驶耗时相对值: " + fitnessValue);
		return fitnessValue;
	}

	public static int getValueAtGene(IChromosome a_potentialSolution,int a_position)
	{
		Integer value = (Integer) a_potentialSolution.getGene(a_position).getAllele();
		return value.intValue();

	}

	public void prohibitLeftTurnLinks(Network network, IChromosome chromosome)
	{
		int numOfGens = chromosome.size();
		// System.out.println("numOfGens "+arg0.toString());

		for (int i = 0; i < numOfGens; i++)
		{
			System.out.println("getGene(" + i + "): " + (Integer) chromosome.getGene(i).getAllele());
			if ((Integer) chromosome.getGene(i).getAllele() == 1)
			{
				// System.out.println("network.getLeftTurnLinks() "+network.getLeftTurnLinks().toString());
				// 1. 在Junction里删除左转边
				List<Junction> junctions = network.getJunctions();
				for (int k = 0; k < junctions.size(); k++) // 遍历所有的路口
				{
					Junction junc = junctions.get(k);
					boolean removeResult = junc.removeInternalEdge(this.network.getLeftTurnLinks().get(i));
					if (removeResult == true)
					{
						break;
					}
	
					junc = null;
				}
				junctions = null;
				// 2. 在路网里删除左转边
				network.removeLink(this.network.getLeftTurnLinks().get(i));
			}
		}
	}

	/**
	 * @param cplex 
	 *		要设置的 cplex
	 */
	public void setCplex(IloCplex cplex)
	{
		this.cplex = cplex;
	}

	/**
	 * @param networkTemp 
	 *		要设置的 networkTemp
	 */
	public void setNetworkTemp(Network networkTemp)
	{
		this.networkTemp = networkTemp;
	}

	/**
	 * @return methodType
	 */
	public String getMethodType()
	{
		return methodType;
	}

	/**
	 * @param methodType 要设置的 methodType
	 */
	public void setMethodType(String methodType)
	{
		this.methodType = methodType;
	}

}
