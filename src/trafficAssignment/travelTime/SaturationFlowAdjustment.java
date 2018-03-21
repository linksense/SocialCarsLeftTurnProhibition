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
 *
 */

package trafficAssignment.travelTime;

import java.util.ArrayList;
import java.util.List;

import network.Junction;
import network.Link;
import network.graph.Vertex;
import signalOptimization.LanebasedSinalTiming;

public class SaturationFlowAdjustment
{
	private LanebasedSinalTiming signalTiming=null;
	
	private List<Vertex> arms = new ArrayList<Vertex>();                      //所有的arms
//	private List<Vertex> outs = new ArrayList<Vertex>();                      //所有的outs
//	private List<Integer> outLanesNum = new ArrayList<Integer>();             //驶出点的车道数
//	private int [][] dirOutIndex;
//	private int armNumber; //Arm的个数
	private List<Integer> armDirNum = new ArrayList<Integer>();               //arm的转向数
	private List<ArrayList<Link>> armDir = new ArrayList<ArrayList<Link>>();  //每个ARM的转向内部通路
	private List<Integer> armLanesNum = new ArrayList<Integer>();             //arm的车道数

	private double[][] demand;
	private Junction junc;
	
	
//	private IloCplex cplex = null;
//	
//	public IloCplex getCplex()
//	{
//		return cplex;
//	}
//
//	/**
//	 * @param cplex 
//	 *		要设置的 cplex
//	 */
//	public void setCplex(IloCplex cplex)
//	{
//		this.cplex = cplex;
//	}
	
/*	public SaturationFlowAdjustment(Junction junc, LanebasedSinalTiming signalTiming, double[][] demand, 
			List<ArrayList<Link>> armDir,List<Vertex> arms,List<Integer> armDirNum)
	{
		this.junc = junc;
		this.signalTiming = signalTiming;
		this.demand =demand;
		this.armDir = armDir;
		this.arms = arms;
		this.armDirNum =armDirNum;
//		this.armNumber =armNumber;
//		this.dirOutIndex =dirOutIndex;
//		this.modFoesMatrix = modFoesMatrix;
//		
//		signalTiming = new LanebasedSinalTiming(cplex);
//		signalTiming.solveMe(armNumber,demand,dirOutIndex,modFoesMatrix,armDir,
//				armLanesNum,armDirNum,outLanesNum,extLinks);

	}*/

	// sumAssignedFlow有可能为0，这样laneSaturationFlow可能返回为NaN值！！需要在某处加入判断
	
