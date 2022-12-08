package me.vinceh121.n2ae;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class LEDataOutputStream extends DataOutputStream {

	public LEDataOutputStream(final OutputStream out) {
		super(out);
	}

	@Override
	public synchronized void write(final int b) throws IOException {
		super.write(b);
	}

	@Override
	public synchronized void write(final byte[] b, final int off, final int len) throws IOException {
		super.write(b, off, len);
	}

	public void writeIntLE(final int i) throws IOException {
		this.writeInt(Integer.reverseBytes(i));
	}

	public void writeShortLE(final short s) throws IOException {
		this.writeShort(Short.reverseBytes(s));
	}

	public void writeFloatLE(final float f) throws IOException {
		this.writeIntLE(Float.floatToRawIntBits(f));
	}

	public void writeCharLE(final char c) throws IOException {
		this.writeChar(Character.reverseBytes(c));
	}

	public void writeUnsignedShortLE(final int us) throws IOException {
		this.writeShortLE((short) us);
	}

	public void writeString(final String str) throws IOException {
		this.writeUnsignedShortLE(str.length());
		final byte[] bytes = str.getBytes("US-ASCII");
		this.write(bytes);
	}

	public long getWrittenBytes() {
		return this.written;
	}
	
	public OutputStream getUnderlyingOutputStream() {
		return this.out;
	}
}
