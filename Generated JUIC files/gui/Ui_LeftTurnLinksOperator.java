/********************************************************************************
** Form generated from reading ui file 'LeftTurnLinksOperator.jui'
**
** Created: ÖÜËÄ 6ÔÂ 1 22:35:37 2017
**      by: Qt User Interface Compiler version 4.5.2
**
** WARNING! All changes made in this file will be lost when recompiling ui file!
********************************************************************************/

package gui;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

public class Ui_LeftTurnLinksOperator implements com.trolltech.qt.QUiForm<QDialog>
{
    public QGridLayout gridLayout;
    public QVBoxLayout verticalLayout_2;
    public QHBoxLayout horizontalLayout;
    public QLineEdit searchLineEdit;
    public QPushButton centerButton;
    public QHBoxLayout horizontalLayout_2;
    public QListView itemsListView;
    public QVBoxLayout verticalLayout;
    public QPushButton forbidButton;
    public QPushButton forbidAllButton;
    public QPushButton closeButton;
    public QSpacerItem verticalSpacer;

    public Ui_LeftTurnLinksOperator() { super(); }

    public void setupUi(QDialog LeftTurnLinksOperator)
    {
        LeftTurnLinksOperator.setObjectName("LeftTurnLinksOperator");
        LeftTurnLinksOperator.resize(new QSize(322, 394).expandedTo(LeftTurnLinksOperator.minimumSizeHint()));
        LeftTurnLinksOperator.setMaximumSize(new QSize(322, 394));
        LeftTurnLinksOperator.setWindowIcon(new QIcon(new QPixmap("classpath:resource/png/Search.png")));
        gridLayout = new QGridLayout(LeftTurnLinksOperator);
        gridLayout.setObjectName("gridLayout");
        verticalLayout_2 = new QVBoxLayout();
        verticalLayout_2.setObjectName("verticalLayout_2");
        horizontalLayout = new QHBoxLayout();
        horizontalLayout.setObjectName("horizontalLayout");
        searchLineEdit = new QLineEdit(LeftTurnLinksOperator);
        searchLineEdit.setObjectName("searchLineEdit");
        searchLineEdit.setMaximumSize(new QSize(275, 24));

        horizontalLayout.addWidget(searchLineEdit);

        centerButton = new QPushButton(LeftTurnLinksOperator);
        centerButton.setObjectName("centerButton");
        centerButton.setIcon(new QIcon(new QPixmap("classpath:resource/png/Directions.png")));

        horizontalLayout.addWidget(centerButton);


        verticalLayout_2.addLayout(horizontalLayout);

        horizontalLayout_2 = new QHBoxLayout();
        horizontalLayout_2.setObjectName("horizontalLayout_2");
        itemsListView = new QListView(LeftTurnLinksOperator);
        itemsListView.setObjectName("itemsListView");

        horizontalLayout_2.addWidget(itemsListView);

        verticalLayout = new QVBoxLayout();
        verticalLayout.setObjectName("verticalLayout");
        forbidButton = new QPushButton(LeftTurnLinksOperator);
        forbidButton.setObjectName("forbidButton");
        forbidButton.setIcon(new QIcon(new QPixmap("classpath:resource/png/Hand.png")));

        verticalLayout.addWidget(forbidButton);

        forbidAllButton = new QPushButton(LeftTurnLinksOperator);
        forbidAllButton.setObjectName("forbidAllButton");
        forbidAllButton.setIcon(new QIcon(new QPixmap("classpath:resource/png/Hand.png")));

        verticalLayout.addWidget(forbidAllButton);

        closeButton = new QPushButton(LeftTurnLinksOperator);
        closeButton.setObjectName("closeButton");
        closeButton.setIcon(new QIcon(new QPixmap("classpath:resource/png/Cancel.png")));

        verticalLayout.addWidget(closeButton);

        verticalSpacer = new QSpacerItem(20, 40, com.trolltech.qt.gui.QSizePolicy.Policy.Minimum, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding);

        verticalLayout.addItem(verticalSpacer);


        horizontalLayout_2.addLayout(verticalLayout);


        verticalLayout_2.addLayout(horizontalLayout_2);


        gridLayout.addLayout(verticalLayout_2, 0, 0, 1, 1);

        retranslateUi(LeftTurnLinksOperator);

        LeftTurnLinksOperator.connectSlotsByName();
    } // setupUi

    void retranslateUi(QDialog LeftTurnLinksOperator)
    {
        LeftTurnLinksOperator.setWindowTitle(com.trolltech.qt.core.QCoreApplication.translate("LeftTurnLinksOperator", "LeftTurnLinks", null));
        centerButton.setText(com.trolltech.qt.core.QCoreApplication.translate("LeftTurnLinksOperator", "Center    ", null));
        forbidButton.setText(com.trolltech.qt.core.QCoreApplication.translate("LeftTurnLinksOperator", "Forbid    ", null));
        forbidAllButton.setText(com.trolltech.qt.core.QCoreApplication.translate("LeftTurnLinksOperator", "Forbid All", null));
        closeButton.setText(com.trolltech.qt.core.QCoreApplication.translate("LeftTurnLinksOperator", "Close     ", null));
    } // retranslateUi

}

