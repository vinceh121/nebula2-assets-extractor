package me.vinceh121.n2ae.script;

import java.util.Arrays;
import java.util.Objects;

public class ClassCommandCall implements ICommandCall {
	private CmdPrototype prototype;
	private NOBClazz clazz;
	private Object[] arguments;

	public ClassCommandCall() {
	}

	public ClassCommandCall(final CmdPrototype prototype, final NOBClazz clazz, final Object[] arguments) {
		this.prototype = prototype;
		this.clazz = clazz;
		this.arguments = arguments;
	}

	public CmdPrototype getPrototype() {
		return this.prototype;
	}

	public void setPrototype(final CmdPrototype prototype) {
		this.prototype = prototype;
	}

	@Override
	public NOBClazz getClazz() {
		return this.clazz;
	}

	public void setClazz(final NOBClazz clazz) {
		this.clazz = clazz;
	}

	public Object[] getArguments() {
		return this.arguments;
	}

	public void setArguments(final Object[] arguments) {
		this.arguments = arguments;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(this.arguments);
		result = prime * result + Objects.hash(this.clazz, this.prototype);
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final ClassCommandCall other = (ClassCommandCall) obj;
		return Arrays.deepEquals(this.arguments, other.arguments) && Objects.equals(this.clazz, other.clazz)
				&& Objects.equals(this.prototype, other.prototype);
	}

	@Override
	public String toString() {
		return "ClassCommandCall [prototype=" + this.prototype + ", clazz=" + this.clazz + ", arguments="
				+ Arrays.toString(this.arguments) + "]";
	}
}
