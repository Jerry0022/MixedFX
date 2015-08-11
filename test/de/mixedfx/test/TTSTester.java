package de.mixedfx.test;

import de.mixedfx.java.TextToSpeech;

public class TTSTester
{
	public static void main(String[] args)
	{
		String text = "Ok Commander, wie geht es weiter mit Battlefield 2?";
		text = "Ok Commander, what's going on with Battlefield 2?";

		boolean male = true;
		male = false;

		TextToSpeech.sayAuto(text, true, male);
	}
}
