package me.vinceh121.n2ae;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LEDataInputStream extends DataInputStream {
	private long readBytes = 0;

	public LEDataInputStream(InputStream in) {
		super(in);
	}

	@Override
	public int read() throws IOException {
		readBytes++;
		return super.read();
	}

	public int readIntLE() throws IOException {
		return Integer.reverseBytes(this.readInt());
	}

	public short readShortLE() throws IOException {
		return Short.reverseBytes(this.readShort());
	}

	public float readFloatLE() throws IOException {
		return Float.intBitsToFloat(this.readIntLE());
	}
	
	public char readCharLE() throws IOException {
		return Character.reverseBytes(this.readChar());
	}

	public int readUnsignedShortLE() throws IOException {
		return this.readShortLE() & 0xFFFF;
	}

	public long getReadBytes() {
		return readBytes;
	}
}
