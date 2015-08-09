package de.mixedfx.windows;

public class AdminRequiredException extends Exception {
	/**
	 * May start Eclipse as Admin!
	 */
	public AdminRequiredException() {
		super("This program must be started as admin in ordert to work!");
	}
}
