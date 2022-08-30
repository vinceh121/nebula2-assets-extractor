package me.vinceh121.n2ae;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class LEDataOutputStream extends DataOutputStream {
	private long writtenBytes = 0;

	public LEDataOutputStream(OutputStream out) {
		super(out);
	}

	@Override
	public synchronized void write(int b) throws IOException {
		super.write(b);
		this.written++;
	}

	@Override
	public synchronized void write(byte[] b, int off, int len) throws IOException {
		super.write(b, off, len);
		this.written += len;
	}

	public void writeIntLE(int i) throws IOException {
		this.writeInt(Integer.reverseBytes(i));
	}

	public void writeShortLE(short s) throws IOException {
		this.writeShort(Short.reverseBytes(s));
	}

	public void writeFloatLE(float f) throws IOException {
		this.writeIntLE(Float.floatToRawIntBits(f));
	}

	public void writeCharLE(char c) throws IOException {
		this.writeChar(Character.reverseBytes(c));
	}

	public void writeUnsignedShortLE(int us) throws IOException {
		this.writeShortLE((short) us);
	}

	public long getWrittenBytes() {
		return writtenBytes;
	}
}
