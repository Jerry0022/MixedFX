package de.mixedfx.test;

import de.mixedfx.textandspeech.TTSConfig;
import de.mixedfx.textandspeech.TextToSpeech;

public class TTSTester
{
	public static void main(String[] args)
	{
		String text = "Ok Commander, wie steht es um Battlefield 2?";
		text = "Ok Commander, what's going on with Battlefield 2?";

		TTSConfig config = new TTSConfig();
		config.block = false;
		config.lang = null;
		config.male = false;
		config.volume = 0.6;

		TextToSpeech.say(text, config);
	}
}
