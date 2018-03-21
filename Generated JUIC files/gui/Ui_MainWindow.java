/********************************************************************************
** Form generated from reading ui file 'MainWindow.jui'
**
** Created: ÖÜËÄ 6ÔÂ 1 22:35:38 2017
**      by: Qt User Interface Compiler version 4.5.2
**
** WARNING! All changes made in this file will be lost when recompiling ui file!
********************************************************************************/

package gui;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

public class Ui_MainWindow implements com.trolltech.qt.QUiForm<QMainWindow>
{
    public QAction actionNetworkConvert;
    public QAction actionLoadLocalNetwork;
    public QAction actionAboutJambi;
    public QAction actionAboutThisSoftware;
    public QAction actionQuit;
    public QAction actionLocateVertex;
    public QAction actionLocateLink;
    public QAction actionLocateJunctions;
    public QAction actionZoomIn;
    public QAction actionZoomOut;
    public QAction actionAdjust;
    public QAction actionRun;
    public QAction actionStop;
    public QAction actionScreenshot;
    public QAction actionLocateLeftTurnLinks;
    public QAction actionLocalNetworkSimplify;
    public QAction actionEditODDemand;
    public QWidget centralwidget;
    public QGridLayout gridLayout;
    public QVBoxLayout verticalLayout;
    public QHBoxLayout horizontalLayout;
    public QLabel labelShow;
    public QSpacerItem horizontalSpacer;
    public QLCDNumber lcdNumber;
    public QGraphicsView graphicsView;
    public QHBoxLayout horizontalLayout_2;
    public QTextBrowser textBrowser;
    public QMenuBar menubar;
    public QMenu menuStart;
    public QMenu menuHelp;
    public QMenu menuLocate;
    public QMenu menuEdit;
    public QMenu menuSimulate;
    public QStatusBar statusbar;
    public QToolBar networkToolBar;
    public QToolBar graphShowToolBar;
    public QToolBar simulationToolBar;
    public QToolBar locateToolBar;

    public Ui_MainWindow() { super(); }

