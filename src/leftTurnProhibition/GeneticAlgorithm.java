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

import java.util.ArrayList;

import org.jgap.*;
import org.jgap.impl.*;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import network.Network;
import network.ODMatrix;
import network.importnetwork.ImportLocalNetImplement;
import leftTurnProhibition.SUEFitness;

public class GeneticAlgorithm
{
	private Network network = null; // 路网
	private ODMatrix odMatrix = null;
	private static final int MAX_ALLOWED_EVOLUTIONS = 50;
	
	private String methodeType;

	public GeneticAlgorithm(Network network,ODMatrix odMatrix,String xmlNetwork, String methodeType) 
			throws InvalidConfigurationException
	{
		this.methodeType = methodeType;
		this.setNetwork(network);
		this.setOdMatrix(odMatrix);

		Configuration conf = new DefaultConfiguration();
		conf.setPreservFittestIndividual(true);
		Configuration.resetProperty(Configuration.PROPERTY_FITEVAL_INST);
		conf.setFitnessEvaluator(new DeltaFitnessEvaluator());
		
		//conf.setKeepPopulationSizeConstant(false);
		@SuppressWarnings("unchecked")
		ArrayList<GeneticOperator> arr= new ArrayList<GeneticOperator>(conf.getGeneticOperators());
        for (int i=0;i<arr.size();i++)
        {
            if (arr.get(i).toString().contains(".MutationOperator@"))
            {
                //((MutationOperator)arr.get(i)).setMutationRate(10);
                System.out.println("Mutation rate: " + ((MutationOperator)arr.get(i)).getMutationRate() + " ");
            }
            
            if (arr.get(i).toString().contains(".CrossoverOperator@"))
            {
                //((CrossoverOperator)arr.get(i)).setAllowFullCrossOver(true);
                //((CrossoverOperator)arr.get(i)).
                System.out.println("Crossover rate: " + ((CrossoverOperator)arr.get(i)).getCrossOverRate() + " ");
            }
        }
		
		// 设置适应度函数
		SUEFitness myfunction = new SUEFitness(network, odMatrix, this.methodeType);
		conf.setFitnessFunction(myfunction);

		// 构建基因，JGAP这个包已经提供了Gene这个接口，只需要设定基因的长度，以及基因的具体类型，
		Gene[] sampleGene = new Gene[network.getLeftTurnLinks().size()];// 基因长度为左转数量
		for (int i = 0; i < network.getLeftTurnLinks().size(); i++)
		{
			sampleGene[i] = new IntegerGene(conf, 0, 1);
		}
		
		// 用上面的基因构建染色体
		IChromosome samplechromosome = new Chromosome(conf, sampleGene);
		conf.setSampleChromosome(samplechromosome);

		// 定义种群的大小为80，也就是说染色体的个数为80.
		conf.setPopulationSize(30);

		// 初始化种群，Genotype，基因型是固定长度的染色体种群。
		// 一个Genotype实例进化，那么它所有的染色体都进化。
		Genotype population = Genotype.randomInitialGenotype(conf);
		population = Genotype.randomInitialGenotype(conf);
		// 种群开始进化，并且计数进化时间
		long starttime = System.currentTimeMillis();
		for (int i = 0; i < MAX_ALLOWED_EVOLUTIONS; i++)
		{
			IloCplex cplex = null;
			try
			{
				cplex = new IloCplex();
			} 
			catch (IloException e)
			{
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			
			Network networktemp = new Network();
			ODMatrix odMatrixTemp = new ODMatrix();
			ImportLocalNetImplement localNetworktemp = new ImportLocalNetImplement(xmlNetwork,networktemp,odMatrixTemp);
			
			localNetworktemp.networkStorage();
			localNetworktemp = null;
			
			myfunction.setNetworkTemp(networktemp);
			myfunction.setCplex(cplex);
			population.evolve();
			
			networktemp.setNetworkName(null);
			networktemp.setAdjacencyMatrix(null);
			networktemp.setJunctions(null);
			networktemp.setLeftTurnLinks(null);
			networktemp.setLinks(null);
			networktemp.setGraph(null);
			networktemp.setLinks(null);
			networktemp = null;
			
			cplex = null;
		}
		long endtime = System.currentTimeMillis();
		System.out.println("The total evolve time:" + (endtime - starttime)/6000+" min");

		// IChromosome是一个染色体接口，而getFittestChromosome（）方法找到种群中适应度最高的染色体。
		IChromosome bestSolutionSoFar = population.getFittestChromosome();
		System.out.println("The best solution has a fitness value of " + bestSolutionSoFar.getFitnessValue());
		
		System.out.println("The best solution is: ");
		for(int i=0;i<network.getLeftTurnLinks().size();i++)
		{

			System.out.println("i "+i+" "+bestSolutionSoFar.getGene(i));
			//System.out.println("i "+i+" "+bestSolutionSoFar.getGenes()[i]);
		}
		
//		for(int i=0;i<network.getLeftTurnLinks().size();i++)
//		{
//			sampleGene[i]= new IntegerGene(conf,0,1);
//			sampleGene[i].setToRandomValue(new GaussianRandomGenerator());
//			System.out.println("i "+i+" "+sampleGene[i].getAllele()+", ");
//		}
	}
	
	public static void main(String[] args) throws InterruptedException
	{
		Network network = new Network();
		ODMatrix odMatrix = new ODMatrix();
		
		//final String xmlSumoNetwork =  "Test_network\\suedstadt.net.xml";
		//final String xmlOD = "Test_network\\suedstadt.districts.xml";
		//final String odDemandMatrix = "Test_network\\suedstadt_OD_Matrix.txt";
		//final String xmlLocalNet = "localnetwork\\suedstadt.localnet.xml";
		//导出本地XML路网
		//ExportLocalNetworkImplement.ExportImplementer(xmlSumoNetwork, xmlOD, odDemandMatrix, xmlLocalNet);

		//导入本地XML路网
		//long storageStartTime=System.currentTimeMillis();   //获取开始时间
		//final String xmlNetwork = "localnetwork\\twoLanesFourIntersectionsEightODs.localnet.xml";
		//final String xmlNetwork = "localnetwork\\FourIntersectionsEightODs.localnet.xml";
		//final String xmlNetwork = "localnetwork\\NineIntersectionTwoODpair.localnet.xml";
		final String xmlNetwork = "localnetwork\\fiveIntersectionsFiveOD.localnet.xml";
		ImportLocalNetImplement localNetwork = new ImportLocalNetImplement(xmlNetwork,network,odMatrix);
		localNetwork.networkStorage();
		localNetwork = null;
		
		try
		{
			@SuppressWarnings("unused")
			GeneticAlgorithm ga = new GeneticAlgorithm(network,odMatrix,xmlNetwork, "StageBased");
		} 
		catch (InvalidConfigurationException e)
		{
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
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
	 *            要设置的 network
	 */
	public void setNetwork(Network network)
	{
		this.network = network;
	}

	/**
	 * @return odMatrix
	 */
	public ODMatrix getOdMatrix()
	{
		return odMatrix;
	}

	/**
	 * @param odMatrix
	 *            要设置的 odMatrix
	 */
	public void setOdMatrix(ODMatrix odMatrix)
	{
		this.odMatrix = odMatrix;
	}

}
