package de.mixedfx.test;

import de.mixedfx.assets.ImageProducer;
import de.mixedfx.gui.RegionManipulator;
import de.mixedfx.textandspeech.IncorrectInputLanguage;
import de.mixedfx.textandspeech.Language;
import de.mixedfx.textandspeech.SpeechToText;
import de.mixedfx.textandspeech.TTSConfig;
import de.mixedfx.textandspeech.TextToSpeech;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class VoiceButton extends Pane
{
	private final BooleanProperty voiceSwitcher = new SimpleBooleanProperty(false);

	public VoiceButton()
	{
		RegionManipulator.bindBackground(this, ImageProducer.getMonoColored(Color.BLUE));
		this.setPrefSize(50, 50);
		this.setMaxSize(50, 50);
		this.setStyle("-fx-cursor: hand");

		// Set TTS config
		final TTSConfig config = new TTSConfig();
		config.lang = Language.GERMAN;
		config.male = false;
		config.block = true;

		// TODO Get username from config
		final String userName = "Jeremy";

		this.voiceSwitcher.addListener(
				(ChangeListener<Boolean>) (observable, oldValue, newValue) -> RegionManipulator.bindBackground(VoiceButton.this, newValue.booleanValue() ? ImageProducer.getMonoColored(Color.GREEN) : ImageProducer.getMonoColored(Color.BLUE)));

		this.setOnMouseClicked(event -> VoiceButton.this.voiceSwitcher.set(VoiceButton.this.voiceSwitcher.get() ? false : true));

		// Incoming voice?
		SpeechToText.language = Language.ENGLISH;
		try
		{
			SpeechToText.startListening("AIzaSyBI012J_HtuCeXTpwvcaNB3awo88uAbIGA", this.voiceSwitcher, gr ->
			{
				System.out.println("Antwort von Google: " + gr.getResponse());
				System.out.println("Jetzt auf Deutsch");
				try
				{
					TextToSpeech.say("Hallo " + userName + ", du hast gerade gesagt: " + gr.getResponse(), config);
				} catch (final IncorrectInputLanguage e2)
				{
					try
					{
						TextToSpeech.say("Es tut mir Leid, aber ich habe dich nicht verstanden!", config);
					} catch (final IncorrectInputLanguage e1)
					{
					}
				}
				System.out.println("Jetzt auf Englisch");
				config.lang = Language.ENGLISH;
				try
				{
					TextToSpeech.say("Hello " + userName + "! you have said " + gr.getResponse(), config);
				} catch (final IncorrectInputLanguage e3)
				{
				}
			});
		} catch (final Exception e)
		{
			System.out.println("No micro found!");
		}
	}
}
