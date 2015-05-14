package de.mixedfx.java;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class EasyGson
{
	private static Gson	gson;

	private static Gson getGson()
	{
		if (EasyGson.gson == null)
			EasyGson.gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

		return EasyGson.gson;
	}

	public static <Type> Type fromGSON(final Class<Type> type, final String json)
	{
		return EasyGson.getGson().fromJson(json, type);
	}

	public static String toGSON(final Object message)
	{
		return EasyGson.getGson().toJson(message);
	}
}
