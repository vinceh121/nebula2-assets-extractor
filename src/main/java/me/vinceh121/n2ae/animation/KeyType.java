package me.vinceh121.n2ae.animation;

import me.vinceh121.n2ae.FourccUtils;

public enum KeyType {
	VANILLA("CDTV"), PACKED("CDTP");

	private int fourcc;

	KeyType(final String fourcc) {
		this.fourcc = FourccUtils.fourcc(fourcc);
	}

	public int getFourcc() {
		return this.fourcc;
	}

	public static KeyType get(final int fourcc) {
		for (final KeyType t : KeyType.values()) {
			if (t.getFourcc() == fourcc) {
				return t;
			}
		}
		throw new IllegalArgumentException("Unknown KeyType fourcc " + FourccUtils.fourccToString(fourcc));
	}
}
