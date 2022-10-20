package me.vinceh121.n2ae.script.nob;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.vinceh121.n2ae.FourccUtils;
import me.vinceh121.n2ae.LEDataInputStream;
import me.vinceh121.n2ae.script.ClassCommandCall;
import me.vinceh121.n2ae.script.CmdPrototype;
import me.vinceh121.n2ae.script.ICommandCall;
import me.vinceh121.n2ae.script.IParser;
import me.vinceh121.n2ae.script.NOBClazz;
import me.vinceh121.n2ae.script.NOBType;
import me.vinceh121.n2ae.script.NewCommandCall;
import me.vinceh121.n2ae.script.ScriptHeader;
import me.vinceh121.n2ae.script.SelCommandCall;
import me.vinceh121.n2ae.script.UnknownClassCommandCall;

public class NOBParser implements IParser {
	public static final Pattern PAT_HEADER = Pattern.compile("\\$parser\\:([a-z]+)\\$ \\$class\\:([a-z]+)\\$");
	public static final String MAGIC_STRING = "NOB0";
	public static final int MAGIC_NUMBER = FourccUtils.fourcc(NOBParser.MAGIC_STRING),
			_NEW = FourccUtils.fourcc("_new"), _SEL = FourccUtils.fourcc("_sel");
	private final Stack<String> classStack = new Stack<>();
	private final LinkedList<ICommandCall> calls = new LinkedList<>();
	private Map<String, NOBClazz> clazzes = new Hashtable<>();
	private ScriptHeader header;
	private boolean keepUnknownCommands;

	@Override
	public void read(final InputStream in) throws IOException {
		this.read(new LEDataInputStream(in));
	}

	public void read(final LEDataInputStream stream) throws IOException {
		this.readHeader(stream);
		while (stream.available() > 0) {
			this.calls.add(this.readBlock(stream));
		}
	}

	private ICommandCall readBlock(final LEDataInputStream stream) throws IOException {
		final int cmdFourcc = stream.readIntLE();

		if (cmdFourcc == NOBParser._NEW) {
			final String clazzName = stream.readString();
			final NOBClazz clazz = this.clazzes.get(clazzName);
			final String name = stream.readString();
			this.classStack.push(clazzName); // _new automatically cds into created object

			final NewCommandCall call = new NewCommandCall(this.getLastClazz(), clazz, name);
			return call;
		} else if (cmdFourcc == NOBParser._SEL) {
			final String path = stream.readString();
			final SelCommandCall call = new SelCommandCall(this.getLastClazz(), path);
			if ("..".equals(path)) {
				this.classStack.pop();
			}
			return call;
		} else {
			final NOBClazz cls = this.clazzes.get(this.classStack.peek());
			if (cls == null) {
				throw new IllegalStateException("Unknown nscript class " + this.classStack.peek());
			}
			final String fourcc = FourccUtils.fourccToString(cmdFourcc);
			final CmdPrototype command = this.recursiveGetMethod(cls, fourcc);

			final short argLength = stream.readShortLE();
			if (command == null) {
				if (this.keepUnknownCommands) {
					System.err.println("Writing unknown " + Integer.toHexString(cmdFourcc) + " " + fourcc);
					final byte[] args = stream.readNBytes(argLength);
					final UnknownClassCommandCall call = new UnknownClassCommandCall(this.getLastClazz(),
							FourccUtils.fourccToString(cmdFourcc),
							args);
					return call;
				} else {
					throw new IllegalStateException("Couldn't find method " + Integer.toHexString(cmdFourcc) + " "
							+ fourcc + " in hiearchy of class " + cls.getName());
				}
			}

			final ClassCommandCall call = new ClassCommandCall();
			call.setClazz(cls);
			call.setPrototype(command);

			final int argCount = command.getInArgs().size();
			final List<Object> args = new ArrayList<>(argCount);
			for (int i = 0; i < argCount; i++) {
				final NOBType arg = command.getInArgs().get(i);
				switch (arg) {
				case INT:
					args.add(stream.readIntLE());
					break;
				case FLOAT:
					args.add(stream.readFloatLE());
					break;
				case STRING:
				case USTRING:
				case CODE:
					args.add(stream.readString());
					break;
				case BOOL:
					args.add(stream.readByte() != 0);
					break;
				case VOID:
					break;
				default:
					throw new IllegalArgumentException("fuck " + arg);
				}
			}
			call.setArguments(args.toArray());
			return call;
		}
	}

	private void readHeader(final LEDataInputStream stream) throws IOException {
		final int magic = stream.readIntLE();
		if (magic != NOBParser.MAGIC_NUMBER) {
			throw new IOException("Invalid magic number");
		}

		final String headerString = stream.readString();
		final Matcher match = NOBParser.PAT_HEADER.matcher(headerString);
		match.find();
		this.header = new ScriptHeader(match.group(1), match.group(2));
	}

	private NOBClazz getLastClazz() {
		return this.clazzes.get(this.classStack.peek());
	}

	public CmdPrototype recursiveGetMethod(final NOBClazz cls, final String fourcc) {
		if (cls.containsMethodByFourcc(fourcc)) {
			return cls.getMethodByFourcc(fourcc);
		} else {
			if (cls.getSuperclass() != null) {
				final NOBClazz sc = this.clazzes.get(cls.getSuperclass());
				if (sc == null) {
					throw new IllegalStateException(cls.getName() + " has unknown superclass " + cls.getSuperclass());
				}
				return this.recursiveGetMethod(sc, fourcc);
			} else {
				return null;
			}
		}
	}

	@Override
	public ScriptHeader getHeader() {
		return this.header;
	}

	@Override
	public LinkedList<ICommandCall> getCalls() {
		return this.calls;
	}

	@Override
	public void setClassModel(final Map<String, NOBClazz> classModel) {
		this.clazzes = classModel;
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
