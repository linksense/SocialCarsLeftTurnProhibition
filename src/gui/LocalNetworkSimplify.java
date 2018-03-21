package gui;

import com.trolltech.qt.core.QDir;
import com.trolltech.qt.core.QFileInfo;
import com.trolltech.qt.gui.*;

public class LocalNetworkSimplify extends QDialog
{
	//~Variables
	//--------------------------------------------------------------------------
	Ui_LocalNetworkSimplify ui = new Ui_LocalNetworkSimplify();
	private String originalLocalNet = null;
	private String simplifiedLocalNet = null;
	
	//~Methods
	//--------------------------------------------------------------------------
	public LocalNetworkSimplify()
	{
		ui.setupUi(this);
	}

	public LocalNetworkSimplify(QWidget parent)
	{
		super(parent);
		ui.setupUi(this);
	}

	@SuppressWarnings("unused")
	private void on_selectButton_clicked()
	{
		final String localNetLocation = QDir.currentPath() + "/localnetwork/";
		String filename = QFileDialog.getOpenFileName(this, 
				tr("Select Original Local Network File..."),  
		        							localNetLocation, 
		        new QFileDialog.Filter(tr("XML-Files (*.localnet.xml)")));
		
		QFileInfo fileInfo = new QFileInfo(filename);
		String localNetFileName = fileInfo.fileName();
		String localNetFileBaseName = fileInfo.baseName();
		String localNetFilePath = fileInfo.canonicalFilePath();
		
		ui.originalNetLineEdit.setText(localNetFileName);
		ui.simplifiedNetLineEdit.setText(localNetFileBaseName);
		setOriginalLocalNet(localNetFilePath);
	}
	
	@SuppressWarnings("unused")
	private void on_simplifiedNetLineEdit_textChanged()
	{
		StringBuilder simplifiedLocalNetPath = new StringBuilder(QDir.currentPath() +
																   "/localnetwork/" + 
												     ui.simplifiedNetLineEdit.text());
		QFileInfo fileInfo = new QFileInfo(simplifiedLocalNetPath.toString());
		if(!fileInfo.suffix().contains("xml"))
		{
			simplifiedLocalNetPath.append(".simplified.xml");
		}
		setSimplifiedLocalNet(simplifiedLocalNetPath.toString());
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
	 * @return originalLocalNet
	 */
	public String getOriginalLocalNet()
	{
		return originalLocalNet;
	}

	/**
	 * @param originalLocalNet 要设置的 originalLocalNet
	 */
	public void setOriginalLocalNet(String originalLocalNet)
	{
		this.originalLocalNet = originalLocalNet;
	}

	/**
	 * @return simplifiedLocalNet
	 */
	public String getSimplifiedLocalNet()
	{
		return simplifiedLocalNet;
	}

	/**
	 * @param simplifiedLocalNet 要设置的 simplifiedLocalNet
	 */
	public void setSimplifiedLocalNet(String simplifiedLocalNet)
	{
		this.simplifiedLocalNet = simplifiedLocalNet;
	}
}
