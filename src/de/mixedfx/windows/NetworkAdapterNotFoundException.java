package de.mixedfx.windows;

public class NetworkAdapterNotFoundException extends Exception {
	public NetworkAdapterNotFoundException(String adapterName) {
		super(adapterName + " was not found!");
	}
}
