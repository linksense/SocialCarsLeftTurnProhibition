/* --------------------------------------------------------------------
 * Junction.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and all Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 15.04.2016
 * 
 * Function:
 *           1.Junction视图元素组合
 *           
 */
package network;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;
import org.ujmp.core.objectmatrix.impl.DefaultDenseObjectMatrix2D;

import network.graph.Vertex;

import com.trolltech.qt.core.QRectF;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QGraphicsItemGroup;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QPen;
import com.trolltech.qt.gui.QStyleOptionGraphicsItem;
import com.trolltech.qt.gui.QWidget;

public class Junction extends QGraphicsItemGroup implements Cloneable
{
	//~Variables
	//--------------------------------------------------------------------------
	private String name = null;			//The name associated with this Junction 
    private String type = null;			//The type associated with this Junction
    private double xCoordinate = 0.0f;	//The x Axis Coordinate this Junction
    private double yCoordinate = 0.0f;	//The y Axis Coordinate this Junction
	
    //内部节点
	private Set<Vertex> internalNode = new HashSet<Vertex>();
	private List<Link> internalLinks = new ArrayList<Link>();  //路口内部边
	private List<Link> externalLinks = new ArrayList<Link>();  //路口外部边
	
	private List<ArrayList<String>> markerList = new ArrayList<ArrayList<String>>();//路口交通标识
	
	private List<ArrayList<String>> phaseList = new ArrayList<ArrayList<String>>();//路口交通灯
	private List<String> foesList = new ArrayList<String>();//路口交通冲突矩阵
	
	private Matrix foesMatrix = null;
	private Matrix modFoesMatrix = null;
	
	private Matrix phaseMatrixLaneBased    = null;
	private Matrix phaseDurationMatrix     = null;
	
	//邻接表,展示内部节点的连接关系
//	private Map<Vertex,LinkedHashSet<Vertex>> adjacencyMatrix = new HashMap<Vertex,LinkedHashSet<Vertex>>();
    
	private static final QPen QPEN_BLACK = new QPen(QColor.black, 0.01);
    
    private int radius = 1;
	
	//~Methods
	//--------------------------------------------------------------------------
	/**
	 * @param myName
	 */
	public Junction(String myName)
	{
		this.setName(myName);
		this.setToolTip(name);
	}
	
	/**
	 * @param myName
	 * @param myType
	 */
	public Junction(String myName, String myType)
	{
		this.setName(myName);
    	this.setType(myType);
		this.setToolTip(name);
	}
	
	/**
	 * @param myName
	 * @param x_coordinate
	 * @param y_coordinate
	 */
	public Junction(String myName, double x_coordinate, double y_coordinate)
	{
		this.setName(myName);
    	this.setX_Coordinate(x_coordinate);
    	this.setY_Coordinate(y_coordinate);
		this.setToolTip(name+" ["+x_coordinate+", "+y_coordinate+"]");
	}
	
	/**
	 * @param myName
	 * @param myType
	 * @param x_coordinate
	 * @param y_coordinate
	 */
	public Junction(String myName, String myType, double x_coordinate, double y_coordinate)
	{
		this.setName(myName);
    	this.setType(myType);
    	this.setX_Coordinate(x_coordinate);
    	this.setY_Coordinate(y_coordinate);
		this.setToolTip(name+" ["+x_coordinate+", "+y_coordinate+"]");
	}
	
	public void addInternalNode(Vertex node)
	{
		if(node != null)
		{
			node.setPos(node.getX_Coordinate(), node.getY_Coordinate()); //设置节点在Scene中的位置
			internalNode.add(node);
			this.addToGroup(node);
			
			int distance = (int) Math.sqrt((xCoordinate-node.getX_Coordinate())
									      *(xCoordinate-node.getX_Coordinate())
									      +(yCoordinate-node.getY_Coordinate())
									      *(yCoordinate-node.getY_Coordinate()));
			if(distance>radius)
			{
				radius = distance;
				this.update(boundingRect());
			}
		}
	}
	
	/**
	 * @param from
	 * @param to
	 */
//	public void addInternalEdge(Vertex from, Vertex to)
//	{
//		LinkedHashSet<Vertex> adjacent = adjacencyMatrix.get(from);
//		if (adjacent == null)
//		{
//			adjacent = new LinkedHashSet<Vertex>();
//			adjacencyMatrix.put(from, adjacent);
//		}
//		adjacent.add(to);
//	}
	
	/**
	 * @param internalLink
	 */
	public void addInternalEdge(Link internalLink)
	{
		if(internalLink != null)
		{
			internalLinks.add(internalLink);
		}
	}
	
