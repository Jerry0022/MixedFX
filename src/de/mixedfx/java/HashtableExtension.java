package de.mixedfx.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

public class HashtableExtension
{
	/**
	 * @param hashtable
	 * @return Returns the a current picture of the Hashtable as {@link ArrayList}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static ArrayList getValuesAsArrayList(final Hashtable hashtable)
	{
		if (hashtable.size() == 0)
			return new ArrayList();
		else
			return new ArrayList(Arrays.asList(hashtable.values().toArray()));
	}
}
