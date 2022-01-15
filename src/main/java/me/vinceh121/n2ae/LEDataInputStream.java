package me.vinceh121.n2ae;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LEDataInputStream extends DataInputStream {

	public LEDataInputStream(InputStream in) {
		super(in);
	}

	public int readIntLE() throws IOException {
		return Integer.reverseBytes(this.readInt());
	}
	
	public short readShortLE() throws IOException {
		return Short.reverseBytes(this.readShort());
	}
	
}
