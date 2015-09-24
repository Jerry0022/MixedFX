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

	/**
	 * Uses {@link GsonBuilder#excludeFieldsWithoutExposeAnnotation()}.
	 * 
	 * @param type The object's class.
	 * @param json The json String representing an object of type type.
	 * @return Returns an object from type type or null if json is null.
	 */
	public static <Type> Type fromGSON(final Class<Type> type, final String json)
	{
		return EasyGson.getGson().fromJson(json, type);
	}

	/**
	 * Uses {@link GsonBuilder#excludeFieldsWithoutExposeAnnotation()}.
	 * 
	 * @param toConvert The object which shall be converted to a String.
	 * @return Returns a String representation of the object.
	 */
	public static String toGSON(final Object toConvert)
	{
		return EasyGson.getGson().toJson(toConvert);
	}
}
