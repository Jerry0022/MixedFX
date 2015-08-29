package de.mixedfx.windows;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import de.mixedfx.java.ComplexString;
import de.mixedfx.logging.Log;

public class NetworkAdapterController
{
	private static final String placeHolder = "_";

	private static final String	exeFile			= "netsh";
	private static final String	enableCommand	= "interface set interface " + NetworkAdapterController.placeHolder + " enabled";
	private static final String	disableCommand	= "interface set interface " + NetworkAdapterController.placeHolder + " disabled";
	private static final String	statusCommand	= "interface show interface " + NetworkAdapterController.placeHolder;

	/**
	 * Disables a windows network adapter (/interface).
	 *
	 * @param adapterName
	 *            The name of the adapter (attention this name may is user specific)!
	 * @throws NetworkAdapterNotFoundException
	 * @throws TimeoutException
	 */
	public static void disable(final String adapterName) throws NetworkAdapterNotFoundException, TimeoutException
	{
		if (!NetworkAdapterController.exists(adapterName))
			throw new NetworkAdapterNotFoundException(adapterName);
		if (!NetworkAdapterController.isEnabled(adapterName))
			return;
		Executor.runAsAdmin(NetworkAdapterController.exeFile, NetworkAdapterController.disableCommand.replace(NetworkAdapterController.placeHolder, adapterName), false);
		MasterController.waitForBoolean(() ->
		{
			try
			{
				return !NetworkAdapterController.isEnabled(adapterName);
			}
			catch (final NetworkAdapterNotFoundException e)
			{
				return false;
			}
		});
		Log.windows.debug("Disabled NetworkAdapter " + adapterName + "!");
	}

	/**
	 * Enables a windows network adapter (/interface).
	 *
	 * @param adapterName
	 *            The name of the adapter (attention this name may is user specific)!
	 * @throws NetworkAdapterNotFoundException
	 * @throws TimeoutException
	 */
	public static void enable(final String adapterName) throws NetworkAdapterNotFoundException, TimeoutException
	{
		if (!NetworkAdapterController.exists(adapterName))
			throw new NetworkAdapterNotFoundException(adapterName);
		if (NetworkAdapterController.isEnabled(adapterName))
			return;
		Executor.runAsAdmin(NetworkAdapterController.exeFile, NetworkAdapterController.enableCommand.replace(NetworkAdapterController.placeHolder, adapterName), false);
		MasterController.waitForBoolean(() ->
		{
			try
			{
				return NetworkAdapterController.isEnabled(adapterName);
			}
			catch (final NetworkAdapterNotFoundException e)
			{
				return false;
			}
		});
		Log.windows.debug("Enabled NetworkAdapter " + adapterName + "!");
	}

	/**
	 * @param adapterName
	 *            The adapter name which shall be checked if it exists.
	 * @return Returns true if this adapter name exists (case insensitive).
	 */
	public static boolean exists(final String adapterName)
	{
		for (final NetworkAdapter adapter : NetworkAdapterController.getList())
			if (adapter.name.equalsIgnoreCase(adapterName))
				return true;
		return false;
	}

	/**
	 * @return Returns a list of all network adapters including the information whether each of them is enabled and/or connected!
	 */
	public static List<NetworkAdapter> getList()
	{
		final ComplexString cmdResult = Executor.runAndWaitForOutput(NetworkAdapterController.exeFile + " " + NetworkAdapterController.statusCommand.replace("_", ""), MasterController.TIMEOUT);
		final List<NetworkAdapter> adapters = new ArrayList<>();
		boolean begin = false;
		for (final String s : cmdResult)
		{
			if (begin)
			{
				if (s.trim().length() > 0)
				{
					final NetworkAdapter adapter = new NetworkAdapter(s.substring(47));
					adapters.add(adapter);
					if (s.contains("Aktiviert"))
						adapter.enabled = true;
					if (s.contains("Verbunden"))
						adapter.connected = true;
				}
			}
			if (s.contains("-------------------------------------------------------------------------"))
				begin = true;
		}
		return adapters;
	}

	/**
	 * Returns the state of a windows network adapter (/interface).
	 *
	 * @param adapterName
	 *            The name of the adapter (attention this name may is user specific)!
	 * @throws NetworkAdapterNotFoundException
	 */
	public static boolean isEnabled(final String adapterName) throws NetworkAdapterNotFoundException
	{
		if (!NetworkAdapterController.exists(adapterName))
			throw new NetworkAdapterNotFoundException(adapterName);
		final ComplexString response = Executor.runAndWaitForOutput(NetworkAdapterController.exeFile + " " + NetworkAdapterController.statusCommand.replace("_", adapterName),
				MasterController.TIMEOUT);
		Log.windows.debug("NetworkAdapter " + adapterName + " is " + (response.containsAllRows("Verwaltungsstatus", "Aktiviert") ? "enabled!" : "disabled!"));
		return response.containsAllRows("Verwaltungsstatus:", "Aktiviert");
	}
}
