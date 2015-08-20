package de.mixedfx.windows;

public class NetworkAdapterNotFoundException extends Exception
{
	public NetworkAdapterNotFoundException(String adapterName)
	{
		super("Network adapter with name " + adapterName + " was not found!");
	}
}
