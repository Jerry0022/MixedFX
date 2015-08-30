package de.mixedfx.windows;

import java.io.FileNotFoundException;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import javafx.util.Duration;

public class MasterController
{
	public interface Conditionable
	{
		/**
		 * @return Returns true if condition was accepted.
		 */
		public boolean done();
	}

	public static final long TIMEOUT = (long) Duration.seconds(3).toMillis();

	/**
	 * Disables first the process then the service if available.
	 *
	 * @param program
	 * @throws TimeoutException
	 */
	public static void disableAll(final Program program) throws TimeoutException
	{
		ProcessController.stop(program);
		ServiceController.stop(program);
	}

	/*
	 * ADMIN METHODS
	 */

	/**
	 * Disables first the process then the service if available. Afterwards disables the network adapter.
	 *
	 * @param program
	 *            The program to disable.
	 * @param networkAdapter
	 *            The network adapter to disable.
	 * @throws NetworkAdapterNotFoundException
	 * @throws TimeoutException
	 */
	public static void disableAll(final Program program, final String networkAdapter) throws NetworkAdapterNotFoundException, TimeoutException
	{
		MasterController.disableAll(program);
		NetworkAdapterController.disable(networkAdapter);
	}

	/**
	 * Disables Hamachi, including process, service and network adapter.
	 *
	 * @throws NetworkAdapterNotFoundException
	 *             Please find an existing one with {@link NetworkAdapterController#getList()}!
	 * @throws TimeoutException
	 */
	public static void disableHamachi() throws NetworkAdapterNotFoundException, TimeoutException
	{
		MasterController.disableAll(DefaultPrograms.HAMACHI, DefaultNetworkAdapter.HAMACHI);
	}

	/**
	 * Disables Tunngle, including process, service and network adapter.
	 *
	 * @throws NetworkAdapterNotFoundException
	 *             Please find an existing one with {@link NetworkAdapterController#getList()}!
	 * @throws TimeoutException
	 */
	public static void disableTunngle() throws NetworkAdapterNotFoundException, TimeoutException
	{
		MasterController.disableAll(DefaultPrograms.TUNNGLE, DefaultNetworkAdapter.TUNNGLE);
	}

	/**
	 * Enables first the service then the process if available.
	 *
	 * @param program
	 *            The program to enable.
	 * @throws FileNotFoundException
	 * @throws TimeoutException
	 */
	public static void enableAll(final Program program) throws FileNotFoundException, TimeoutException
	{
		ServiceController.run(program);
		ProcessController.run(program);
	}

	/**
	 * Enables first the process then the service if available. Afterwards enables the network adapter.
	 *
	 * @param program
	 *            The program to enable.
	 * @param networkAdapter
	 *            The network adapter to enable.
	 * @throws NetworkAdapterNotFoundException
	 * @throws FileNotFoundException
	 *             If program could not be found!
	 * @throws IllegalStateException
	 *             If system runs on Windows 10 NetworkPriorityController is not supported! (Last thrown)
	 * @throws TimeoutException
	 */
	public static void enableAll(final Program program, final String networkAdapter) throws NetworkAdapterNotFoundException, FileNotFoundException, IllegalStateException, TimeoutException
	{
		NetworkAdapterController.enable(networkAdapter);
		MasterController.enableAll(program);
		NetworkPriorityController.toTop(networkAdapter);
	}

	/**
	 * Enables Hamachi, including process, service and network adapter.
	 *
	 * @throws NetworkAdapterNotFoundException
	 *             Please find an existing one with {@link NetworkAdapterController#getList()}!
	 * @throws IllegalStateException
	 *             If system runs on Windows 10 NetworkPriorityController is not supported! (Last thrown)
	 * @throws FileNotFoundException
	 * @throws TimeoutException
	 */
	public static void enableHamachi() throws NetworkAdapterNotFoundException, FileNotFoundException, IllegalStateException, TimeoutException
	{
		// Fix for Hamachi because sometimes GUI is not shown and not available but process is running at least under Windows 10
		ProcessController.stop(DefaultPrograms.HAMACHI);
		MasterController.enableAll(DefaultPrograms.HAMACHI, DefaultNetworkAdapter.HAMACHI);
	}

	/*
	 * Methods for convenience for proprietary gaming tunneling.
	 */

	/**
	 * Enables Tunngle, including process, service and network adapter.
	 *
	 * @throws NetworkAdapterNotFoundException
	 *             Please find an existing one with {@link NetworkAdapterController#getList()}!
	 * @throws IllegalStateException
	 *             If system runs on Windows 10 NetworkPriorityController is not supported! (Last thrown)
	 * @throws FileNotFoundException
	 * @throws TimeoutException
	 */
	public static void enableTunngle() throws NetworkAdapterNotFoundException, FileNotFoundException, IllegalStateException, TimeoutException
	{
		MasterController.enableAll(DefaultPrograms.TUNNGLE, DefaultNetworkAdapter.TUNNGLE);
	}

	/**
	 * @return Returns true if the current windows user has admin rights, otherwise false.
	 */
	public static boolean hasCurrentUserAdminRights()
	{
		final String groups[] = (new com.sun.security.auth.module.NTSystem()).getGroupIDs();
		for (final String group : groups)
		{
			if (group.equals("S-1-5-32-544"))
				return true;
		}
		return false;
	}

	/**
	 * @return Returns true if this program has admin rights, otherwise false.
	 */
	public static boolean isRunningAsAdmin()
	{
		try
		{
			final String command = "reg query \"HKU\\S-1-5-19\"";
			final Process p = Runtime.getRuntime().exec(command);
			p.waitFor(); // Wait for for command to finish
			final int exitValue = p.exitValue(); // If exit value 0, then admin user.

			return 0 == exitValue;
		}
		catch (final Exception e)
		{
			return false;
		}
	}

	public static final void waitForBoolean(final Conditionable condition) throws TimeoutException
	{
		MasterController.waitForBoolean(condition, MasterController.TIMEOUT);
	}

	public static final void waitForBoolean(final Conditionable condition, final long timeout) throws TimeoutException
	{
		final long startTime = new Date().getTime();
		while (!condition.done())
		{
			if ((new Date().getTime() - startTime) > timeout)
				throw new TimeoutException();
		}
	}
}
