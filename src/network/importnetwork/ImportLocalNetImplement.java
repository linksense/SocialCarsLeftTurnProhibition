/* --------------------------------------------------------------------
 * ImportLocalNetImplement.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and all Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 06.04.2016
 * 
 * Function:
 *           1.导入SUMO路网的执行类
 *           2.获得路口信息矩阵并对其信息进行整理
 *           3.获得道路信息矩阵并对其信息进行整理
 *           4.获得路口内部通路信息矩阵并对其信息进行整理
 *           5.获取源目标列表及其Demand矩阵
 *           
 */
package network.importnetwork;

import java.io.File;
import java.util.List;

import network.Junction;
import network.Link;
import network.Network;
import network.OD;
import network.ODMatrix;
import network.graph.Vertex;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class ImportLocalNetImplement
{
	//~Variables
	//--------------------------------------------------------------------------
	private Document document = null;  //XML被载入后存入Document
	private Network network   = null;  //建立新的网络，其中存储了路网的信息
	private ODMatrix odMatrix = null;  //OD Demand矩阵

	//~Methods
	//--------------------------------------------------------------------------
	/**
	 * 构造函数
	 * @param filename
	 */
	public ImportLocalNetImplement(final String filename,Network network,ODMatrix odMatrix)
	{
		this.network = network;
		this.odMatrix = odMatrix;
		loadXML(filename);  //载入本软件指定格式的XML路网文件
	}
	
	/**
	 * Load XML File, Read it into Document
	 * @param filename
	 * 		The Name of the file which will be analysis
	 */
	public Document loadXML(final String filename)
	{
		//Document document = null;
		try 
		{
			SAXReader saxReader = new SAXReader();
			File netFile = new File(filename);
			document = saxReader.read(netFile); //将路网读入Document中
			String networkName = netFile.getName();
			int pos = networkName.indexOf(".");
			if (pos > 0) 
			{
				networkName = networkName.substring(0, pos);
			}
			network.setNetworkName(networkName);
			networkName = null;
			saxReader = null;
		}
		catch (final Exception ex)
		{
			ex.printStackTrace();
		}
		return document;
	}
	
	/**
	 * 从载入的XML文件中提取信息以构建路网
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public void networkStorage()
	{
		Element root = document.getRootElement(); //获取XML文件的根节点
		
		//step 1.获取根节点下的路口节点列表，再通过这些路口节点获取路口内部通路信息
		List<Element> junctionList = root.selectNodes(LocalXmlAttributeMatch.JUNCTION); 	
		//step 2.获取根节点下的道路节点列表，再通过这些道路节点获取更多道路相关的信息信息
		List<Element> extLinkList = root.selectNodes(LocalXmlAttributeMatch.EXTLINK);	
		//step 3.获取根结点下的源目标节点列表
		List<Element> originList = root.selectNodes(LocalXmlAttributeMatch.ORIGIN);
		root = null;
		
		//step 1.添加路口到路网网络
		addJunctionToNetwork(junctionList); 
		junctionList = null;
		//step 2.添加道路到路网网络
		addExtLinkToNetwork(extLinkList);   
		extLinkList  = null;
		//step 3.添加源目标到路网网络
		addODToODMatrix(originList);        
		originList   = null;
		
		document = null;
		this.network =null;
		this.odMatrix = null;
		System.gc();
		return;
	}
	
	/**
	 * 添加路口及其内部通路相关信息到路网网络
	 * @param junctionList
	 * 			路口列表
	 */
	@SuppressWarnings("unchecked")
	private void addJunctionToNetwork(List<Element> junctionList)
	{
		int junctionListSize = junctionList.size();  //路口的个数
		System.out.println("Junction's count: " + junctionListSize); 
		
		for(int i = 0; i<junctionListSize; i++)     //逐个分析路口信息及其内部通路信息并将它们存入路网
		{
			Element element = junctionList.get(i);  //Get a Junction from the Junction List
			
			List<Attribute> listJunctionAttribute = element.attributes();  //获取当前路口节点的属性列表
			
			//生成一个新的路口
			//---------------------------------------------------
			String id 	   = listJunctionAttribute.get(0).getValue(); //当前路口的ID
			String type    = listJunctionAttribute.get(1).getValue(); //当前路口的类型
			String x       = listJunctionAttribute.get(2).getValue(); //当前路口的X坐标
			String y 	   = listJunctionAttribute.get(3).getValue(); //当前路口的Y坐标
			String intNode = listJunctionAttribute.get(4).getValue(); //当前路口的内部节点
			listJunctionAttribute = null;
			
			Junction junction = new Junction(id);		        //设置路口名
			junction.setType(type);     			            //设置路口类型
			junction.setX_Coordinate(Double.parseDouble(x));	//设置路口X坐标
			junction.setY_Coordinate(Double.parseDouble(y));	//设置路口Y坐标
			
			id   = null;
			type = null;
			x    = null;
			y    = null;
			//添加内部节点到路口
			//---------------------------------------------------
			//step 1.  处理路口内部节点列表字符串[x x x x x, x x x x x, x x x x x]
			intNode = intNode.replace("[", "");	     //把内部节点列表字符串中的‘[’去掉
			intNode = intNode.replace("]", "");	     //把内部节点列表字符串中的‘]’去掉
			String[]intNodes = intNode.split(", ");  //把内部节点列表字符串中的剩下的字符串用‘, ’分割获取节点数组
			intNode = null;
			//step 2.  向路口中添加内部节点
			for(int p=0; p<intNodes.length; p++)
			{//提取每个内部节点的信息
				String[] node = intNodes[p].split(" ");                //分割内部节点及其坐标信息
				
				Vertex interNode = new Vertex(node[0],"Internal",      //节点名和类型
									     Double.parseDouble(node[1]),  //节点X坐标
									     Double.parseDouble(node[2])); //节点Y坐标
				interNode.setSpeed(Double.parseDouble(node[3]));       //驶入节点或驶出节点的速度
				interNode.setLaneNum(Integer.parseInt(node[4]));
				
				node = null;
				junction.addInternalNode(interNode);                   //为路口添加一个内部节点
			}
			intNodes = null;
			//step 3.  添加内部连接到路口
			//-----------------------------------------------------
			List<Element> intLinkList = element.selectNodes(LocalXmlAttributeMatch.INTCONNECTION); //获取路口内部通路	
			for(int j = 0; j<intLinkList.size(); j++)
			{
				//step 1.获取内部通路属性
				List<Attribute> listIntLinkAttribute = intLinkList.get(j).attributes(); //获取内部通路属性列表
				String intLinkID        = listIntLinkAttribute.get(0).getValue();  //内部通路ID
				String intLinkNumLanes  = listIntLinkAttribute.get(1).getValue();  //内部通路车道数
				
				String intLinkFromLI    = listIntLinkAttribute.get(2).getValue();  //内部通路车道数
				String intLinkToLI      = listIntLinkAttribute.get(3).getValue();  //内部通路车道数
				
				String intLinkFrom      = listIntLinkAttribute.get(4).getValue();  //内部通路起点
				String intLinkFromX     = listIntLinkAttribute.get(5).getValue();  //内部通路起点X坐标
				String intLinkFromY     = listIntLinkAttribute.get(6).getValue();  //内部通路起点Y坐标
				String intLinkFromSpeed = listIntLinkAttribute.get(7).getValue();  //内部通路驶入速度
				String intLinkFromLane  = listIntLinkAttribute.get(8).getValue();  //内部通路驶入车道数
				
				String intLinkTo        = listIntLinkAttribute.get(9).getValue();  //内部通路终点
				String intLinkToX       = listIntLinkAttribute.get(10).getValue();  //内部通路终点X坐标
				String intLinkToY       = listIntLinkAttribute.get(11).getValue();  //内部通路终点Y坐标
				String intLinkToSpeed   = listIntLinkAttribute.get(12).getValue();  //内部通路驶出速度
				String intLinkToLane    = listIntLinkAttribute.get(13).getValue();  //内部通路驶出车道数
				
				String dir              = listIntLinkAttribute.get(14).getValue(); //内部通路转向信息
				listIntLinkAttribute = null;
				
				double fx = Double.parseDouble(intLinkFromX);      //内部通路起始内部节点X坐标
				double fy = Double.parseDouble(intLinkFromY);      //内部通路起始内部节点Y坐标
				double tx = Double.parseDouble(intLinkToX);        //内部通路终止内部节点X坐标
				double ty = Double.parseDouble(intLinkToY);        //内部通路终止内部节点Y坐标
				double length = Math.sqrt(((tx-fx)*(tx-fx) + (ty-fy)*(ty-fy)));//内部连接长度
				
				//step 2.生成内部通路的起止点
				Vertex interFromNode = new Vertex(intLinkFrom,"Internal",fx,fy); //内部通路起点
				interFromNode.setSpeed(Double.parseDouble(intLinkFromSpeed));    //驶入内部节点的速度
				interFromNode.setLaneNum(Integer.parseInt(intLinkFromLane));     //驶入内部节点的车道数
				
				
				Vertex interToNode = new Vertex(intLinkTo,"Internal",tx,ty);     //内部通路终点
				interToNode.setSpeed(Double.parseDouble(intLinkToSpeed));        //驶出内部节点的速度
				interToNode.setLaneNum(Integer.parseInt(intLinkToLane));         //驶出内部节点的车道数
				
				
				//step 3.添加内部通路到路口
//				junction.addInternalEdge(interFromNode, interToNode);            //添加内部通路到路口--邻接表
				String[] fromLaneC = intLinkFromLI.split(",");
				String[] toLaneC   = intLinkToLI.split(",");
				
				if(fromLaneC.length > 1 && toLaneC.length > 1 && fromLaneC.length == toLaneC.length)
				{
					for(int l=0; l<fromLaneC.length; l++)
					{
						junction.addMarker(fromLaneC[l], toLaneC[l], dir);
					}
				}
				else
				{
					junction.addMarker(intLinkFromLI, intLinkToLI, dir);
				}
				
				//step 4.将内部通路作为(类型为Internal的)道路并将其添加到路网网络
				double interLintSpeed = (Double.parseDouble(intLinkFromSpeed) + 
										 Double.parseDouble(intLinkToSpeed))/2;
				Link link = new Link(interFromNode,interToNode);      //生成一条道路
				link.setName(intLinkID);                              //设置道路的ID
				link.setLinkType("Internal");                         //设置道路类型
				link.setLaneCount(Integer.parseInt(intLinkNumLanes)); //设置道路车道
				link.setLength(length);                               //设置道路长度
				link.setSpeed(interLintSpeed);                        //设置道路速度
				link.setDirection(dir);                               //设置转向信息
				
				if(dir.equals("l") || dir.equals("L")) //若为左转
				{
					link.setSaturationFlow(1805);                    //设置饱和流
				}
				else if(dir.equals("s") || dir.equals("S")) //若为直行
				{
					link.setSaturationFlow(1900);                   //设置饱和流
				}
				else if(dir.equals("r") || dir.equals("R")) //若为右转
				{
					link.setSaturationFlow(1615);                   //设置饱和流
				}
				
				junction.addInternalEdge(link);                       //添加内部通路到路口
				network.addLink(link);                                //添加道路到路网网络
				
				intLinkID        = null;  //内部通路ID
				intLinkNumLanes  = null;  //内部通路车道数	
				intLinkFrom      = null;  //内部通路起点
				intLinkFromX     = null;  //内部通路起点X坐标
				intLinkFromY     = null;  //内部通路起点Y坐标
				intLinkFromSpeed = null;  //内部通路驶入速度
				intLinkFromLane  = null;  //内部通路驶入车道数	
				intLinkTo        = null;  //内部通路终点
				intLinkToX       = null;  //内部通路终点X坐标
				intLinkToY       = null;  //内部通路终点Y坐标
				intLinkToSpeed   = null;  //内部通路驶出速度
				intLinkToLane    = null;  //内部通路驶出车道数
				dir              = null; //内部通路转向信息
			}
			intLinkList = null;
			
			//step 4. 添加交通灯到路口
			//-----------------------------------------------------
			List<Element> phaseList = element.selectNodes(LocalXmlAttributeMatch.PHASE); //获取路口内部通路
			//ArrayList<String> logic = new ArrayList<String>();
			//logic.add(id);
			for(int j = 0; j<phaseList.size(); j++)
			{
				//step 1.获取phase属性
				List<Attribute> listPhaseAttribute = phaseList.get(j).attributes(); //获取内部通路属性列表
				String duration = listPhaseAttribute.get(1).getValue();
				String state    = listPhaseAttribute.get(2).getValue();
				listPhaseAttribute = null;
				junction.addPhase(duration, state);
				//logic.add(duration);
				//logic.add(state);
			}
			phaseList = null;
			//step 5. 添加交通灯冲突到路口
			//-----------------------------------------------------
			List<Element> requesList = element.selectNodes(LocalXmlAttributeMatch.REQUEST); //获取路口内部通路
			//ArrayList<String> juncFoes = new ArrayList<String>();
			//juncFoes.add(id);
			for(int j = 0; j<requesList.size(); j++)
			{
				//step 1.获取request属性
				String foes = requesList.get(j).attribute("foes").getValue();
				
				junction.addFoes(foes);
				//juncFoes.add(foes);
			}
			requesList = null;
			
			//step 6. 转化为冲突矩阵
			//-----------------------------------------------------
			if(!junction.getFoesList().isEmpty())
			{
				//System.out.println("冲突列表是空的无法转化…");
				junction.foesListToMatrix();
			}
			if(!junction.getPhaseList().isEmpty())
			{
				//System.out.println("冲突列表是空的无法转化…");
				junction.phaseListToMatrixLaneBased();
				junction.createPhaseDurationMatrix();
			}
			
			
			//step 7. 添加路口到网络
			//-----------------------------------------------------
			network.addJunction(junction); //添加路口到网络
//			if(phaseList.size() != 0)
//			{
//				network.addTlLogic(logic);
//				network.addFoes(juncFoes);
//				
//			}
		}
	}
	
	/**
	 * 添加(类型为External的)道路到路网网络中
	 * @param extLinkList
	 * 			(类型为External的)道路列表
	 */
	@SuppressWarnings("unchecked")
	private void addExtLinkToNetwork(List<Element> extLinkList)
	{
		int extLinkListSize = extLinkList.size();
		System.out.println("ExtLink's count: " + extLinkListSize); 
		for(int i = 0; i<extLinkListSize; i++)  //逐个分析(类型为External的)道路列表并将其信息存入路网网络
		{
			//Get a node from the node list
			Element element = extLinkList.get(i);                           //获取一个(类型为External的)道路
			
			List<Attribute> listExtLinkAttribute = element.attributes();    //道路的属性列表
			String id = listExtLinkAttribute.get(0).getValue();             //道路的名称
			String fromJunction = listExtLinkAttribute.get(1).getValue();   //起始路口
			String toJunction = listExtLinkAttribute.get(2).getValue();     //终止路口
			String numLanes = listExtLinkAttribute.get(3).getValue();       //道路的车道数
			String speed    = listExtLinkAttribute.get(4).getValue();       //道路的速度
			String length   = listExtLinkAttribute.get(5).getValue();       //道路的长度
			listExtLinkAttribute = null;
			listExtLinkAttribute = null;
			
			
			List<Element> extConnectionList = element.selectNodes(LocalXmlAttributeMatch.EXTCONNECTION); //外部通路信息
			//int numExtConnection = extConnectionList.size();	
			
			List<Attribute> listExtConnectionAttribute = extConnectionList.get(0).attributes();
			String fromNode = listExtConnectionAttribute.get(0).getValue();	//外部道路内部起点
			String from_x   = listExtConnectionAttribute.get(1).getValue(); //起点X坐标
			String from_y   = listExtConnectionAttribute.get(2).getValue(); //起点Y坐标
			String toNode   = listExtConnectionAttribute.get(3).getValue(); //外部道路内部终点
			String to_x     = listExtConnectionAttribute.get(4).getValue();	//终点X坐标
			String to_y     = listExtConnectionAttribute.get(5).getValue(); //终点Y坐标
			String dir      = listExtConnectionAttribute.get(6).getValue();	//转向信息
			listExtConnectionAttribute = null;
			extConnectionList = null;
			
			Vertex linkStartNodeVertex = new Vertex(fromNode,"Internal",     //外部道路起点
											    Double.parseDouble(from_x),  //外部道路起点X坐标
											    Double.parseDouble(from_y)); //外部道路起点Y坐标
			linkStartNodeVertex.setSpeed(Double.parseDouble(speed));         //外部道路速度
			linkStartNodeVertex.setLaneNum(Integer.parseInt(numLanes));      //外部道路车道数
			
			Vertex linkEndNodeVertex = new Vertex(toNode,"Internal",         //外部道路终点
										      Double.parseDouble(to_x),      //外部道路终点X坐标
										      Double.parseDouble(to_y));     //外部道路终点Y坐标
			linkEndNodeVertex.setSpeed(Double.parseDouble(speed));           //外部道路速度
			linkEndNodeVertex.setLaneNum(Integer.parseInt(numLanes));        //外部道路车道数
			
			//step1. 创建外部道路
			//-----------------------------------------------------------
			Link link = new Link(linkStartNodeVertex, linkEndNodeVertex);   //生成一条道路                        
			link.setName(id);                                               //道路ID
			link.setLaneCount(Integer.parseInt(numLanes));                  //道路车道数
			link.setLinkType("External");                                   //道路类型
			link.setSpeed(Double.parseDouble(speed));                       //道路速度
			link.setLength(Double.parseDouble(length));                     //道路长度
			link.setDirection(dir);                                         //转向信息
			
			fromNode = null;	//外部道路内部起点
			from_x   = null; //起点X坐标
			from_y   = null; //起点Y坐标
			toNode   = null; //外部道路内部终点
			to_x     = null;	//终点X坐标
			to_y     = null; //终点Y坐标
			dir      = null;	//转向信息

			//step2. 添加外部道路到路口
			//-----------------------------------------------------------
			for(int j=0; j<network.getJunctions().size(); j++)
			{
				Junction junc = network.getJunctions().get(j);
				if(junc.getName().equals(fromJunction))   //该道路的起始路口
				{
					junc.addExternalEdge(link); //将该道路添加到该路口
				}
				else if(junc.getName().equals(toJunction))//该道路的终止路口
				{
					junc.addExternalEdge(link); //将该道路添加到该路口
				}
			}
			
			id = null;             //道路的名称
			fromJunction = null;   //起始路口
			toJunction = null;     //终止路口
			numLanes = null;       //道路的车道数
			speed    = null;       //道路的速度
			length   = null;       //道路的长度
			//step3. 添加外部道路到路网
			//-----------------------------------------------------------
			network.addLink(link);                                          //添加(类型为External的)道路到路网
		}
	}

	/**
	 * 向OD Matrix 添加OD Demand
	 * @param originList
	 * 			起点列表
	 */
	@SuppressWarnings("unchecked")
	private void addODToODMatrix(List<Element> originList)
	{
		int originListSize = originList.size(); //行车路线起点列表大小
		System.out.println("Origin's count: " + originListSize); 
		
		for(int i = 0; i<originListSize; i++)
		{					
			Element element = originList.get(i);                  //获取一个行车路线起点
			String orgid   = element.attribute("id").getValue();  //获取该起点的ID
			String orgx    = element.attribute("x").getValue();   //获取该起点的X坐标
			String orgy    = element.attribute("y").getValue();   //获取该起点的Y坐标
			String orgLane = element.attribute("lane").getValue();//获取该起点的车道数
			
			List<Element> destinationList = element.selectNodes(LocalXmlAttributeMatch.DESTINATION);//与起点对应的终点
			element = null;
			
			for(int j = 0; j<destinationList.size(); j++)
			{
				Element destinaton = destinationList.get(j); //获取与行车路线起点对应的行车路线终点
				String desid   = destinaton.attribute("id").getValue();     //终点ID
				String desx    = destinaton.attribute("x").getValue();      //终点X坐标
				String desy    = destinaton.attribute("y").getValue();      //终点Y坐标
				String desLane = destinaton.attribute("lane").getValue();   //终点车道数
				String demand  = destinaton.attribute("demand").getValue(); //行车路线起点和终点之间的Demand
				destinaton = null;
				
				Vertex orgV = new Vertex(orgid,"Internal",            //行车路线的起点
						Double.parseDouble(orgx),                     //行车路线起点X坐标
						Double.parseDouble(orgy));                    //行车路线起点Y坐标
				orgV.setLaneNum(Integer.parseInt(orgLane));           //行车路线起点车道数
				
				Vertex desV = new Vertex(desid,"Internal",            //行车路线的终点
									Double.parseDouble(desx),         //行车路线终点X坐标
									Double.parseDouble(desy));        //行车路线终点Y坐标
				desV.setLaneNum(Integer.parseInt(desLane));           //行车路线终点车道数
				
				OD od = new OD(orgV,desV,Double.parseDouble(demand)); //生成一个OD
				odMatrix.addOD(od);                                   //将此OD添加至OD Matrix
				
				desid   = null;     //终点ID
				desx    = null;      //终点X坐标
				desy    = null;      //终点Y坐标
				desLane = null;   //终点车道数
				demand  = null; //行车路线起点和终点之间的Demand
			}
			destinationList = null;
			orgid   = null;  //获取该起点的ID
			orgx    = null;   //获取该起点的X坐标
			orgy    = null;   //获取该起点的Y坐标
			orgLane = null;//获取该起点的车道数
		}
	}
	
	
	@Override
	public void finalize()
	{           
        //super.finalize();  
        //System.out.println("####### ImportLocalNetImplement finalize method was called! #######");
    }
	
	/****************************************************************
	 *******************Getter and Setter****************************
	 ****************************************************************/
	/**
	 * @return network
	 */
	public Network getNetwork()
	{
		return network;
	}

	/**
	 * @return odMatrix
	 */
	public ODMatrix getODMatrix()
	{
		return odMatrix;
	}
}
