package de.mixedfx.network.archive;

import rice.p2p.commonapi.Message;

public class MyMessage implements Message
{

	private static final long	serialVersionUID	= 1L;

	private final String		message;

	public MyMessage(final String msg)
	{
		this.message = msg;
	}

	public String getMessage()
	{
		return this.message;
	}

	@Override
	public int getPriority()
	{
		// TODO Auto-generated method stub
		return 0;
	}

}
