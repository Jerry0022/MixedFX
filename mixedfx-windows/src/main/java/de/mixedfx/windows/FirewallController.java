package de.mixedfx.windows;

import de.mixedfx.java.ComplexString;
import de.mixedfx.logging.Log;

import java.util.concurrent.TimeoutException;

public class FirewallController
{
	private final static String	exeFile			= "netsh";
	private final static String	enableCommand	= "advfirewall set allprofiles state on";
	private final static String	disableCommand	= "advfirewall set allprofiles state off";
	private final static String	statusCommand	= "advfirewall show allprofiles";

	/**
	 * Disables all windows firewalls.
	 *
	 * @throws TimeoutException
	 */
	public static synchronized void disable() throws TimeoutException
	{
		Executor.runAsAdmin(FirewallController.exeFile, FirewallController.disableCommand, false);
		MasterController.waitForBoolean(() -> !FirewallController.isEnabled());
		Log.windows.debug("Disabled Windows Firewalls!");
	}

	/**
	 * Enables all windows firewalls.
	 *
	 * @throws TimeoutException
	 */
	public static synchronized void enable() throws TimeoutException
	{
		Executor.runAsAdmin(FirewallController.exeFile, FirewallController.enableCommand, false);
		MasterController.waitForBoolean(() -> FirewallController.isEnabled());
		Log.windows.debug("Enabled Windows Firewalls!");
	}

	/**
	 * @return False if all firewalls are disabled or could not retrieve status! True if at least one firewall is online.
	 */
	public static synchronized boolean isEnabled()
	{
		final ComplexString response = Executor.runAndWaitForOutput(FirewallController.exeFile + " " + FirewallController.statusCommand, MasterController.TIMEOUT);
		try
		{
			boolean result;
			if (response.containsAllRows("Status", "EIN"))
				result = true;
			else
				result = !response.containsAllRows("Status", "AUS");
			Log.windows.debug("Windows Firewalls are " + (result ? "enabled!" : "disabled!"));
			return result;
		}
		catch (final Exception e)
		{
			Log.windows.error("Windows Firewall wrong command!");
			return false;
		}
	}
}
