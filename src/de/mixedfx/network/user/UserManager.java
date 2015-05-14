package de.mixedfx.network.user;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

public class UserManager
{
	public ListProperty<User>	list;

	public UserManager(final User myUser)
	{
		this.list = new SimpleListProperty<>(FXCollections.observableArrayList());
		this.list.add(myUser);

		// NetworkManager.online.addListener((ChangeListener<Boolean>) (observable, oldValue,
		// newValue) ->
		// {
		// synchronized (this.list)
		// {
		// if (newValue.booleanValue())
		// {
		// if (NetworkManager.online.get())
		// {
		//
		// }
		// else
		// {
		//
		// }
		// // Host User Management
		// // Register on Server
		// // Send request of user list
		// }
		// else
		// {
		// // clear list
		// }
		// }
		// });
	}
}
