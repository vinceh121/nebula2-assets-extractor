package me.vinceh121.n2ae.script;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;

public interface IWriter {
	void write(OutputStream out) throws IOException;

	void writeHeader(OutputStream out, ScriptHeader header) throws IOException;

	void writeCalls(OutputStream out, LinkedList<ICommandCall> calls) throws IOException;

	void writeCall(OutputStream out, ICommandCall call) throws IOException;

	void setKeepUnknownCommands(boolean keepUnknownCommands);

	boolean isKeepUnknownCommands();

	void setCalls(LinkedList<ICommandCall> calls);

	LinkedList<ICommandCall> getCalls();

	void setHeader(ScriptHeader header);

	ScriptHeader getHeader();
}
