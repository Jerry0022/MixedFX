package de.mixedfx.network.archive;

import java.io.IOException;

class UnknownNetworkErrorException extends IOException
{
	protected UnknownNetworkErrorException(String error)
	{
		super(error);
	}
}
