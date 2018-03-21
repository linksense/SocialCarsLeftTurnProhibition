/* --------------------------------------------------------------------
 * SignalTimingFactory.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and all Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 06.05.2017
 * 
 * Function:
 *           
 */
package signalOptimization;

import ilog.cplex.IloCplex;

public class SignalTimingFactory
{
	public SignalTiming getSignalTiming(IloCplex cplex, String methodeType)
	{	
	      if(methodeType.equalsIgnoreCase("StageBased"))
	      {
	         return new StagebasedSignalTiming(cplex);
	      } 
	      else if(methodeType.equalsIgnoreCase("LaneBased"))
	      {
	         return new LanebasedSinalTiming(cplex);
	      }
	      else
	      {
	    	  System.out.print(" 不存在的SignalTiming方法! ");
	    	  System.exit(0);
	      }
	      return null;
	}
}
