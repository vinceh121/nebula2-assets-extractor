package me.vinceh121.n2ae.gltf;

public class BufferView {
	private int buffer;
	private long byteLength, byteOffset;

	public int getBuffer() {
		return this.buffer;
	}

	public void setBuffer(final int buffer) {
		this.buffer = buffer;
	}

	public long getByteLength() {
		return this.byteLength;
	}

	public void setByteLength(final long byteLength) {
		this.byteLength = byteLength;
	}

	public long getByteOffset() {
		return this.byteOffset;
	}

	public void setByteOffset(final long byteOffset) {
		this.byteOffset = byteOffset;
	}
}
