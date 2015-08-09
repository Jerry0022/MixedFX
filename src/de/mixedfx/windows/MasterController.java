package de.mixedfx.windows;

import java.util.ArrayList;
import java.util.List;

public class MasterController {
	/*
	 * ADMIN METHODS
	 */

	/**
	 * @return Returns true if the current windows user has admin rights, otherwise false.
	 */
	public static boolean hasCurrentUserAdminRights() {
		String groups[] = (new com.sun.security.auth.module.NTSystem()).getGroupIDs();
		for (String group : groups) {
			if (group.equals("S-1-5-32-544"))
				return true;
		}
		return false;
	}

	/**
	 * @return Returns true if this program has admin rights, otherwise false.
	 */
	public static boolean isRunningAsAdmin() {
		try {
			String command = "reg query \"HKU\\S-1-5-19\"";
			Process p = Runtime.getRuntime().exec(command);
			p.waitFor(); // Wait for for command to finish
			int exitValue = p.exitValue(); // If exit value 0, then admin user.

			return 0 == exitValue;
		} catch (Exception e) {
			return false;
		}
	}

	/*
	 * FULL ENABLER AND DISABLER
	 */

	/**
	 * Disables first the process then the service if available.
	 * 
	 * @param program
	 */
	public static void disableAll(Program program) throws AdminRequiredException {
		if (!isRunningAsAdmin())
			throw new AdminRequiredException();
		ProcessController.stop(program);
		ServiceController.stop(program);
	}

	/**
	 * Disables first the process then the service if available. Afterwards disables the network adapter.
	 * 
	 * @param program
	 *            The program to disable.
	 * @param networkAdapter
	 *            The network adapter to disable.
	 * @throws AdminRequiredException
	 *             This program must run as Admin to do this action. Please check {@link MasterController#isRunningAsAdmin()}!
	 * @throws NetworkAdapterNotFoundException
	 */
	public static void disableAll(Program program, String networkAdapter) throws AdminRequiredException, NetworkAdapterNotFoundException {
		if (!isRunningAsAdmin())
			throw new AdminRequiredException();
		disableAll(program);
		NetworkAdapterController.disable(networkAdapter);
	}

