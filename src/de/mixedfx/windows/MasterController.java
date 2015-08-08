package de.mixedfx.windows;

public class MasterController {
	/**
	 * @return Returns true if the current windows user has admin rights, otherwise false. 
	 */
	public static boolean hasCurrentUserAdminRights()
	{
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
	        p.waitFor();                            // Wait for for command to finish
	        int exitValue = p.exitValue();          // If exit value 0, then admin user.

	        return 0 == exitValue;
	    } catch (Exception e) {
	        return false;
	    }
	}

	/**
	 * Disables first the process then the service if available.
	 * 
	 * @param program
	 */
	public static void disableAll(Program program) {
		ProcessController.stop(program);
		ServiceController.stop(program);
	}

	/**
	 * Disables first the process then the service if available. Afterwards
	 * disables the network adapter.
	 * 
	 * @param program
	 *            The program to disable.
	 * @param networkAdapter
	 *            The network adapter to disable.
	 */
	public static void disableAll(Program program, String networkAdapter) {
		disableAll(program);
		NetworkAdapterController.disable(networkAdapter);
	}

	/**
	 * Enables first the process then the service if available.
	 * 
	 * @param program
	 *            The program to enable.
	 */
	public static void enableAll(Program program) {
		ServiceController.run(program);
		ProcessController.run(program);
	}

	/**
	 * Enables first the process then the service if available. Afterwards
	 * enables the network adapter.
	 * 
	 * @param program
	 *            The program to enable.
	 * @param networkAdapter
	 *            The network adapter to enable.
	 */
	public static void enableAll(Program program, String networkAdapter) {
		NetworkAdapterController.enable(networkAdapter);
		NetworkPriorityController.toTop(networkAdapter);
		enableAll(program);
	}

	/**
	 * See also {@link FirewallController#disable()}.
	 */
	public static void disableFirewall() {
		FirewallController.enable();
	}

	/**
	 * See also {@link FirewallController#enable()}.
	 */
	public static void enableFirewall() {
		FirewallController.enable();
	}

	/*
	 * Methods for convenience for properitary gaming tunneling.
	 */

	/**
	 * Disables Hamachi, including process, service and network adapter.
	 */
	public static void disableHamachi() {
		disableAll(DefaultPrograms.HAMACHI, DefaultNetworkAdapter.HAMACHI);
	}

	/**
	 * Enables Hamachi, including process, service and network adapter.
	 */
	public static void enableHamachi() {
		enableAll(DefaultPrograms.HAMACHI, DefaultNetworkAdapter.HAMACHI);
	}

	/**
	 * Disables Tunngle, including process, service and network adapter.
	 */
	public static void disableTunngle() {
		disableAll(DefaultPrograms.TUNNGLE, DefaultNetworkAdapter.TUNNGLE);
	}

	/**
	 * Enables Tunngle, including process, service and network adapter.
	 */
	public static void enableTunngle() {
		enableAll(DefaultPrograms.TUNNGLE, DefaultNetworkAdapter.TUNNGLE);
	}
}
