/* --------------------------------------------------------------------
 * LocalXmlAttributeMatch.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 01.04.2016
 * 
 * Function:
 *           1.定义搜索本软件定义路网XML文件节点之过滤条件常量
 *           
 */
package network.importnetwork;

public class LocalXmlAttributeMatch
{
	//~Variables
	//--------------------------------------------------------------------------
	
	/*常量-指定要在XML文件中查找的带某个特定属性的节点*/
	public static final String JUNCTION      = "junction";       //路口
	public static final String INTCONNECTION = "intconnction";   //路口内部的通路
	public static final String PHASE         = "phase";          //路口交通灯
	public static final String REQUEST       = "request";        //路口交通灯冲突
	public static final String EXTLINK       = "extlink";        //连接路口于路口的道路--道路起始点定义为路口
	public static final String EXTCONNECTION = "extconnction";   //连接路口与路口的道路--道路起始点定义为路口内部点
	public static final String ORIGIN        = "origin";         //行驶路径的源点
	public static final String DESTINATION   = "destination";    //行驶路径的目标点
	
	//~Methods
	//--------------------------------------------------------------------------
}
