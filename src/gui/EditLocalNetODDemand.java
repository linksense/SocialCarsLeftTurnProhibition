package gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.gui.*;


public class EditLocalNetODDemand extends QDialog
{
	//~Variables
	//--------------------------------------------------------------------------
	Ui_EditLocalNetODDemand ui = new Ui_EditLocalNetODDemand();
	private String localNetwork = null;
	private Document document = null;
	
	private QStandardItemModel model = new QStandardItemModel(this);
	private int indexRow = 0;
	private int indexColumn = 0;
	
	public Signal3<String,String,String> odSelected = new Signal3<String,String,String>();
	//~Methods
	//--------------------------------------------------------------------------
	public EditLocalNetODDemand(final String localNetwork)
	{
		ui.setupUi(this);
		setLocalNetwork(localNetwork);
		
		loadOriginalXML(localNetwork);
		initialization();
	}

	public EditLocalNetODDemand(QWidget parent, final String localNetwork)
	{
		super(parent);
		ui.setupUi(this);
		setLocalNetwork(localNetwork);
		
		loadOriginalXML(localNetwork);
		initialization();
	}
	
	/**
	 * Load XML File, Read it into Document
	 * @param filename
	 * 		The Name of the file which will be analysis
	 */
	public void loadOriginalXML(final String filename)
	{
		try 
		{
			final SAXReader saxReader = new SAXReader();
			document = saxReader.read(new File(filename));
		}
		catch (final Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void initialization()
	{
		model.itemChanged.connect(this, "on_model_itemChanged(QStandardItem)");
		
		Element root = document.getRootElement();
		final List<Element> originList = root.selectNodes("origin"); //获取Origin
		
		for(int i=0; i<originList.size(); i++)
		{
			Element origin = originList.get(i);
			final String originID = origin.attribute("id").getValue();
			
			List<QStandardItem> items = new ArrayList<QStandardItem>();
			final List<Element> destinationList = origin.selectNodes("destination");
			
			for(int j=0; j<destinationList.size(); j++)
			{
				Element destination = destinationList.get(j);
				final String destinationID = destination.attribute("id").getValue();
				final String demand = destination.attribute("demand").getValue();
				
				//model.setHorizontalHeaderItem(j, new QStandardItem(tr(destinationID)));
				items.add(new QStandardItem(demand));
				
				if(i == 0)
					ui.hComboBox.addItem(destinationID);
			}
			model.appendRow(items);
			//model.setVerticalHeaderItem(i, new QStandardItem(tr(originID)));
			
			ui.vComboBox.addItem(originID);
		}
		
		//ui.odDemandTableView.horizontalHeader().setResizeMode(QHeaderView.ResizeMode.ResizeToContents);
		ui.odDemandTableView.setModel(model);
	}
	
	@SuppressWarnings("unused")
	private void on_odDemandTableView_clicked(QModelIndex index)
	{
		int row = index.row();
		int column =index.column();
		
		ui.vComboBox.setCurrentIndex(row);
		ui.hComboBox.setCurrentIndex(column);
		
		String origin = ui.vComboBox.itemText(row);
		String destination = ui.hComboBox.itemText(column);
		
		odSelected.emit(origin, destination, model.data(index).toString());
		//System.out.println(row+","+column+": "+model.data(index).toString());
	}
	
	@SuppressWarnings("unused")
	private void on_odDemandTableView_doubleClicked(QModelIndex index)
	{
		int row = index.row();
		int column =index.column();
		
		ui.vComboBox.setCurrentIndex(row);
		ui.hComboBox.setCurrentIndex(column);
		
		//System.out.println(row+","+column+": "+model.data(index));
	}
	
	@SuppressWarnings({ "unused", "unchecked" })
	private void on_model_itemChanged(QStandardItem item)
	{
		int row = item.index().row();
		int column = item.index().column();
		System.out.println(row+","+column+": "+model.data(item.index()));
		
		Element root = document.getRootElement();
		
		String origin = ui.vComboBox.itemText(row);
		String originString = "origin[@id='"+origin+"']";
		String destination = ui.hComboBox.itemText(column);
		String destinationString = "destination[@id='"+destination+"']";
		
		final List<Element> originList = root.selectNodes(originString); //获取Origin
		Element originElement = originList.get(0);
		final List<Element> destinationList = originElement.selectNodes(destinationString);
		Element destinationElement = destinationList.get(0);
		destinationElement.attribute("demand").setValue(model.data(item.index()).toString());
	}
	
	@SuppressWarnings("unused")
	private void on_vComboBox_currentIndexChanged(int row)
	{
		indexRow = row;
		setItemAsFocus();
	}
	
	@SuppressWarnings("unused")
	private void on_hComboBox_currentIndexChanged(int column)
	{
		indexColumn = column;
		setItemAsFocus();
	}
	
	private void setItemAsFocus()
	{
		QModelIndex index = model.index(indexRow, indexColumn);
		ui.odDemandTableView.setCurrentIndex(index);
	}
	
	@SuppressWarnings("unused")
	private void on_okButton_clicked() throws IOException
	{
		/*************************************************
		 **************将更改写入文档**********************
		 *************************************************/
		OutputFormat format = new OutputFormat("", false);
		format.setEncoding("UTF-8");//设置编码格式
		
		final String localNet = getLocalNetwork();
		List<String> localNetPaths = new ArrayList<String>();//要写入的文档
		
		if(localNet.contains(".localnet.xml"))
		{//如果选择的是原始的本地路网
			localNetPaths.add(localNet);
			String simplifiedLocalNet = localNet;
			simplifiedLocalNet = simplifiedLocalNet.replace(".localnet.xml", 
															".simplified.xml");
			localNetPaths.add(simplifiedLocalNet);//要写入的两个文档
		}
		else if(localNet.contains(".simplified.xml"))
		{//如果选择的是原始的本地路网
			localNetPaths.add(localNet);
			String originalLocalNet = localNet;
			originalLocalNet = originalLocalNet.replace(".simplified.xml", 
														".localnet.xml" );
			localNetPaths.add(originalLocalNet);//要写入的两个文档
		}
		//System.out.println(localNetPaths);
		for(int i=0; i<localNetPaths.size(); i++)
		{
			String fileToRewrite = localNetPaths.get(i);
			FileOutputStream outputStream = new FileOutputStream(fileToRewrite);
			XMLWriter xmlWriter = new XMLWriter(outputStream, format);
			xmlWriter.write(document);
			xmlWriter.close();
		}
		
		this.accept();
	}
	
	@SuppressWarnings("unused")
	private void on_cancelButton_clicked()
	{
		this.reject();
	}

	/**
	 * @return localNetwork
	 */
	public String getLocalNetwork()
	{
		return localNetwork;
	}

	/**
	 * @param localNetwork 要设置的 localNetwork
	 */
	public void setLocalNetwork(String localNetwork)
	{
		this.localNetwork = localNetwork;
	}
}