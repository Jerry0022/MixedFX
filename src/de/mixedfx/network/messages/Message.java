package de.mixedfx.network.messages;

import java.io.Serializable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

/**
 * Master Message object which contains the information whether the message was sent by the server.
 * All sub classes must declare {@link Expose} to let the fields being serialized.
 *
 * @author Jerry
 */
@SuppressWarnings("serial")
public class Message implements Serializable
{
	private static Gson	gson;

	private static Gson getGson()
	{
		if (Message.gson == null)
			Message.gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		return Message.gson;
	}

	public static Message fromGSON(final String json)
	{
		return Message.getGson().fromJson(json, Message.class);
	}

	public static String toGSON(final Message message)
	{
		return Message.getGson().toJson(message);
	}

	/*
	 * START Message object
	 */

	@Expose
	public boolean	fromServer;

	public Message()
	{
		this.fromServer = false;
	}
}
