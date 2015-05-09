package de.mixedfx.network;

import javafx.beans.value.ChangeListener;
import de.mixedfx.network.Overall.NetworkStatus;

public class TCPCoordinator
{
	public TCPCoordinator()
	{
		Overall.status.addListener((ChangeListener<NetworkStatus>) (observable, oldValue, newValue) ->
		{
			synchronized (Overall.status)
			{
				// TODO If server = new open TCP Server
				// TODO Else if old = server close TCP Server
			}
		});

		synchronized (Overall.status)
		{
			/**
			 * <pre>
			 * TODO If TCP Client Connection fails
			 * => Set to Unbound
			 * => Close TCP Server
			 * </pre>
			 */
		}
	}
}
