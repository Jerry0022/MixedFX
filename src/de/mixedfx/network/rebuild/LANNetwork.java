package de.mixedfx.network.rebuild;

public class LANNetwork extends OverlayNetwork
{

	@Override
	public String[] getRange()
	{
		return new String[]
		{ "192.168.0.0/16" };
	}

}
