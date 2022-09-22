package me.vinceh121.n2ae.script;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;

public class TCLWriter implements IWriter {
	private ScriptHeader header;
	private LinkedList<ICommandCall> calls;
	private String indent = "\t";
	private int depth;

	@Override
	public void write(OutputStream out) throws IOException {
		this.write(new PrintWriter(out, true));
	}

	public void write(PrintWriter writer) throws IOException {
		this.writeHeader(writer, this.header);
		this.writeCalls(writer, this.calls);
	}

	@Override
	public void writeHeader(OutputStream out, ScriptHeader header) throws IOException {
		this.writeHeader(new PrintWriter(out, true), header);
	}

	public void writeHeader(PrintWriter writer, ScriptHeader header) throws IOException {
		writer.println("# ---");
		writer.print("# $parser:");
		writer.print(header.getParser());
		writer.print("$ $class:");
		writer.print(header.getRootClass());
		writer.println("$");
		writer.println("# ---");
	}

	@Override
	public void writeCalls(OutputStream out, LinkedList<ICommandCall> calls) throws IOException {
		this.writeCalls(new PrintWriter(out, true), calls);
	}

	public void writeCalls(PrintWriter writer, LinkedList<ICommandCall> calls) throws IOException {
		for (ICommandCall c : calls) {
			this.writeCall(writer, c);
		}
	}

	@Override
	public void writeCall(OutputStream out, ICommandCall call) throws IOException {
		this.writeCall(new PrintWriter(out, true), call);
	}

	public void writeCall(PrintWriter writer, ICommandCall call) throws IOException {
		if (call instanceof NewCommandCall) {
			this.writeIndent(writer);
			NewCommandCall newCall = (NewCommandCall) call;
			writer.print("new ");
			writer.print(newCall.getNewClazz().getName());
			writer.print(" ");
			writer.println(newCall.getVarName());
			this.depth++;
		} else if (call instanceof SelCommandCall) {
			SelCommandCall selCall = (SelCommandCall) call;
			if ("..".equals(selCall.getPath())) {
				this.depth--;
			}
			this.writeIndent(writer);
			writer.print("sel ");
			writer.println(selCall.getPath());
		} else if (call instanceof ClassCommandCall) {
			ClassCommandCall clsCall = (ClassCommandCall) call;
			this.writeIndent(writer);
			writer.print(".");
			writer.print(clsCall.getPrototype().getName());
			writer.print(" ");

			Object[] args = clsCall.getArguments();
			for (int i = 0; i < args.length; i++) {
				Object a = args[i];
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
				if (i+1 != args.length) {
					writer.print(" ");
				}
			}
			writer.println();
		} else if (call instanceof UnknownClassCommandCall) {
			UnknownClassCommandCall clsCall = (UnknownClassCommandCall) call;
			writer.print("UNK_");
			writer.print(clsCall.getFourcc());
			writer.print(" 0x");
			this.writeHex(writer, clsCall.getArguments());
			writer.println();
		} else {
			throw new IllegalStateException("Don't know how to writer call " + call);
		}
	}
	
	private void writeHex(PrintWriter writer, byte[] data) {
		for (byte b : data) {
			final int upperNibble = (b & 0xF0) >> 4;
			final int lowerNibble = b & 0xF;
			writer.print(Integer.toHexString(upperNibble));
			writer.print(Integer.toHexString(lowerNibble));
		}
	}

	private void writeIndent(PrintWriter writer) throws IOException {
		for (int i = 0; i < depth; i++) {
			writer.print(this.indent);
		}
	}

	public ScriptHeader getHeader() {
		return header;
	}

	public void setHeader(ScriptHeader header) {
		this.header = header;
	}

	public LinkedList<ICommandCall> getCalls() {
		return calls;
	}

	public void setCalls(LinkedList<ICommandCall> calls) {
		this.calls = calls;
	}

	public String getIndent() {
		return indent;
	}

	public void setIndent(String indent) {
		this.indent = indent;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}
}
