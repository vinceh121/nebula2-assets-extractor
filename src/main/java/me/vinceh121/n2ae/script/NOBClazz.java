package me.vinceh121.n2ae.script;

import java.util.Hashtable;
import java.util.Map;

public class NOBClazz {
	private String name, superclass;
	private final Map<String, CmdPrototype> methods = new Hashtable<>();

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(final String name) {
		this.name = name;
	}

	public String getSuperclass() {
		return this.superclass;
	}

	public void setSuperclass(final String superclass) {
		this.superclass = superclass;
	}

	/**
	 * @return the methods
	 */
	public Map<String, CmdPrototype> getMethods() {
		return this.methods;
	}

	public CmdPrototype getMethod(final String key) {
		return this.methods.get(key);
	}

	public CmdPrototype putMethod(final String key, final CmdPrototype value) {
		return this.methods.put(key, value);
	}

	public boolean containsMethod(final String key) {
		return this.methods.containsKey(key);
	}

	@Override
	public String toString() {
		return "NOBClazz [name=" + this.name + ", superclass=" + this.superclass + ", methods=" + this.methods + "]";
	}
}
