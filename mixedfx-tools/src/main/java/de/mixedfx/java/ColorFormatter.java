package de.mixedfx.java;

import java.util.Locale;

import javafx.scene.paint.Color;

public class ColorFormatter
{
	/**
	 * @param color
	 *            JavaFX Color
	 * @return Returns e. g. "#FD0037"
	 */
	public static String toRGB(final Color color)
	{
		return String.format("#%02X%02X%02X", (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
	}

	/**
	 * @param color
	 *            JavaFX Color
	 * @return Returns e. g. a string like "rgb(102, 76, 76, 0.30)"
	 */
	public static String toRGBA(final Color color)
	{
		return String.format(Locale.US, "rgba(%d, %d, %d, %01.2f)", (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255), color.getOpacity());
	}

	/**
	 * @param color
	 *            E. g. a string like "rgba(102, 76, 76, 0.30)" / see {@link Color#web(String)}
	 * @return Returns the JavaFX Color or throws an exception
	 */
	public static Color fromRGBA(final String color) throws Exception
	{
		return Color.web(color);
	}
}
