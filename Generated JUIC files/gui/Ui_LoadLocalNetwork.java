/********************************************************************************
** Form generated from reading ui file 'LoadLocalNetwork.jui'
**
** Created: ÖÜËÄ 6ÔÂ 1 22:35:37 2017
**      by: Qt User Interface Compiler version 4.5.2
**
** WARNING! All changes made in this file will be lost when recompiling ui file!
********************************************************************************/

package gui;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

public class Ui_LoadLocalNetwork implements com.trolltech.qt.QUiForm<QDialog>
{
    public QGridLayout gridLayout;
    public QVBoxLayout verticalLayout;
    public QHBoxLayout horizontalLayout;
    public QLabel loadLocalLabel;
    public QLineEdit localNetLineEdit;
    public QPushButton localNetButton;
    public QHBoxLayout horizontalLayout_2;
    public QSpacerItem horizontalSpacer;
    public QPushButton okButton;
    public QPushButton cancelButton;

    public Ui_LoadLocalNetwork() { super(); }

    public void setupUi(QDialog LoadLocalNetwork)
    {
        LoadLocalNetwork.setObjectName("LoadLocalNetwork");
        LoadLocalNetwork.resize(new QSize(395, 88).expandedTo(LoadLocalNetwork.minimumSizeHint()));
        LoadLocalNetwork.setWindowIcon(new QIcon(new QPixmap("classpath:resource/png/Go In.png")));
        gridLayout = new QGridLayout(LoadLocalNetwork);
        gridLayout.setObjectName("gridLayout");
        verticalLayout = new QVBoxLayout();
        verticalLayout.setObjectName("verticalLayout");
        horizontalLayout = new QHBoxLayout();
        horizontalLayout.setObjectName("horizontalLayout");
        loadLocalLabel = new QLabel(LoadLocalNetwork);
        loadLocalLabel.setObjectName("loadLocalLabel");

        horizontalLayout.addWidget(loadLocalLabel);

        localNetLineEdit = new QLineEdit(LoadLocalNetwork);
        localNetLineEdit.setObjectName("localNetLineEdit");

        horizontalLayout.addWidget(localNetLineEdit);

        localNetButton = new QPushButton(LoadLocalNetwork);
        localNetButton.setObjectName("localNetButton");
        localNetButton.setIcon(new QIcon(new QPixmap("classpath:resource/png/Folder.png")));

        horizontalLayout.addWidget(localNetButton);


        verticalLayout.addLayout(horizontalLayout);

        horizontalLayout_2 = new QHBoxLayout();
        horizontalLayout_2.setObjectName("horizontalLayout_2");
        horizontalSpacer = new QSpacerItem(40, 20, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding, com.trolltech.qt.gui.QSizePolicy.Policy.Minimum);

        horizontalLayout_2.addItem(horizontalSpacer);

        okButton = new QPushButton(LoadLocalNetwork);
        okButton.setObjectName("okButton");
        okButton.setIcon(new QIcon(new QPixmap("classpath:resource/png/Ok.png")));

        horizontalLayout_2.addWidget(okButton);

        cancelButton = new QPushButton(LoadLocalNetwork);
        cancelButton.setObjectName("cancelButton");
        cancelButton.setIcon(new QIcon(new QPixmap("classpath:resource/png/Cancel.png")));

        horizontalLayout_2.addWidget(cancelButton);


        verticalLayout.addLayout(horizontalLayout_2);


        gridLayout.addLayout(verticalLayout, 0, 0, 1, 1);

        retranslateUi(LoadLocalNetwork);

        LoadLocalNetwork.connectSlotsByName();
    } // setupUi

    void retranslateUi(QDialog LoadLocalNetwork)
    {
        LoadLocalNetwork.setWindowTitle(com.trolltech.qt.core.QCoreApplication.translate("LoadLocalNetwork", "Load Local Network", null));
        loadLocalLabel.setText(com.trolltech.qt.core.QCoreApplication.translate("LoadLocalNetwork", "Local Net:", null));
        localNetButton.setText(com.trolltech.qt.core.QCoreApplication.translate("LoadLocalNetwork", "Select", null));
        okButton.setText(com.trolltech.qt.core.QCoreApplication.translate("LoadLocalNetwork", "Ok", null));
        cancelButton.setText(com.trolltech.qt.core.QCoreApplication.translate("LoadLocalNetwork", "Cancel", null));
    } // retranslateUi

}

