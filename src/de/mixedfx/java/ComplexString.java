package de.mixedfx.java;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

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
	 * Goes through all lines. Each line which content matches the indicator the line must contain also all the needles.
	 *
	 * @param indicator
	 *            The indicator which is ALWAYS present. (case insensitive)
	 * @param needles
	 *            All strings which shall be in a line with the indicator. (case insensitive)
	 * @return Returns true if indicator found and all needles, too. Returns false if at least in one line with the indicator has not all needles.
	 * @throws Exception
	 *             If indicator can't be found.
	 */
	public boolean containsAllRows(final String indicator, final String... needles) throws Exception
	{
		boolean result = true;

		boolean hasIndicator = false;
		for (final String s : this)
		{
			if (StringUtils.containsIgnoreCase(s, indicator))
			{
				hasIndicator = true;
				for (final String needle : needles)
				{
					if (!StringUtils.containsIgnoreCase(s, needle))
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

	@Override
	public String toString()
	{
		String result = "";
		for (String line : this)
			result += line + "\n";
		return result;
	}
}