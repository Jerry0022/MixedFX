package de.mixedfx.list;

public interface Identifiable
{
	/**
	 * @return Returns a unique not network related id. If it returns null there is someone connected but not yet identified - Later it will be replaced by the user.
	 */
	Object getIdentifier();
}
