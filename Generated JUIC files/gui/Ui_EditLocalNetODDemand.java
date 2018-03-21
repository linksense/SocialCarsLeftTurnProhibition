/********************************************************************************
** Form generated from reading ui file 'EditLocalNetODDemand.jui'
**
** Created: ÖÜËÄ 6ÔÂ 1 22:35:37 2017
**      by: Qt User Interface Compiler version 4.5.2
**
** WARNING! All changes made in this file will be lost when recompiling ui file!
********************************************************************************/

package gui;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

public class Ui_EditLocalNetODDemand implements com.trolltech.qt.QUiForm<QDialog>
{
    public QGridLayout gridLayout;
    public QVBoxLayout verticalLayout;
    public QTableView odDemandTableView;
    public QHBoxLayout horizontalLayout;
    public QLabel searchVLabel;
    public QComboBox vComboBox;
    public QLabel searchHLabel;
    public QComboBox hComboBox;
    public QPushButton okButton;
    public QPushButton cancelButton;

    public Ui_EditLocalNetODDemand() { super(); }

    public void setupUi(QDialog EditLocalNetODDemand)
    {
        EditLocalNetODDemand.setObjectName("EditLocalNetODDemand");
        EditLocalNetODDemand.resize(new QSize(579, 381).expandedTo(EditLocalNetODDemand.minimumSizeHint()));
        EditLocalNetODDemand.setWindowIcon(new QIcon(new QPixmap("classpath:resource/png/Write.png")));
        gridLayout = new QGridLayout(EditLocalNetODDemand);
        gridLayout.setObjectName("gridLayout");
        verticalLayout = new QVBoxLayout();
        verticalLayout.setObjectName("verticalLayout");
        odDemandTableView = new QTableView(EditLocalNetODDemand);
        odDemandTableView.setObjectName("odDemandTableView");

        verticalLayout.addWidget(odDemandTableView);

        horizontalLayout = new QHBoxLayout();
        horizontalLayout.setObjectName("horizontalLayout");
        searchVLabel = new QLabel(EditLocalNetODDemand);
        searchVLabel.setObjectName("searchVLabel");
        searchVLabel.setMaximumSize(new QSize(38, 21));

        horizontalLayout.addWidget(searchVLabel);

        vComboBox = new QComboBox(EditLocalNetODDemand);
        vComboBox.setObjectName("vComboBox");

        horizontalLayout.addWidget(vComboBox);

        searchHLabel = new QLabel(EditLocalNetODDemand);
        searchHLabel.setObjectName("searchHLabel");
        searchHLabel.setMaximumSize(new QSize(52, 21));

        horizontalLayout.addWidget(searchHLabel);

        hComboBox = new QComboBox(EditLocalNetODDemand);
        hComboBox.setObjectName("hComboBox");

        horizontalLayout.addWidget(hComboBox);

        okButton = new QPushButton(EditLocalNetODDemand);
        okButton.setObjectName("okButton");
        okButton.setIcon(new QIcon(new QPixmap("classpath:resource/png/Ok.png")));

        horizontalLayout.addWidget(okButton);

        cancelButton = new QPushButton(EditLocalNetODDemand);
        cancelButton.setObjectName("cancelButton");
        cancelButton.setIcon(new QIcon(new QPixmap("classpath:resource/png/Cancel.png")));

        horizontalLayout.addWidget(cancelButton);


        verticalLayout.addLayout(horizontalLayout);


        gridLayout.addLayout(verticalLayout, 0, 0, 1, 1);

        retranslateUi(EditLocalNetODDemand);

        EditLocalNetODDemand.connectSlotsByName();
    } // setupUi

    void retranslateUi(QDialog EditLocalNetODDemand)
    {
        EditLocalNetODDemand.setWindowTitle(com.trolltech.qt.core.QCoreApplication.translate("EditLocalNetODDemand", "Edit Local Network OD Demand", null));
        searchVLabel.setText(com.trolltech.qt.core.QCoreApplication.translate("EditLocalNetODDemand", "Vertical:", null));
        searchHLabel.setText(com.trolltech.qt.core.QCoreApplication.translate("EditLocalNetODDemand", "Horizontal:", null));
        okButton.setText(com.trolltech.qt.core.QCoreApplication.translate("EditLocalNetODDemand", "OK", null));
        cancelButton.setText(com.trolltech.qt.core.QCoreApplication.translate("EditLocalNetODDemand", "Cancel", null));
    } // retranslateUi

}

