package me.vinceh121.n2ae.model;

import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractMeshReader implements IMeshReader {
	protected final InputStream inputStream;

	public AbstractMeshReader(final InputStream inputStream) {
		this.inputStream = inputStream;
	}

	@Override
	public void close() throws IOException {
		this.inputStream.close();
	}
}
