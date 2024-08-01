package me.vinceh121.n2ae.script.tcl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;

import me.vinceh121.n2ae.script.ClassCommandCall;
import me.vinceh121.n2ae.script.ICommandCall;
import me.vinceh121.n2ae.script.IWriter;
import me.vinceh121.n2ae.script.NewCommandCall;
import me.vinceh121.n2ae.script.ScriptHeader;
import me.vinceh121.n2ae.script.SelCommandCall;
import me.vinceh121.n2ae.script.UnknownClassCommandCall;

public class TCLWriter implements IWriter {
	private ScriptHeader header;
	private LinkedList<ICommandCall> calls;
	private String indent = "\t";
	private boolean keepUnknownCommands;
	private int depth;

	@Override
	public void write(final OutputStream out) throws IOException {
		this.write(new PrintWriter(out, true));
	}

	public void write(final PrintWriter writer) throws IOException {
		this.writeHeader(writer, this.header);
		this.writeCalls(writer, this.calls);
	}

	@Override
	public void writeHeader(final OutputStream out, final ScriptHeader header) throws IOException {
		this.writeHeader(new PrintWriter(out, true), header);
	}

	public void writeHeader(final PrintWriter writer, final ScriptHeader header) throws IOException {
		writer.println("# ---");
		writer.print("# $parser:");
		writer.print(header.getParser());
		writer.print("$ $class:");
		writer.print(header.getRootClass());
		writer.println("$");
		writer.println("# ---");
	}

	@Override
	public void writeCalls(final OutputStream out, final LinkedList<ICommandCall> calls) throws IOException {
		this.writeCalls(new PrintWriter(out, true), calls);
	}

	public void writeCalls(final PrintWriter writer, final LinkedList<ICommandCall> calls) throws IOException {
		for (final ICommandCall c : calls) {
			this.writeCall(writer, c);
		}
	}

	@Override
	public void writeCall(final OutputStream out, final ICommandCall call) throws IOException {
		this.writeCall(new PrintWriter(out, true), call);
	}

	public void writeCall(final PrintWriter writer, final ICommandCall call) throws IOException {
		if (call instanceof NewCommandCall) {
			this.writeIndent(writer);
			final NewCommandCall newCall = (NewCommandCall) call;
			writer.print("new ");
			writer.print(newCall.getNewClazz().getName());
			writer.print(" ");
			writer.println(newCall.getVarName());
			this.depth++;
		} else if (call instanceof final SelCommandCall selCall) {
			if ("..".equals(selCall.getPath())) {
				this.depth--;
			}
			this.writeIndent(writer);
			writer.print("sel ");
			writer.println(selCall.getPath());
		} else if (call instanceof final ClassCommandCall clsCall) {
			this.writeIndent(writer);
			writer.print(".");
			writer.print(clsCall.getPrototype().getName());
			writer.print(" ");

			final Object[] args = clsCall.getArguments();
			for (int i = 0; i < args.length; i++) {
				final Object a = args[i];
				if (a instanceof Integer) {
					writer.print((int) a);
				} else if (a instanceof Float) {
					writer.print((float) a);
				} else if (a instanceof String) {
					writer.print("\"");
					writer.print((String) a);
					writer.print("\"");
				} else if (a instanceof Boolean) {
					writer.print((boolean) a);
				}
				if (i + 1 != args.length) {
					writer.print(" ");
				}
			}
			writer.println();
		} else if (call instanceof UnknownClassCommandCall) {
			if (!this.keepUnknownCommands) {
				throw new IllegalStateException("Trying to write unknown class call " + call);
			}
			final UnknownClassCommandCall clsCall = (UnknownClassCommandCall) call;
			writer.print("UNK_");
			writer.print(clsCall.getFourcc());
			writer.print(" 0x");
			this.writeHex(writer, clsCall.getArguments());
			writer.println();
		} else {
			throw new IllegalStateException("Don't know how to writer call " + call);
		}
	}

	private void writeHex(final PrintWriter writer, final byte[] data) {
		for (final byte b : data) {
			final int upperNibble = (b & 0xF0) >> 4;
			final int lowerNibble = b & 0xF;
			writer.print(Integer.toHexString(upperNibble));
			writer.print(Integer.toHexString(lowerNibble));
		}
	}

	private void writeIndent(final PrintWriter writer) throws IOException {
		for (int i = 0; i < this.depth; i++) {
			writer.print(this.indent);
		}
	}

	@Override
	public ScriptHeader getHeader() {
		return this.header;
	}

	@Override
	public void setHeader(final ScriptHeader header) {
		this.header = header;
	}

	@Override
	public LinkedList<ICommandCall> getCalls() {
		return this.calls;
	}

	@Override
	public void setCalls(final LinkedList<ICommandCall> calls) {
		this.calls = calls;
	}

	public String getIndent() {
		return this.indent;
	}

	public void setIndent(final String indent) {
		this.indent = indent;
	}

	public int getDepth() {
		return this.depth;
	}

	public void setDepth(final int depth) {
		this.depth = depth;
	}

	@Override
	public void setKeepUnknownCommands(final boolean keepUnknownCommands) {
		this.keepUnknownCommands = keepUnknownCommands;
	}

	@Override
	public boolean isKeepUnknownCommands() {
		return this.keepUnknownCommands;
	}
}
