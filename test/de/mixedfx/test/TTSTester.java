package de.mixedfx.test;

import de.mixedfx.java.TTSConfig;
import de.mixedfx.java.TextToSpeech;

public class TTSTester
{
	public static void main(String[] args)
	{
		String text = "Ok Commander, wie geht es weiter mit Battlefield 2?";
		text = "Ok Commander, what's going on with Battlefield 2?";

		TTSConfig config = new TTSConfig();
		config.block = false;
		config.lang = null;
		config.male = false;
		config.volume = 0.8;

		TextToSpeech.say(text, config);
	}
}
