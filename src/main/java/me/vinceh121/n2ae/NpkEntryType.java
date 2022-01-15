package me.vinceh121.n2ae;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public enum NpkEntryType {
	DIR("DIR_"), DEND, FILE, DATA;

	private final String start;

	private NpkEntryType() {
		this.start = this.name();
	};

	private NpkEntryType(String start) {
		this.start = start;
	}

	public String getStart() {
		return start;
	}

	public int getStartInt() {
		if (getStart().length() != 4)
			throw new IllegalArgumentException("Start bytes will be trimmed to 4 bytes");
		try {
			byte[] bytes = getStart().getBytes("US-ASCII");
			/*
			 * for (int i = 0; i < bytes.length / 2; i++) { // reverse the array bytes[i] ^=
			 * bytes[bytes.length - i - 1]; bytes[bytes.length - i - 1] ^= bytes[i];
			 * bytes[i] ^= bytes[bytes.length - i - 1]; }
			 */
			return ByteBuffer.wrap(bytes).getInt();
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e); // shouldn't happen
		}
	}
}
