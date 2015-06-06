package de.mixedfx.network.examples;

import de.mixedfx.network.ServiceManager.UniqueService;
import de.mixedfx.network.messages.RegisteredMessage;

public class ExampleUniqueService implements UniqueService
{

	@Override
	public void stop()
	{
		System.err.println("STOP");

	}

	@Override
	public void client()
	{
		System.err.println("CLIENT");

	}

	@Override
	public void host()
	{
		System.err.println("HOST");

	}

	@Override
	public RegisteredMessage receive(final RegisteredMessage message)
	{
		return null;
	}

}
