package me.vinceh121.n2ae.script;

public class ScriptHeader {
	private final String parser, rootClass;

	public ScriptHeader(final String parser, final String rootClass) {
		this.parser = parser;
		this.rootClass = rootClass;
	}

	/**
	 * @return The parser to use for the Nebula Engine. Either "ntclserver" or
	 *         "nbinscriptserver"
	 */
	public String getParser() {
		return this.parser;
	}

	/**
	 * @return The name of the root class to use for this script
	 */
	public String getRootClass() {
		return this.rootClass;
	}
}
