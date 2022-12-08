package me.vinceh121.n2ae.animation;

import me.vinceh121.n2ae.FourccUtils;

public enum KeyType {
	VANILLA("CDTV"), PACKED("CDTP");

	private int fourcc;

	private KeyType(String fourcc) {
		this.fourcc = FourccUtils.fourcc(fourcc);
	}

	public int getFourcc() {
		return fourcc;
	}

	public static KeyType get(int fourcc) {
		for (KeyType t : KeyType.values()) {
			if (t.getFourcc() == fourcc) {
				return t;
			}
		}
		throw new IllegalArgumentException("Unknown KeyType fourcc " + FourccUtils.fourccToString(fourcc));
	}
}
