package de.mixedfx.network.examples;

import de.mixedfx.logging.Log;
import de.mixedfx.network.ServiceManager.UniqueService;
import de.mixedfx.network.messages.RegisteredMessage;

public class ExampleUniqueService implements UniqueService
{

	@Override
	public void stop()
	{
		Log.network.debug("STOP");

	}

	@Override
	public void client()
	{
		Log.network.debug("CLIENT");

	}

	@Override
	public void host()
	{
		Log.network.debug("HOST");
	}

	@Override
	public RegisteredMessage receive(final RegisteredMessage message)
	{
		return null;
	}

}
