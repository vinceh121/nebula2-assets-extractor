package me.vinceh121.n2ae.model;

import java.io.Closeable;
import java.io.IOException;

public interface IMeshReader extends Closeable, AutoCloseable {
	public Mesh readMesh() throws IOException;
}
