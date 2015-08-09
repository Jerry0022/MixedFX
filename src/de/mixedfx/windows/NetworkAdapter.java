package de.mixedfx.windows;

public class NetworkAdapter {
	public String name = "";
	public boolean enabled = false;
	public boolean connected = false;

	@Override
	public String toString() {
		return "NetworkAdapter " + name + " is " + (enabled ? "enabled" : "disabled") + " and " + (connected ? "connected" : "disconnected");
	}
}
