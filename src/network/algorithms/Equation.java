/* --------------------------------------------------------------------
 * 
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 
 * 
 * Usage:
 * 		方程组:
		x + y = 60
		0.8*x - 1.2*y = 60
		//////////////////////////////////////////
		BigDecimal[][] matrix = new BigDecimal[][] 
				{
					{ new BigDecimal("1"), new BigDecimal("1"),new BigDecimal("60") },
					{ new BigDecimal("0.8"), new BigDecimal("-1.2"),new BigDecimal("-60") } 
				};
		BigDecimal[] rst = new Equation().solveEquation(matrix, 3,RoundingMode.HALF_UP);
		for (int i = 0; i < rst.length; ++i)
		{
			System.out.println(rst[i]);
		}
 *
 *
 */
package network.algorithms;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 解多元一次方程组 只能解决n个变量n个方程的情况
 */
public class Equation
{
	//~Variables
	//--------------------------------------------------------------------------
	//BigDecimal 由任意精度的整数非标度值 和 32 位的整数标度 (scale) 组成。
	//如果为零或正数，则标度是小数点后的位数。如果为负数，则将该数的非标度值乘以 10 的负 scale 次幂。
	//因此，BigDecimal 表示的数值是 (unscaledValue × 10-scale)。 
	private static final BigDecimal ZERO = new BigDecimal("0");
	private static final BigDecimal ONE  = new BigDecimal("1");
	//private BigDecimal[][] coefficientMatrix = null;
	//~Methods
	//--------------------------------------------------------------------------
	
	//==================================================================//
	// Test Code
	//==================================================================//
	public static void main(String[] args)
	{
		/**
		 * 方程组:
		 * x + y = 60
		 * 0.8*x - 1.2*y = 60
		 */
		// S_PT = 200   S_S  = 1900   S_RT = 223.53
		// F_L = 500  F_S = 800  F_R = 500
		double coef11 = new Double(1)/new Double(1900);
		double coef12 = new Double(-1)/new Double(1900);
		double coef13 = new Double(500)/new Double(223.53) - new Double(500)/new Double(200);
		double coef21 = new Double(1);
		double coef22 = new Double(1);
		double coef23 = new Double(800);
		
		BigDecimal[][] matrix = new BigDecimal[][] 
				{
					{new BigDecimal(coef11), new BigDecimal(coef12),new BigDecimal(coef13)},
					{new BigDecimal(coef21), new BigDecimal(coef22),new BigDecimal(coef23)} 
				};
				
		BigDecimal[] rst = new Equation(matrix).solveEquation(matrix,3,RoundingMode.HALF_UP);
		for (int i = 0; i < rst.length; ++i)
		{
			System.out.println(rst[i]);
		}
	}
	
	public Equation(BigDecimal[][] matrix)
	{
		
	}
	
	/**
	 * 判断系数矩阵是否是null或空数组
	 * 
	 * @param matrix
	 *            方程组系数矩阵
	 * @return null或空数组返回true,否则返回false
	 */
	private boolean isNullOrEmptyMatrix(BigDecimal[][] matrix)
	{
		//方程组系数矩阵为空
		if (matrix == null || matrix.length == 0)
		{
			return true;
		}
		
		int row = matrix.length;    //方程式的个数
		int col = matrix[0].length; //方程系数个数
		for (int i = 0; i < row; ++i)
		{
			for (int j = 0; j < col; j++)
			{
				if (matrix[i][j] == null)
				{
					return true;    //有系数是零
				}
			}
		}
		return false;
	}
	
	/**
	 * 解多元一次方程组 只能解决n个变量n个方程的情况,即矩阵是n*(n+1)的形式
	 * 
	 * @param matrix
	 *            方程组系数矩阵
	 * @param scale
	 *            精确小数位数，即设置精确度是必须的！
	 * @param roundingMode
	 *            舍入模式
	 * @return
	 */
	public BigDecimal[] solveEquation(BigDecimal[][] matrix,int scale,
											 RoundingMode roundingMode)
	{
		//方程组系数矩阵为空，不计算，返回空
		if (isNullOrEmptyMatrix(matrix))
		{
			return new BigDecimal[0];
		}
		
		// 1. 用高斯消元法将矩阵变为上三角形矩阵
		BigDecimal[][] triangular = elimination(matrix, scale, roundingMode);
		System.out.println(triangular);
		
		// 2. 回代求解(针对上三角形矩阵)
		BigDecimal[] result = substitutionUpMethod(triangular, scale, roundingMode);
		return result;
	}
	
	/**
	 * 用高斯消元法将矩阵变为上三角形矩阵
	 *
	 * @param matrix
	 * @param scale
	 *            精确小数位数
	 * @param roundingMode
	 *            舍入模式
	 * @return
	 */
	private BigDecimal[][] elimination(BigDecimal[][] matrix,int scale,
											  RoundingMode roundingMode)
	{
		// 处理异常
		if (isNullOrEmptyMatrix(matrix) || matrix.length != matrix[0].length - 1)
		{
			return new BigDecimal[0][0];
		}
		
		int matrixLine = matrix.length; //方程个数
		for (int i=0; i<matrixLine-1; i++)
		{
			// 第j行的数据 - (第i行的数据 / matrix[i][i])*matrix[j][i]
			for (int j = i + 1; j < matrixLine; j++)
			{
				for (int k=i+1; k<=matrixLine; k++)
				{
					// matrix[j][k] = matrix[j][k] -
					// (matrix[i][k]/matrix[i][i])*matrix[j][i];
					matrix[j][k] = matrix[j][k].subtract((matrix[i][k]
									.divide(matrix[i][i], scale, roundingMode)
									.multiply(matrix[j][i])));
				}
				matrix[j][i] = ZERO;
			}
		}
		return matrix;
	}

	/**
	 * 回代求解(针对上三角形矩阵)
	 *
	 * @param matrix
	 *            上三角阵
	 * @param scale
	 *            精确小数位数
	 * @param roundingMode
	 *            舍入模式
	 */
	private BigDecimal[] substitutionUpMethod(BigDecimal[][] matrix, int scale,
													  RoundingMode roundingMode)
	{
		// 方程无解或者解不惟一
		int row = matrix.length;  //行个数
		for (int i=0; i<row; i++)
		{
			if (matrix[i][i].equals(ZERO.setScale(scale)))
			{
				return new BigDecimal[0];
			}
		}
		
		BigDecimal[] result = new BigDecimal[row];
		for (int i=0; i<result.length; i++)
		{
			result[i] = ONE;
		}
		
		BigDecimal tmp;
		for (int i=row-1; i>=0; i--)
		{
			tmp = ZERO;
			int j = row-1;
			while(j>i)
			{
				tmp = tmp.add(matrix[i][j].multiply(result[j]));
				--j;
			}
			result[i] = matrix[i][row].subtract(tmp).divide(matrix[i][i], scale,roundingMode);
		}
		return result;
	}

//	/**
//	 * @return coefficientMatrix
//	 */
//	public BigDecimal[][] getCoefficientMatrix()
//	{
//		return coefficientMatrix;
//	}
//
//	/**
//	 * @param coefficientMatrix 
//	 *		要设置的 coefficientMatrix
//	 */
//	public void setCoefficientMatrix(BigDecimal[][] coefficientMatrix)
//	{
//		this.coefficientMatrix = coefficientMatrix;
//	}
	
}
