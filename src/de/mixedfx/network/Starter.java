package de.mixedfx.network;

import java.net.InetAddress;

import javafx.collections.ListChangeListener;

public class Starter
{
	public static void main(final String[] args)
	{
		final TCPCoordinator t = new TCPCoordinator();
		final UDPCoordinator u = new UDPCoordinator();
		u.allAdresses.addListener((ListChangeListener<InetAddress>) c ->
		{
			c.next();
			System.out.println("ALL: " + c.getAddedSubList().get(0));
		});
		u.allServerAdresses.addListener((ListChangeListener<InetAddress>) c ->
		{
			c.next();
			System.out.println("SERVER: " + c.getAddedSubList().get(0));
		});
		while (true)
			;
	}
}
