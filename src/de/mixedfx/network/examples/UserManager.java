package de.mixedfx.network.examples;

import de.mixedfx.network.ServiceManager.P2PService;

public class UserManager implements P2PService
{
	@Override
	public void stop()
	{
		System.err.println("STOP CALLED");
		// TODO Unlisten
		// TODO Clear UserList
	}

	@Override
	public void start()
	{
		System.err.println("START CALLED");
		// TODO Fire myUser as broadcast
		// TODO Listen to PIDs to update list
		// TODO Listen to PIDs to send the myUser to the new one
	}

}
