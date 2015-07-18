package de.mixedfx.java;

public class StringTool
{
	public static boolean validate(final String toValidate, final int minLength, final int maxLength, final String... blackPattern)
	{
		if (!(toValidate.length() >= minLength && toValidate.length() <= maxLength))
		{
			return false;
		}

		for (final String pattern : blackPattern)
		{
			if (toValidate.contains(pattern))
			{
				return false;
			}
		}

		return true;
	}

	public static String multiplicateString(final String toMultiplicateString, final int times)
	{
		String result = "";

		for (int i = 0; i < times; i++)
		{
			result += toMultiplicateString;
		}

		return result;
	}
}
