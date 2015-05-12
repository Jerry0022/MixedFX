package de.mixedfx.network.messages;

import java.util.ArrayList;

import de.mixedfx.network.user.User;

public class UserListMessage extends Message
{
	private final ArrayList<User>	list;

	public UserListMessage(final ArrayList<User> list)
	{
		this.list = list;
	}

	public ArrayList<User> getList()
	{
		return this.list;
	}
}