	/**
	 * @param junc
	 * @param arms         路口的Arm
	 * @param armDir       与Arm相关的转向内部边
	 * @param armLanesNum  与Arm转向相关的车道数
	 * @param armDirNum    Arm的转向个数
	 * @param outs         驶出节点
	 * @param outLanesNum  驶出节点的车道数
	 */
	public void juncInfoClassification(Junction junc,List<Vertex> arms,
										 List<ArrayList<Link>> armDir,
										 List<Integer> armLanesNum,List<Vertex> outs,
										 List<Integer> outLanesNum)
	{
		//1. 获取当前的Junction
		/////////////////////////////////////////////////
		String juncName = junc.getName();
		//List<ArrayList<String>> markerList = junc.getMarkerList();//以lane为基础的转向，与内部连接顺序一致
		
		List<Link> intLinks    = junc.getInternalLinks(); //内部通路
		if(intLinks.size()==0)
		{
			System.out.println("当前路口:"+ juncName);
		}
		
		
		//3. 归类提取信息
		/////////////////////////////////////////////////
		//3.1 Arm 和 Out信息提取
		for(int j=0; j<intLinks.size(); j++) //找出所有的起始点 ARM
		{
			Link link    = intLinks.get(j);
			Vertex begin = link.getBegin();
			Vertex end   = link.getEnd();
			link = null;
			//arm,即为起始点, 则提取其转向内部通路
			if(arms.indexOf(begin) == -1)//若该点在arm里不存在，添加之
			{
				arms.add(begin);
				armLanesNum.add(begin.getLaneNum()); //记录此arm的Lane数目
			}

			if(outs.indexOf(end) == -1)//若该点在outs里不存在，添加之
			{
				outs.add(end);
				outLanesNum.add(end.getLaneNum()); //记录此outs的Lane数目
			}
		}

		//3.2 与Arm相关的信息提取
		for(int v=0; v<arms.size(); v++) //找出所有的起始点 ARM
		{
			ArrayList<Link> dir = new ArrayList<Link>();//存储当前内部点的转向内部边
			//ArrayList<String> dirLane = new ArrayList<String>(); //存储当前内部点的转向lane
			
			Vertex vertex = arms.get(v);
			
			//一个arm的dir link
			for(int j=0; j<intLinks.size(); j++) 
			{
				Link link = intLinks.get(j);
				Vertex begin = link.getBegin();

				if(begin.equals(vertex))
				{
					dir.add(link); //该ARM的转向
					
					//添加marker到arm
					//dirLane
				}
			}

			if(!dir.isEmpty()) //该点为arm,
			{
				armDir.add(dir); //添加其转向
			}
		}
	}
	
