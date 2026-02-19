package me.vinceh121.n2ae.model;

import java.io.IOException;
import java.io.OutputStream;

public abstract class AbstractMeshWriter implements IMeshWriter {
	protected final OutputStream outputStream;

	public AbstractMeshWriter(final OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@Override
	public void close() throws IOException {
		this.outputStream.close();
	}
}
