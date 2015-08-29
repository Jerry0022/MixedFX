package de.mixedfx.windows;

public class NetworkAdapter
{
	public String	name		= "";
	public boolean	enabled		= false;
	public boolean	connected	= false;

	public NetworkAdapter(final String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return "NetworkAdapter " + this.name + " is " + (this.enabled ? "enabled" : "disabled") + " and " + (this.connected ? "connected" : "disconnected");
	}
}
