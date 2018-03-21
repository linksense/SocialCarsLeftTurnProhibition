/* --------------------------------------------------------------------
 * MainWindow.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and all Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 20.04.2016
 * 
 * Function:
 *           1.主窗口
 *           2.主线程
 *           
 */
package gui;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import leftTurnProhibition.GeneticAlgorithm;
import leftTurnProhibition.LeftTurnToProhibit;

import org.jgap.InvalidConfigurationException;
import org.ujmp.core.DenseMatrix;
import org.ujmp.core.Matrix;

import trafficAssignment.StochasticUserEquilibirum;

import com.trolltech.qt.QSignalEmitter;
import com.trolltech.qt.QThread;
import com.trolltech.qt.gui.*;
import com.trolltech.qt.core.QDir;
import com.trolltech.qt.core.Qt;

import network.Junction;
import network.Link;
import network.LinkPathMatrix;
import network.Network;
import network.OD;
import network.ODMatrix;
import network.algorithms.GetAllPathDFS;
import network.exportlocalnetwork.ExportLinkPathMatrix;
import network.exportlocalnetwork.ExportLocalNetworkImplement;
import network.exportlocalnetwork.ExportSimplifiedLocalNetwork;
import network.graph.Vertex;
import network.importnetwork.ImportLocalNetImplement;

public class MainWindow extends QMainWindow
{
	//~Variables
	//--------------------------------------------------------------------------
	private Ui_MainWindow ui = new Ui_MainWindow();

	protected String localNetworkXML = null;
	protected Network network = null;      //路网
	protected ODMatrix odMatrix = null;    //OD Demand矩阵
	protected List<Link> links  = null;    //道路
	protected List<Vertex> vertexs = null; //道路顶点
	
	private List<Link> leftTurnLinks = new ArrayList<Link>(); //左转内部通路
	
	QThread runner = null;
	PathSearchThread pathSearch = null;
	
	QGraphicsPixmapItem itemOrigin = null;
	QGraphicsPixmapItem itemDestination = null;
	int widthRatioLP = 0;
	int heightRatioLP = 0;
	
	//~Methods
	//--------------------------------------------------------------------------
	/**
	 * 构造函数
	 */
	public MainWindow()
	{
		ui.setupUi(this);
		initialization();
	}

	/**
	 * 构造函数
	 * @param parent
	 */
	public MainWindow(QWidget parent)
	{
		super(parent);
		ui.setupUi(this);
		initialization();
	}
	
	/**
	 * 初始化窗口
	 */
	private void initialization()
	{
		viewOperatorEnable(false);
		locateItemOperatorEnable(false);
		simulateOperatorEnable(false);
		
		ui.graphicsView.verticalScrollBar().setDisabled(true);
		ui.graphicsView.setRenderHint(QPainter.RenderHint.Antialiasing);
		ui.graphicsView.setDragMode(QGraphicsView.DragMode.ScrollHandDrag);
		ui.graphicsView.setViewportUpdateMode(QGraphicsView.ViewportUpdateMode.FullViewportUpdate);
	}
	
	private void viewOperatorEnable(boolean bool)
	{
		ui.actionZoomIn.setEnabled(bool);
		ui.actionZoomOut.setEnabled(bool);
		ui.actionAdjust.setEnabled(bool);
		ui.actionScreenshot.setEnabled(bool);
	}
	
	private void locateItemOperatorEnable(boolean bool)
	{
		ui.actionLocateJunctions.setEnabled(bool);
		ui.actionLocateVertex.setEnabled(bool);
		ui.actionLocateLink.setEnabled(bool);
		ui.actionLocateLeftTurnLinks.setEnabled(bool);
	}
	
	private void simulateOperatorEnable(boolean bool)
	{
		ui.actionRun.setEnabled(bool);
		ui.actionStop.setEnabled(bool);
		
	}
	
