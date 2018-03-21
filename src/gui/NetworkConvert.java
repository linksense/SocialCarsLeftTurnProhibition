package gui;

import com.trolltech.qt.core.QDir;
import com.trolltech.qt.core.QFileInfo;
import com.trolltech.qt.gui.*;

/**
 * @author Ansleliu
 *
 */
/**
 * @author Ansleliu
 *
 */
public class NetworkConvert extends QDialog
{
	//~Variables
	//--------------------------------------------------------------------------
	private Ui_NetworkConvert ui = new Ui_NetworkConvert();
	
	private String sumoNetXML;
	private String odXML;
	private String odDemandTXT;
	private String localNetXML;

	//~Methods
	//--------------------------------------------------------------------------
	/**
	 * 构造函数
	 */
	public NetworkConvert()
	{
		ui.setupUi(this);
	}
	
	/**
	 * 构造函数
	 * @param parent
	 */
	public NetworkConvert(QWidget parent)
	{
		super(parent);
		ui.setupUi(this);
	}
	
	@SuppressWarnings("unused")
	private void on_sumoNetButton_clicked()
	{
		final String sumoLocation = QDir.currentPath()+"/Test_network/";
		String filename = QFileDialog.getOpenFileName(this, tr("Select sumo Netfile..."),  sumoLocation, 
												      new QFileDialog.Filter(tr("XML-Files (*.net.xml)")));
		
		QFileInfo fileInfo = new QFileInfo(filename);	
		String sumoNetFileName = fileInfo.fileName();
		String sumoNetFilePath = fileInfo.canonicalFilePath();
		
		ui.sumoNetworkEdit.setText(sumoNetFileName);
		sumoNetXML = sumoNetFilePath;
	}

	@SuppressWarnings("unused")
	private void on_odButton_clicked()
	{
		final String odLocation = QDir.currentPath()+"/Test_network/";
		String filename = QFileDialog.getOpenFileName(this,tr("Select OD Districtsfile..."),  odLocation, 
												      new QFileDialog.Filter(tr("XML-Files (*.districts.xml)")));
		
		QFileInfo fileInfo = new QFileInfo(filename);
		
		String odFileName = fileInfo.fileName();
		String odFileBaseName = fileInfo.baseName();
		String odFilePath = fileInfo.canonicalFilePath();
		
		ui.odXmlEdit.setText(odFileName);
		ui.localNetworkEdit.setText(odFileBaseName);
		odXML = odFilePath;
	}
	
	@SuppressWarnings("unused")
	private void on_odDemandTXTButton_clicked()
	{
		final String odLocation = QDir.currentPath()+"/Test_network/";
		String filename = QFileDialog.getOpenFileName(this,tr("Select OD Demand Matrixfile..."),  odLocation, 
												      new QFileDialog.Filter(tr("TXT-Files (*.txt)")));
		
		QFileInfo fileInfo = new QFileInfo(filename);
		
		String odFileName = fileInfo.fileName();
		String odFileBaseName = fileInfo.baseName();
		String odTXTFilePath = fileInfo.canonicalFilePath();
		
		ui.odDemandTXTEdit.setText(odFileName);
		odDemandTXT = odTXTFilePath;
	}
	
	@SuppressWarnings("unused")
	private void on_localNetworkEdit_textChanged()
	{
		StringBuilder localNet = new  StringBuilder(QDir.currentPath() +"/localnetwork/" + 
																 ui.localNetworkEdit.text());
		QFileInfo fileInfo = new QFileInfo(localNet.toString());
		if(!fileInfo.suffix().contains("xml"))
		{
			localNet.append(".localnet.xml");
		}
		localNetXML = localNet.toString();
	}
	
	@SuppressWarnings("unused")
	private void on_okButton_clicked()
	{
		this.accept();
	}
	
	@SuppressWarnings("unused")
	private void on_cancelButton_clicked()
	{
		this.reject();
	}
	
	/****************************************************************
	 *******************Getter and Setter****************************
	 ****************************************************************/
	/**
	 * @return sumoNetXML
	 */
	public String getSumoNetXML()
	{
		return sumoNetXML;
	}

	/**
	 * @return odXML
	 */
	public String getOdXML()
	{
		return odXML;
	}

	/**
	 * @return localNetXML
	 */
	public String getLocalNetXML()
	{
		return localNetXML;
	}

	/**
	 * @return odDemandTXT
	 */
	public String getOdDemandTXT()
	{
		return odDemandTXT;
	}
}
