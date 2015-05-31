package de.mixedfx._network;

import java.util.function.Predicate;

public class UserPredicates
{
	public static Predicate<User> getByPID(final Integer pid)
	{
		return t -> t.pid == pid;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Object> org.apache.commons.collections.Predicate toApachePredicate(final Predicate<T> predicate)
	{
		return object ->
		{
			return predicate.test((T) object);
		};
	}
}
