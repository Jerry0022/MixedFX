package de.mixedfx.network.messages;

import java.util.ArrayList;
import java.util.UUID;

import com.google.gson.annotations.Expose;

@SuppressWarnings("serial")
public class ParticipantMessage extends Message
{
	@Expose
	public String				uID;

	@Expose
	public ArrayList<Integer>	ids;

	public ParticipantMessage()
	{
		this.uID = UUID.randomUUID().toString();
		this.ids = new ArrayList<>();
	}
}
