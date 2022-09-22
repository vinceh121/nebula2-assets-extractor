package me.vinceh121.n2ae.script;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Map;

public interface IParser {
	default void read(String script) throws IOException {
		this.read(script.getBytes());
	}

	default void read(byte[] bytes) throws IOException {
		this.read(new ByteArrayInputStream(bytes));
	}

	void read(InputStream in) throws IOException;

	ScriptHeader getHeader();

	LinkedList<ICommandCall> getCalls();

	void setClassModel(Map<String, NOBClazz> classModel);
}
