package gui;

import com.trolltech.qt.gui.*;

public class SignalDisplay extends QWidget 
{

    Ui_SignalDisplay ui = new Ui_SignalDisplay();

    public static void main(String[] args) 
    {
        QApplication.initialize(args);

        SignalDisplay testSignalDisplay = new SignalDisplay();
        testSignalDisplay.show();

        QApplication.exec();
    }

    public SignalDisplay() 
    {
        ui.setupUi(this);
    }

    public SignalDisplay(QWidget parent) 
    {
        super(parent);
        ui.setupUi(this);
    }
}
