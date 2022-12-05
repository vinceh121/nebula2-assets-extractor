package me.vinceh121.n2ae.gltf;

public class BufferView {
	private int buffer;
	private long byteLength, byteOffset;

	public int getBuffer() {
		return buffer;
	}

	public void setBuffer(int buffer) {
		this.buffer = buffer;
	}

	public long getByteLength() {
		return byteLength;
	}

	public void setByteLength(long byteLength) {
		this.byteLength = byteLength;
	}

	public long getByteOffset() {
		return byteOffset;
	}

	public void setByteOffset(long byteOffset) {
		this.byteOffset = byteOffset;
	}
}
