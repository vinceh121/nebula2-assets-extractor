package me.vinceh121.n2ae.pkg;

import me.vinceh121.n2ae.FourccUtils;

public enum NpkEntryType {
	DIR("DIR_"), DEND, FILE, DATA;

	private final String start;
	private final int fourcc;

	NpkEntryType() {
		this.start = this.name();
		this.fourcc = FourccUtils.fourcc(start);
	}

	NpkEntryType(final String start) {
		this.start = start;
		this.fourcc = FourccUtils.fourcc(start);
	}

	public String getStart() {
		return this.start;
	}

	public int getStartInt() {
		return this.fourcc;
	}
}
