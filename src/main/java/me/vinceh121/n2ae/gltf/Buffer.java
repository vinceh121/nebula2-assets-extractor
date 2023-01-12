package me.vinceh121.n2ae.gltf;

public class Buffer {
	private long byteLength;
	private String uri;

	public long getByteLength() {
		return this.byteLength;
	}

	public void setByteLength(final long byteLength) {
		this.byteLength = byteLength;
	}

	public String getUri() {
		return this.uri;
	}

	public void setUri(final String uri) {
		this.uri = uri;
	}
}
