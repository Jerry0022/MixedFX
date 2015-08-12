package de.mixedfx.network.p2p;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;

public class P2PMessage implements Message
{
	private Id from;
	private Id to;

	public P2PMessage(Id from, Id to)
	{
		this.from = from;
		this.to = to;
	}

	@Override
	public String toString()
	{
		return "MyMsg from " + from + " to " + to;
	}

	@Override
	public int getPriority()
	{
		// TODO Auto-generated method stub
		return 0;
	}

}
