package de.mixedfx.java;

public class ExtString {
	public enum Lang {
		DE, EN;
	}

	private final String text;
	public final Lang language;

	public ExtString(String text, Lang language) {
		this.text = text;
		this.language = language;
	}
}
