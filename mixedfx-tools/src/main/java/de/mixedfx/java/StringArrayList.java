package de.mixedfx.java;

import java.util.ArrayList;
import java.util.Collection;

/**
 * An Arraylist of String which doesn't look on the case if you call {@link #contains(Object)}.
 * 
 * @author Jerry
 */
@SuppressWarnings("serial")
public class StringArrayList extends ArrayList<String>
{
	public StringArrayList(final Collection<String> initial)
	{
		this.addAll(initial);
	}

	@Override
	public boolean contains(final Object o)
	{
		if (o instanceof String)
		{
			final String paramStr = (String) o;
			for (final String s : this)
			{
				if (paramStr.equalsIgnoreCase(s))
				{
					return true;
				}
			}
		}
		return false;
	}
}