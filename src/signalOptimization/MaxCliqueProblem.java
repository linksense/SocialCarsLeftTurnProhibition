/*
 * --------------------------------------------------------------------
 * MaxCliqueProblem.java
 * -------------------------------------------------------------------- (C)
 * Copyright 2015-2017, by Huijun Liu and all Contributors.
 * 
 * Original Author: Huijun Liu Contributor(s): Qinrui Tang
 * 
 * Last Change Date: 05.03.2017
 * 
 * Function: 1.最大团问题
 */
package signalOptimization;

import java.util.ArrayList;
import java.util.List;

public class MaxCliqueProblem
{
	// ~Variables
	// --------------------------------------------------------------------------
	int[][] adjacency;                              // 邻接矩阵,1连通,0不连通
	int numOfVertex;                                // 当前图中顶点的数量
	int[][] foesVector;
	// 最大团相关
	public Long[] currentx;                         // 当前解
	public int currentNumOfVertex;                  // 当前顶点数
	public int mostNumOfVertex;                     // 当前最大顶点数
	public Long[] bestx;                            // 当前最优解
	public List<Long[]> result = new ArrayList<Long[]>(); // 最大团结果
	public int numOfMaxClique;

	//~Methods
	//--------------------------------------------------------------------------
	public MaxCliqueProblem()
	{
		
	}
	
	public int[][] getAdjacentMatrix()
	{
		return adjacency;
	}
	
	/**
	 * 计算最大团
	 */
	/**
	 * @param adjacency
	 * @return
	 */
	public List<Long[]> maxCliqueComputation(int[][] foesVector)
	{
//		System.out.println("定点个数为:" + adjacency.length);
		this.foesVector = foesVector;
		this.numOfVertex = foesVector.length;
		
		//=======================================================//
		// 反转冲突矩阵的值
		//=======================================================//
		
		//System.out.println("邻接矩阵转换之前： ");
		adjacency = new int[numOfVertex][numOfVertex];
        for (int i = 0; i < numOfVertex; i++) 
        {
            for (int j = 0; j < numOfVertex; j++) 
            {
            	//System.out.print(foesVector[i][j]+"\t");
            	if(foesVector[i][j] == 0)
            	{
            		adjacency[i][j] = 1;
            	}
            	else if(foesVector[i][j] == 1)
            	{
            		adjacency[i][j] = 0;
            	}
            	else
            	{
            		System.out.println("邻接矩阵为有非 0/1 的值，无法计算，退出!");
            		System.exit(0);
            	}
            	if(i==j)adjacency[i][j] = 0;
            }
           
        }
        
//        System.out.println("邻接矩阵为 in MaxCliqueProblem.java：");
//        for (int i = 0; i < numOfVertex; i++) 
//        {
//            for (int j = 0; j < numOfVertex; j++) 
//            {
//            	System.out.print(adjacency[i][j] + "  ");
//            }
//            System.out.println("");
//        }
        
		
		currentx = new Long[numOfVertex];
		bestx = currentx;// 最优解
		currentNumOfVertex = 0;
		mostNumOfVertex = 0;
		
		//=======================================================//
		// 求最大团
		//=======================================================//
		backtrackForMaxClique(0);
		 
		//直接用邻接矩阵测试
//		for (int i = 0; i < numOfVertex; i++) 
//		{
//			Long[] temp = new Long[numOfVertex];
//			 for (int j = 0; j < numOfVertex; j++) 
//			 {
//				 temp[j]=Long.valueOf(adjacency[i][j]);;
//			 }
//			 result.add(temp);
//		}
//		
		return result;
	}

