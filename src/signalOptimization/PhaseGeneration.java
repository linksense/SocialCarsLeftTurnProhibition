/* --------------------------------------------------------------------
 * PhaseGeneration.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and all Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 15.06.2016
 * 
 * Function:
 *           1.
 *           
 */
//package signalOptimization;
package signalOptimization;

import java.util.ArrayList;
import java.util.List;

import org.ujmp.core.Matrix;

import ilog.concert.*;
import ilog.cplex.*;

public class PhaseGeneration
{
	//~Variables
	//--------------------------------------------------------------------------	
	// private Matrix modFoesMatrixLaneBased = null;
	// private Matrix phaseMatrixLaneBased   = null;
	
	// List<Vertex> arms = null;
	// List<ArrayList<Link>> armDir = null;
	// List<Integer> armLanesNum = null;
	
	List<ArrayList<String>> phaseMatrix = new ArrayList<ArrayList<String>>();//存储Phase
	private IloCplex cplex = null;
	private int[][] adjacentMatrix;

	
	//~Methods
	//--------------------------------------------------------------------------
	public PhaseGeneration(IloCplex cplex)
	{
		this.cplex = cplex;
	}

	public void phaseGeneration(List<ArrayList<Integer>> sharedLineDirIndex, Matrix foesMatrix)
	{
		// List<ArrayList<String>> phaseList = new ArrayList<ArrayList<String>>();//存储Phase
		
		// --------------------------------------------------------- //
		// 1. 处理 foes Matrix 
		// --------------------------------------------------------- //
		int[][] foeVectors = foesMatrix.toIntArray();
		int junDirCount = foeVectors.length;
//		System.out.println("original conflict matrix in PhaseGeneration.java:");
//		System.out.println(foesMatrix.toString());
		
		// 创建一个新的tempSharedLineDir，用来存应该有同样的冲突情况的dir的index， 
		//比如 在sharedLineDirIndex中是[6,7], [7,8]，  在tempSharedLineDir中则是[6,7,8]，因为这三个dir应该有同样的冲突矩阵
		List<ArrayList<Integer>> TempSharedLineDirIndex  =  new ArrayList<ArrayList<Integer>>();
		if(sharedLineDirIndex.size()==1)
		{
			ArrayList<Integer> sharedLineDir = sharedLineDirIndex.get(0);
			TempSharedLineDirIndex.add(sharedLineDir);
		}
		else
		{
			for(int i=1; i<sharedLineDirIndex.size(); i++)
			{
				@SuppressWarnings("unused")
				ArrayList<Integer> sharedLineDir = sharedLineDirIndex.get(i);
				ArrayList<Integer> tempSharedLineDir = new ArrayList<Integer>();
				if(sharedLineDirIndex.get(i-1).get(sharedLineDirIndex.get(i-1).size()-1)==sharedLineDirIndex.get(i).get(0))
				{
					tempSharedLineDir.addAll(sharedLineDirIndex.get(i-1));
					tempSharedLineDir.removeAll(sharedLineDirIndex.get(i));
					tempSharedLineDir.addAll(sharedLineDirIndex.get(i));
				}
				else
				{
					tempSharedLineDir.addAll(sharedLineDirIndex.get(i-1));				
				}
				TempSharedLineDirIndex.add(tempSharedLineDir);
				if(i==sharedLineDirIndex.size()-1)
				{
					TempSharedLineDirIndex.add(sharedLineDirIndex.get(i));
				}
			}
		}
		
//		System.out.println("TempSharedLineDirIndex.toString() in PhaseGeneration.java: "+TempSharedLineDirIndex.toString());
		//使用修改的冲突矩阵更新Phase
		for(int i=0; i<TempSharedLineDirIndex.size(); i++)
		{
			ArrayList<Integer> sharedLineDir = TempSharedLineDirIndex.get(i);
			
			int[] sum = new int[junDirCount];
			for(int k=0; k<junDirCount; k++)
			{
				for(int j=0; j<sharedLineDir.size(); j++)
				{
				int juncDirIndex = sharedLineDir.get(j);
				 if(foeVectors[juncDirIndex][k]==1)
					 sum[k]+=foeVectors[juncDirIndex][k];
				}
			}
		
//		for(int i=0; i<sharedLineDirIndex.size(); i++)
//		{
//			ArrayList<Integer> sharedLineDir = sharedLineDirIndex.get(i);
//			System.out.println("sharedLineDir.toString() sharing lanes in PhaseGeneration.java: "+sharedLineDir.toString());
//			//int [] finalFoesVector = new int[junDirCount]; 	
//			
//			int[] sum = new int[junDirCount];
//			for(int k=0; k<junDirCount; k++)
//			{
//				for(int j=0; j<sharedLineDir.size(); j++)
//				{
//				int juncDirIndex = sharedLineDir.get(j);
//				 if(foeVectors[juncDirIndex][k]==1)
//					 sum[k]+=foeVectors[juncDirIndex][k];
//				}
//			}
			
//			System.out.println("sum of conflict:");
//			for(int k=0; k<junDirCount; k++)
//			{
//				System.out.print(sum[k]+"  ");
//			}
//			System.out.println();
//			System.out.println("modified conflict in PhaseGeneration.java");
			for(int j=0; j<sharedLineDir.size(); j++)
			{
				int juncDirIndex = sharedLineDir.get(j);
				for(int k=0; k<junDirCount; k++)
				{
					if(sum[k]>0){
						foeVectors[juncDirIndex][k]=1;
						foeVectors[k][juncDirIndex]=1;
					}
//					System.out.print(foeVectors[juncDirIndex][k]+"  ");
				}
//				System.out.println();
			}
			
//			for(int j=0; j<sharedLineDir.size(); j++)
//			{
//				int juncDirIndex = sharedLineDir.get(j);
//				foeVectors[juncDirIndex] = finalFoesVector;
//			}
		} 
		// sharedLineDirIndex
		
//		System.out.println("经过共享处理的conflict matrix");
//		 for (int i = 0; i < junDirCount; i++) 
//	        {
//	            for (int j = 0; j < junDirCount; j++) 
//	            {
//	            	System.out.print(foeVectors[i][j] + "  ");
//	            }
//	            System.out.println("");
//	        }
		
		// --------------------------------------------------------- //
		// 2. 求最大团 
		// --------------------------------------------------------- //
//		for(int i=0; i<foeVectors.length; i++)
//		{
//			ArrayUtils.reverse(foeVectors[i]); 
//		}
		
		
		
		MaxCliqueProblem maxClique = new MaxCliqueProblem();
		
		List<Long[]> maxCliqueResult = maxClique.maxCliqueComputation(foeVectors);
		adjacentMatrix = maxClique.getAdjacentMatrix();
//		System.out.println("最大团结果为:");
//		for (Long[] l : maxCliqueResult)
//		{
//			for (int i = 0; i < l.length; i++)
//			{
//				// if (l[i] == 1)
//				{
//					System.out.print(l[i] + ",");
//				}
//			}
//			System.out.println();
//		}
//
//		System.out.println("最大团个数为：" + maxCliqueResult.size());

		
		// --------------------------------------------------------- //
		// 3. 创建PhaseList
		// --------------------------------------------------------- //
		//cplex = null;
		createNewPhaseList(maxCliqueResult);
		cplex = null;
	}
	
