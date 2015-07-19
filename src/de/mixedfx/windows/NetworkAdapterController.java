package de.mixedfx.windows;

import de.mixedfx.java.ComplexString;

public class NetworkAdapterController
{
	private static final String	placeHolder		= "_";

	public static final String	enableCommand	= "netsh interface set interface " + NetworkAdapterController.placeHolder + " enabled";
	public static final String	disableCommand	= "netsh interface set interface " + NetworkAdapterController.placeHolder + " disabled";
	public static final String	statusCommand	= "netsh interface show interface " + NetworkAdapterController.placeHolder;

	public static void main(final String[] args)
	{
		System.out.println(NetworkAdapterController.isAdapterEnabled("HAMACHI"));
		NetworkAdapterController.enableAdapter("HAMACHI");
		System.out.println(NetworkAdapterController.isAdapterEnabled("HAMACHI"));
		NetworkAdapterController.disableAdaptar("HAMACHI");
		System.out.println(NetworkAdapterController.isAdapterEnabled("HAMACHI"));
	}

	public static void disableAdaptar(final String name)
	{
		Executor.runAndWaitForOutput(NetworkAdapterController.disableCommand.replace(NetworkAdapterController.placeHolder, name));
		while (NetworkAdapterController.isAdapterEnabled(name))
		{
			;
		}
	}

	public static void enableAdapter(final String name)
	{
		Executor.runAndWaitForOutput(NetworkAdapterController.enableCommand.replace(NetworkAdapterController.placeHolder, name));
		while (!NetworkAdapterController.isAdapterEnabled(name))
		{
			;
		}
	}

	public static boolean isAdapterEnabled(final String name)
	{
		final ComplexString response = Executor.runAndWaitForOutput(NetworkAdapterController.statusCommand.replace(NetworkAdapterController.placeHolder, name));

		try
		{
			return response.containsAllRows("Verwaltungsstatus", "Aktiviert");
		}
		catch (final Exception e)
		{
			return false;
		}
	}
}
