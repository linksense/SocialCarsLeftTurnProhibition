/********************************************************************************
** Form generated from reading ui file 'AlgorithmSelection.jui'
**
** Created: ÖÜ¶þ 6ÔÂ 6 18:58:31 2017
**      by: Qt User Interface Compiler version 4.5.2
**
** WARNING! All changes made in this file will be lost when recompiling ui file!
********************************************************************************/

package gui;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

public class Ui_AlgorithmSelection implements com.trolltech.qt.QUiForm<QDialog>
{
    public QGroupBox signalTimingGroupBox;
    public QRadioButton lanebasedRadioButton;
    public QRadioButton stagebasedRadioButton;
    public QPushButton okButton;
    public QPushButton cancelButton;
    public QGroupBox groupBox;
    public QRadioButton enumRadioButton;
    public QRadioButton geneticAlgmaRadioButton;
    public QCheckBox multiThreadCheckBox;

    public Ui_AlgorithmSelection() { super(); }

    public void setupUi(QDialog AlgorithmSelection)
    {
        AlgorithmSelection.setObjectName("AlgorithmSelection");
        AlgorithmSelection.resize(new QSize(271, 152).expandedTo(AlgorithmSelection.minimumSizeHint()));
        AlgorithmSelection.setWindowIcon(new QIcon(new QPixmap("classpath:resource/png/Info2.png")));
        signalTimingGroupBox = new QGroupBox(AlgorithmSelection);
        signalTimingGroupBox.setObjectName("signalTimingGroupBox");
        signalTimingGroupBox.setGeometry(new QRect(150, 10, 111, 101));
        lanebasedRadioButton = new QRadioButton(signalTimingGroupBox);
        lanebasedRadioButton.setObjectName("lanebasedRadioButton");
        lanebasedRadioButton.setGeometry(new QRect(11, 30, 91, 20));
        stagebasedRadioButton = new QRadioButton(signalTimingGroupBox);
        stagebasedRadioButton.setObjectName("stagebasedRadioButton");
        stagebasedRadioButton.setGeometry(new QRect(10, 70, 91, 19));
        okButton = new QPushButton(AlgorithmSelection);
        okButton.setObjectName("okButton");
        okButton.setGeometry(new QRect(100, 120, 75, 24));
        okButton.setIcon(new QIcon(new QPixmap("classpath:resource/png/Ok.png")));
        cancelButton = new QPushButton(AlgorithmSelection);
        cancelButton.setObjectName("cancelButton");
        cancelButton.setGeometry(new QRect(180, 120, 75, 24));
        cancelButton.setIcon(new QIcon(new QPixmap("classpath:resource/png/Cancel.png")));
        groupBox = new QGroupBox(AlgorithmSelection);
        groupBox.setObjectName("groupBox");
        groupBox.setGeometry(new QRect(10, 10, 131, 101));
        enumRadioButton = new QRadioButton(groupBox);
        enumRadioButton.setObjectName("enumRadioButton");
        enumRadioButton.setGeometry(new QRect(10, 30, 111, 20));
        geneticAlgmaRadioButton = new QRadioButton(groupBox);
        geneticAlgmaRadioButton.setObjectName("geneticAlgmaRadioButton");
        geneticAlgmaRadioButton.setGeometry(new QRect(10, 60, 111, 21));
        multiThreadCheckBox = new QCheckBox(AlgorithmSelection);
        multiThreadCheckBox.setObjectName("multiThreadCheckBox");
        multiThreadCheckBox.setGeometry(new QRect(10, 120, 81, 19));
        retranslateUi(AlgorithmSelection);

        AlgorithmSelection.connectSlotsByName();
    } // setupUi

    void retranslateUi(QDialog AlgorithmSelection)
    {
        AlgorithmSelection.setWindowTitle(com.trolltech.qt.core.QCoreApplication.translate("AlgorithmSelection", "Algorithm Selection", null));
        signalTimingGroupBox.setTitle(com.trolltech.qt.core.QCoreApplication.translate("AlgorithmSelection", "Signal Timing", null));
        lanebasedRadioButton.setText(com.trolltech.qt.core.QCoreApplication.translate("AlgorithmSelection", "Lane Based", null));
        stagebasedRadioButton.setText(com.trolltech.qt.core.QCoreApplication.translate("AlgorithmSelection", "Stage Based", null));
        okButton.setText(com.trolltech.qt.core.QCoreApplication.translate("AlgorithmSelection", "Ok", null));
        cancelButton.setText(com.trolltech.qt.core.QCoreApplication.translate("AlgorithmSelection", "Cancel", null));
        groupBox.setTitle(com.trolltech.qt.core.QCoreApplication.translate("AlgorithmSelection", "Left Turn Prohibition", null));
        enumRadioButton.setText(com.trolltech.qt.core.QCoreApplication.translate("AlgorithmSelection", "Enumeration", null));
        geneticAlgmaRadioButton.setText(com.trolltech.qt.core.QCoreApplication.translate("AlgorithmSelection", "Genetic Algorithm", null));
        multiThreadCheckBox.setText(com.trolltech.qt.core.QCoreApplication.translate("AlgorithmSelection", "Multi-Thread", null));
    } // retranslateUi

}

