package de.mixedfx.java;

import java.util.ArrayList;

/**
 * Each Row of the String should be an element.
 *
 * @author Jerry
 */
@SuppressWarnings("serial")
public class ComplexString extends ArrayList<String>
{
	public ComplexString()
	{
	}

	/**
	 *
	 * If at least one of theses matched lines doesn't contain the needle it returns false,
	 * otherwise true;
	 *
	 * @param indicator
	 * @param needle
	 * @return
	 */
	/**
	 * Goes through all lines. Each line which content matches the indicator must contain also the .
	 *
	 * @param indicator
	 * @param needles
	 * @return
	 * @throws Exception
	 */
	public boolean containsAllRows(final String indicator, final String... needles) throws Exception
	{
		boolean result = true;

		boolean hasIndicator = false;
		for (final String s : this)
		{
			if (s.contains(indicator))
			{
				hasIndicator = true;
				for (final String needle : needles)
				{
					if (!s.contains(needle))
					{
						return result = false;
					}
				}
			}
		}

		if (!hasIndicator)
		{
			new Exception("Didn't find indicator! Indicator: " + indicator).printStackTrace();
		}

		return result;
	}
}