    public void setupUi(QMainWindow MainWindow)
    {
        MainWindow.setObjectName("MainWindow");
        MainWindow.resize(new QSize(1031, 640).expandedTo(MainWindow.minimumSizeHint()));
        MainWindow.setWindowIcon(new QIcon(new QPixmap("classpath:resource/png/Applications.png")));
        actionNetworkConvert = new QAction(MainWindow);
        actionNetworkConvert.setObjectName("actionNetworkConvert");
        actionNetworkConvert.setIcon(new QIcon(new QPixmap("classpath:resource/png/Refresh.png")));
        actionLoadLocalNetwork = new QAction(MainWindow);
        actionLoadLocalNetwork.setObjectName("actionLoadLocalNetwork");
        actionLoadLocalNetwork.setIcon(new QIcon(new QPixmap("classpath:resource/png/Go In.png")));
        actionAboutJambi = new QAction(MainWindow);
        actionAboutJambi.setObjectName("actionAboutJambi");
        actionAboutJambi.setIcon(new QIcon(new QPixmap("classpath:resource/png/Info2.png")));
        actionAboutThisSoftware = new QAction(MainWindow);
        actionAboutThisSoftware.setObjectName("actionAboutThisSoftware");
        actionAboutThisSoftware.setIcon(new QIcon(new QPixmap("classpath:resource/png/Email.png")));
        actionQuit = new QAction(MainWindow);
        actionQuit.setObjectName("actionQuit");
        actionQuit.setIcon(new QIcon(new QPixmap("classpath:resource/png/Standby.png")));
        actionLocateVertex = new QAction(MainWindow);
        actionLocateVertex.setObjectName("actionLocateVertex");
        actionLocateVertex.setIcon(new QIcon(new QPixmap("classpath:resource/png/Player Record.png")));
        actionLocateLink = new QAction(MainWindow);
        actionLocateLink.setObjectName("actionLocateLink");
        actionLocateLink.setIcon(new QIcon(new QPixmap("classpath:resource/png/Direction Vert.png")));
        actionLocateJunctions = new QAction(MainWindow);
        actionLocateJunctions.setObjectName("actionLocateJunctions");
        actionLocateJunctions.setIcon(new QIcon(new QPixmap("classpath:resource/png/Card4.png")));
        actionZoomIn = new QAction(MainWindow);
        actionZoomIn.setObjectName("actionZoomIn");
        actionZoomIn.setIcon(new QIcon(new QPixmap("classpath:resource/png/Zoom In.png")));
        actionZoomOut = new QAction(MainWindow);
        actionZoomOut.setObjectName("actionZoomOut");
        actionZoomOut.setIcon(new QIcon(new QPixmap("classpath:resource/png/Zoom Out.png")));
        actionAdjust = new QAction(MainWindow);
        actionAdjust.setObjectName("actionAdjust");
        actionAdjust.setIcon(new QIcon(new QPixmap("classpath:resource/png/Directions.png")));
        actionRun = new QAction(MainWindow);
        actionRun.setObjectName("actionRun");
        actionRun.setIcon(new QIcon(new QPixmap("classpath:resource/png/Player Play.png")));
        actionStop = new QAction(MainWindow);
        actionStop.setObjectName("actionStop");
        actionStop.setIcon(new QIcon(new QPixmap("classpath:resource/png/Player Stop.png")));
        actionScreenshot = new QAction(MainWindow);
        actionScreenshot.setObjectName("actionScreenshot");
        actionScreenshot.setIcon(new QIcon(new QPixmap("classpath:resource/png/Photo.png")));
        actionLocateLeftTurnLinks = new QAction(MainWindow);
        actionLocateLeftTurnLinks.setObjectName("actionLocateLeftTurnLinks");
        actionLocateLeftTurnLinks.setIcon(new QIcon(new QPixmap("classpath:resource/png/Hand.png")));
        actionLocalNetworkSimplify = new QAction(MainWindow);
        actionLocalNetworkSimplify.setObjectName("actionLocalNetworkSimplify");
        actionLocalNetworkSimplify.setIcon(new QIcon(new QPixmap("classpath:resource/png/Graph.png")));
        actionEditODDemand = new QAction(MainWindow);
        actionEditODDemand.setObjectName("actionEditODDemand");
        actionEditODDemand.setIcon(new QIcon(new QPixmap("classpath:resource/png/Write.png")));
        centralwidget = new QWidget(MainWindow);
        centralwidget.setObjectName("centralwidget");
        gridLayout = new QGridLayout(centralwidget);
        gridLayout.setObjectName("gridLayout");
        verticalLayout = new QVBoxLayout();
        verticalLayout.setObjectName("verticalLayout");
        horizontalLayout = new QHBoxLayout();
        horizontalLayout.setObjectName("horizontalLayout");
        labelShow = new QLabel(centralwidget);
        labelShow.setObjectName("labelShow");
        labelShow.setAlignment(com.trolltech.qt.core.Qt.AlignmentFlag.createQFlags(com.trolltech.qt.core.Qt.AlignmentFlag.AlignBottom,com.trolltech.qt.core.Qt.AlignmentFlag.AlignLeft));

        horizontalLayout.addWidget(labelShow);

        horizontalSpacer = new QSpacerItem(40, 20, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding, com.trolltech.qt.gui.QSizePolicy.Policy.Minimum);

        horizontalLayout.addItem(horizontalSpacer);

        lcdNumber = new QLCDNumber(centralwidget);
        lcdNumber.setObjectName("lcdNumber");
        lcdNumber.setFrameShape(com.trolltech.qt.gui.QFrame.Shape.NoFrame);
        lcdNumber.setFrameShadow(com.trolltech.qt.gui.QFrame.Shadow.Sunken);
        lcdNumber.setSmallDecimalPoint(false);
        lcdNumber.setNumDigits(16);
        lcdNumber.setSegmentStyle(com.trolltech.qt.gui.QLCDNumber.SegmentStyle.Flat);

        horizontalLayout.addWidget(lcdNumber);


        verticalLayout.addLayout(horizontalLayout);

        graphicsView = new QGraphicsView(centralwidget);
        graphicsView.setObjectName("graphicsView");
        graphicsView.setVerticalScrollBarPolicy(com.trolltech.qt.core.Qt.ScrollBarPolicy.ScrollBarAlwaysOff);
        graphicsView.setHorizontalScrollBarPolicy(com.trolltech.qt.core.Qt.ScrollBarPolicy.ScrollBarAlwaysOff);

        verticalLayout.addWidget(graphicsView);

        horizontalLayout_2 = new QHBoxLayout();
        horizontalLayout_2.setObjectName("horizontalLayout_2");
        textBrowser = new QTextBrowser(centralwidget);
        textBrowser.setObjectName("textBrowser");
        textBrowser.setMaximumSize(new QSize(16777215, 100));

        horizontalLayout_2.addWidget(textBrowser);


        verticalLayout.addLayout(horizontalLayout_2);


        gridLayout.addLayout(verticalLayout, 0, 0, 1, 1);

        MainWindow.setCentralWidget(centralwidget);
        menubar = new QMenuBar(MainWindow);
        menubar.setObjectName("menubar");
        menubar.setGeometry(new QRect(0, 0, 1031, 23));
        menuStart = new QMenu(menubar);
        menuStart.setObjectName("menuStart");
        menuHelp = new QMenu(menubar);
        menuHelp.setObjectName("menuHelp");
        menuLocate = new QMenu(menubar);
        menuLocate.setObjectName("menuLocate");
        menuEdit = new QMenu(menubar);
        menuEdit.setObjectName("menuEdit");
        menuSimulate = new QMenu(menubar);
        menuSimulate.setObjectName("menuSimulate");
        MainWindow.setMenuBar(menubar);
        statusbar = new QStatusBar(MainWindow);
        statusbar.setObjectName("statusbar");
        MainWindow.setStatusBar(statusbar);
        networkToolBar = new QToolBar(MainWindow);
        networkToolBar.setObjectName("networkToolBar");
        MainWindow.addToolBar(com.trolltech.qt.core.Qt.ToolBarArea.TopToolBarArea, networkToolBar);
        graphShowToolBar = new QToolBar(MainWindow);
        graphShowToolBar.setObjectName("graphShowToolBar");
        MainWindow.addToolBar(com.trolltech.qt.core.Qt.ToolBarArea.TopToolBarArea, graphShowToolBar);
        simulationToolBar = new QToolBar(MainWindow);
        simulationToolBar.setObjectName("simulationToolBar");
        MainWindow.addToolBar(com.trolltech.qt.core.Qt.ToolBarArea.TopToolBarArea, simulationToolBar);
        locateToolBar = new QToolBar(MainWindow);
        locateToolBar.setObjectName("locateToolBar");
        MainWindow.addToolBar(com.trolltech.qt.core.Qt.ToolBarArea.TopToolBarArea, locateToolBar);

        menubar.addAction(menuStart.menuAction());
        menubar.addAction(menuEdit.menuAction());
        menubar.addAction(menuLocate.menuAction());
        menubar.addAction(menuSimulate.menuAction());
        menubar.addAction(menuHelp.menuAction());
        menuStart.addAction(actionNetworkConvert);
        menuStart.addAction(actionLoadLocalNetwork);
        menuStart.addSeparator();
        menuStart.addAction(actionQuit);
        menuHelp.addAction(actionAboutJambi);
        menuHelp.addAction(actionAboutThisSoftware);
        menuLocate.addAction(actionLocateJunctions);
        menuLocate.addAction(actionLocateVertex);
        menuLocate.addAction(actionLocateLink);
        menuLocate.addAction(actionLocateLeftTurnLinks);
        menuEdit.addAction(actionZoomOut);
        menuEdit.addAction(actionZoomIn);
        menuEdit.addAction(actionAdjust);
        menuEdit.addAction(actionScreenshot);
        menuEdit.addSeparator();
        menuEdit.addAction(actionLocalNetworkSimplify);
        menuEdit.addAction(actionEditODDemand);
        menuSimulate.addAction(actionRun);
        menuSimulate.addAction(actionStop);
        networkToolBar.addAction(actionNetworkConvert);
        networkToolBar.addAction(actionLocalNetworkSimplify);
        networkToolBar.addAction(actionLoadLocalNetwork);
        networkToolBar.addAction(actionEditODDemand);
        graphShowToolBar.addAction(actionZoomIn);
        graphShowToolBar.addAction(actionZoomOut);
        graphShowToolBar.addAction(actionAdjust);
        graphShowToolBar.addAction(actionScreenshot);
        simulationToolBar.addAction(actionRun);
        simulationToolBar.addAction(actionStop);
        locateToolBar.addAction(actionLocateVertex);
        locateToolBar.addAction(actionLocateLink);
        locateToolBar.addAction(actionLocateLeftTurnLinks);
        retranslateUi(MainWindow);

        MainWindow.connectSlotsByName();
    } // setupUi