	/*public double saturationFlowForLane(int armIndex,int laneIndex)
	{
		double laneSaturationFlow=0;
		double sumAssignedFlow=0;
		for (int j = 0; j < armDirNum.get(armIndex); j++)
		{
			//System.out.println("signalTiming.getAssignedFlow()[armIndex][j][laneIndex] in saturtaionFlowAdjustment: "+signalTiming.getAssignedFlow()[armIndex][j][laneIndex]);
			sumAssignedFlow += signalTiming.getAssignedFlow()[armIndex][j][laneIndex];
		}

		// 如果左转为permissive，需要修改saturation flow
		if(leftTurnTypeIndicator[]==0)
		saturationFlowPermissiveLT(int LTgreenDuration, int cycleLength,int leftTurnArmIndex,
					List<ArrayList<Link>> armDir,List<Vertex> arms,Junction junc);
		if(sumAssignedFlow!=0)
		for (int j = 0; j < armDirNum.get(armIndex); j++)
		{
			//原本assignedFlow与实际上的flow有比例关系，但是 assignedFlow与sumassignedFlow相除，抵消了这个比例关系
			laneSaturationFlow += (signalTiming.getAssignedFlow()[armIndex][j][laneIndex]*
					armDir.get(armIndex).get(j).getSaturationFlow())/sumAssignedFlow;
		}
		else
			laneSaturationFlow=0;
		
		return laneSaturationFlow;
	}
	
	//判断左转是否为 leading-lagging LT
	public double saturationFlowLeadingLaggingLT(int LTgreenDuration, int leftTurnArmIndex, double permSaturationFlow)
	{
		int saturationFlow=0;
		
		Link leftTurnLink = null;
		Link straihgtLink = null;

		int greenProtected=0;
		int leftTurnDirIndex=-1;
		int cycleLength =(int)signalTiming.getCycleLengthEpslon();
		@SuppressWarnings("unused")

		double armTHFlow = 0.0f;
		int oppArmIndex = -1;//对面直行的arm信息
		int oppTHDirIndex = -1;
		Link oppTHLink = null;
		double opposingTHFlow=0;
		
		for (int j = 0; j < armDirNum.get(leftTurnArmIndex); j++)
		{
			String dirS = armDir.get(leftTurnArmIndex).get(j).getDirection();//第 i个arm的第j个转向

			if(dirS.equals("l")) //若为permissive左转
			{
				leftTurnLink = armDir.get(leftTurnArmIndex).get(j);
				leftTurnDirIndex=j;
			}
			if(dirS.equals("s") || dirS.equals("S")) //若为直行
			{
				straihgtLink = armDir.get(leftTurnArmIndex).get(j);
			}
		}

		//该对面arm存在直行边
		oppArmIndex = opposingThroughArmIndex(leftTurnArmIndex);
		//System.out.println("LanebasedSignalTiming.java leftTurnArmIndex: "+leftTurnArmIndex+" oppArmIndex: "+ oppArmIndex);
		if(oppArmIndex!=-1&& LTgreenDuration>0)
		{
			for (int j = 0; j < armDirNum.get(oppArmIndex); j++)
			{
				String dirS = armDir.get(oppArmIndex).get(j).getDirection();//该arm的第j个转向

				if(dirS.equals("s") || dirS.equals("S")) //若为直行
				{
					oppTHLink = armDir.get(oppArmIndex).get(j); //对面直行的信息，link类型
					oppTHDirIndex = j; //对面直行的 movement index
					opposingTHFlow = demand[oppArmIndex][j]; //对面直行的flow
					break;
				}
			}
			
		
			int startGreenOTH = signalTiming.getStartOfGreenMovementTheta()[oppArmIndex][oppTHDirIndex];
			int durationGreenOTH = signalTiming.getDurationMovementPhi()[oppArmIndex][oppTHDirIndex];
			int endGreenOTH = startGreenOTH+durationGreenOTH;

			int startGreenLT = signalTiming.getStartOfGreenMovementTheta()[leftTurnArmIndex][leftTurnDirIndex];
			//int durationGreenLT = signalTiming.getDurationMovementPhi()[leftTurnArmIndex][leftTurnDirIndex];
			int endGreenLT = startGreenLT+LTgreenDuration;


			//Compound Phasing
			if(startGreenLT<endGreenOTH && startGreenOTH<startGreenLT && endGreenOTH<endGreenLT)      //Lagging
			{
				greenProtected = endGreenLT-endGreenOTH;
			}
			else if(startGreenOTH<endGreenLT && startGreenOTH>startGreenLT && endGreenOTH>endGreenLT)//Leading
			{
				greenProtected = startGreenOTH-startGreenLT;
			}
			else if(startGreenOTH>startGreenLT && endGreenOTH<endGreenLT)//Lagging&Leading
			{
				greenProtected = LTgreenDuration-durationGreenOTH;
			}

			// Have Leading/Lagging/Leading-Lagging
			if(greenProtected>0)
			{
				// 计算新的左转饱和流
				System.out.println("left turn is compound!");
				double greenPermissive = LTgreenDuration-greenProtected;
				double armTHSaturationFlow =oppTHLink.getSaturationFlow();
				double protSaturationFlow = 0.95*armTHSaturationFlow;
				saturationFlow = (int)Math.round((permSaturationFlow*greenPermissive+
						protSaturationFlow*greenProtected)/LTgreenDuration);

				leftTurnLink.setSaturationFlow((int)saturationFlow); //更新饱和流
				leftTurnLink = null;
			}
			else if(greenProtected == 0)
			{
				//Do Something
				saturationFlow = (int)permSaturationFlow;
				leftTurnLink.setSaturationFlow((int)permSaturationFlow);

			}
		}
		return saturationFlow;
	}
	
	public double saturationFlowPermissiveLT(int LTgreenDuration,int leftTurnArmIndex)///
	{
		double saturationFlow=0;
		Link leftTurnLink = null;
		Link straihgtLink = null;

		int cycleLength =(int)signalTiming.getCycleLengthEpslon();
		@SuppressWarnings("unused")

		double armTHFlow = 0.0f;
		int oppArmIndex = -1;//对面直行的arm信息
		int oppTHDirIndex = -1;
		Link oppTHLink = null;
		double opposingTHFlow=0;
		
		for (int j = 0; j < armDirNum.get(leftTurnArmIndex); j++)
		{
			String dirS = armDir.get(leftTurnArmIndex).get(j).getDirection();//第 i个arm的第j个转向

			if(dirS.equals("l")) //若为permissive左转
			{
				leftTurnLink = armDir.get(leftTurnArmIndex).get(j);
			}
			if(dirS.equals("s") || dirS.equals("S")) //若为直行
			{
				straihgtLink = armDir.get(leftTurnArmIndex).get(j);
			}
		}

		//该对面arm存在直行边
		oppArmIndex = opposingThroughArmIndex(leftTurnArmIndex);
		//System.out.println("LanebasedSignalTiming.java leftTurnArmIndex: "+leftTurnArmIndex+" oppArmIndex: "+ oppArmIndex);
		if(oppArmIndex!=-1&& LTgreenDuration>0)
		{
			for (int j = 0; j < armDirNum.get(oppArmIndex); j++)
			{
				String dirS = armDir.get(oppArmIndex).get(j).getDirection();//该arm的第j个转向

				if(dirS.equals("s") || dirS.equals("S")) //若为直行
				{
					oppTHLink = armDir.get(oppArmIndex).get(j); //对面直行的信息，link类型
					oppTHDirIndex = j; //对面直行的 movement index
					opposingTHFlow = demand[oppArmIndex][j]; //对面直行的flow
					break;
				}
			}
			
		
		double l_c=4.5;
		double l_f=2.5;
		double n_f = 1.5;
		double filteredSaturation=(opposingTHFlow/3600)*Math.exp(-opposingTHFlow*l_c/3600)
				/(1-Math.exp(-opposingTHFlow*l_f/3600)); //unit:veh/s
		double gu =Math.max(0, ((1.0*oppTHLink.getSaturationFlow()/3600)*LTgreenDuration 
				- (opposingTHFlow/3600)*cycleLength)/((1.0*oppTHLink.getSaturationFlow()/3600)- (opposingTHFlow/3600)));
		saturationFlow =(1.0*straihgtLink.getSaturationFlow()/3600)*(filteredSaturation*gu+n_f)/(LTgreenDuration);//unit: veh/s
		saturationFlow =3600*saturationFlow;//unit:veh/h
//		System.out.println("opposingTHFlow "+opposingTHFlow);
//		System.out.println("filteredSaturation "+filteredSaturation);
//		System.out.println("gu "+gu);
//		System.out.println("LTgreenDuration "+LTgreenDuration);
//		System.out.println("straihgtLink.getSaturationFlow() "+straihgtLink.getSaturationFlow());
//		
//		System.out.println("saturationFlow "+saturationFlow);
		}
		else
			saturationFlow=1900*0.95;//否则赋值为protected的饱和流
//		leftTurnLink.setSaturationFlow((int)saturationFlow);
		
		
		return saturationFlow;
	}
	
	// 左转对面直行的arm的index
	public int opposingThroughArmIndex(int leftTurnArmIndex)///
	{

		List<Link> extLinks    = junc.getExternalLinks(); //外部道路
		boolean Straight = false;
		boolean oppStraight=false;

		Link leftTurnLink = null;
		Link straihgtLink = null;

		@SuppressWarnings("unused")


		int oppArmIndex = -1;//对面直行的arm信息


		for (int j = 0; j < armDirNum.get(leftTurnArmIndex); j++)
		{
			String dirS = armDir.get(leftTurnArmIndex).get(j).getDirection();//第 i个arm的第j个转向

			if(dirS.equals("l")) //若为permissive左转
			{
				leftTurnLink = armDir.get(leftTurnArmIndex).get(j);
				//System.out.println(leftTurnArmIndex+ " left turn "+leftTurnLink);
			}
			if(dirS.equals("s") || dirS.equals("S")) //若为直行
			{
				straihgtLink = armDir.get(leftTurnArmIndex).get(j);
				//System.out.println(leftTurnArmIndex+ " strainght "+straihgtLink);
				Straight=true;
			}
		}

		if(Straight == true)//arm i 有LT 和 TH(S)
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
			oppArmIndex = arms.indexOf(opposingTHArm);//对面直行的arm信息
			//System.out.println("opposing arm index: "+ oppArmIndex);

			for (int j = 0; j < armDirNum.get(oppArmIndex); j++)
			{
				String dirS = armDir.get(oppArmIndex).get(j).getDirection();//该arm的第j个转向

				if(dirS.equals("s") || dirS.equals("S")) //若为直行
				{
					oppStraight = true; //左转存在对面直行
					break;
				}
			}
		}
		//System.out.println("oppStraight = true: "+ oppStraight);
		if(oppStraight == true)
			return oppArmIndex;
		else
			return -1;
	}
	
	public void finalize()
	{           
        //super.finalize();  
        //System.out.println("####### SaturationFlow finalize method was called! #######");
    }*/
	
