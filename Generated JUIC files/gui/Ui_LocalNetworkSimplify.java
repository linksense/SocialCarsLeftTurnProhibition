/********************************************************************************
** Form generated from reading ui file 'LocalNetworkSimplify.jui'
**
** Created: ÖÜËÄ 6ÔÂ 1 22:35:37 2017
**      by: Qt User Interface Compiler version 4.5.2
**
** WARNING! All changes made in this file will be lost when recompiling ui file!
********************************************************************************/

package gui;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

public class Ui_LocalNetworkSimplify implements com.trolltech.qt.QUiForm<QDialog>
{
    public QGridLayout gridLayout_2;
    public QVBoxLayout verticalLayout;
    public QGridLayout gridLayout;
    public QLabel originalNetLabel;
    public QLineEdit originalNetLineEdit;
    public QPushButton selectButton;
    public QLabel simplifiedNetLabel;
    public QLineEdit simplifiedNetLineEdit;
    public QSpacerItem horizontalSpacer;
    public QHBoxLayout horizontalLayout;
    public QSpacerItem horizontalSpacer_2;
    public QPushButton okButton;
    public QPushButton cancelButton;

    public Ui_LocalNetworkSimplify() { super(); }

    public void setupUi(QDialog LocalNetworkSimplify)
    {
        LocalNetworkSimplify.setObjectName("LocalNetworkSimplify");
        LocalNetworkSimplify.resize(new QSize(400, 112).expandedTo(LocalNetworkSimplify.minimumSizeHint()));
        LocalNetworkSimplify.setMaximumSize(new QSize(400, 112));
        LocalNetworkSimplify.setWindowIcon(new QIcon(new QPixmap("classpath:resource/png/Graph.png")));
        gridLayout_2 = new QGridLayout(LocalNetworkSimplify);
        gridLayout_2.setObjectName("gridLayout_2");
        verticalLayout = new QVBoxLayout();
        verticalLayout.setObjectName("verticalLayout");
        gridLayout = new QGridLayout();
        gridLayout.setObjectName("gridLayout");
        originalNetLabel = new QLabel(LocalNetworkSimplify);
        originalNetLabel.setObjectName("originalNetLabel");

        gridLayout.addWidget(originalNetLabel, 0, 0, 1, 1);

        originalNetLineEdit = new QLineEdit(LocalNetworkSimplify);
        originalNetLineEdit.setObjectName("originalNetLineEdit");
        originalNetLineEdit.setMinimumSize(new QSize(220, 20));
        originalNetLineEdit.setMaximumSize(new QSize(220, 20));

        gridLayout.addWidget(originalNetLineEdit, 0, 1, 1, 1);

        selectButton = new QPushButton(LocalNetworkSimplify);
        selectButton.setObjectName("selectButton");
        selectButton.setIcon(new QIcon(new QPixmap("classpath:resource/png/Folder.png")));

        gridLayout.addWidget(selectButton, 0, 2, 1, 1);

        simplifiedNetLabel = new QLabel(LocalNetworkSimplify);
        simplifiedNetLabel.setObjectName("simplifiedNetLabel");

        gridLayout.addWidget(simplifiedNetLabel, 1, 0, 1, 1);

        simplifiedNetLineEdit = new QLineEdit(LocalNetworkSimplify);
        simplifiedNetLineEdit.setObjectName("simplifiedNetLineEdit");
        simplifiedNetLineEdit.setMinimumSize(new QSize(220, 20));
        simplifiedNetLineEdit.setMaximumSize(new QSize(220, 20));

        gridLayout.addWidget(simplifiedNetLineEdit, 1, 1, 1, 1);

        horizontalSpacer = new QSpacerItem(68, 20, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding, com.trolltech.qt.gui.QSizePolicy.Policy.Minimum);

        gridLayout.addItem(horizontalSpacer, 1, 2, 1, 1);


        verticalLayout.addLayout(gridLayout);

        horizontalLayout = new QHBoxLayout();
        horizontalLayout.setObjectName("horizontalLayout");
        horizontalSpacer_2 = new QSpacerItem(40, 20, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding, com.trolltech.qt.gui.QSizePolicy.Policy.Minimum);

        horizontalLayout.addItem(horizontalSpacer_2);

        okButton = new QPushButton(LocalNetworkSimplify);
        okButton.setObjectName("okButton");
        okButton.setIcon(new QIcon(new QPixmap("classpath:resource/png/Ok.png")));

        horizontalLayout.addWidget(okButton);

        cancelButton = new QPushButton(LocalNetworkSimplify);
        cancelButton.setObjectName("cancelButton");
        cancelButton.setIcon(new QIcon(new QPixmap("classpath:resource/png/Cancel.png")));

        horizontalLayout.addWidget(cancelButton);


        verticalLayout.addLayout(horizontalLayout);


        gridLayout_2.addLayout(verticalLayout, 0, 0, 1, 1);

        retranslateUi(LocalNetworkSimplify);

        LocalNetworkSimplify.connectSlotsByName();
    } // setupUi

    void retranslateUi(QDialog LocalNetworkSimplify)
    {
        LocalNetworkSimplify.setWindowTitle(com.trolltech.qt.core.QCoreApplication.translate("LocalNetworkSimplify", "Local Network Simplify", null));
        originalNetLabel.setText(com.trolltech.qt.core.QCoreApplication.translate("LocalNetworkSimplify", "Original    Net:", null));
        selectButton.setText(com.trolltech.qt.core.QCoreApplication.translate("LocalNetworkSimplify", "Select", null));
        simplifiedNetLabel.setText(com.trolltech.qt.core.QCoreApplication.translate("LocalNetworkSimplify", "Simplified Net:", null));
        okButton.setText(com.trolltech.qt.core.QCoreApplication.translate("LocalNetworkSimplify", "OK", null));
        cancelButton.setText(com.trolltech.qt.core.QCoreApplication.translate("LocalNetworkSimplify", "Cancel", null));
    } // retranslateUi

}

