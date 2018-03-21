package gui;

import java.util.List;

import network.Link;
import network.graph.Vertex;

import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QPointF;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.*;

public class LocalNetworkItems extends QDialog
{
	//~Variables
	//--------------------------------------------------------------------------
	private Ui_LocalNetworkItems ui = new Ui_LocalNetworkItems();
	private List<Vertex> vertexs = null;
	private List<Link> links = null;
	private QStandardItemModel standardItemModel = null;
	
	public Signal1<Integer> itemSelected = new Signal1<Integer>();
	int itemIndex = 0;
	//~Methods
	//--------------------------------------------------------------------------
	/**
	 * 构造函数
	 */
	public LocalNetworkItems(List<Vertex> vertexs, List<Link> links)
	{
		ui.setupUi(this);
		this.vertexs = vertexs;
		this.links = links;
		itemsListShow();
	}

	/**
	 * 构造函数
	 */
	public LocalNetworkItems(QWidget parent, List<Vertex> vertexs, List<Link> links)
	{
		super(parent);
		ui.setupUi(this);
		this.vertexs = vertexs;
		this.links = links;
		itemsListShow();
	}
	
	private void itemsListShow()
	{
		standardItemModel = new QStandardItemModel(this);
		if(vertexs != null)
		{
			int nCount = vertexs.size();  
		    for(int i = 0; i < nCount; i++)  
		    {
		    	String name = vertexs.get(i).getName();
		    	QStandardItem item = new QStandardItem(name);
		    	if(i % 2 == 1)  
		        {  
		            QLinearGradient linearGrad = new QLinearGradient(new QPointF(0, 0), new QPointF(200, 200));  
		            linearGrad.setColorAt(0, QColor.lightGray);  
		            linearGrad.setColorAt(1, QColor.white);  
		            QBrush brush = new QBrush(linearGrad);  
		            item.setBackground(brush);  
		        }
		    	standardItemModel.appendRow(item);
		    }
		}
		else if(links != null)
		{
			int nCount = links.size();  
		    for(int i = 0; i < nCount; i++)  
		    {
		    	String name = links.get(i).getName();
		    	QStandardItem item = new QStandardItem(name);
		    	if(i % 2 == 1)  
		        {  
		            QLinearGradient linearGrad = new QLinearGradient(new QPointF(0, 0), new QPointF(200, 200));  
		            linearGrad.setColorAt(0, QColor.lightGray);  
		            linearGrad.setColorAt(1, QColor.white);  
		            QBrush brush = new QBrush(linearGrad);  
		            item.setBackground(brush);  
		        }
		    	standardItemModel.appendRow(item);
		    }
		}
		ui.itemsListView.setModel(standardItemModel);
	}
	
	@SuppressWarnings("unused")
	private void on_searchLineEdit_textChanged(String name)
	{
		List<QStandardItem>items = standardItemModel.findItems(name, 
										new Qt.MatchFlags(Qt.MatchFlag.MatchStartsWith));
		System.out.println(items);
		if(!items.isEmpty())
		{
			QModelIndex index = standardItemModel.indexFromItem(items.get(0));
			ui.itemsListView.setCurrentIndex(index);
		}
	}	
	
	@SuppressWarnings("unused")
	private void on_itemsListView_clicked(QModelIndex index)
	{
		itemIndex = index.row();
		System.out.println("选择的item是： " + itemIndex + "其值为： " + index.data().toString());
	}
	
	@SuppressWarnings("unused")
	private void on_centerButton_clicked()
	{
		itemSelected.emit(itemIndex);
	}
	
	@SuppressWarnings("unused")
	private void on_closeButton_clicked()
	{
		this.close();
	}
}
