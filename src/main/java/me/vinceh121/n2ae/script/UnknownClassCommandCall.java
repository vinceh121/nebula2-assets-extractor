package me.vinceh121.n2ae.script;

import java.util.Arrays;
import java.util.Objects;

public class UnknownClassCommandCall implements ICommandCall {
	private NOBClazz clazz;
	private String fourcc;
	private byte[] arguments;

	public UnknownClassCommandCall() {
	}

	public UnknownClassCommandCall(NOBClazz clazz, String fourcc, byte[] arguments) {
		this.clazz = clazz;
		this.fourcc = fourcc;
		this.arguments = arguments;
	}

	@Override
	public NOBClazz getClazz() {
		return this.clazz;
	}

	public void setClazz(NOBClazz clazz) {
		this.clazz = clazz;
	}

	public String getFourcc() {
		return fourcc;
	}

	public void setFourcc(String fourcc) {
		this.fourcc = fourcc;
	}

	public byte[] getArguments() {
		return arguments;
	}

	public void setArguments(byte[] arguments) {
		this.arguments = arguments;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(arguments);
		result = prime * result + Objects.hash(clazz, fourcc);
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
		UnknownClassCommandCall other = (UnknownClassCommandCall) obj;
		return Arrays.equals(arguments, other.arguments) && Objects.equals(clazz, other.clazz)
				&& Objects.equals(fourcc, other.fourcc);
	}

	@Override
	public String toString() {
		return "UnknownClassCommandCall [clazz=" + clazz + ", fourcc=" + fourcc + ", arguments="
				+ Arrays.toString(arguments) + "]";
	}
}
