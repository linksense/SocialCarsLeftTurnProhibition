/********************************************************************************
** Form generated from reading ui file 'NetworkConvert.jui'
**
** Created: ÖÜËÄ 6ÔÂ 1 22:35:38 2017
**      by: Qt User Interface Compiler version 4.5.2
**
** WARNING! All changes made in this file will be lost when recompiling ui file!
********************************************************************************/

package gui;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

public class Ui_NetworkConvert implements com.trolltech.qt.QUiForm<QDialog>
{
    public QGridLayout gridLayout_2;
    public QVBoxLayout verticalLayout;
    public QGridLayout gridLayout;
    public QLabel sumoLabel;
    public QLineEdit sumoNetworkEdit;
    public QPushButton sumoNetButton;
    public QLabel odLabel;
    public QPushButton odButton;
    public QLabel localNetLabel;
    public QLineEdit localNetworkEdit;
    public QSpacerItem horizontalSpacer;
    public QLineEdit odXmlEdit;
    public QLabel odTXTLabel;
    public QPushButton odDemandTXTButton;
    public QLineEdit odDemandTXTEdit;
    public QHBoxLayout horizontalLayout;
    public QSpacerItem horizontalSpacer_2;
    public QPushButton okButton;
    public QPushButton cancelButton;

    public Ui_NetworkConvert() { super(); }

    public void setupUi(QDialog NetworkConvert)
    {
        NetworkConvert.setObjectName("NetworkConvert");
        NetworkConvert.resize(new QSize(430, 183).expandedTo(NetworkConvert.minimumSizeHint()));
        NetworkConvert.setMaximumSize(new QSize(430, 183));
        NetworkConvert.setWindowIcon(new QIcon(new QPixmap("classpath:resource/png/Refresh.png")));
        gridLayout_2 = new QGridLayout(NetworkConvert);
        gridLayout_2.setObjectName("gridLayout_2");
        verticalLayout = new QVBoxLayout();
        verticalLayout.setObjectName("verticalLayout");
        gridLayout = new QGridLayout();
        gridLayout.setObjectName("gridLayout");
        sumoLabel = new QLabel(NetworkConvert);
        sumoLabel.setObjectName("sumoLabel");

        gridLayout.addWidget(sumoLabel, 0, 0, 1, 1);

        sumoNetworkEdit = new QLineEdit(NetworkConvert);
        sumoNetworkEdit.setObjectName("sumoNetworkEdit");
        sumoNetworkEdit.setMinimumSize(new QSize(260, 21));
        sumoNetworkEdit.setReadOnly(true);

        gridLayout.addWidget(sumoNetworkEdit, 0, 1, 1, 1);

        sumoNetButton = new QPushButton(NetworkConvert);
        sumoNetButton.setObjectName("sumoNetButton");
        sumoNetButton.setMaximumSize(new QSize(77, 16777215));
        sumoNetButton.setIcon(new QIcon(new QPixmap("classpath:resource/png/Folder.png")));

        gridLayout.addWidget(sumoNetButton, 0, 3, 1, 1);

        odLabel = new QLabel(NetworkConvert);
        odLabel.setObjectName("odLabel");

        gridLayout.addWidget(odLabel, 1, 0, 1, 1);

        odButton = new QPushButton(NetworkConvert);
        odButton.setObjectName("odButton");
        odButton.setMaximumSize(new QSize(77, 16777215));
        odButton.setIcon(new QIcon(new QPixmap("classpath:resource/png/Folder.png")));

        gridLayout.addWidget(odButton, 1, 3, 1, 1);

        localNetLabel = new QLabel(NetworkConvert);
        localNetLabel.setObjectName("localNetLabel");

        gridLayout.addWidget(localNetLabel, 3, 0, 1, 1);

        localNetworkEdit = new QLineEdit(NetworkConvert);
        localNetworkEdit.setObjectName("localNetworkEdit");
        localNetworkEdit.setMinimumSize(new QSize(260, 21));

        gridLayout.addWidget(localNetworkEdit, 3, 1, 1, 1);

        horizontalSpacer = new QSpacerItem(40, 20, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding, com.trolltech.qt.gui.QSizePolicy.Policy.Minimum);

        gridLayout.addItem(horizontalSpacer, 3, 3, 1, 1);

        odXmlEdit = new QLineEdit(NetworkConvert);
        odXmlEdit.setObjectName("odXmlEdit");
        odXmlEdit.setMinimumSize(new QSize(260, 21));
        odXmlEdit.setReadOnly(true);

        gridLayout.addWidget(odXmlEdit, 1, 1, 1, 1);

        odTXTLabel = new QLabel(NetworkConvert);
        odTXTLabel.setObjectName("odTXTLabel");

        gridLayout.addWidget(odTXTLabel, 2, 0, 1, 1);

        odDemandTXTButton = new QPushButton(NetworkConvert);
        odDemandTXTButton.setObjectName("odDemandTXTButton");
        odDemandTXTButton.setIcon(new QIcon(new QPixmap("classpath:resource/png/Folder.png")));

        gridLayout.addWidget(odDemandTXTButton, 2, 3, 1, 1);

        odDemandTXTEdit = new QLineEdit(NetworkConvert);
        odDemandTXTEdit.setObjectName("odDemandTXTEdit");

        gridLayout.addWidget(odDemandTXTEdit, 2, 1, 1, 1);


        verticalLayout.addLayout(gridLayout);

        horizontalLayout = new QHBoxLayout();
        horizontalLayout.setObjectName("horizontalLayout");
        horizontalSpacer_2 = new QSpacerItem(40, 20, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding, com.trolltech.qt.gui.QSizePolicy.Policy.Minimum);

        horizontalLayout.addItem(horizontalSpacer_2);

        okButton = new QPushButton(NetworkConvert);
        okButton.setObjectName("okButton");
        okButton.setIcon(new QIcon(new QPixmap("classpath:resource/png/Ok.png")));

        horizontalLayout.addWidget(okButton);

        cancelButton = new QPushButton(NetworkConvert);
        cancelButton.setObjectName("cancelButton");
        cancelButton.setIcon(new QIcon(new QPixmap("classpath:resource/png/Cancel.png")));

        horizontalLayout.addWidget(cancelButton);


        verticalLayout.addLayout(horizontalLayout);


        gridLayout_2.addLayout(verticalLayout, 0, 0, 1, 1);

        retranslateUi(NetworkConvert);

        NetworkConvert.connectSlotsByName();
    } // setupUi

