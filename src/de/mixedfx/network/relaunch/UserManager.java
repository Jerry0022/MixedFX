package de.mixedfx.network.relaunch;

import de.mixedfx.network.relaunch.ServiceManager.P2PService;

public class UserManager implements P2PService
{
	@Override
	public void stop()
	{
		// TODO Unlisten
		// TODO Clear UserList
	}

	@Override
	public void start()
	{
		// TODO Fire myUser as broadcast
		// TODO Listen to PIDs to update list
		// TODO Listen to PIDs to send the myUser to the new one
	}

}
