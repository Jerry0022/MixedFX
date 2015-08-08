package de.mixedfx.windows;

public class WindowsAdminException extends Exception {
	/**
	 * May start Eclipse as Admin!
	 */
	public WindowsAdminException() {
		super("This program must be started as admin in ordert to work!");
	}
}
