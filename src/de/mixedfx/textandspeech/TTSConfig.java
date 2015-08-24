package de.mixedfx.textandspeech;

public class TTSConfig
{
	/**
	 * Pay attention auto detection is limited to 1MB per day and 5000 requests/day with the API Key {@link TTSConfig#DETECTLANGUAGE_API_KEY}! Just before calling
	 * {@link TextToSpeech#say(String, TTSConfig)} you may easily change the API key, see <a href="https://detectlanguage.com/">https://detectlanguage.com/</a>!
	 */
	public String DETECTLANGUAGE_API_KEY = "d4bb31a242aba2eb925ae4cada947095";

	/**
	 * If true the method {@link TextToSpeech#say(String, TTSConfig)} blocks until the voice has finished! Default is false!
	 */
	public boolean block = false;

	/**
	 * The volume of the voice. 1.0 is default. May use a lower value! Higher values are not recommended!
	 */
	public double volume = 1.0;

	/**
	 * German and English as female or male voice are available. Default is {@link Language#ENGLISH}. If null the voice is auto detected from the input text. Pay attention auto detection, see also
	 * {@link #DETECTLANGUAGE_API_KEY}.
	 */
	public Language lang = Language.ENGLISH;

	/**
	 * If true a male voice speaks. Female is default.
	 */
	public boolean male = false;

	public TTSConfig()
	{

	}
}
