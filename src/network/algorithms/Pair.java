/* --------------------------------------------------------------------
 * Pair.java
 * --------------------------------------------------------------------
 * (C) Copyright 2015-2016, by Huijun Liu and all Contributors.
 *
 * Original Author:  Huijun Liu
 * Contributor(s):   Qinrui Tang
 *
 * Last Change Date: 19.04.2016
 * 
 * Function:
 *           1.二维数据结构辅助类
 *           
 */
package network.algorithms;

import java.io.Serializable;

public class Pair<K, V> implements Serializable
{
	private static final long serialVersionUID = 1L;
	private K key;
	private V value;

	/**
	 * @param key
	 * @param value
	 */
	public Pair(K key, V value)
	{
		this.key = key;
		this.value = value;
	}

	/**
	 * @return
	 */
	public K getKey()
	{
		return key;
	}

	/**
	 * @return
	 */
	public V getValue()
	{
		return value;
	}

	/* （非 Javadoc）
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return key + "=" + value;
	}

	/* （非 Javadoc）
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return key.hashCode() * 13 + (value == null ? 0 : value.hashCode());
	}

	/* （非 Javadoc）
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o instanceof Pair)
		{
			Pair pair = (Pair) o;
			if (key != null ? !key.equals(pair.key) : pair.key != null)
				return false;
			if (value != null ? !value.equals(pair.value) : pair.value != null)
				return false;
			return true;
		}
		return false;
	}
}