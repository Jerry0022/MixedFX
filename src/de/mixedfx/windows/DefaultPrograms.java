package de.mixedfx.windows;

import de.mixedfx.file.DataHandler;
import de.mixedfx.file.FileObject;

public class DefaultPrograms {
	public static final Program HAMACHI = new Program("Hamachi", FileObject.create().setPath(DataHandler.fuse(System.getenv("ProgramFiles"), "LogMeIn Hamachi")).setFullName("hamachi-2-ui.exe"),
			"Hamachi-2.exe", "Hamachi2Svc");
	public static final Program TUNNGLE = new Program("Tunngle", FileObject.create().setPath(DataHandler.fuse(System.getenv("ProgramFiles"), "Tunngle")).setFullName("Launcher.exe"), "Tunngle.exe",
			"TunngleService");
}
