package me.vinceh121.n2ae.script;

import java.util.Arrays;
import java.util.Objects;

public class ClassCommandCall implements ICommandCall {
	private CmdPrototype prototype;
	private NOBClazz clazz;
	private Object[] arguments;

	public ClassCommandCall() {
	}

	public ClassCommandCall(CmdPrototype prototype, NOBClazz clazz, Object[] arguments) {
		this.prototype = prototype;
		this.clazz = clazz;
		this.arguments = arguments;
	}

	public CmdPrototype getPrototype() {
		return prototype;
	}

	public void setPrototype(CmdPrototype prototype) {
		this.prototype = prototype;
	}

	@Override
	public NOBClazz getClazz() {
		return clazz;
	}

	public void setClazz(NOBClazz clazz) {
		this.clazz = clazz;
	}

	public Object[] getArguments() {
		return arguments;
	}

	public void setArguments(Object[] arguments) {
		this.arguments = arguments;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(arguments);
		result = prime * result + Objects.hash(clazz, prototype);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClassCommandCall other = (ClassCommandCall) obj;
		return Arrays.deepEquals(arguments, other.arguments) && Objects.equals(clazz, other.clazz)
				&& Objects.equals(prototype, other.prototype);
	}

	@Override
	public String toString() {
		return "ClassCommandCall [prototype=" + prototype + ", clazz=" + clazz + ", arguments="
				+ Arrays.toString(arguments) + "]";
	}
}
