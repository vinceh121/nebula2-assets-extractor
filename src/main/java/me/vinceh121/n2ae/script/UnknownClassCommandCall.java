package me.vinceh121.n2ae.script;

import java.util.Arrays;
import java.util.Objects;

public class UnknownClassCommandCall implements ICommandCall {
	private NOBClazz clazz;
	private String fourcc;
	private byte[] arguments;

	public UnknownClassCommandCall() {
	}

	public UnknownClassCommandCall(final NOBClazz clazz, final String fourcc, final byte[] arguments) {
		this.clazz = clazz;
		this.fourcc = fourcc;
		this.arguments = arguments;
	}

	@Override
	public NOBClazz getClazz() {
		return this.clazz;
	}

	public void setClazz(final NOBClazz clazz) {
		this.clazz = clazz;
	}

	public String getFourcc() {
		return this.fourcc;
	}

	public void setFourcc(final String fourcc) {
		this.fourcc = fourcc;
	}

	public byte[] getArguments() {
		return this.arguments;
	}

	public void setArguments(final byte[] arguments) {
		this.arguments = arguments;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(this.arguments);
		result = prime * result + Objects.hash(this.clazz, this.fourcc);
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
		final UnknownClassCommandCall other = (UnknownClassCommandCall) obj;
		return Arrays.equals(this.arguments, other.arguments) && Objects.equals(this.clazz, other.clazz)
				&& Objects.equals(this.fourcc, other.fourcc);
	}

	@Override
	public String toString() {
		return "UnknownClassCommandCall [clazz=" + this.clazz + ", fourcc=" + this.fourcc + ", arguments="
				+ Arrays.toString(this.arguments) + "]";
	}
}
