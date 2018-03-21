/* --------------------------------------------------------------------
 * LeftTurnToProhibit.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 21.05.2016
 *
 * Function:
 *          1.计算所有可能的禁止左转的可能
 */
package leftTurnProhibition;

import java.util.ArrayList;
import java.util.List;
import network.Link;

/**
 * @author Ansleliu
 *
 */
public class LeftTurnToProhibit
{
	//~Variables
	//--------------------------------------------------------------------------
	
	
	//~Methods
	//--------------------------------------------------------------------------
	
	public ArrayList<ArrayList<Link>> subsets(List<Link> links)
	{
		if (links.isEmpty())
		{
			return null;
		}
		
		ArrayList<ArrayList<Link>> result = new ArrayList<ArrayList<Link>>();

		for(int i=0; i<links.size(); i++)
		{
			ArrayList<ArrayList<Link>> temp = new ArrayList<ArrayList<Link>>();

			//get sets that are already in result
			for(ArrayList<Link> a:result)
			{
				temp.add(new ArrayList<Link>(a));
			}

			//add links.get(i) to existing sets
			for (ArrayList<Link> a:temp)
			{
				a.add(links.get(i));
			}

			// add S[i] only as a set
			ArrayList<Link> single = new ArrayList<Link>();
			single.add(links.get(i));
			temp.add(single);

			result.addAll(temp);
			temp = null;
		}

		// add empty set
		result.add(new ArrayList<Link>());

		return result;
	}

	public ArrayList<ArrayList<Link>> subsetsWithDup(List<Link> links)
	{
		if (links.isEmpty())
			return null;

		ArrayList<ArrayList<Link>> result = new ArrayList<ArrayList<Link>>();
		ArrayList<ArrayList<Link>> prev = new ArrayList<ArrayList<Link>>();

		for(int i=links.size()-1; i>=0; i--)
		{
			// get existing sets
			if (i==links.size()-1 || links.get(i)!=links.get(i+1) || prev.size()==0)
			{
				prev = new ArrayList<ArrayList<Link>>();
				for (int j = 0; j < result.size(); j++)
				{
					prev.add(new ArrayList<Link>(result.get(j)));
				}
			}

			// add current number to each element of the set
			for (ArrayList<Link> temp : prev)
			{
				temp.add(0, links.get(i));
			}

			// add each single number as a set, only if current element is
			// different with previous
			if (i==links.size()-1 || links.get(i)!=links.get(i+1))
			{
				ArrayList<Link> temp = new ArrayList<Link>();
				temp.add(links.get(i));
				prev.add(temp);
			}

			// add all set created in this iteration
			for (ArrayList<Link> temp : prev)
			{
				result.add(new ArrayList<Link>(temp));
			}
		}

		// add empty set
		result.add(new ArrayList<Link>());

		return result;
	}
}
