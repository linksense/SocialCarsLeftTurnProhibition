package network.test;
/*package network.test;

import java.io.IOException;
import java.util.Iterator;

import network.graph.Vertex;
import network.Link;
import network.Junction;
import network.Network;
import network.exportlocalnetwork.ExportNetworkToExcel;
import network.exportlocalnetwork.ExportNetworkToXML;
import network.importnetwork.ImportSumoNetwork;
import network.importnetwork.SumoXmlAttributeMatch;

import org.ujmp.core.Matrix;
import org.dom4j.DocumentException;

public class NetworkStorage
{
	//~Variables
	//--------------------------------------------------------------------------	
	//Network to Storage
	private Network network = new Network();
	private ExportNetworkToXML exportXML = null;
	
	//~Methods
	//--------------------------------------------------------------------------
	public NetworkStorage()throws IOException
	{
		/////////////////////////////////////////////////////////////////
		//存储从XML文件中读取的信息到矩阵
		Matrix matrixJunction = null;
		Matrix matrixEdge = null;
		Matrix matrixConnection = null;
		//////////////////////////////////////////////////////////////////
		//Import XML to get information in the Network
		//final String xmlNetwork = "Test_network\\MySUMOFile.net.xml";
		final String xmlNetwork = "Test_network\\suedstadt.net.xml";
		final ImportSumoNetwork importXMLNetwork = new ImportSumoNetwork(xmlNetwork);
		
		//导入SUMO路网到矩阵中并进行相关运算与整合
		//--------------------------------------------------------------
		long importStartTime=System.currentTimeMillis();   //获取开始时间
		try
		{		
			//Junction Matrix
			matrixJunction = importXMLNetwork.elementAnalyser(SumoXmlAttributeMatch.JUNCTION);
			System.out.println(matrixJunction);				
			//Edge Matrix
			matrixEdge = importXMLNetwork.elementAnalyser(SumoXmlAttributeMatch.EDGE);
			System.out.println(matrixEdge);			
			//Connection Matrix
			matrixConnection = importXMLNetwork.elementAnalyser(SumoXmlAttributeMatch.CONNECTION);
		} 
		catch(DocumentException e)
		{
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}	
		connectionMatrixManage(matrixEdge,matrixConnection);   //整理连接矩阵
		System.out.println(matrixConnection);
		long importEndTime=System.currentTimeMillis(); //获取结束时间
		System.out.println("SUMO路网导入时间： " + (importEndTime-importStartTime) +"ms");
		
		//导出本地路网信息到本地文件
		//-----------------------------------------------
		long exportStartTime=System.currentTimeMillis();   //获取开始时间
		ExportNetworkToExcel.exportToXLSXFile(matrixJunction, "localnetwork\\Junction.xlsx", "Junction");
		ExportNetworkToExcel.exportToXLSXFile(matrixEdge, "localnetwork\\Edge.xlsx", "Edge");
		ExportNetworkToExcel.exportToXLSXFile(matrixConnection, "localnetwork\\Connection.xlsx", "Connection");
		long exportEndTime=System.currentTimeMillis(); //获取结束时间
		System.out.println("本地EXCEL路网导出时间： " + (exportEndTime-exportStartTime) +"ms");
		
		//存储网络信息到Network当中
		//------------------------------------------------
		//long exportXMLStartTime=System.currentTimeMillis();   //获取开始时间
		exportXML = new ExportNetworkToXML();
		addJunctionToNetwork(matrixJunction, matrixConnection);//添加路口到网络中
		addExternalLinkToNetwork(matrixEdge);                  //添加外部连接到路网中
		exportXML.createXMLFile("localnetwork\\localnetwork.xml");
		//long exportXMLEndTime=System.currentTimeMillis(); //获取结束时间
		//System.out.println("本地路网XML导出时间： " + (exportXMLEndTime-exportXMLStartTime) +"ms");
	}
	
	*//**
	 * 添加路口到路网中
	 * @param matrixJunction
	 * @param matrixConnection
	 *//*
	private void addJunctionToNetwork(final Matrix matrixJunction, final Matrix matrixConnection)
	{
		final long junctionMatrixRowCount = matrixJunction.getRowCount();
		final long connectionMatrixRowCount = matrixConnection.getRowCount();

		//从路口矩阵中获取信息存入路网中
		for(int i=0; i<junctionMatrixRowCount; i++)
		{
			int count = 0;		
			//当前要添加的路口
			//------------------------------------------------------------------------
			String junctionID = matrixJunction.getAsString(i,0);//路口名
			String junction_X = matrixJunction.getAsString(i,1);//路口X坐标
			String junction_Y = matrixJunction.getAsString(i,2);//路口Y坐标
			
			Junction junction = null;
			junction = new Junction(junctionID);						//设置路口名
			junction.setX_Coordinate(Double.parseDouble(junction_X));	//设置路口X坐标
			junction.setY_Coordinate(Double.parseDouble(junction_Y));	//设置路口Y坐标
			
			exportXML.addJunction();
			//System.out.println("---------Junction <" + matrixJunction.getAsString(i,0)+">------------");	
			//在连接矩阵中寻找所有的内部Link,并存入到路口的邻接表,路网的连接表中
			for(int j=0; j<connectionMatrixRowCount; j++)
			{
				final String junctionIdInConn = matrixConnection.getAsString(j,6);
				
				//确定某条连接属于某个路口，即获取属于当前路口的内部连接
				if(junctionIdInConn.equals(junctionID))
				{
					Vertex interFromNode = null;//内部连接的起点
					Vertex interToNode = null;  //内部连接的终点
					
					String internalFromNode = matrixConnection.getAsString(j,0);//起点名
					String internalToNode = matrixConnection.getAsString(j,3);  //终点名
					//System.out.println(internalFromNode + " -> " + internalToNode);

					//From Node 构建内部连接的起点
					//---------------------------------------------------------
					String interFromNode_X = matrixConnection.getAsString(j,1);//起点X坐标
					String interFromNode_Y = matrixConnection.getAsString(j,2);//起点Y坐标
					
					interFromNode = new Vertex(internalFromNode,"Internal",
												Double.parseDouble(interFromNode_X),
												Double.parseDouble(interFromNode_Y));			
					
					//To Node 构建内部连接的终点
					//---------------------------------------------------------
					String interToNode_X = matrixConnection.getAsString(j,4);//终点X坐标
					String interToNode_Y = matrixConnection.getAsString(j,5);//终点Y坐标
					
					interToNode = new Vertex(internalToNode,"Internal",
												Double.parseDouble(interToNode_X),
												Double.parseDouble(interToNode_Y));
					
					//Junction add internalLink 
					//添加内部连接到邻接表中,添加节点到路口
					//---------------------------------------------------
					junction.setType("Junction"); //设置类型
					junction.addInternalNode(interFromNode);
					junction.addInternalNode(interToNode);
					junction.addInternalEdge(interFromNode, interToNode);
					
					//Network add Link 
					//添加一条Link到路网当中
					//--------------------------------------------------
					Link link = null;
					link = new Link(interFromNode,interToNode);
					
					final String direction = matrixConnection.getAsString(j,8); //转向信息
					int index = network.getLinks().indexOf(link);
					if(index >= 0)
					{					
						int templaneCount = network.getLinks().get(index).getLaneCount();
						network.getLinks().get(index).setLaneCount(templaneCount+1);						
					}
					else
					{
						final String interLinkName = "InternalLink"+ count + "@" + junctionIdInConn;
						
						link.setName(interLinkName);
						link.setLinkType("Internal");
						link.setLaneCount(1);
						link.setDirection(direction);
						
						network.addLink(link);
						count++;
					}
					
					//添加内部link到xml中
					exportXML.addJunctionConnection(junctionIdInConn, 
													internalFromNode, 
													interFromNode_X, interFromNode_Y, 
													internalToNode, 
													interToNode_X, interToNode_Y, 
													direction);	
				}//找到一个当前路口的内部连接
			}//找到当前路口的所有内部连接，结束循环
			
			//是其他类型的路口
			//---------------------------------------------------
			if(junction.getAdjacencyMatrix().isEmpty())
			{
				//System.out.println("没有内部网络\n");
				junction.setType("Other_Junction"); //设置类型		
			}
			//System.out.println("Junction Type: " + junction.getType());
			
			exportXML.setJunctionAttribute(junctionID, "Junction", 
											junction_X, junction_Y, 
											junction.getInternalNode().toString());
			
			//Add a Junction to Network
			//添加当前的路口到路网中
			//---------------------------------------------------
			network.addJunction(junction);
		}//所有路口都已经处理完毕，循环结束
	}
	
	*//**
	 * 添加外部连接到路网
	 * @param matrixEdge
	 *//*
	private void addExternalLinkToNetwork(final Matrix matrixEdge)
	{
		final long edgeMatrixRowCount = matrixEdge.getRowCount();
		for(int i=0; i<edgeMatrixRowCount; i++)
		{			
			final String linkName = matrixEdge.getAsString(i,0);       //连接名
			final String linkFrom = matrixEdge.getAsString(i,1);       //起始路口
			final String linkTo = matrixEdge.getAsString(i,2);         //终止路口
			final String linkLaneCount = matrixEdge.getAsString(i,3);  //车道数量

			final String linkStartNode = matrixEdge.getAsString(i,6);  //起始点
			final String linkStartNode_X = matrixEdge.getAsString(i,7);//起始点X坐标
			final String linkStartNode_Y = matrixEdge.getAsString(i,8);//起始点Y坐标

			final String linkEndNode = matrixEdge.getAsString(i,9);    //终止点
			final String linkEndNode_X = matrixEdge.getAsString(i,10); //终止点X坐标
			final String linkEndNode_Y = matrixEdge.getAsString(i,11); //终止点Y坐标

			//连接的起点和终点
			//-------------------------------------------------------
			Vertex linkStartNodeVertex = null;//连接的起点
			linkStartNodeVertex = new Vertex(linkStartNode,"Internal",
												Double.parseDouble(linkStartNode_X),
												Double.parseDouble(linkStartNode_Y));
			
			Vertex linkEndNodeVertex = null;  //连接的终点
			linkEndNodeVertex = new Vertex(linkEndNode,"Internal",
											Double.parseDouble(linkEndNode_X),
											Double.parseDouble(linkEndNode_Y));
			
			//为OD路口设置内部节点
			//-------------------------------------------------------
			final Iterator<Junction> it = network.getJunctions().iterator();
			while(it.hasNext())
			{
				Junction junction = it.next();
				if(junction.getAdjacencyMatrix().isEmpty())
				{//没有内部连接的路口
					if(junction.getName().equals(linkFrom))
					{//添加内部节点
						junction.addInternalNode(linkStartNodeVertex);
					}
					if(junction.getName().equals(linkTo))
					{//添加内部节点
						junction.addInternalNode(linkEndNodeVertex);
					}
				}
			}
			
			//添加外部连接到路网
			//-----------------------------------------------------------
			Link link = new Link(linkStartNodeVertex, linkEndNodeVertex);
			link.setName(linkName);
			link.setLaneCount(Integer.parseInt(linkLaneCount));
			link.setLinkType("External");
			link.setDirection("s");
			
			exportXML.addExtLink();
			exportXML.addExtLinkConnection(linkStartNode, linkStartNode_X, linkStartNode_Y, 
												linkEndNode, linkEndNode_X, linkEndNode_Y);
			exportXML.setExtLinkAttribute(linkName, linkFrom, linkTo, linkLaneCount, "13.9", "1234");
			
			network.addLink(link);
		}
	}
	
	*//**
	 * Connection Matrix Management
	 * @param null
	 *//*
	private void connectionMatrixManage(final Matrix matrixEdge, final Matrix matrixConnection)
	{
		final long edgeMatrixRowCount = matrixEdge.getRowCount();
		final long connectionMatrixRowCount = matrixConnection.getRowCount();
		
		//将连接矩阵中边到边的信息改为点到点的信息
		for(int i=0; i<edgeMatrixRowCount; i++)
		{
			for(int j=0; j<connectionMatrixRowCount; j++)
			{
				String connFrom = matrixConnection.getAsString(j,0);
				String connTo = matrixConnection.getAsString(j,3);
				
				if(connFrom.equals(matrixEdge.getAsString(i,0)))
				{//替换起始边为起始点
					String toNodeID = matrixEdge.getAsString(i,2);
					String internalConnNode = matrixEdge.getAsString(i,9);
					String internalConnNode_x = matrixEdge.getAsString(i,10);
					String internalConnNode_y = matrixEdge.getAsString(i,11);
					
					matrixConnection.setAsObject(internalConnNode, j,0);
					matrixConnection.setAsObject(internalConnNode_x, j,1);
					matrixConnection.setAsObject(internalConnNode_y, j,2);
					matrixConnection.setAsObject(toNodeID, j,7);
				}
				if(connTo.equals(matrixEdge.getAsString(i,0)))
				{//替换终止边为终止点
					String fromoNodeID = matrixEdge.getAsString(i,1);
					String internalConnNode = matrixEdge.getAsString(i,6);
					String internalConnNode_x = matrixEdge.getAsString(i,7);
					String internalConnNode_y = matrixEdge.getAsString(i,8);
					
					matrixConnection.setAsObject(internalConnNode, j,3);
					matrixConnection.setAsObject(internalConnNode_x, j,4);
					matrixConnection.setAsObject(internalConnNode_y, j,5);
					matrixConnection.setAsObject(fromoNodeID, j,6);
				}
			}
		}
	}

	*//**
	 * @return network
	 *//*
	public Network getNetwork()
	{
		return network;
	}

	*//**
	 * @param network 
	 *		要设置的 network
	 *//*
	public void setNetwork(Network network)
	{
		this.network = network;
	}
}
*/