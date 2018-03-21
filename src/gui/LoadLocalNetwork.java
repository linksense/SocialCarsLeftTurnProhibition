package gui;

import com.trolltech.qt.core.QDir;
import com.trolltech.qt.core.QFileInfo;
import com.trolltech.qt.gui.*;

public class LoadLocalNetwork extends QDialog
{
	//~Variables
	//--------------------------------------------------------------------------
	private Ui_LoadLocalNetwork ui = new Ui_LoadLocalNetwork();
	private String localNetwork;
	
	//~Methods
	//--------------------------------------------------------------------------
	public LoadLocalNetwork()
	{
		ui.setupUi(this);
	}

	public LoadLocalNetwork(QWidget parent)
	{
		super(parent);
		ui.setupUi(this);
	}
	
	@SuppressWarnings("unused")
	private void on_localNetButton_clicked()
	{
		final String localNetLocation = QDir.currentPath() + "/localnetwork/";
		String filename = QFileDialog.getOpenFileName(this, tr("Select Local Network File..."),  
				                      localNetLocation, new QFileDialog.Filter(tr("XML-Files (*.xml)")));
		
		QFileInfo fileInfo = new QFileInfo(filename);
		String localNetFileName = fileInfo.fileName();
		String localNetFilePath = fileInfo.canonicalFilePath();
		
		ui.localNetLineEdit.setText(localNetFileName);
		localNetwork = localNetFilePath;
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

	/**
	 * @return localNetwork
	 */
	public String getLocalNetwork()
	{
		return localNetwork;
	}
}