	/*****************************************************************
	 ************************ 路网转换 ********************************
	 *****************************************************************/
	@SuppressWarnings("unused")
	private void on_actionNetworkConvert_triggered()
	{
		NetworkConvert netConvert = new NetworkConvert(this);
		if(netConvert.exec() == QDialog.DialogCode.Accepted.value())
		{
			final String xmlSumoNetwork =  netConvert.getSumoNetXML();
			final String xmlOD = netConvert.getOdXML();
			final String xmlLocalNet = netConvert.getLocalNetXML();
			final String odDemandMatrix = netConvert.getOdDemandTXT();
			if(xmlSumoNetwork != null && xmlOD != null && xmlLocalNet != null)
			{
				long exportXMLStartTime=System.currentTimeMillis();//获取开始时间
				
				try
				{//导出本地XML路网
					ExportLocalNetworkImplement.ExportImplementer(xmlSumoNetwork, 
																		   xmlOD,
																  odDemandMatrix,
																	xmlLocalNet);
				}
				catch(IOException e)
				{
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
				
				long exportXMLEndTime=System.currentTimeMillis(); //获取结束时间
				long time = exportXMLEndTime-exportXMLStartTime;
				displayTime(time);   //显示时间
				ui.textBrowser.append("路网转化时间为: "+ time + "ms");
			}
		}
		netConvert.close();
	}
	
	/*****************************************************************
	 ************************ 简化本地路网 ****************************
	 *****************************************************************/
	@SuppressWarnings("unused")
	private void on_actionLocalNetworkSimplify_triggered()
	{
		LocalNetworkSimplify localNetSimplify = new LocalNetworkSimplify();
		if(localNetSimplify.exec() == QDialog.DialogCode.Accepted.value())
		{
			final String originalNet = localNetSimplify.getOriginalLocalNet();
			final String simplifiedNet = localNetSimplify.getSimplifiedLocalNet();
			
			if(originalNet != null && simplifiedNet != null)
			{
				long simplifyStartTime=System.currentTimeMillis();//获取开始时间
				
				ExportSimplifiedLocalNetwork snetwork = null;
				snetwork = new ExportSimplifiedLocalNetwork(originalNet,
															simplifiedNet);
				
				long simplifyEndTime=System.currentTimeMillis(); //获取结束时间
				long time = simplifyEndTime-simplifyStartTime;
				displayTime(time);   //显示时间
				ui.textBrowser.append("路网简化时间为: "+ time + "ms");
			}
		}
		localNetSimplify.close();
	}
	
	/*****************************************************************
	 ************************ 载入本地路网 ****************************
	 *****************************************************************/
	@SuppressWarnings("unused")
	private void on_actionLoadLocalNetwork_triggered()
	{
		LoadLocalNetwork loadLocalNetwork = new LoadLocalNetwork();
		if(loadLocalNetwork.exec() == QDialog.DialogCode.Accepted.value())
		{
			//---------------------------------------------------------------------//
			//-----------------------导入本地XML路网 和 OD信息-----------------------//
			//---------------------------------------------------------------------//
			//导入本地XML路网
			localNetworkXML = loadLocalNetwork.getLocalNetwork();
			
			if(localNetworkXML != null)
			{
				long storageStartTime = System.currentTimeMillis();   //获取开始时间
				network = new Network();
				odMatrix = new ODMatrix();
				
				ImportLocalNetImplement localNetwork = new ImportLocalNetImplement(localNetworkXML,network,odMatrix);
				localNetwork.networkStorage();
				localNetwork = null;
				network.linkRemoved.connect(this, "getLinkList()");
				
				links = network.getLinks();            //获取路网的所有连接
				vertexs = network.getInternalNodes();  //获取路网的所有节点
				
				//自适应显示路网到视图窗口
				//-------------------------------------------------------------
				ui.graphicsView.setScene(network);	//将路网添加到视图窗口
				on_actionAdjust_triggered();        //自适应显示路网
				
				viewOperatorEnable(true);           //使能视窗显示操作按钮
				locateItemOperatorEnable(true);     //使能本地路网Item查看选取按钮
				simulateOperatorEnable(true);       //使能仿真按钮
				ui.actionStop.setEnabled(false);
				
				//获取OD信息
				//-------------------------------------------------------------
				long storageEndTime=System.currentTimeMillis(); //获取结束时间
				long time = storageEndTime-storageStartTime;
				displayTime(time);  //显示时间
				
				//在输出窗口显示结果
				//-------------------------------------------------------------
				ui.textBrowser.clear();
				ui.textBrowser.append("本地路网读取时间为：  " + time + "ms");
				ui.textBrowser.append("ODMatrix结果为：");
				for(int i=0; i<odMatrix.getODMatrix().size(); i++)
				{			
					ui.textBrowser.append(odMatrix.getODMatrix().get(i).toString());
				}
				System.out.println("ODMatrix: "+odMatrix.getODMatrix());
				
//				System.out.println("tlLogic List "+network.getTlLogicList().size()+":"+network.getTlLogicList());
//				System.out.println("Foes List "+network.getFoesList().size()+":"+network.getFoesList());
			}
		}
		loadLocalNetwork.close();
	}
	
	@SuppressWarnings("unused")
	private void getLinkList()
	{
		//当连接被删除时可以通过获得的删除信号来更新连接List
		links = network.getLinks(); //获取路网的所有连接
		System.out.println("一条Link被删除, LinkList 被更新");
		System.out.println("更新后的LinkList大小为：" + links.size());
		ui.textBrowser.append("一条Link被删除, LinkList被更新");
	}
	
	/*****************************************************************
	 ************************ 视图放大 ********************************
	 *****************************************************************/
	@SuppressWarnings("unused")
	private void on_actionZoomIn_triggered()
	{
		ui.graphicsView.scale(2, 2);
	}
	
	/*****************************************************************
	 ************************ 视图缩小 ********************************
	 *****************************************************************/
	@SuppressWarnings("unused")
	private void on_actionZoomOut_triggered()
	{
		ui.graphicsView.scale(0.5, 0.5);
	}
	
	/*****************************************************************
	 ************************ 还原显示 ********************************
	 *****************************************************************/
	private void on_actionAdjust_triggered()
	{
		ui.graphicsView.resetTransform();
		double sceneWidth = network.sceneRect().width();
		double sceneHeight = network.sceneRect().height();
		
		int viewWidth = ui.graphicsView.width();
		int viewHeight = ui.graphicsView.height();
		double widthRatio = viewWidth/sceneWidth;
		double heightRatio = viewHeight/sceneHeight;
		
		if(widthRatio<=heightRatio)
			ui.graphicsView.scale(viewWidth/sceneWidth,viewWidth/sceneWidth);
		else
			ui.graphicsView.scale(viewHeight/sceneHeight,viewHeight/sceneHeight);
	}
	
	/*****************************************************************
	 ************************ 屏幕截图 *******************************
	 *****************************************************************/
	@SuppressWarnings("unused")
	private void on_actionScreenshot_triggered()
	{		
		String format = "png";
        String initialPath = QDir.currentPath() + "/screenshot" + tr("/untitled.") + format;
        String filter = String.format(tr("%1$s Files (*.%2$s);;All Files (*)"), 
        											      format.toUpperCase(), format);
        String fileName = QFileDialog.getSaveFileName(this, tr("Save As"), initialPath, 
        												new QFileDialog.Filter(filter));

        if (!fileName.equals(""))
        {
        	QPixmap pixmap = QPixmap.grabWidget(ui.graphicsView);
        	pixmap.save(fileName, format);
        }
	}
	
	/*****************************************************************
	 ************************ 查看节点 ********************************
	 *****************************************************************/
	@SuppressWarnings("unused")
	private void on_actionLocateVertex_triggered()
	{	
		LocalNetworkItems localNetItems = new LocalNetworkItems(this, vertexs, null);
		localNetItems.setWindowTitle("Local Network Vertexs");
		localNetItems.itemSelected.connect(this, "setVertexItemAsFocus(int)");
		localNetItems.show();
	}
	
	/*****************************************************************
	 ************************ 查看连接 ********************************
	 *****************************************************************/
	@SuppressWarnings("unused")
	private void on_actionLocateLink_triggered()
	{	
		LocalNetworkItems localNetItems = new LocalNetworkItems(this, null, links);
		localNetItems.setWindowTitle("Local Network Links");
		localNetItems.itemSelected.connect(this, "setLinkItemAsFocus(int)");
		localNetItems.show();
	}
	
	@SuppressWarnings("unused")
	private void setVertexItemAsFocus(int index)
	{
		ui.graphicsView.resetTransform();
		ui.graphicsView.centerOn(vertexs.get(index));
		
		double itemWidth = vertexs.get(index).boundingRect().width();
		double itemHeight = vertexs.get(index).boundingRect().height();
		
		int viewWidth = ui.graphicsView.width();
		int viewHeight = ui.graphicsView.height();
		double widthRatio = viewWidth/itemWidth;
		double heightRatio = viewHeight/itemHeight;
		
		if(widthRatio<=heightRatio)
			ui.graphicsView.scale(viewWidth/itemWidth, viewWidth/itemWidth);
		else
			ui.graphicsView.scale(viewHeight/itemHeight, viewHeight/itemHeight);
	}
	
	@SuppressWarnings("unused")
	private void setLinkItemAsFocus(int index)
	{
		ui.graphicsView.resetTransform();
		ui.graphicsView.centerOn(links.get(index));
		
		double itemWidth = links.get(index).boundingRect().width();
		double itemHeight = links.get(index).boundingRect().height();
		
		int viewWidth = ui.graphicsView.width();
		int viewHeight = ui.graphicsView.height();
		double widthRatio = viewWidth/itemWidth;
		double heightRatio = viewHeight/itemHeight;
		
		if(widthRatio<=heightRatio)
			ui.graphicsView.scale(viewWidth/itemWidth, viewWidth/itemWidth);
		else
			ui.graphicsView.scale(viewHeight/itemHeight, viewHeight/itemHeight);
	}
	
	/*****************************************************************
	 ************************* 左转禁止 *******************************
	 *****************************************************************/
	@SuppressWarnings("unused")
	private void on_actionLocateLeftTurnLinks_triggered()
	{
		// /////////////////////////////////////////////////
		// 测试代码
		//ArrayList<ArrayList<Link>> result = new ArrayList<ArrayList<Link>>(
		//		LeftTurnToProhibit.subsets(network.getLeftTurnLinks()));
		//System.out.println("左转个数:" + network.getLeftTurnLinks().size());
		//System.out.println("禁止情况:" + result.size());
		//for (int j = 0; j < result.get(25).size(); j++)
		//{
		//	System.out.println("要禁止的左转："
		//			+ result.get(25).get(j).getName());
		//	network.removeItem(result.get(25).get(j));
		//	network.removeLink(result.get(25).get(j));
			
		//	network.update();
		//}
		//System.out.println("左转剩余个数:"
		//		+ network.getLeftTurnLinks().size());
		//
		// /////////////////////////////////////////////////
		leftTurnLinks.clear();
		leftTurnLinks.addAll(network.getLeftTurnLinks()); //获取左转连接List
		
		LeftTurnLinksOperator leftTurn = new LeftTurnLinksOperator(this, leftTurnLinks);
		leftTurn.itemSelected.connect(this, "setLeftTurnLinkItemAsFocus(int)", 
									 		Qt.ConnectionType.QueuedConnection);
		leftTurn.itemForbidded.connect(this, "setLeftTurnLinkForbid(int)", 
									   		 Qt.ConnectionType.QueuedConnection);
		leftTurn.itemAllForbidded.connect(this, "setLeftTurnLinkAllForbid()", 
										  		Qt.ConnectionType.QueuedConnection);
		leftTurn.show();
	}
	
	private void setLeftTurnLinkItemAsFocus(int index)
	{
		ui.graphicsView.resetTransform();
		Link leftTurnLink = leftTurnLinks.get(index);
		if(leftTurnLink != null)
		{
			ui.graphicsView.centerOn(leftTurnLink);
			
			double itemWidth = leftTurnLink.boundingRect().width();
			double itemHeight = leftTurnLink.boundingRect().height();
			
			int viewWidth = ui.graphicsView.width();
			int viewHeight = ui.graphicsView.height();
			double widthRatio = viewWidth/itemWidth;
			double heightRatio = viewHeight/itemHeight;
			
			if(widthRatio<=heightRatio)
				ui.graphicsView.scale(viewWidth/itemWidth, viewWidth/itemWidth);
			else
				ui.graphicsView.scale(viewHeight/itemHeight, viewHeight/itemHeight);
		}
	}
	
	@SuppressWarnings("unused")
	private void setLeftTurnLinkForbid(int index)
	{
		if(!leftTurnLinks.isEmpty())
		{
			Link leftTurnLink = leftTurnLinks.get(index);
			if(leftTurnLink != null)
			{
				if(network.items().contains(leftTurnLink))
				{
					network.removeItem(leftTurnLink);
					network.removeLink(leftTurnLink);
					
					ui.textBrowser.append("Remove Link: " + leftTurnLink.getName() 
														  + ", 转向信息为: " 
														  + leftTurnLink.getDirection());
					network.update();
				}		
			}
			leftTurnLinks.remove(index);
		}
	}
	
	@SuppressWarnings("unused")
	private void setLeftTurnLinkAllForbid()
	{
		if(!leftTurnLinks.isEmpty())
		{
			for(int i=0; i<leftTurnLinks.size(); i++)
			{
				Link leftTurnLink = leftTurnLinks.get(i);
				if(leftTurnLink != null)
				{
					if(network.items().contains(leftTurnLink))
					{
						setLeftTurnLinkItemAsFocus(i);
						network.removeItem(leftTurnLink);
						network.removeLink(leftTurnLink);
						ui.textBrowser.append("Remove Link: " 
											  + leftTurnLink.getName() 
											  + ", 转向信息为: " 
											  + leftTurnLink.getDirection());
						network.update();
					}	
				}
			}
			leftTurnLinks.clear();
		}
	}
	
	/*****************************************************************
	 ************************ 编辑OD Demand **************************
	 *****************************************************************/
	@SuppressWarnings("unused")
	private void on_actionEditODDemand_triggered()
	{
		LoadLocalNetwork loadLocalNetwork = new LoadLocalNetwork();
		loadLocalNetwork.setWindowTitle("To Edit OD Matrix XML Local Network Select");
		
		if(loadLocalNetwork.exec() == QDialog.DialogCode.Accepted.value())
		{
			final String localNetPath = loadLocalNetwork.getLocalNetwork();
			if(localNetPath != null)
			{
				EditLocalNetODDemand editLocalNetODDemand = new EditLocalNetODDemand(this,localNetPath);
				
				QPixmap pixmap = new QPixmap("src\\resource\\png\\Location.png");
				double sceneWidth = network.sceneRect().width();
				double sceneHeight = network.sceneRect().height();
				
				widthRatioLP = (int) (sceneWidth/pixmap.width())*13;
				heightRatioLP = (int) (sceneHeight/pixmap.height())*13;
				//System.out.println(widthRatio+","+heightRatio);
				if(widthRatioLP<=heightRatioLP)	
					pixmap = pixmap.scaled(widthRatioLP, widthRatioLP, 
										   Qt.AspectRatioMode.IgnoreAspectRatio, 
										   Qt.TransformationMode.SmoothTransformation);
				else
					pixmap = pixmap.scaled(heightRatioLP, heightRatioLP, 
										   Qt.AspectRatioMode.IgnoreAspectRatio, 
										   Qt.TransformationMode.SmoothTransformation);
				itemOrigin = new QGraphicsPixmapItem(pixmap);
				itemDestination = new QGraphicsPixmapItem(pixmap);
				
				editLocalNetODDemand.accepted.connect(this, "odWindowClosed()");
				editLocalNetODDemand.rejected.connect(this, "odWindowClosed()");
				editLocalNetODDemand.odSelected.connect(this, 
						"setOriginDestinationAsFocus(String,String,String)");
				
				editLocalNetODDemand.show();
			}
		}
		loadLocalNetwork.close();
	}
	
	@SuppressWarnings("unused")
	private void setOriginDestinationAsFocus(final String originID, final String destinationID,
											 						final String demandS)
	{
		List<OD> odM = odMatrix.getODMatrix();
		for(int i=0; i<odM.size(); i++)
		{
			OD od = odM.get(i);
			Vertex origin = od.getOrigin();
			Vertex destination = od.getDestination();
			double demand = od.getDemand();
			
			if(originID.equals(origin.getName()) && destinationID.equals(destination.getName()))
			{
				if(network.items().contains(itemOrigin) && network.items().contains(itemDestination))
				{
					network.removeItem(itemOrigin);
					network.removeItem(itemDestination);
				}
				network.addItem(itemOrigin);
				itemOrigin.setPos(origin.getX_Coordinate()-widthRatioLP/2, 
								  origin.getY_Coordinate()-widthRatioLP);
				
				network.addItem(itemDestination);
				itemDestination.setPos(destination.getX_Coordinate()-widthRatioLP/2, 
									   destination.getY_Coordinate()-widthRatioLP);
				
				on_actionAdjust_triggered();
				network.update();
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void odWindowClosed()
	{
		if(network.items().contains(itemOrigin) && network.items().contains(itemDestination))
		{
			network.removeItem(itemOrigin);
			network.removeItem(itemDestination);
		}
		on_actionAdjust_triggered();
		network.update();
	}
	/*****************************************************************
	 ************************* 进行仿真 *******************************
	 *****************************************************************/
	@SuppressWarnings("unused")
	private void on_actionRun_triggered()
	{
		ui.actionStop.setEnabled(true);
		ui.actionRun.setEnabled(false);
		
		// Step 1. Select Algorithm
		// ---------------------------------------------------------------- //
		String leftTurnSetSelection = null;
		String signalTimingType     = null;
		boolean useMultiThread      = false;
		
		AlgorithmSelection algorithmSelection = new AlgorithmSelection();
		if(algorithmSelection.exec() == QDialog.DialogCode.Accepted.value())
		{
			leftTurnSetSelection = algorithmSelection.getLeftTurnSetSelection();
			signalTimingType     = algorithmSelection.getSignalTimingType();
			useMultiThread       = algorithmSelection.getUseMultiThread();
		}
		
		long storageStartTime=System.currentTimeMillis();   //获取开始时间
		// Step 2. LeftTurnSet 
		// ---------------------------------------------------------------- //
		System.out.println("左转个数:"+network.getLeftTurnLinks().size());
		// System.out.println("左转:"+network.getLeftTurnLinks());
		
		IloCplex cplex = null;
		try
		{
			cplex = new IloCplex();
		}
		catch(IloException e)
		{
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		
		if(leftTurnSetSelection.equals("ENUM"))
		{
			LeftTurnToProhibit leftTurnProh = new LeftTurnToProhibit();
			ArrayList<ArrayList<Link>> result = new ArrayList<ArrayList<Link>>(leftTurnProh.subsets(network.getLeftTurnLinks()));
			
			// System.out.println("禁止情况个数:"+result.size());
			// leftTurnProh = null;
			
			//循环禁止情况, 对于每一种禁止情况进行 SUE
			///////////////////////////////////////////////////////////////////
			double minTotalEmission=1E10;
			ArrayList<Link> minLTPsolution = null;
			for (int i = 0; i < result.size(); i++) 
			{
				System.out.println("禁止情况"+i+":\n"+result.get(i));
				//为每一种禁止情况建立一个新的临时路网
				//并在此基础上进行相关的运算
				Network networktemp = new Network();
				ODMatrix odMatrixTemp = new ODMatrix();
				
				ImportLocalNetImplement localNetworktemp = new ImportLocalNetImplement(localNetworkXML,networktemp,odMatrixTemp);
				localNetworktemp.networkStorage();
				localNetworktemp = null;

				//1. 从路网中移除某种禁止情况的所有的要禁止的边
				//   但是路口内部的边并没有被删除
				//   所以还需要对路口内部对应的边进行删除
				//-----------------------------------------------------
				for(int j=0; j<result.get(i).size(); j++)
				{
					//System.out.println("prohibited result "+j+": "+result.get(i).get(j).getName());
					//1. 在路网里删除要禁止的边
					networktemp.removeLink(result.get(i).get(j));
					
					//2. 在路口里删除要禁止的边
					//   并对冲突矩阵进行相应的修改
					List<Junction> junctions = networktemp.getJunctions();
					for(int k=0; k<junctions.size(); k++) //遍历所有的路口
					{
						Junction junc = junctions.get(k);
						
						boolean removeResult = junc.removeLeftTurnEdge(result.get(i).get(j));
						//System.out.println("markerList: "+junc.getMarkerList());
						if(removeResult == true)
						{
							//System.out.println("在"+junc.getName()+"删除一条左转边成功!");
							break;
						}
						
						junc = null;
					}
					junctions = null;
				}
				//System.out.println("左转剩余个数:"+networktemp.getLeftTurnLinks().size());
				//System.gc();
			
				//2. 计算SUE
				//------------------------------------------------------
				StochasticUserEquilibirum sueBPR = new StochasticUserEquilibirum(networktemp, odMatrixTemp,cplex, "BPR"); // , "StageBased"
				try
				{
					sueBPR.algorithmMSA();
				}
				catch(IOException e)
				{
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
//				System.out.println("SUE with BPR: ");
				
				int[] SUELinkFlow = new int[sueBPR.getLinkFlowAllOD().length];
				int temp=0;
				for(int j=0;j<sueBPR.getLinkFlowAllOD().length;j++)
				{
					SUELinkFlow[j] = (int)Math.round(sueBPR.getLinkFlowAllOD()[j]);
//					System.out.println(Math.round(sueBPR.getLinkFlowAllOD()[j]));
					temp+=SUELinkFlow[j];
				}
				
				if(temp!=0)
				{
					StochasticUserEquilibirum sue = new StochasticUserEquilibirum(networktemp, odMatrixTemp, cplex, SUELinkFlow, signalTimingType);
					try
					{
						sue.algorithmMSA();
					}
					catch(IOException e)
					{
						// TODO 自动生成的 catch 块
						e.printStackTrace();
					}
					
					System.out.println("SUE with stage-based: ");
					double sumTT=0;
					for(int j=0;j<sueBPR.getLinkFlowAllOD().length;j++)
					{
						//System.out.println(Math.round(sueStagebased.getLinkFlowAllOD()[j]));
		//				System.out.println(sueStagebased.getTravelTime()[j]);
						sumTT+=Math.round(sue.getLinkFlowAllOD()[j])*(sue.getTravelTime()[j]);
					}
					System.out.println("Total travel time: "+sumTT);	
					
					if(sumTT<minTotalEmission)
					{
						minTotalEmission=sumTT;
						// minLTPsolution[0]=result.get(i).get(0).getName();
						// System.out.println("## LTP "+minLTPsolution[0]);
						minLTPsolution = new ArrayList<Link>(result.get(i));	
						//System.out.println("## LTP "+minLTPsolution);
					}
					
					sue.setNetwork(null);
					sue.setOdMatrix(null);
					sue.setCplex(null);
					sue = null;
				}
				// Output result by SUE
				sueBPR.setNetwork(null);
				sueBPR.setOdMatrix(null);
				sueBPR.setCplex(null);
				sueBPR = null;
				
				odMatrixTemp.setOdMatrix(null);
				odMatrixTemp = null;
				//System.gc();
				networktemp.setNetworkName(null);
				networktemp.setAdjacencyMatrix(null);
				networktemp.setJunctions(null);
				networktemp.setLeftTurnLinks(null);
				networktemp.setLinks(null);
				networktemp.setGraph(null);
				networktemp.setLinks(null);
				networktemp = null;	
				
			}
			System.out.println("## The minimum total emission: "+minTotalEmission);
			System.out.println("## Left turn prohibition solution: "+minLTPsolution);
			//最优解情况下的平均degree of saturation
			long storageEndTime=System.currentTimeMillis(); //获取结束时间
			System.out.println("Total running time: " + (storageEndTime-storageStartTime)/60000 +"min");
			result = null;
		}
		else if(leftTurnSetSelection.equals("GA"))
		{
			try
			{
				GeneticAlgorithm ga = new GeneticAlgorithm(network,odMatrix,localNetworkXML, signalTimingType);
			} 
			catch (InvalidConfigurationException e)
			{
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
		else
		{
			
		}
	}
	
	@SuppressWarnings("unused")
	private void showTextInTextBrowser(final String text)
	{
		ui.textBrowser.append(text);
	}
	
	@SuppressWarnings("unused")
	private void on_actionStop_triggered()
	{
		if(runner != null && runner.isAlive())
		{
			System.out.println("线程  "+runner.toString()+" 将被强制结束");
			pathSearch.stopThread();
		}
		
	}
	/*****************************************************************
	 ************************ 鼠标事件 ********************************
	 *****************************************************************/
	protected void wheelEvent(QWheelEvent event)  //滚轮事件
	{
	   if(event.delta() < 0)  //如果鼠标滚轮远离使用者，则delta()返回正值
		   ui.graphicsView.scale(0.5,0.5);  //视图缩放
	   else
		   ui.graphicsView.scale(2,2);
	}
	
	protected void mousePressEvent(QMouseEvent event)
	{
		/*
		if(event.button() == Qt.MouseButton.RightButton)
			ui.graphicsView.rotate(90);  //视图旋转顺时针90度
		 */
		if(event.button() == Qt.MouseButton.RightButton)
		{
			//System.out.println(network.itemAt(event.posF()).toString());
		}
	}
	
	/*****************************************************************
	 ************************ 辅助方法 ********************************
	 *****************************************************************/
	private void displayTime(final long time)
	{
	    long hour = time/3600000;
	    String hourStr;
	    if( hour < 10)
	        hourStr = "0" + String.valueOf(hour);
	    else
	        hourStr = String.valueOf(hour);

	    long min = (time%3600000)/60000;
	    String minStr;
	    if(min < 10)
	        minStr = "0" + String.valueOf(min);
	    else
	        minStr = String.valueOf(min);

	    long sec = (time%3600000)%60000/1000;
	    String secStr;
	    if(sec < 10)
	        secStr = "0" + String.valueOf(sec);
	    else
	        secStr = String.valueOf(sec);
	    
	    long msec = ((time%3600)%60000)%1000;
	    String msecStr;
	    if(msec < 10)
	    	msecStr = "00" + String.valueOf(msec);
	    else if(msec < 100)
	    	msecStr = "0" + String.valueOf(msec);
	    else
	    	msecStr = String.valueOf(msec);
	    
	    String display = hourStr + ":" + minStr + ":" + secStr + ":" + msecStr;
	    ui.lcdNumber.display(display);
	}
	
	/**
	 * 关于Jambi 
	 */
	@SuppressWarnings("unused")
	private void on_actionAboutJambi_triggered()
	{
		QApplication.aboutQtJambi();
	}
	
	/**
	 * 关于本软件
	 */
	@SuppressWarnings("unused")
	private void on_actionAboutThisSoftware_triggered()
	{
		QMessageBox.about(this,
                tr("About Application"),
                tr("This <b>Application</b> is a road traffic simulation software"));
	}
	
	/**
	 * 退出软件 
	 */
	@SuppressWarnings("unused")
	private void on_actionQuit_triggered()
	{
		if(QMessageBox.information(this, tr("提示信息"), tr("您确定要退出吗?"),
		   QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No) == QMessageBox.StandardButton.Yes)
		{
			if(runner != null && runner.isAlive())
			{
				System.out.println("线程  "+runner.toString()+" 将被强制结束");
				pathSearch.stopThread();
			}
			QApplication.closeAllWindows();
		}
	}

	/**********************************************************************************
	 ****************************** Main函数 程序入口 ***********************************
	 **********************************************************************************/  
	public static void main(String[] args)
	{
		QApplication.initialize(args);

		MainWindow testMainWindow = new MainWindow();
		testMainWindow.show();

		QApplication.exec();
	}
}

/*****************************************************************
 ************************ 路径搜索 ********************************
 *****************************************************************/
class PathSearchThread extends QSignalEmitter implements Runnable 
{
	//~Variables
	//--------------------------------------------------------------------------
	private Network network = null;
	private ODMatrix odMatrix = null;
	
	private volatile boolean shutdownRequested = false;
	
	public Signal1<String> stringBildCompleted = new Signal1<String>();
	public Signal1<Long> timeCalculated = new Signal1<Long>();
	//~Methods
	//--------------------------------------------------------------------------
	public PathSearchThread(Network network, ODMatrix odMatrix)
	{
		this.network = network;
		this.odMatrix = odMatrix;
	}
	
    public void run()
    {
		try
		{
			while(!shutdownRequested)
			{
				System.out.println(Thread.currentThread().getName() + " is running..");  
				try
				{
					doWork();
				} 
				catch (IOException e)
				{
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			}
		} 
		catch(InterruptedException e)
		{
			e.printStackTrace(); 
		}
		finally
		{
			System.out.println(Thread.currentThread().getName() + " is exiting under request.");
			shutdownRequested = false;
		}
    }
    
    private void doWork() throws InterruptedException, IOException
    {
    	ExportLinkPathMatrix exportLinkPath = new ExportLinkPathMatrix(); //导出LinkPath矩阵
    	List<OD> odM = odMatrix.getODMatrix();	
		for(int i=0; i<odM.size(); i++)
		{
			Vertex org = odM.get(i).getOrigin();      //获取当前源点
			Vertex des = odM.get(i).getDestination(); //获取当前目标点
			double demand = odM.get(i).getDemand();   //获取当前源目标点之间的Demand
			if(demand != 0)                           //若是一个合法的源目标对
			{
				StringBuilder sb = new StringBuilder("");
				System.out.println(org);
				System.out.println(des);
				sb.append("Origin:      " + org + "\n");
				sb.append("Destination: " + des + "\n");
				
				//Step1.深度优先搜索寻找两点之间的所有路径
				//---------------------------------------------------------------------
				long pathStartTime=System.currentTimeMillis();   //获取开始时间	
				
				List<ArrayList<Vertex>> paths = new ArrayList<ArrayList<Vertex>>();   //路径
				List<Vertex> visited = new ArrayList<Vertex>();
				GetAllPathDFS allPath = new GetAllPathDFS();
				allPath.DepthFirstRecursive(network, visited, paths, org, des); //深度优先搜索寻找所有路径	
				System.out.println("DepthFirstRecursive' Size:  "+ paths.size());
				sb.append("DepthFirstRecursive' Size:  " + paths.size() + "\n");
				
				/*if(paths.size() != 0)
				{
					for(int j=0; j<paths.size(); j++)
					{
						System.out.println("Path: "+paths.get(j));
						//sb.append("Path: "+paths.get(j)+"\n");
					}
				}*/
				
				long pathEndTime=System.currentTimeMillis();   //获取结束时间
				long time = pathEndTime-pathStartTime;
				
				System.out.println("搜索路径时间: " + time + "ms");
				sb.append("搜索路径时间: " + time + "ms" + "\n");
				timeCalculated.emit(time);
				
				//Step2.生成LinkPath矩阵并导出之
				//---------------------------------------------------------------------
				long matrixStartTime=System.currentTimeMillis();   //获取开始时间
				
				exportLinkPath.addOD();
				exportLinkPath.setODAttribute(org.getName(), des.getName());
				List<ArrayList<Double>> lengthTimes = new ArrayList<ArrayList<Double>>();	
				Matrix linkPathM = DenseMatrix.Factory.zeros(network.getLinks().size(), paths.size());
				
				LinkPathMatrix linkPathMatrix = new LinkPathMatrix();
				linkPathMatrix.linkPathMatrix(network.getLinks(),paths,linkPathM, lengthTimes,exportLinkPath);
				
				long matrixEndTime=System.currentTimeMillis();     //获取结束时间
				System.out.println("连接路径矩阵计算时间: " + (matrixEndTime-matrixStartTime)+"ms");
				sb.append("连接路径矩阵计算时间: " + (matrixEndTime-matrixStartTime) +"ms" + "\n");
				timeCalculated.emit((matrixEndTime-matrixStartTime));
				
				if(paths.size() != 0)
				{
					System.out.println("LinkPathMatrix: \n"+linkPathM.toString()+"\n");
					//sb.append("LinkPathMatrix: \n"+linkPathM.toString()+"\n");
				}
				stringBildCompleted.emit(sb.toString());
			}
		}
		exportLinkPath.createXMLFile(network.getNetworkName());
		shutdownRequested = true;
		System.out.println("all things had been done!!"); 
    }
    
    public void stopThread()
    {
    	shutdownRequested = true;
    	Thread.currentThread().interrupt();
    	try
		{
			Thread.currentThread().join();
		} catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}