    void retranslateUi(QMainWindow MainWindow)
    {
        MainWindow.setWindowTitle(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "MainWindow", null));
        actionNetworkConvert.setText(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Network Convert", null));
        actionNetworkConvert.setShortcut(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Ctrl+Alt+C", null));
        actionLoadLocalNetwork.setText(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Load Local Network", null));
        actionLoadLocalNetwork.setShortcut(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Ctrl+Alt+L", null));
        actionAboutJambi.setText(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "About Jambi", null));
        actionAboutThisSoftware.setText(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "About This Software", null));
        actionQuit.setText(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Quit", null));
        actionQuit.setShortcut(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Ctrl+Alt+Q", null));
        actionLocateVertex.setText(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Locate Vertexs", null));
        actionLocateLink.setText(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Locate Links", null));
        actionLocateJunctions.setText(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Locate Junctions", null));
        actionZoomIn.setText(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Zoom in", null));
        actionZoomIn.setShortcut(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Ctrl+=", null));
        actionZoomOut.setText(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Zoom out", null));
        actionZoomOut.setShortcut(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Ctrl+-", null));
        actionAdjust.setText(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Auto adjust", null));
        actionAdjust.setShortcut(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Ctrl+Alt+A", null));
        actionRun.setText(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Run", null));
        actionRun.setShortcut(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Ctrl+Alt+R", null));
        actionStop.setText(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Stop", null));
        actionStop.setShortcut(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Ctrl+Alt+T", null));
        actionScreenshot.setText(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Screenshot", null));
        actionScreenshot.setShortcut(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Ctrl+Alt+J", null));
        actionLocateLeftTurnLinks.setText(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Locate LeftTurnLinks", null));
        actionLocalNetworkSimplify.setText(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Local Network Simplify", null));
        actionLocalNetworkSimplify.setShortcut(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Ctrl+Alt+S", null));
        actionEditODDemand.setText(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Edit OD Demand", null));
        actionEditODDemand.setShortcut(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Ctrl+Alt+D", null));
        labelShow.setText(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Show Local Network", null));
        menuStart.setTitle(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Start", null));
        menuHelp.setTitle(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Help", null));
        menuLocate.setTitle(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Locate", null));
        menuEdit.setTitle(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Edit", null));
        menuSimulate.setTitle(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Simulate", null));
        networkToolBar.setWindowTitle(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "toolBar", null));
        graphShowToolBar.setWindowTitle(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "toolBar_2", null));
        simulationToolBar.setWindowTitle(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "toolBar_3", null));
        locateToolBar.setWindowTitle(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "toolBar", null));
    } // retranslateUi

}

