package me.vinceh121.n2ae.script;

import java.util.Hashtable;
import java.util.Map;

public class NOBClazz {
	private final Map<String, CmdPrototype> methodsByFourcc = new Hashtable<>();
	private String name, superclass;

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
	 * @return the methods by fourcc
	 */
	public Map<String, CmdPrototype> getMethods() {
		return this.methodsByFourcc;
	}

	public CmdPrototype getMethodByFourcc(final String fourcc) {
		return this.methodsByFourcc.get(fourcc);
	}

	public CmdPrototype getMethodByName(final String name) {
		for (final CmdPrototype proto : this.methodsByFourcc.values()) {
			if (proto.getName().equals(name)) {
				return proto;
			}
		}
		return null;
	}

	public void putMethod(final CmdPrototype value) {
		this.methodsByFourcc.put(value.getFourcc(), value);
	}

	public boolean containsMethodByFourcc(final String fourcc) {
		return this.methodsByFourcc.containsKey(fourcc);
	}

	public boolean containsMethodByName(final String name) {
		for (final CmdPrototype proto : this.methodsByFourcc.values()) {
			if (proto.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "NOBClazz [name=" + this.name + ", superclass=" + this.superclass + ", methods=" + this.methodsByFourcc
				+ "]";
	}
}
