package de.mixedfx.network;

import java.io.IOException;

class UnknownNetworkErrorException extends IOException
{
	protected UnknownNetworkErrorException(String error)
	{
		super(error);
	}
}
