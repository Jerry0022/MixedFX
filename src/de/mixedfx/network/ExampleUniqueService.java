package de.mixedfx.network;

import de.mixedfx.network.ServiceManager.UniqueService;

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

}
