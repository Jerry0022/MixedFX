package de.mixedfx.java;

import java.util.function.Predicate;

public class ApacheTools
{
	/**
	 * Returns a {@link org.apache.commons.collections.Predicate} of {@link Predicate} without type
	 * check!
	 *
	 * @param predicate
	 *            A {@link Predicate} to convert.
	 * @return Returns a {@link org.apache.commons.collections.Predicate} representation.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Object> org.apache.commons.collections.Predicate convert(final Predicate<T> predicate)
	{
		return object ->
		{
			return predicate.test((T) object);
		};
	}
}
