package de.mixedfx.network;

public class TunngleNetwork extends OverlayNetwork
{
	@Override
	public String[] getRange()
	{
		return new String[]
		{ "7.0.0.0/8", "10.0.0.0/8" };
	}

	@Override
	public String toString()
	{
		return "Tunngle";
	}
}
