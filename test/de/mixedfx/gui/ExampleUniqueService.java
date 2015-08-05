package de.mixedfx.gui;

import de.mixedfx.logging.Log;
import de.mixedfx.network.ServiceManager.UniqueService;
import de.mixedfx.network.messages.RegisteredMessage;

public class ExampleUniqueService implements UniqueService
{

	@Override
	public void stop()
	{
		Log.network.debug("ExampleUniqueService stopped!");

	}

	@Override
	public void client()
	{
		Log.network.debug("ExampleUniqueService started as client!");

	}

	@Override
	public void host()
	{
		Log.network.debug("ExampleUniqueService started as host!");
	}

	@Override
	public RegisteredMessage hostReceive(final RegisteredMessage message)
	{
		return null;
	}

}
