/* --------------------------------------------------------------------
 * leftTurnTypeDetermination.java
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

import network.Junction;
import network.Link;
import network.graph.Vertex;
import signalOptimization.LanebasedSinalTiming;

/**
 * @author Q. Tang
 *
 */
public class LeftTurnTypeDetermination
{
	private List<Vertex> arms = new ArrayList<Vertex>();                      //所有的arms
//	private List<Vertex> outs = new ArrayList<Vertex>();                      //所有的outs
//	private List<Integer> outLanesNum = new ArrayList<Integer>();             //驶出点的车道数
//	private int [][] dirOutIndex;
	private int armNumber; //Arm的个数
	private List<Integer> armDirNum = new ArrayList<Integer>();               //arm的转向数
	private List<ArrayList<Link>> armDir = new ArrayList<ArrayList<Link>>();  //每个ARM的转向内部通路
	private List<Integer> armLanesNum = new ArrayList<Integer>();             //arm的车道数
	private List<Link> extLinks = new ArrayList<Link>();
	private List<Link> intLinks = new ArrayList<Link>();
	private double[][] demand;
	private Junction junc;
	
	
	private LanebasedSinalTiming signalTiming=null;
	
	public LeftTurnTypeDetermination(Junction junc, LanebasedSinalTiming signalTiming, 
									 double[][] demand, List<ArrayList<Link>> armDir,
									 List<Vertex> arms,List<Integer> armDirNum,int armNumber)
	{
		this.junc = junc;

		this.demand =demand;
		this.armDir = armDir;
		this.arms = arms;
		this.armDirNum =armDirNum;
		this.armNumber = armNumber;
		this.signalTiming = signalTiming;
//		this.cplex = cplex;
//		travelTime = new double[network.getLinks().size()];
//		degreeOfSaturation = new double[network.getLinks().size()];
	}
	
	// 如果左转存在对面直行，取对面直行的flow和对面直行的lane的数量，用条件判断左转类型，然后setDirection("l")或setDirection("L")	
	//修改冲突矩阵相应的位置
	public void HCMLTTypeCondtions()
	{
		extLinks    = junc.getExternalLinks();
		intLinks = junc.getInternalLinks();
		
		//Matrix modFoesMatrix = junc.getModFoesMatrix();
		junc.initModFoesMatrix();
		//modFoesMatrix = junc.getModFoesMatrix();
		
		////////////////////////////////////////////////////////
		//4.   每个路口Junction的 每个arm的对面直行Opposing TH  //
		////////////////////////////////////////////////////////
		for (int itemp = 0; itemp < armNumber; itemp++)
		{
			//arm i 的LT 所对应的对面直行 opposing TH
			//////////////////////////////////////////
			boolean leftTurn = false;
			boolean Straight = false;

			Link leftTurnLink = null;
			Link straihgtLink = null;

			double leftTurnFlow = 0.0f;
			@SuppressWarnings("unused")
			double straihgtFlow = 0.0f;
			@SuppressWarnings("unused")
			int leftTurnDirIndex = -1;
			@SuppressWarnings("unused")
			int straihgtDirIndex = -1;

			double armTHFlow = 0.0f;

			for (int j = 0; j < armDirNum.get(itemp); j++)
			{
				String dirS = armDir.get(itemp).get(j).getDirection();//第 i个arm的第j个转向

				if(dirS.equals("l") || dirS.equals("L")) //若为左转
				{
					leftTurn = true;
					leftTurnLink = armDir.get(itemp).get(j);
					leftTurnFlow = demand[itemp][j];
					leftTurnDirIndex = j;
				}
				if(dirS.equals("s") || dirS.equals("S")) //若为直行
				{
					Straight = true;
					straihgtLink = armDir.get(itemp).get(j);
					straihgtFlow = demand[itemp][j];
					straihgtDirIndex = j;
				}
			}

			if(leftTurn == true && Straight == true)//arm i 有LT 和 TH(S)
			{
				Vertex straightOut = straihgtLink.getEnd();
				Vertex straightOutExtEnd = null;
				Vertex opposingTHArm = null; //对面直行的Arm
				for(int p=0; p<extLinks.size(); p++)
				{
					if(extLinks.get(p).getBegin().equals(straightOut))
					{
						straightOutExtEnd = extLinks.get(p).getEnd();
						break;
					}
				}

				for(int p=0; p<extLinks.size(); p++)
				{
					String juncSOEE = straightOutExtEnd.getName();
					int index = juncSOEE.indexOf("@");
					String juncS = juncSOEE.substring(0,index);

					Vertex opposingTHBegin = extLinks.get(p).getBegin();
					String juncOpp = opposingTHBegin.getName();
					index = juncOpp.indexOf("@");
					String juncO = juncOpp.substring(0,index);

					if(juncS.equals(juncO))
					{
						opposingTHArm = extLinks.get(p).getEnd(); //找到对面直行的Arm
						break;
					}
				}


				//该对面arm存在直行边
				int armIndex = arms.indexOf(opposingTHArm);

				int armTHDir = -1;
				boolean armTH = false;
				Link armTHLink = null;

				for (int j = 0; j < armDirNum.get(armIndex); j++)
				{
					String dirS = armDir.get(armIndex).get(j).getDirection();//该arm的第j个转向

					if(dirS.equals("s") || dirS.equals("S")) //若为直行
					{
						armTH = true;
						armTHLink = armDir.get(armIndex).get(j);
						armTHDir = j;
						armTHFlow = demand[armIndex][j];
						break;
					}
				}

				if(armTH)//对面arm有直行边
				{
					////////////////////////////////////////////////////
					//1.        对于arm i j=S 对k求和                                               //
					////////////////////////////////////////////////////
					double opposingTHLane = 0.0f;
					for (int k = 0; k < armLanesNum.get(armIndex); k++)
					{
						//对于arm i j=S 对k求和
						double delta = signalTiming.getPermittedDelta()[armIndex][armTHDir][k];
						opposingTHLane += delta;
					}

					////////////////////////////////////////////////////
					//2. 对于有LT和TH且对面arm有TH的arm, 判断左转的类型  //
					////////////////////////////////////////////////////
					@SuppressWarnings("unused")
					double permSaturationFlow  = 0.0f; //permissive Saturation Flow of the LT
					if(leftTurnFlow > 240)
					{
						leftTurnLink.setDirection("L");
					}
					else if(opposingTHLane == 1 && (leftTurnFlow*armTHFlow) > 50000)
					{
						leftTurnLink.setDirection("L");
					}
					else if(opposingTHLane == 2 && (leftTurnFlow*armTHFlow) > 90000)
					{
						leftTurnLink.setDirection("L");
					}
					else if(opposingTHLane >= 3 && (leftTurnFlow*armTHFlow) > 110000)
					{
						leftTurnLink.setDirection("L");
					}
					else //LT Permissive
					{
						///////////////////////////////////////////////
						//3.      若左转是permissive的话                                    //
						///////////////////////////////////////////////
//						System.out.println("ARM: " + itemp + " 的左转是permissive！");
						leftTurnLink.setDirection("l");

						int row    = intLinks.indexOf(leftTurnLink);
						int column = intLinks.indexOf(armTHLink);
//						System.out.println("row: " + row + " column: " + column);

						// 修改冲突矩阵
						//										System.out.println("路口冲突矩阵:");
						//										System.out.println(foesMatrix);
						
						junc.modifyFoesMatrix(row, column, 0);
						junc.modifyFoesMatrix(column, row, 0);

//						System.out.println("路口冲突矩阵被修改:");
//						System.out.println(modFoesMatrix);
					}
				}		
			}
		}
	}
}
