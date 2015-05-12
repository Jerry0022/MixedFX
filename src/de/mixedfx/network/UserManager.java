package de.mixedfx.network;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import de.mixedfx.network.user.User;

public class UserManager
{
	public ListProperty<User>	list;

	public UserManager(final User myUser)
	{
		this.list = new SimpleListProperty<>(FXCollections.observableArrayList());
		this.list.add(myUser);

		NetworkManager.online.addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) ->
		{
			synchronized (this.list)
			{
				if (newValue.booleanValue())
				{
					if (NetworkConfig.status.get().equals(NetworkConfig.States.Server))
					{

					}
					else
					{

					}
					// Host User Management
					// Register on Server
					// Send request of user list
				}
				else
				{
					// clear list
				}
			}
		});
	}
}
