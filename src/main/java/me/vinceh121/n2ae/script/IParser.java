package me.vinceh121.n2ae.script;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Map;

public interface IParser {
	default void read(final String script) throws IOException, ParseException {
		this.read(script.getBytes());
	}

	default void read(final byte[] bytes) throws IOException, ParseException {
		this.read(new ByteArrayInputStream(bytes));
	}

	void read(InputStream in) throws IOException, ParseException;

	ScriptHeader getHeader();

	LinkedList<ICommandCall> getCalls();

	void setClassModel(Map<String, NOBClazz> classModel);

	void setKeepUnknownCommands(final boolean keepUnknownCommands);

	boolean isKeepUnknownCommands();
}
