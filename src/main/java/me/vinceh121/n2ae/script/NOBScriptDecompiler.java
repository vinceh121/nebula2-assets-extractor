package me.vinceh121.n2ae.script;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;

import me.vinceh121.n2ae.FourccUtils;
import me.vinceh121.n2ae.LEDataInputStream;

public class NOBScriptDecompiler {
	public static final String MAGIC_STRING = "NOB0";
	public static final int MAGIC_NUMBER = FourccUtils.fourcc(NOBScriptDecompiler.MAGIC_STRING);
	private final LEDataInputStream stream;
	/**
	 * Stores context of created vars with `_new`. Key: var name Value: var class
	 */
	private final Map<String, String> context = new Hashtable<>();
	private Map<String, NOBClazz> clazzes = new Hashtable<>();
	private final Stack<String> classStack = new Stack<>();
	private final boolean ignoreUnknownMethods = true;

	public NOBScriptDecompiler(final InputStream stream) {
		this.stream = new LEDataInputStream(stream);
	}

	public String readHeader() throws IOException {
		final int magic = this.stream.readIntLE();
		if (magic != NOBScriptDecompiler.MAGIC_NUMBER) {
			throw new IOException("Invalid magic number");
		}

		final String header = this.readString();
		final StringBuilder sb = new StringBuilder();
		sb.append("# ---\n");
		sb.append("# " + header + "\n");
		sb.append("# ---\n");
		return sb.toString();
	}

	public String readBlock() throws IOException {
		final StringBuilder sb = new StringBuilder();
		final int cmd = this.stream.readIntLE();

		if (cmd == FourccUtils.fourcc("_new")) {
			final String clazz = this.readString();
			final String name = this.readString();
			this.context.put(name, clazz);
			this.classStack.push(clazz); // _new automatically cds into created object
			sb.append("new " + clazz + " " + name + "\n");
		} else if (cmd == FourccUtils.fourcc("_sel")) {
			final String path = this.readString();
			if ("..".equals(path)) {
				this.classStack.pop();
			}
			sb.append("sel " + path + " # " + this.classStack + "\n\n");
		} else {
			final NOBClazz cls = this.clazzes.get(this.classStack.peek());
			if (cls == null) {
				throw new IllegalStateException("Unknown nscript class " + this.classStack.peek());
			}
			final String fourcc = FourccUtils.fourccToString(cmd);
			final CmdPrototype method = this.recursiveGetMethod(cls, fourcc);
			final short argLength = this.stream.readShortLE();
			if (method == null) {
				if (this.ignoreUnknownMethods) {
					System.err.println("Skipping " + Integer.toHexString(cmd) + " " + fourcc + " " + method);
					this.stream.skip(argLength);
					return sb.toString();
				} else {
					throw new IllegalStateException("Couldn't find method " + Integer.toHexString(cmd) + " " + fourcc
							+ " in hiearchy of class " + cls.getName());
				}
			}
			sb.append("." + method.getName());
			final int argCount = method.getInArgs().size();
			for (int i = 0; i < argCount; i++) {
				final NOBType arg = method.getInArgs().get(i);
				sb.append(" ");
				switch (arg) {
				case INT:
					sb.append(this.stream.readIntLE());
					break;
				case FLOAT:
					sb.append(this.stream.readFloatLE());
					break;
				case STRING:
				case USTRING:
				case CODE:
					sb.append("\"" + this.readString() + "\"");
					break;
				case BOOL:
					sb.append(this.stream.readByte() != 0);
					break;
				case VOID:
					break;
				default:
					throw new IllegalArgumentException("fuck " + arg);
				}
			}
			sb.append("\n");
		}
		return sb.toString();
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

	public String readString() throws IOException { // maybe this should be moved to
													// LEDataInputStream if it's common to
													// the entire engine
		final int size = this.stream.readUnsignedShortLE();
		return new String(this.stream.readNBytes(size));
	}

	/**
	 * @return the clazzes
	 */
	public Map<String, NOBClazz> getClazzes() {
		return this.clazzes;
	}

	/**
	 * @param clazzes the clazzes to set
	 */
	public void setClazzes(final Map<String, NOBClazz> clazzes) {
		this.clazzes = clazzes;
	}
}
