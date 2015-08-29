package de.mixedfx.windows;

import de.mixedfx.file.DataHandler;
import de.mixedfx.file.FileObject;

public class DefaultPrograms
{
	public static final Program	HAMACHI	= new Program("Hamachi", FileObject.create().setPath(DataHandler.fuse(System.getenv("ProgramFiles"), "LogMeIn Hamachi")).setFullName("hamachi-2-ui.exe"),
			"Hamachi-2.exe", "Hamachi2Svc");
	/**
	 * Parameters of {@link Program#fullPath} MUST NOT be null. Please set it to {"JoinPrivNet", "LanTool"} or {"JoinPrivNet", "CHANNELNAME"}! Empty
	 * or other parameters do not work!
	 */
	public static final Program	TUNNGLE	= new Program("Tunngle",
			FileObject.create().setPath(DataHandler.fuse(System.getenv("ProgramFiles"), "Tunngle")).setFullName("Launcher.exe").setParameter("JoinPrivNet", "LanTool"), "Tunngle.exe",
			"TunngleService");
}
