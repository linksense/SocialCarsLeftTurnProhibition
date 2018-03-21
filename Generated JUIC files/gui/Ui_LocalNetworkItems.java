/********************************************************************************
** Form generated from reading ui file 'LocalNetworkItems.jui'
**
** Created: ÖÜËÄ 6ÔÂ 1 22:35:37 2017
**      by: Qt User Interface Compiler version 4.5.2
**
** WARNING! All changes made in this file will be lost when recompiling ui file!
********************************************************************************/

package gui;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

public class Ui_LocalNetworkItems implements com.trolltech.qt.QUiForm<QDialog>
{
    public QGridLayout gridLayout;
    public QVBoxLayout verticalLayout_2;
    public QHBoxLayout horizontalLayout;
    public QLineEdit searchLineEdit;
    public QPushButton centerButton;
    public QHBoxLayout horizontalLayout_2;
    public QListView itemsListView;
    public QVBoxLayout verticalLayout;
    public QPushButton closeButton;
    public QSpacerItem verticalSpacer;

    public Ui_LocalNetworkItems() { super(); }

    public void setupUi(QDialog LocalNetworkItems)
    {
        LocalNetworkItems.setObjectName("LocalNetworkItems");
        LocalNetworkItems.resize(new QSize(322, 394).expandedTo(LocalNetworkItems.minimumSizeHint()));
        LocalNetworkItems.setMaximumSize(new QSize(322, 394));
        LocalNetworkItems.setWindowIcon(new QIcon(new QPixmap("classpath:resource/png/Search.png")));
        gridLayout = new QGridLayout(LocalNetworkItems);
        gridLayout.setObjectName("gridLayout");
        verticalLayout_2 = new QVBoxLayout();
        verticalLayout_2.setObjectName("verticalLayout_2");
        horizontalLayout = new QHBoxLayout();
        horizontalLayout.setObjectName("horizontalLayout");
        searchLineEdit = new QLineEdit(LocalNetworkItems);
        searchLineEdit.setObjectName("searchLineEdit");

        horizontalLayout.addWidget(searchLineEdit);

        centerButton = new QPushButton(LocalNetworkItems);
        centerButton.setObjectName("centerButton");
        centerButton.setIcon(new QIcon(new QPixmap("classpath:resource/png/Directions.png")));

        horizontalLayout.addWidget(centerButton);


        verticalLayout_2.addLayout(horizontalLayout);

        horizontalLayout_2 = new QHBoxLayout();
        horizontalLayout_2.setObjectName("horizontalLayout_2");
        itemsListView = new QListView(LocalNetworkItems);
        itemsListView.setObjectName("itemsListView");
        itemsListView.setAutoFillBackground(true);

        horizontalLayout_2.addWidget(itemsListView);

        verticalLayout = new QVBoxLayout();
        verticalLayout.setObjectName("verticalLayout");
        closeButton = new QPushButton(LocalNetworkItems);
        closeButton.setObjectName("closeButton");
        closeButton.setIcon(new QIcon(new QPixmap("classpath:resource/png/Cancel.png")));

        verticalLayout.addWidget(closeButton);

        verticalSpacer = new QSpacerItem(20, 40, com.trolltech.qt.gui.QSizePolicy.Policy.Minimum, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding);

        verticalLayout.addItem(verticalSpacer);


        horizontalLayout_2.addLayout(verticalLayout);


        verticalLayout_2.addLayout(horizontalLayout_2);


        gridLayout.addLayout(verticalLayout_2, 0, 0, 1, 1);

        retranslateUi(LocalNetworkItems);

        LocalNetworkItems.connectSlotsByName();
    } // setupUi

    void retranslateUi(QDialog LocalNetworkItems)
    {
        LocalNetworkItems.setWindowTitle(com.trolltech.qt.core.QCoreApplication.translate("LocalNetworkItems", "Local Network", null));
        centerButton.setText(com.trolltech.qt.core.QCoreApplication.translate("LocalNetworkItems", "Center", null));
        closeButton.setText(com.trolltech.qt.core.QCoreApplication.translate("LocalNetworkItems", "Close ", null));
    } // retranslateUi

}