	/**
	 * 回溯查找最大团
	 *
	 * @param i
	 */
	public void backtrackForMaxClique(int i)
	{
		//numOfVertex 图的顶点数 n
		// currentNumOfVertex 当前顶点数 cn
		// mostNumOfVertex 当前最大顶点数 bestn
		// currentx 当前最优解 bestx
		// adjacentMatrix： 邻接矩阵
		// 
		// 到达叶节点
//		System.out.println("i "+i);
//		System.out.println("numOfVertex "+numOfVertex);
		if (i >= numOfVertex)
		{ 
//			if (currentNumOfVertex > mostNumOfVertex)
//			{
//				result.clear();
//			}
//			
			if (currentNumOfVertex >= 1)
			{
				Long[] temp = new Long[currentx.length];
				System.arraycopy(currentx, 0, temp, 0, currentx.length);
				result.add(temp);
				mostNumOfVertex = currentNumOfVertex;
			}
		}
		else
		{
			boolean ok = true;
			for (int j = 0; j < i; j++)
			{// 检查顶点i是否与当前团全部连接
				
				if (currentx[j] == 1 && adjacency[i][j] == 0)
				{
					ok = false;
					break;
				}
			}
			
			// 从顶点i到已选入的顶点集中每一个顶点都有边相连
			if (ok)
			{
				// 进入左子树
				currentx[i] = (long) 1;
				currentNumOfVertex++;
				backtrackForMaxClique(i + 1);
				currentx[i] = (long) 0;
				currentNumOfVertex--;
			}
			
			// 当前顶点数加上未遍历的课选择顶点>=当前最优顶点数目时才进入右子树;
			// 如果不需要找到所有的解，则不需要等于
			if (currentNumOfVertex + numOfVertex - i >= mostNumOfVertex)
			{
				// 进入右子树
				currentx[i] = (long) 0;
				backtrackForMaxClique(i + 1);
			}
		}
	}

	//==================================================================//
	// Test Code
	//==================================================================//
//	public static void main(String[] args)
//	{
//		int[][] adjacency = { { 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0 }, 
//							  { 1, 1, 1, 1, 0, 0, 1, 1, 0, 0, 0, 0 },
//							  { 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0 }, 
//							  { 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0 }, 
//							  { 1, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 1 },
//							  { 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 0 },
//							  { 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0 },
//							  { 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0 },
//							  { 1, 1, 0, 0, 0, 0, 1, 1, 0, 0, 1, 1 },
//							  { 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0 },
//							  { 0, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 0 },
//							  { 0, 0, 0, 1, 1, 0, 0, 1, 1, 1, 1, 0 } };
//		for(int i=0; i<adjacency.length; i++)
//		{
//			ArrayUtils.reverse(adjacency[i]); 
//		}
//		 
//		
//		MaxCliqueProblem max = new MaxCliqueProblem();
//
//		List<Long[]> res = max.maxCliqueComputation(adjacency);
//
//		System.out.println("最大团结果为:");
//		for (Long[] l : res)
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
//		System.out.println("最大团个数为：" + res.size());
//		
//		phaseGeneration(res);
//	}
//	
//	public static void phaseGeneration(List<Long[]> res)
//	{
//		int numOfMaxClique = res.size();
//		int numOfDir = res.get(0).length;
//		//int[][] parameterMatrix = new int[numOfMaxClique][res.get(0).length];
//		
//		try
//		{
//			IloCplex cplex = new IloCplex();
//			//variables
//			IloNumVar[] selectMaxClique = new IloNumVar[numOfMaxClique];
//			selectMaxClique = cplex.boolVarArray(numOfMaxClique);
//			
//			//objective
//			IloLinearNumExpr objective = cplex.linearNumExpr();
//			for(int i=0;i<numOfMaxClique;i++)
//			{
//				objective.addTerm(1.0, selectMaxClique[i]);
//			}
//			cplex.addMinimize(objective);
//			
//			//constraints
//			//IloLinearNumExpr[] permittedMovement = new IloLinearNumExpr[numOfDir];
//			
//			for(int j=0;j<numOfDir;j++)
//			{
//				IloLinearNumExpr permittedMovement = cplex.linearNumExpr();
//				for(int i=0;i<numOfMaxClique;i++)
//				{
//					//System.out.println(selectMaxClique[i]);
//					permittedMovement.addTerm(res.get(i)[j].doubleValue(), selectMaxClique[i]);
//				}
//				cplex.addGe(permittedMovement, 1.0);
//			}
//			
//			cplex.setParam(IloCplex.Param.Simplex.Display, 0);
//			//solve
//			if (cplex.solve()) 
//			{
//				System.out.println();
//				System.out.println("The minimum number of phases is: "+cplex.getObjValue());
//				System.out.println("The final phases are: ");
//				for(int i=0;i<numOfMaxClique;i++)
//				{
//					if(cplex.getValue(selectMaxClique[i])==1)
//					{
//						for(int j=0;j<numOfDir;j++)
//						{
//						 System.out.print(res.get(i)[j]+"\t");
//						}
//						System.out.println();
//					}
//				}
//			}
//			else
//			{
//        		System.out.println("problem not solved "+cplex.getStatus());
//        		// cplex.getStatus();
//        	}
//			
//		}
//		catch (IloException e)
//		{
//			e.printStackTrace();
//		}
//	}
}
