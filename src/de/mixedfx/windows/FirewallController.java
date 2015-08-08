package de.mixedfx.windows;

import de.mixedfx.java.ComplexString;
import de.mixedfx.logging.Log;

public class FirewallController
{
	private final static String	enableCommand	= "netsh advfirewall set allprofiles state on";
	private final static String	disableCommand	= "netsh advfirewall set allprofiles state off";
	private final static String	statusCommand	= "netsh advfirewall show allprofiles";

	/**
	 * @return False if all firewalls are disabled or status could not be retrieved! True if at least one firewall is online.
	 */
	public static boolean isEnabled()
	{
		final ComplexString response = Executor.runAndWaitForOutput(FirewallController.statusCommand);
		try
		{
			Log.windows.debug("Windows Firewalls are " + (response.containsAllRows("Status", "EIN")?"enabled!":"disabled!"));
			return response.containsAllRows("Status", "EIN");
		}
		catch (final Exception e)
		{
			Log.windows.debug("Windows Firewall is disabled!");
			return false;
		}
	}

	/**
	 * Enables all windows firewalls.
	 */
	public static void enable()
	{
		if (FirewallController.isEnabled())
			return;
		Executor.runAndWaitForOutput(FirewallController.enableCommand);
		while (!FirewallController.isEnabled())
		{
			;
		}
		Log.windows.debug("Windows Firewalls were enabled!");
	}

	/**
	 * Disables all windows firewalls.
	 */
	public static void disable()
	{
		if (!FirewallController.isEnabled())
			return;
		Executor.runAndWaitForOutput(FirewallController.disableCommand);
		while (FirewallController.isEnabled())
		{
			;
		}
		Log.windows.debug("Windows Firewalls were disabled!");
	}
}
