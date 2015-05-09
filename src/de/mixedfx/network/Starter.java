package de.mixedfx.network;

import java.net.InetAddress;

import javafx.collections.ListChangeListener;

public class Starter
{
	public static TCPCoordinator	t;
	public static UDPCoordinator	u;

	public static void main(final String[] args)
	{
		Starter.t = new TCPCoordinator();
		Starter.u = new UDPCoordinator();

		Starter.u.allAdresses.addListener((ListChangeListener<InetAddress>) c ->
		{
			c.next();
			System.out.println("ALL: " + c.getAddedSubList().get(0));
		});
		Starter.u.allServerAdresses.addListener((ListChangeListener<InetAddress>) c ->
		{
			c.next();
			System.out.println("SERVER: " + c.getAddedSubList().get(0));
		});

		while (true)
			;
	}
}
