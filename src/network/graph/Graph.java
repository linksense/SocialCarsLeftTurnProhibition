package network.graph;
/**
 * @author Ansleliu
 * 使用邻接表存储图
 */
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class Graph implements Cloneable
{
	//~Variables
	//--------------------------------------------------------------------------
	//邻接表
	private Map<String, LinkedHashSet<String>> map = null;
	
	//~Methods
	//--------------------------------------------------------------------------
	public Graph()
	{
		map = new HashMap<String, LinkedHashSet<String>>();
	}
	
	/**
	 * @param node1
	 * @param node2
	 */
	public void addEdge(String from, String to)
	{
		LinkedHashSet<String> adjacent = map.get(from);
		if (adjacent == null)
		{
			adjacent = new LinkedHashSet<String>();
			map.put(from, adjacent);
		}
		adjacent.add(to);
	}

	/**
	 * @param node1
	 * @param node2
	 */
	public void addTwoWayVertex(String from, String to)
	{
		addEdge(from, to);
		addEdge(to, from);
	}

	/**
	 * @param node1
	 * @param node2
	 * @return
	 */
	public boolean isConnected(String from, String to)
	{
		Set<String> adjacent = map.get(from);
		if (adjacent == null)
		{
			return false;
		}
		return adjacent.contains(to);
	}
	
	/**
	 * @param last
	 * @return
	 */
	public LinkedList<String> adjacentNodes(String last)
	{
		LinkedHashSet<String> adjacent = map.get(last);
		if (adjacent == null)
		{
			return new LinkedList<String>();
		}
		return new LinkedList<String>(adjacent);
	}

	/* （非 Javadoc）
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone()
	{  
		Graph cloneGraph = null;
		try
		{
			cloneGraph = (Graph)super.clone();
		} 
		catch (CloneNotSupportedException e)
		{
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		return cloneGraph;  
	}  
	
	/**
	 * @return
	 */
	public Map<String, LinkedHashSet<String>> getMap()
	{
		return map;
	}
	
}
