package de.mixedfx.list;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

public class ListMaker
{

	public static void main(final String[] args)
	{
		final SimpleListProperty<Identifiable> userList = new SimpleListProperty<>(FXCollections.observableArrayList());
		final SimpleListProperty<SessionInterface> ltuserList = new SimpleListProperty<>(FXCollections.observableArrayList());

		for (int i = 9; i >= 3; i--)
		{
			ltuserList.add(new LTExUser(i));
		}

		for (int i = 7; i < 15; i++)
		{
			userList.add(new User(i));
		}

		ListLinker.link(userList, ltuserList);

		System.out.println(userList.size());
		System.out.println(ltuserList.size());
		userList.remove(0);
		System.out.println(userList.size());
		System.out.println(ltuserList.size());
	}
}
