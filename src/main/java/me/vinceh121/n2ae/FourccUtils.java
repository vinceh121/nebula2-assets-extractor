package me.vinceh121.n2ae;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public final class FourccUtils {
	public static String fourccToString(final int fourcc) {
		final ByteBuffer buf = ByteBuffer.allocate(4);
		buf.putInt(fourcc);
		return new String(buf.array());
	}

	public static int fourcc(final String str) {
		if (str.length() != 4) {
			throw new IllegalArgumentException("String size not 4");
		}

		try {
			return ByteBuffer.wrap(str.getBytes("US-ASCII")).getInt();
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException(e); // shouldn't happen
		}
	}
}
