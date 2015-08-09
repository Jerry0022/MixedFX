package de.mixedfx.windows;

import java.util.ArrayList;
import java.util.List;

import de.mixedfx.java.ComplexString;
import de.mixedfx.logging.Log;

public class NetworkAdapterController {
	private static final String placeHolder = "_";

	public static final String enableCommand = "netsh interface set interface " + NetworkAdapterController.placeHolder + " enabled";
	public static final String disableCommand = "netsh interface set interface " + NetworkAdapterController.placeHolder + " disabled";
	public static final String statusCommand = "netsh interface show interface " + NetworkAdapterController.placeHolder;

	/**
	 * @return Returns a list of all network adapters including the informatinon wether each of them is enabled and/or connected!
	 */
	public static List<NetworkAdapter> getList() {
		ComplexString cmdResult = Executor.runAndWaitForOutput(statusCommand.replace("_", ""));
		List<NetworkAdapter> adapters = new ArrayList<>();
		boolean begin = false;
		for (String s : cmdResult) {
			if (begin) {
				NetworkAdapter adapter = new NetworkAdapter();
				if (s.contains("Aktiviert"))
					adapter.enabled = true;
				if (s.contains("Verbunden"))
					adapter.connected = true;
				if (s.trim().length() > 0) {
					adapter.name = s.substring(47);
					adapters.add(adapter);
				}
			}
			if (s.contains("-------------------------------------------------------------------------"))
				begin = true;
		}
		return adapters;
	}

	/**
	 * @param adapterName
	 *            The adapter name which shall be checked if it exists.
	 * @return Returns true if this adapter name exists (case insensitive).
	 */
	public static boolean exists(String adapterName) {
		for (NetworkAdapter adapter : getList())
			if (adapter.name.equalsIgnoreCase(adapterName))
				return true;
		return false;
	}

	/**
	 * Returns the state of a windows network adapter (/interface).
	 * 
	 * @param adapterName
	 *            The name of the adapter (attention this name may is user specific)!
	 * @throws NetworkAdapterNotFoundException
	 */
	public static boolean isEnabled(final String adapterName) throws NetworkAdapterNotFoundException {
		if (!NetworkAdapterController.exists(adapterName))
			throw new NetworkAdapterNotFoundException(adapterName);
		final ComplexString response = Executor.runAndWaitForOutput(NetworkAdapterController.statusCommand.replace(NetworkAdapterController.placeHolder, adapterName));
		try {
			Log.windows.debug("NetworkAdapter " + adapterName + " is " + (response.containsAllRows("Verwaltungsstatus", "Aktiviert") ? "enabled!" : "disabled!"));
			return response.containsAllRows("Verwaltungsstatus:", "Aktiviert");
		} catch (final Exception e) {
			Log.windows.debug("NetworkAdapter " + adapterName + " status couldn't be determined!");
			return false;
		}
	}

	/**
	 * Disables a windows network adapter (/interface).
	 * 
	 * @param adapterName
	 *            The name of the adapter (attention this name may is user specific)!
	 * @throws NetworkAdapterNotFoundException
	 */
	public static void disable(final String adapterName) throws NetworkAdapterNotFoundException {
		if (!NetworkAdapterController.exists(adapterName))
			throw new NetworkAdapterNotFoundException(adapterName);
		if (!NetworkAdapterController.isEnabled(adapterName))
			return;
		Executor.runAndWaitForOutput(NetworkAdapterController.disableCommand.replace(NetworkAdapterController.placeHolder, adapterName));
		while (NetworkAdapterController.isEnabled(adapterName)) {
			;
		}
		Log.windows.debug("NetworkAdapter " + adapterName + " was disabled!");
	}

	/**
	 * Enables a windows network adapter (/interface).
	 * 
	 * @param adapterName
	 *            The name of the adapter (attention this name may is user specific)!
	 * @throws NetworkAdapterNotFoundException
	 */
	public static void enable(final String adapterName) throws NetworkAdapterNotFoundException {
		if (!NetworkAdapterController.exists(adapterName))
			throw new NetworkAdapterNotFoundException(adapterName);
		if (NetworkAdapterController.isEnabled(adapterName))
			return;
		Executor.runAndWaitForOutput(NetworkAdapterController.enableCommand.replace(NetworkAdapterController.placeHolder, adapterName));
		while (!NetworkAdapterController.isEnabled(adapterName)) {
			;
		}
		Log.windows.debug("NetworkAdapter " + adapterName + " was enabled!");
	}
}
