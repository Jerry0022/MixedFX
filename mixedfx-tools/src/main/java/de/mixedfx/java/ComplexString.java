package de.mixedfx.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

import de.mixedfx.logging.Log;

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
		super();
	}

	public ComplexString(final Collection<String> lines)
	{
		super(lines);
	}

	public ComplexString(final String[] lines)
	{
		super(Arrays.asList(lines));
	}

	/**
	 * Goes through all lines. Each line which content matches the indicator the line must contain also all the needles.
	 *
	 * @param indicator
	 *            The indicator which is ALWAYS present. (case insensitive)
	 * @param needles
	 *            All strings which shall be in a line with the indicator. (case sensitive)
	 * @return Returns true if indicator found and all needles, too. Returns false if at least in one line with the indicator has not all needles.
	 */
	public boolean containsAllRows(final String indicator, final String... needles)
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
					if (!s.contains(needle))
					{
						return result = false;
					}
				}
			}
		}

		if (!hasIndicator)
			Log.DEFAULT.error(new IllegalArgumentException("Didn't find indicator! Indicator: " + indicator));

		return result;
	}

	/**
	 * Goes through all lines. One of the lines which content matches the indicator the line must contain also all the needles.
	 *
	 * @param indicator
	 *            The indicator which is ALWAYS present. (case insensitive)
	 * @param needles
	 *            All strings which shall be in a line with the indicator. (case insensitive)
	 * @return Returns true if indicator found and at least once all needles, too. Returns false if not even one line with the indicator has all
	 *         needles.
	 */
	public boolean containsOneRow(final String indicator, final String... needles)
	{
		boolean hasIndicator = false;
		for (final String s : this)
		{
			if (StringUtils.containsIgnoreCase(s, indicator))
			{
				hasIndicator = true;
				boolean hasAllNeedles = true;
				for (final String needle : needles)
				{
					if (!StringUtils.containsIgnoreCase(s, needle))
					{
						hasAllNeedles = false;
					}
				}
				if (hasAllNeedles)
					return true;
			}
		}

		if (!hasIndicator)
			Log.DEFAULT.error(new IllegalArgumentException("Didn't find indicator! Indicator: " + indicator));

		return false;
	}

	@Override
	public String toString()
	{
		String result = "";
		for (final String line : this)
			result += line + "\n";
		return result;
	}
}