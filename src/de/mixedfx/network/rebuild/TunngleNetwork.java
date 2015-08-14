package de.mixedfx.network.rebuild;

public class TunngleNetwork extends OverlayNetwork
{
	@Override
	public String[] getRange()
	{
		return new String[]
		{ "7.0.0.0/8", "10.0.0.0/8" };
	}

}
