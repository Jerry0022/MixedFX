package de.mixedfx.windows;

import de.mixedfx.java.ComplexString;

public class NetworkAdapterController
{
	private static final String placeHolder = "_";

	public static final String	enableCommand	= "netsh interface set interface " + NetworkAdapterController.placeHolder + " enabled";
	public static final String	disableCommand	= "netsh interface set interface " + NetworkAdapterController.placeHolder + " disabled";
	public static final String	statusCommand	= "netsh interface show interface " + NetworkAdapterController.placeHolder;

	/**
	 * Returns the state of a windows network adapter (/interface).
	 * 
	 * @param adapterName
	 *            The name of the adapter (attention this name may is user specific)!
	 */
	public static boolean isEnabled(final String adapterName)
	{
		final ComplexString response = Executor.runAndWaitForOutput(NetworkAdapterController.statusCommand.replace(NetworkAdapterController.placeHolder, adapterName));

		try
		{
			return response.containsAllRows("Verwaltungsstatus", "Aktiviert");
		}
		catch (final Exception e)
		{
			return false;
		}
	}

	/**
	 * Disables a windows network adapter (/interface).
	 * 
	 * @param adapterName
	 *            The name of the adapter (attention this name may is user specific)!
	 */
	public static void disable(final String adapterName)
	{
		if (!NetworkAdapterController.isEnabled(adapterName))
			return;
		Executor.runAndWaitForOutput(NetworkAdapterController.disableCommand.replace(NetworkAdapterController.placeHolder, adapterName));
		while (NetworkAdapterController.isEnabled(adapterName))
		{
			;
		}
	}

	/**
	 * Enables a windows network adapter (/interface).
	 * 
	 * @param adapterName
	 *            The name of the adapter (attention this name may is user specific)!
	 */
	public static void enable(final String adapterName)
	{
		if (NetworkAdapterController.isEnabled(adapterName))
			return;
		Executor.runAndWaitForOutput(NetworkAdapterController.enableCommand.replace(NetworkAdapterController.placeHolder, adapterName));
		while (!NetworkAdapterController.isEnabled(adapterName))
		{
			;
		}
	}
}