	/**
	 * Enables first the process then the service if available.
	 * 
	 * @param program
	 *            The program to enable.
	 * @throws AdminRequiredException
	 *             This program must run as Admin to do this action. Please check {@link MasterController#isRunningAsAdmin()}!
	 */
	public static void enableAll(Program program) throws AdminRequiredException {
		if (!isRunningAsAdmin())
			throw new AdminRequiredException();
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
	 * @throws AdminRequiredException
	 *             This program must run as Admin to do this action. Please check {@link MasterController#isRunningAsAdmin()}!
	 * @throws NetworkAdapterNotFoundException
	 */
	public static void enableAll(Program program, String networkAdapter) throws AdminRequiredException, NetworkAdapterNotFoundException {
		if (!isRunningAsAdmin())
			throw new AdminRequiredException();
		NetworkAdapterController.enable(networkAdapter);
		NetworkPriorityController.toTop(networkAdapter);
		enableAll(program);
	}

	/**
	 * See also {@link FirewallController#disable()}.
	 * 
	 * @throws AdminRequiredException
	 *             This program must run as Admin to do this action. Please check {@link MasterController#isRunningAsAdmin()}!
	 */
	public static void disableFirewalls() throws AdminRequiredException {
		if (!isRunningAsAdmin())
			throw new AdminRequiredException();
		FirewallController.enable();
	}

	/**
	 * See also {@link FirewallController#enable()}.
	 * 
	 * @throws AdminRequiredException
	 *             This program must run as Admin to do this action. Please check {@link MasterController#isRunningAsAdmin()}!
	 */
	public static void enableFirewalls() throws AdminRequiredException {
		if (!isRunningAsAdmin())
			throw new AdminRequiredException();
		FirewallController.enable();
	}

	/*
	 * Methods for convenience for properitary gaming tunneling.
	 */

	/**
	 * Disables Hamachi, including process, service and network adapter.
	 * 
	 * @throws AdminRequiredException
	 *             This program must run as Admin to do this action. Please check {@link MasterController#isRunningAsAdmin()}!
	 * @throws ProgramNotFoundException
	 * @throws NetworkAdapterNotFoundException
	 *             Please find an existing one with {@link NetworkAdapterController#getList()}!
	 */
	public static void disableHamachi() throws AdminRequiredException, ProgramNotFoundException, NetworkAdapterNotFoundException {
		if (!DefaultPrograms.HAMACHI.fullPath.toFile().exists())
			throw new ProgramNotFoundException("Hamachi path not found! Default path: " + DefaultPrograms.HAMACHI.fullPath);
		disableAll(DefaultPrograms.HAMACHI, DefaultNetworkAdapter.HAMACHI);
	}

	/**
	 * Enables Hamachi, including process, service and network adapter.
	 * 
	 * @throws AdminRequiredException
	 *             This program must run as Admin to do this action. Please check {@link MasterController#isRunningAsAdmin()}!
	 * @throws ProgramNotFoundException
	 * @throws NetworkAdapterNotFoundException
	 *             Please find an existing one with {@link NetworkAdapterController#getList()}!
	 */
	public static void enableHamachi() throws AdminRequiredException, ProgramNotFoundException, NetworkAdapterNotFoundException {
		if (!DefaultPrograms.HAMACHI.fullPath.toFile().exists())
			throw new ProgramNotFoundException("Hamachi path not found! Default path: " + DefaultPrograms.HAMACHI.fullPath);
		enableAll(DefaultPrograms.HAMACHI, DefaultNetworkAdapter.HAMACHI);
	}

	/**
	 * Disables Tunngle, including process, service and network adapter.
	 * 
	 * @throws AdminRequiredException
	 *             This program must run as Admin to do this action. Please check {@link MasterController#isRunningAsAdmin()}!
	 * @throws ProgramNotFoundException
	 * @throws NetworkAdapterNotFoundException
	 *             Please find an existing one with {@link NetworkAdapterController#getList()}!
	 */
	public static void disableTunngle() throws AdminRequiredException, ProgramNotFoundException, NetworkAdapterNotFoundException {
		if (!DefaultPrograms.TUNNGLE.fullPath.toFile().exists())
			throw new ProgramNotFoundException("Tunngle path not found! Default path: " + DefaultPrograms.TUNNGLE.fullPath);
		disableAll(DefaultPrograms.TUNNGLE, DefaultNetworkAdapter.TUNNGLE);
	}

	/**
	 * Enables Tunngle, including process, service and network adapter.
	 * 
	 * @param channelName
	 *            The name of the Tunngle channel to which to connect automatically or an empty String so that Tunngle doesn't connect automatically to a channel.
	 * @throws AdminRequiredException
	 *             This program must run as Admin to do this action. Please check {@link MasterController#isRunningAsAdmin()}!
	 * @throws ProgramNotFoundException
	 * @throws NetworkAdapterNotFoundException
	 *             Please find an existing one with {@link NetworkAdapterController#getList()}!
	 */
	public static void enableTunngle(String channelName) throws AdminRequiredException, ProgramNotFoundException, NetworkAdapterNotFoundException {
		if (!DefaultPrograms.TUNNGLE.fullPath.toFile().exists())
			throw new ProgramNotFoundException("Tunngle path not found! Default path: " + DefaultPrograms.TUNNGLE.fullPath);
		List<String> parameters = new ArrayList<>();
		if (!channelName.equals(""))
			parameters.add("JoinNetwork " + channelName);
		DefaultPrograms.TUNNGLE.fullPath.setParameter(parameters.toArray(new String[parameters.size()]));
		System.out.println(DefaultPrograms.TUNNGLE.fullPath.getFullPathWithParameter());
		enableAll(DefaultPrograms.TUNNGLE, DefaultNetworkAdapter.TUNNGLE);
	}
}
