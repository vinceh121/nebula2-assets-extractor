package me.vinceh121.n2ae.script.nob;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;

import me.vinceh121.n2ae.FourccUtils;
import me.vinceh121.n2ae.LEDataOutputStream;
import me.vinceh121.n2ae.script.ClassCommandCall;
import me.vinceh121.n2ae.script.ICommandCall;
import me.vinceh121.n2ae.script.IWriter;
import me.vinceh121.n2ae.script.NewCommandCall;
import me.vinceh121.n2ae.script.ScriptHeader;
import me.vinceh121.n2ae.script.SelCommandCall;
import me.vinceh121.n2ae.script.UnknownClassCommandCall;

public class NOBWriter implements IWriter {
	private ScriptHeader header;
	private LinkedList<ICommandCall> calls;
	private boolean keepUnknownCommands;

	@Override
	public void write(final OutputStream out) throws IOException {
		this.write(new LEDataOutputStream(out));
	}

	public void write(final LEDataOutputStream stream) throws IOException {
		this.writeHeader(stream, this.header);
		this.writeCalls(stream, this.calls);
	}

	@Override
	public void writeHeader(final OutputStream out, final ScriptHeader header) throws IOException {
		this.writeHeader(new LEDataOutputStream(out), header);
	}

	public void writeHeader(final LEDataOutputStream stream, final ScriptHeader header) throws IOException {
		stream.writeIntLE(NOBParser.MAGIC_NUMBER);
		stream.writeString("$parser:" + header.getParser() + "$ $class:" + header.getRootClass() + "$");
	}

	@Override
	public void writeCalls(final OutputStream out, final LinkedList<ICommandCall> calls) throws IOException {
		this.writeCalls(new LEDataOutputStream(out), calls);
	}

	public void writeCalls(final LEDataOutputStream stream, final LinkedList<ICommandCall> calls) throws IOException {
		for (final ICommandCall call : calls) {
			this.writeCall(stream, call);
		}
	}

	@Override
	public void writeCall(final OutputStream out, final ICommandCall call) throws IOException {
		this.writeCall(new LEDataOutputStream(out), call);
	}

	public void writeCall(final LEDataOutputStream stream, final ICommandCall call) throws IOException {
		if (call instanceof NewCommandCall) {
			final NewCommandCall newCall = (NewCommandCall) call;
			stream.writeIntLE(NOBParser._NEW);
			stream.writeString(newCall.getClazz().getName());
			stream.writeString(newCall.getVarName());
		} else if (call instanceof SelCommandCall) {
			final SelCommandCall selCall = (SelCommandCall) call;
			stream.writeIntLE(NOBParser._SEL);
			stream.writeString(selCall.getPath());
		} else if (call instanceof ClassCommandCall) {
			final ClassCommandCall classCall = (ClassCommandCall) call;
			stream.writeIntLE(FourccUtils.fourcc(classCall.getPrototype().getFourcc()));

			// need to buffer arguments so we can get their length in bytes
			final ByteArrayOutputStream argsBuf = new ByteArrayOutputStream();
			final LEDataOutputStream argsStream = new LEDataOutputStream(argsBuf);

			for (final Object o : classCall.getArguments()) {
				if (o instanceof Integer) {
					argsStream.writeIntLE((int) o);
				} else if (o instanceof Float) {
					argsStream.writeFloatLE((float) o);
				} else if (o instanceof String) {
					argsStream.writeString((String) o);
				} else if (o instanceof Boolean) {
					argsStream.writeBoolean((boolean) o);
				} else {
					throw new IllegalArgumentException("Cannot write argument of type " + o);
				}
			}

			stream.writeShortLE((short) argsBuf.size());
			stream.write(argsBuf.toByteArray());
		} else if (call instanceof UnknownClassCommandCall) {
			if (!this.keepUnknownCommands) {
				throw new IllegalStateException("Trying to write unknown class call " + call);
			}
			final UnknownClassCommandCall classCall = (UnknownClassCommandCall) call;
			stream.writeIntLE(FourccUtils.fourcc(classCall.getFourcc()));
			stream.writeShortLE((short) classCall.getArguments().length);
			stream.write(classCall.getArguments());
		} else {
			throw new IllegalArgumentException("Cannot write command call " + call);
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

	@Override
	public boolean isKeepUnknownCommands() {
		return this.keepUnknownCommands;
	}

	@Override
	public void setKeepUnknownCommands(final boolean keepUnknownCommands) {
		this.keepUnknownCommands = keepUnknownCommands;
	}
}
