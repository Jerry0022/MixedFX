package de.mixedfx.windows;

public class NetworkAdapterNotFoundException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3353469369145762761L;

	public NetworkAdapterNotFoundException(String adapterName)
	{
		super("Network adapter with name " + adapterName + " was not found!");
	}
}