    void retranslateUi(QDialog NetworkConvert)
    {
        NetworkConvert.setWindowTitle(com.trolltech.qt.core.QCoreApplication.translate("NetworkConvert", "Network Convertor", null));
        sumoLabel.setText(com.trolltech.qt.core.QCoreApplication.translate("NetworkConvert", "SUMO  Net:", null));
        sumoNetButton.setText(com.trolltech.qt.core.QCoreApplication.translate("NetworkConvert", "Select", null));
        odLabel.setText(com.trolltech.qt.core.QCoreApplication.translate("NetworkConvert", "OD    XML:", null));
        odButton.setText(com.trolltech.qt.core.QCoreApplication.translate("NetworkConvert", "Select", null));
        localNetLabel.setText(com.trolltech.qt.core.QCoreApplication.translate("NetworkConvert", "Local Net:", null));
        localNetworkEdit.setInputMask("");
        odTXTLabel.setText(com.trolltech.qt.core.QCoreApplication.translate("NetworkConvert", "OD    TXT:", null));
        odDemandTXTButton.setText(com.trolltech.qt.core.QCoreApplication.translate("NetworkConvert", "Select", null));
        okButton.setText(com.trolltech.qt.core.QCoreApplication.translate("NetworkConvert", "Ok", null));
        cancelButton.setText(com.trolltech.qt.core.QCoreApplication.translate("NetworkConvert", "Cancel", null));
    } // retranslateUi

}

