package de.mixedfx.network;

public class HamachiNetwork extends OverlayNetwork
{
	@Override
	public String[] getRange()
	{
		return new String[]
		{ "25.0.0.0/8" };
	}

	@Override
	public String toString()
	{
		return "Hamachi";
	}
}
