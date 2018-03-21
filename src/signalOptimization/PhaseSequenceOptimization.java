/* --------------------------------------------------------------------
 * phaseSequenceOptimization.java
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
package signalOptimization;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Q. Tang
 *
 */
public class PhaseSequenceOptimization
{

	 private static int N;

	 private int cl;             //当前路径的长度
	 private int fl;             //当前只当的最大路径长度
	 private int [] x;	         //= new int[4];
	 private int [] bestX;
	 private int [][] weight;

	 private List<ArrayList<String>> fixedPhaseMatrix;

	 public PhaseSequenceOptimization()
	 {

	 }

	 /**
	  * 判断第k个数是否不同与前k-1个数
	  * @param k
	  * @return bool
	  */
	 private boolean nextValue(int k)
	 {
		 int i = 0;
		 while(i < k)
		 {
			 if(x[k] == x[i])
			 {
				 return false;
			 }
			 i += 1;
		 }
		 return true;
	 }

	 /**
	  * 第k条路径选择
	  * @param k
	  */
	 private void backUp(int k)
	 {
		 if(k==N-1)
		 {
			 for (int j=1;j<=N;j++)
			 {
				 x[k] = Math.floorMod(x[k]+1, N);
				 if(nextValue(k) && cl + weight[x[k-1]][x[k]] + weight[x[k]][0] < fl) 
				 {//如果最短路径,更新最优解
					 fl = cl + weight[x[k - 1]][x[k]] + weight[x[k]][0];
					 for (int i = 0; i < N; i++) 
					 {
						 bestX[i] = x[i];
					 }
				 }
			 }
		 }
		 else
		 {
			 for(int j=1; j<=N; j++)
			 {
				 x[k] = Math.floorMod(x[k]+1, N);
				 if(nextValue(k) && cl+weight[x[k-1]][x[k]] <= fl)
				 {
					 //此路可行
					 cl += weight[x[k-1]][x[k]];
					 backUp(k+1);
					 cl -= weight[x[k-1]][x[k]];
				 }
			 }
		 }
	 }

	 public void solve(int[][] weight, List<ArrayList<String>> phaseMatrix)
	 {
		 N = phaseMatrix.size();
		 x=new int[N];
		 bestX= new int[N]; 	
		 this.weight = weight;

		 int k = 1; //第0个顶点是固定的,从第一个顶点开始选择
		 cl = 0;
		 fl = Integer.MAX_VALUE;
		 backUp(k);

		 fixedPhaseMatrix = new ArrayList<ArrayList<String>>();
		 for(int i=0; i<N; i++)
		 {
			 ArrayList<String> currPhase1 = new ArrayList<String>(phaseMatrix.get(bestX[i])); // 当前的phase

			 if(!fixedPhaseMatrix.contains(currPhase1))
			 {
				 fixedPhaseMatrix.add(currPhase1); // 将当前phase加入整理后的phaseMatrix中
			 }
		 }
	 }


	 /****************************************************************
	  ******************* Getter and Setter****************************
	  ****************************************************************/
	 public List<ArrayList<String>> getFixedPhaseMatrix() 
	 {
		 return fixedPhaseMatrix;
	 }


	 public int[] getBestX() 
	 {
		 return bestX;
	 }

	 public int getMinLen()
	 {
		 return fl;
	 }
	    
}
