package gui;

import com.trolltech.qt.gui.*;

public class AlgorithmSelection extends QDialog 
{
	//~Variables
	//--------------------------------------------------------------------------
	private Ui_AlgorithmSelection ui = new Ui_AlgorithmSelection();
    
	private String leftTurnSetSelection = null;
	private String signalTimingType = null;
	private boolean useMultiThread = false;

    //~Methods
  	//--------------------------------------------------------------------------
    public AlgorithmSelection() 
    {
        ui.setupUi(this);
    }

    public AlgorithmSelection(QWidget parent) 
    {
        super(parent);
        ui.setupUi(this);
    }
    
    @SuppressWarnings("unused")
    private void on_enumRadioButton_clicked()
	{
		// this.leftTurnSetSelection = ui.enumRadioButton.text();
		this.leftTurnSetSelection = "ENUM";
	}
    
    @SuppressWarnings("unused")
    private void on_geneticAlgmaRadioButton_clicked()
	{
		// this.leftTurnSetSelection = ui.geneticAlgmaRadioButton.text();
    	this.leftTurnSetSelection = "GA";
	}
    
    @SuppressWarnings("unused")
    private void on_lanebasedRadioButton_clicked()
	{
		// this.leftTurnSetSelection = ui.enumRadioButton.text();
		this.signalTimingType = "LaneBased";
	}
    
    @SuppressWarnings("unused")
    private void on_stagebasedRadioButton_clicked()
	{
		// this.leftTurnSetSelection = ui.geneticAlgmaRadioButton.text();
    	this.signalTimingType = "StageBased";
	}
    
    @SuppressWarnings("unused")
    private void on_multiThreadCheckBox_clicked()
	{
		this.useMultiThread = true;
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
	 * @return leftTurnSetSelection
	 */
	public String getLeftTurnSetSelection()
	{
		return leftTurnSetSelection;
	}

	/**
	 * @param leftTurnSetSelection 要设置的 leftTurnSetSelection
	 */
	public void setLeftTurnSetSelection(String leftTurnSetSelection)
	{
		this.leftTurnSetSelection = leftTurnSetSelection;
	}

	/**
	 * @return signalTimingType
	 */
	public String getSignalTimingType()
	{
		return signalTimingType;
	}

	/**
	 * @param signalTimingType 要设置的 signalTimingType
	 */
	public void setSignalTimingType(String signalTimingType)
	{
		this.signalTimingType = signalTimingType;
	}

	/**
	 * @return useMultiThread
	 */
	public boolean getUseMultiThread()
	{
		return useMultiThread;
	}

	/**
	 * @param useMultiThread 要设置的 useMultiThread
	 */
	public void setUseMultiThread(boolean useMultiThread)
	{
		this.useMultiThread = useMultiThread;
	}

}
