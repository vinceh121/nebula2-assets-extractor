package me.vinceh121.n2ae.model;

import java.io.Closeable;
import java.io.IOException;

public interface IMeshWriter extends Closeable, AutoCloseable {
	public void writeMesh(Mesh mesh) throws IOException;
}