	/**
	 * @return signalTiming
	 */
	public LanebasedSinalTiming getSignalTiming()
	{
		return signalTiming;
	}
	/**
	 * @param signalTiming 
	 *		要设置的 signalTiming
	 */
	public void setSignalTiming(LanebasedSinalTiming signalTiming)
	{
		this.signalTiming = signalTiming;
	}
	/**
	 * @return arms
	 */
	public List<Vertex> getArms()
	{
		return arms;
	}
	/**
	 * @param arms 
	 *		要设置的 arms
	 */
	public void setArms(List<Vertex> arms)
	{
		this.arms = arms;
	}
	/**
	 * @return armDirNum
	 */
	public List<Integer> getArmDirNum()
	{
		return armDirNum;
	}
	/**
	 * @param armDirNum 
	 *		要设置的 armDirNum
	 */
	public void setArmDirNum(List<Integer> armDirNum)
	{
		this.armDirNum = armDirNum;
	}
	/**
	 * @return armDir
	 */
	public List<ArrayList<Link>> getArmDir()
	{
		return armDir;
	}
	/**
	 * @param armDir 
	 *		要设置的 armDir
	 */
	public void setArmDir(List<ArrayList<Link>> armDir)
	{
		this.armDir = armDir;
	}
	/**
	 * @return armLanesNum
	 */
	public List<Integer> getArmLanesNum()
	{
		return armLanesNum;
	}
	/**
	 * @param armLanesNum 
	 *		要设置的 armLanesNum
	 */
	public void setArmLanesNum(List<Integer> armLanesNum)
	{
		this.armLanesNum = armLanesNum;
	}
	/**
	 * @return demand
	 */
	public double[][] getDemand()
	{
		return demand;
	}
	/**
	 * @param demand 
	 *		要设置的 demand
	 */
	public void setDemand(double[][] demand)
	{
		this.demand = demand;
	}
	/**
	 * @return junc
	 */
	public Junction getJunc()
	{
		return junc;
	}
	/**
	 * @param junc 
	 *		要设置的 junc
	 */
	public void setJunc(Junction junc)
	{
		this.junc = junc;
	}
	
}