	private void createNewPhaseList(List<Long[]> maxCliqueResult)
	{
		int numOfMaxClique = maxCliqueResult.size();
		int numOfDirLane = maxCliqueResult.get(0).length;
		//int[][] parameterMatrix = new int[numOfMaxClique][res.get(0).length];
		
		try
		{
//			cplex = new IloCplex();
			//variables
			IloNumVar[] selectMaxClique = new IloNumVar[numOfMaxClique];
			selectMaxClique = cplex.boolVarArray(numOfMaxClique);
			
			//objective
			IloLinearNumExpr objective = cplex.linearNumExpr();
			for(int i=0;i<numOfMaxClique;i++)
			{
				objective.addTerm(1.0, selectMaxClique[i]);
			}
			cplex.addMinimize(objective);
			
			//constraints
			//IloLinearNumExpr[] permittedMovement = new IloLinearNumExpr[numOfDir];
			
			for(int j=0;j<numOfDirLane;j++)
			{
				IloLinearNumExpr permittedMovement = cplex.linearNumExpr();
				for(int i=0;i<numOfMaxClique;i++)
				{
					//System.out.println(selectMaxClique[i]);
					permittedMovement.addTerm(maxCliqueResult.get(i)[j].doubleValue(), selectMaxClique[i]);
				}
				cplex.addGe(permittedMovement, 1.0);
			}
			
			cplex.setOut(null);
			//cplex.setParam(IloCplex.Param.Simplex.Display, 0);
			//solve
			if (cplex.solve()) 
			{
//				System.out.println();
//				System.out.println("The minimum number of phases is: "+cplex.getObjValue());
//				System.out.println("The final phases are: ");
				for(int i=0;i<numOfMaxClique;i++)
				{
					if(cplex.getValue(selectMaxClique[i])==1)
					{
						ArrayList<String> phaseList = new ArrayList<String>();
						
						for(int j=0;j<numOfDirLane;j++)
						{
//							System.out.print(maxCliqueResult.get(i)[j]+"\t");
							
							if(maxCliqueResult.get(i)[j] == 1)
							{
								phaseList.add("G");
							}
							else if(maxCliqueResult.get(i)[j] == 0)
							{
								phaseList.add("r");
							}
							else
							{
								System.out.println("## maxCliqueResult里有非0/1值！ 程序结束，请处理异常！");
							}
						} // numOfDirLane
						
						phaseMatrix.add(phaseList);
//						System.out.println();
					} // if
				} // numOfMaxClique
			}
			else
			{
        		System.out.println("problem not solved "+cplex.getStatus());
        		// cplex.getStatus();
        	}
			
			cplex.clearLazyConstraints();
			cplex.clearUserCuts();
			cplex.clearCuts();
			cplex.clearModel();
			
		}
		catch (IloException e)
		{
			e.printStackTrace();
		}
	}
	
	/****************************************************************
	 *******************Getter and Setter****************************
	 ****************************************************************/
	/**
	 * @return phaseMatrix
	 */
	public List<ArrayList<String>> getPhaseMatrix()
	{
		return phaseMatrix;
	}

	/**
	 * @param phaseMatrix 
	 *		要设置的 phaseMatrix
	 */
	public void setPhaseMatrix(List<ArrayList<String>> phaseMatrix)
	{
		this.phaseMatrix = phaseMatrix;
	}
	
	public int[][] getAdjacentMatrix()
	{
		return adjacentMatrix;
	}

	/**
	 * @return cplex
	 */
	public IloCplex getCplex()
	{
		return cplex;
	}

	/**
	 * @param cplex 
	 *		要设置的 cplex
	 */
	public void setCplex(IloCplex cplex)
	{
		this.cplex = cplex;
	}
	
}