	/**
	 * 删除某条内部边
	 * 同时要对冲突矩阵和Phase矩阵进行相应的更改
	 * @param link
	 * @return
	 */
	public boolean removeInternalEdge(final Link link)
	{
		int index = internalLinks.indexOf(link);  //要删除的Link的ID
		if(index != -1) //如果该ID存在
		{
			//1. 从内部通路列表里将这条边删除
			//==================================================
			internalLinks.remove(index);
			
			//2. 从冲突矩阵当中删除与之相关的冲突信息
			//==================================================
			//if(foesMatrix == null)
			//{
			//	foesListToMatrix(); //获得冲突矩阵
			//}
			//System.out.println("路口"+name+"做删除操作前的冲突矩阵：");
			//System.out.println(foesMatrix);
			foesMatrix = foesMatrix.deleteRows(Ret.NEW, index);
			foesMatrix = foesMatrix.deleteColumns(Ret.NEW, index);
			
			//System.out.println("路口"+name+"做删除操作后的冲突矩阵：" + "删除index" + index);
			//System.out.println(foesMatrix);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean removeLeftTurnEdge(final Link link)
	{
		int index = internalLinks.indexOf(link);  //要删除的Link的ID
		if(index != -1) //如果该ID存在
		{
//			//==================================================
//			// 1. 从冲突矩阵当中删除与之相关的冲突信息
			//    冲突矩阵是以Dir为单位并且已经转换为从左到右
//			//==================================================
			foesMatrix = foesMatrix.deleteRows(Ret.NEW, index);     // 删除左转所在的行
			foesMatrix = foesMatrix.deleteColumns(Ret.NEW, index);  // 删除左转所在的列
//			//System.out.println("路口"+name+"做删除操作后的冲突矩阵：" + "删除index" + index);
//			//System.out.println(foesMatrix);
			
			//==================================================
			// 2. 判断该左转是否和其他转向共用车道
			//==================================================
			int indexLeftTurn = -1;  // Left Turn Index in markerList
			for(int i=0; i<=index; i++)
			{
				Link currLink = internalLinks.get(i);              // 当前的内部通路
				int currLinkLaneCount = currLink.getLaneCount();   // 当前内部通路的车道个数
				indexLeftTurn += currLinkLaneCount;                // 
			}
			
			int leftTurnLaneCount = link.getLaneCount();           // 左转的车道个数
			
			int adjacentLinkLaneIndex = indexLeftTurn - leftTurnLaneCount; // 临近内部通路的index
			
			Link adjacentLink = internalLinks.get(index-1);
			int adjacentLinkLaneCount = adjacentLink.getLaneCount();
			// String currDir = adjacentLink.getDirection();
			
			// ========================================== //
			// 1. 修改临近转向的车道数量
			// ========================================== //
			adjacentLink.setLaneCount(adjacentLinkLaneCount+leftTurnLaneCount);
			internalLinks.set(index-1, adjacentLink);
			
			// ========================================== //
			// 2. 修改markerList
			// ========================================== //
			ArrayList<String> currAdjacentLinkLane = markerList.get(adjacentLinkLaneIndex); //
			String fromLaneID = currAdjacentLinkLane.get(0);
			String toLaneID   = currAdjacentLinkLane.get(1);
			String currDir    = currAdjacentLinkLane.get(2);
			
			for(int i=0; i< leftTurnLaneCount; i++)
			{
				int currLeftTurnIndex = indexLeftTurn - i;
				ArrayList<String> currLeftTurn = markerList.get(currLeftTurnIndex); //
				currLeftTurn.set(0, fromLaneID);
				currLeftTurn.set(1, toLaneID);
				currLeftTurn.set(2, currDir);
				markerList.set(currLeftTurnIndex, currLeftTurn);
			}
			
			// ========================================== //
			// 3. 将左转Link从internalLinks中删除
			// ========================================== //
			internalLinks.remove(index);
			
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * @param from
	 * @param to
	 */
	public void addMarker(final String fromLI, final String toLI, final String dir)
	{
		if(!fromLI.isEmpty() && !toLI.isEmpty() && !dir.isEmpty())
		{
			ArrayList<String> marker = new ArrayList<String>();
			marker.add(fromLI);
			marker.add(toLI);
			marker.add(dir);
			markerList.add(marker);
		}
	}
	
	/**
	 * @param externalLink
	 */
	public void addExternalEdge(Link externalLink)
	{
		if(externalLink != null)
		{
			externalLinks.add(externalLink);
		}
	}
	
	/**
	 * @param from
	 * @param to
	 */
	public void addPhase(final String duration, final String state)
	{
		if(!duration.isEmpty() && !state.isEmpty())
		{
			ArrayList<String> phaseInfo = new ArrayList<String>();
			phaseInfo.add(duration);
			phaseInfo.add(state);
			phaseList.add(phaseInfo);
		}
	}
	
	/**
	 * @param foes
	 */
	public void addFoes(final String foes)
	{
		if(!foes.isEmpty())
		{
			String foesInfo = new String(foes);
			foesList.add(foesInfo);
		}
	}
	
	/**
	 * 
	 */
	public Boolean foesListToMatrix()
	{
		if(foesList.isEmpty())
		{
			System.out.println("## 冲突列表是空的无法转化…");
			return false;
		}
		
		// ======================================================== //
		// 冲突矩阵是以Dir为单位的！！！！！！！！！！！！！！！！！！
		// ======================================================== //
		foesMatrix = new DefaultDenseObjectMatrix2D(internalLinks.size(), internalLinks.size());
		int index = -1;
		for(int i=0; i<internalLinks.size(); i++)
		{
			int laneNum = internalLinks.get(i).getLaneCount();
			index += laneNum;
			
			String foesString = foesList.get(index);
			String[] foes = foesString.split("");
			foesString = null;
			
			int indexColumn = -1;
			for(int j=0; j<internalLinks.size(); j++)
			{
				int laneNumTemp = internalLinks.get(j).getLaneCount();
				indexColumn += laneNumTemp;
				
				foesMatrix.setAsString(foes[(foes.length-1)-indexColumn], i,j);
			}
			foes = null;
		}
		foesList = null;
		/*foesMatrix = new DefaultDenseObjectMatrix2D(foesList.size(), foesList.get(0).split("").length);
		for(int i=0; i<foesList.size(); i++)
		{
			String foesString = foesList.get(i);
			String[] foes = foesString.split("");
			for(int j=foes.length-1; j>=0 ; j--)
			{
				foesMatrix.setAsString(foes[j], i,foes.length-(j+1));
			}
		}*/
		return true;
	}
	
	/**
	 * 与lane相关的phase矩阵
	 */
	public Boolean phaseListToMatrixLaneBased()
	{
		if(phaseList.isEmpty())
		{
			System.out.println("## phase列表是空的无法转化… ");
			return false;
		}
		
		int phaseCount = phaseList.size();
		int laneCount  = phaseList.get(0).get(1).split("").length;
//		if(laneCount != phaseList.get(0).get(1).split("").length)
//		{
//			System.out.println("## phase矩阵的行数与列数不相等… ");
//			return false;
//		}
		
		phaseMatrixLaneBased = new DefaultDenseObjectMatrix2D(phaseCount,laneCount);
		for(int i=0; i<phaseList.size(); i++)
		{
			String phaseString = phaseList.get(i).get(1);
			String[] phases    = phaseString.split("");
			for(int j=phases.length-1; j>=0 ; j--)
			{
				phaseMatrixLaneBased.setAsString(phases[j], i,phases.length-(j+1));
			}
		}
		return true;
	}
	
	/**
	 * 与lane相关的phaseduration矩阵
	 */
	public Boolean createPhaseDurationMatrix()
	{
		if(phaseList.isEmpty())
		{
			System.out.println("## phase列表是空的无法转化… ");
			return false;
		}
		
		int phaseCount = phaseList.size();
//		System.out.println("Phase Duration Count: " + phaseCount);
//		System.out.println("Phase Duration List: "  + phaseList);
//		if(laneCount != phaseList.get(0).get(1).split("").length)
//		{
//			System.out.println("## phase矩阵的行数与列数不相等… ");
//			return false;
//		}
		
		phaseDurationMatrix = new DefaultDenseObjectMatrix2D(phaseCount,1);
		//System.out.println("Phase Duration Matrix: " + phaseDurationMatrix);
		for(int i=0; i<phaseCount; i++)
		{
			String phaseDuration = phaseList.get(i).get(0);
//			System.out.println(phaseDuration);
			phaseDurationMatrix.setAsString(phaseDuration,i,0);
		}
		return true;
	}
	
	
	/**
	 * 修改冲突矩阵
	 * @param row
	 * @param column
	 * @param value
	 */
	public void modifyFoesMatrix(int row, int column, int value)
	{
		//修改冲突矩阵
		modFoesMatrix.setAsInt(value, row, column);
	}
	
	public void initModFoesMatrix()
	{
		modFoesMatrix = null;
		modFoesMatrix = new  DefaultDenseObjectMatrix2D(internalLinks.size(), internalLinks.size());
		modFoesMatrix = foesMatrix.clone();
	}
	
	/* (non-Javadoc)
     * @see com.trolltech.qt.gui.QGraphicsItem#boundingRect()
     */
    @Override
	public QRectF boundingRect()
	{
		//TODO Auto-generated method stub
    	double adjust = 2;
    	QRectF boundingRect = new QRectF(-radius-adjust, -radius-adjust, 
    									2*radius+adjust, 2*radius+adjust);
		return boundingRect;
	}

	/* (non-Javadoc)
	 * @see com.trolltech.qt.gui.QGraphicsItem#paint(com.trolltech.qt.gui.QPainter, 
	 * 												 com.trolltech.qt.gui.QStyleOptionGraphicsItem, 
	 * 												 com.trolltech.qt.gui.QWidget)
	 */
	@Override
	public void paint(QPainter painter, QStyleOptionGraphicsItem option, QWidget widget)
	{
		//TODO Auto-generated method stub
		painter.setRenderHint(QPainter.RenderHint.Antialiasing, true); //Anti aliasing
		painter.setPen(Qt.PenStyle.NoPen);
        painter.setBrush(QColor.fromRgba(QColor.cyan.rgb() & 0x7fffffff));
        painter.drawEllipse(-radius, -radius, 2*radius, 2*radius);

        painter.setPen(QPEN_BLACK);
        painter.drawEllipse(-radius, -radius, 2*radius, 2*radius);
	}
	
	/* （非 Javadoc）
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone()
	{  
		Junction cloneJunction = null;
		try
		{
			cloneJunction = (Junction)super.clone();
		} 
		catch (CloneNotSupportedException e)
		{
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		return cloneJunction;  
	} 
	/****************************************************************
	 *******************Getter and Setter****************************
	 ****************************************************************/
	/**
	 * @return internalNode
	 */
	public Set<Vertex> getInternalNode()
	{
		return internalNode;
	}

	/**
	 * @param internalNode 
	 *		要设置的 internalNode
	 */
	public void setInternalNode(Set<Vertex> internalNode)
	{
		this.internalNode = internalNode;
	}

	/**
	 * @return adjacencyMatrix
	 */
//	public Map<Vertex, LinkedHashSet<Vertex>> getAdjacencyMatrix()
//	{
//		return adjacencyMatrix;
//	}

	/**
	 * @param adjacencyMatrix 
	 *		要设置的 adjacencyMatrix
	 */
//	public void setAdjacencyMatrix(Map<Vertex, LinkedHashSet<Vertex>> adjacencyMatrix)
//	{
//		this.adjacencyMatrix = adjacencyMatrix;
//	}

	/**
	 * @return name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name 
	 *		要设置的 name
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return type
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @param type 
	 *		要设置的 type
	 */
	public void setType(String type)
	{
		this.type = type;
	}

	/**
	 * @return xCoordinate
	 */
	public double getX_Coordinate()
	{
		return xCoordinate;
	}

	/**
	 * @param xCoordinate 
	 *		要设置的 xCoordinate
	 */
	public void setX_Coordinate(double xCoordinate)
	{
		this.xCoordinate = xCoordinate;
	}

	/**
	 * @return yCoordinate
	 */
	public double getY_Coordinate()
	{
		return yCoordinate;
	}

	/**
	 * @param yCoordinate 
	 *		要设置的 yCoordinate
	 */
	public void setY_Coordinate(double yCoordinate)
	{
		this.yCoordinate = yCoordinate;
	}

	/**
	 * @return internalLinks
	 */
	public List<Link> getInternalLinks()
	{
		return internalLinks;
	}

	/**
	 * @param internalLinks 
	 *		要设置的 internalLinks
	 */
	public void setInternalLinks(List<Link> internalLinks)
	{
		this.internalLinks = internalLinks;
	}

	/**
	 * @return phaseList
	 */
	public List<ArrayList<String>> getPhaseList()
	{
		return phaseList;
	}

	/**
	 * @param phaseList 
	 *		要设置的 phaseList
	 */
	public void setPhaseList(List<ArrayList<String>> phaseList)
	{
		this.phaseList = phaseList;
	}

	/**
	 * @return externalLinks
	 */
	public List<Link> getExternalLinks()
	{
		return externalLinks;
	}
	
	/**
	 * @return markerList
	 */
	public List<ArrayList<String>> getMarkerList()
	{
		return markerList;
	}

	/**
	 * @param markerList 
	 *		要设置的 markerList
	 */
	public void setMarkerList(List<ArrayList<String>> markerList)
	{
		this.markerList = markerList;
	}

	/**
	 * @return foesList
	 */
	public List<String> getFoesList()
	{
		return foesList;
	}

	/**
	 * @return modFoesMatrix
	 */
	public Matrix getModFoesMatrix()
	{
		return modFoesMatrix;
	}

	/**
	 * @param modFoesMatrix 
	 *		要设置的 modFoesMatrix
	 */
	public void setModFoesMatrix(Matrix modFoesMatrix)
	{
		this.modFoesMatrix = modFoesMatrix;
	}

	/**
	 * @param foesList 
	 *		要设置的 foesList
	 */
	public void setFoesList(List<String> foesList)
	{
		this.foesList = foesList;
	}

	/**
	 * @return foesMatrix
	 */
	public Matrix getFoesMatrix()
	{
		//if(foesMatrix == null)
		//{
			//生成冲突矩阵
		//	foesListToMatrix();
		//}
		return foesMatrix;
	}

	/**
	 * @param foesMatrix 
	 *		要设置的 foesMatrix
	 */
	public void setFoesMatrix(Matrix foesMatrix)
	{
		this.foesMatrix = foesMatrix;
	}
	
	/**
	 * @return phaseMatrixLaneBased
	 */
	public Matrix getPhaseMatrixLaneBased()
	{
		return phaseMatrixLaneBased;
	}

	/**
	 * @param phaseMatrixLaneBased 
	 *		要设置的 phaseMatrixLaneBased
	 */
	public void setPhaseMatrixLaneBased(Matrix phaseMatrixLaneBased)
	{
		this.phaseMatrixLaneBased = phaseMatrixLaneBased;
	}

	/**
	 * @return phaseDurationMatrix
	 */
	public Matrix getPhaseDurationMatrix()
	{
		return phaseDurationMatrix;
	}

	/**
	 * @param phaseDurationMatrix 
	 *		要设置的 phaseDurationMatrix
	 */
	public void setPhaseDurationMatrix(Matrix phaseDurationMatrix)
	{
		this.phaseDurationMatrix = phaseDurationMatrix;
	}
}
