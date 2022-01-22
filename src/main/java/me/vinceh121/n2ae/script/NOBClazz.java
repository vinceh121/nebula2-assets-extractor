package me.vinceh121.n2ae.script;

import java.util.Hashtable;
import java.util.Map;

public class NOBClazz {
	private String name;
	private Map<String, CmdPrototype> methods = new Hashtable<>();

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the methods
	 */
	public Map<String, CmdPrototype> getMethods() {
		return methods;
	}

	public CmdPrototype getMethod(String key) {
		return methods.get(key);
	}

	public CmdPrototype putMethod(String key, CmdPrototype value) {
		return methods.put(key, value);
	}

	public boolean containsMethod(String key) {
		return this.methods.containsKey(key);
	}

	@Override
	public String toString() {
		return "NOBClazz [name=" + name + ", methods=" + methods + "]";
	}
}
