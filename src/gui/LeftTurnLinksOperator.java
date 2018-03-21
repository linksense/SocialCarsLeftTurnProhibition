package gui;

import java.util.List;

import network.Link;

import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QPointF;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.*;

public class LeftTurnLinksOperator extends QDialog
{
	//~Variables
	//--------------------------------------------------------------------------
	private Ui_LeftTurnLinksOperator ui = new Ui_LeftTurnLinksOperator();
	private List<Link> leftTurnLinks = null;
	private QStandardItemModel standardItemModel = null;
	
	public Signal1<Integer> itemSelected = new Signal1<Integer>();
	public Signal1<Integer> itemForbidded = new Signal1<Integer>();
	public Signal0 itemAllForbidded = new Signal0();
	int itemIndex = 0;
	//~Methods
	//--------------------------------------------------------------------------
	/**
	 * 构造函数
	 */
	public LeftTurnLinksOperator(List<Link> leftTurnLinks)
	{
		ui.setupUi(this);
		this.leftTurnLinks = leftTurnLinks;
		itemsListShow();
	}
	
	/**
	 * 构造函数
	 */
	public LeftTurnLinksOperator(QWidget parent, List<Link> leftTurnLinks)
	{
		super(parent);
		ui.setupUi(this);
		this.leftTurnLinks = leftTurnLinks;
		itemsListShow();
	}
	
	private void itemsListShow()
	{
		ui.centerButton.setEnabled(false);
		ui.forbidButton.setEnabled(false);
		standardItemModel = new QStandardItemModel(this);
		int nCount = leftTurnLinks.size();  
		for(int i = 0; i < nCount; i++)  
		{
			String name = leftTurnLinks.get(i).getName();
			QStandardItem item = new QStandardItem(name);
			if(i % 2 == 1)  
			{  
				QLinearGradient linearGrad = new QLinearGradient(new QPointF(0, 0), 
																 new QPointF(200, 200));  
				linearGrad.setColorAt(0, QColor.lightGray);  
				linearGrad.setColorAt(1, QColor.white);  
				QBrush brush = new QBrush(linearGrad);  
				item.setBackground(brush);  
			}
			standardItemModel.appendRow(item);
		}
		ui.itemsListView.setModel(standardItemModel);
	}

	@SuppressWarnings("unused")
	private void on_searchLineEdit_textChanged(String name)
	{
		ui.centerButton.setEnabled(false);
		ui.forbidButton.setEnabled(false);
		
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
		System.out.println("选择的是第  " + itemIndex + " 个左转连接, 其值为： " 
													+ index.data().toString());
		ui.centerButton.setEnabled(true);
		ui.forbidButton.setEnabled(false);
	}
	
	@SuppressWarnings("unused")
	private void on_centerButton_clicked()
	{
		itemSelected.emit(itemIndex);
		ui.centerButton.setEnabled(false);
		ui.forbidButton.setEnabled(true);
	}
	
	@SuppressWarnings("unused")
	private void on_forbidButton_clicked()
	{
		itemForbidded.emit(itemIndex);
		standardItemModel.removeRow(itemIndex);
		ui.itemsListView.setModel(standardItemModel);
		ui.forbidButton.setEnabled(false);
		/*
		QLinearGradient linearGrad = new QLinearGradient(new QPointF(0, 0), 
														 new QPointF(200, 200));  
		linearGrad.setColorAt(0, QColor.red);  
		linearGrad.setColorAt(1, QColor.white);  
		QBrush brush = new QBrush(linearGrad); 
		
		QStandardItem item = standardItemModel.item(itemIndex);
		item.setBackground(brush);
		standardItemModel.setItem(itemIndex,item);
		
		ui.itemsListView.setModel(standardItemModel);
		ui.itemsListView.update();
		*/
	}
	
	@SuppressWarnings("unused")
	private void on_forbidAllButton_clicked()
	{
		itemAllForbidded.emit();
		on_closeButton_clicked();
	}
	
	private void on_closeButton_clicked()
	{
		this.close();
	}
}
