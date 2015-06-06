package de.mixedfx.network.examples;

import de.mixedfx.logging.Log;
import de.mixedfx.network.ServiceManager.P2PService;

public class UserManager implements P2PService
{
	/*
	 * TODO Listen to UDPCoordinator.allAddresses to know which InetAdresses can be reached! How to
	 * connect this information to the User? May implement a service who just broadcast his
	 * IP-Addresses and his user identification if the network devices online state changed.
	 */

	@Override
	public void stop()
	{
		Log.network.debug("UserManager stopped!");
		// TODO Unlisten
		// TODO Clear UserList
	}

	@Override
	public void start()
	{
		Log.network.debug("UserManager started!");
		// TODO Fire myUser as broadcast
		// TODO Listen to PIDs to update list
		// TODO Listen to PIDs to send the myUser to the new one
	}

}
