package me.vinceh121.n2ae.script;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import me.vinceh121.n2ae.FourccUtils;
import me.vinceh121.n2ae.LEDataInputStream;

public class NOBScriptReader {
	public static final String MAGIC_STRING = "NOB0";
	public static final int MAGIC_NUMBER = FourccUtils.fourcc(MAGIC_STRING);
	private final LEDataInputStream stream;
	/**
	 * Stores context of created vars with `_new`. Key: var name Value: var class
	 */
	private Map<String, String> context = new Hashtable<>();
	private Map<String, NOBClazz> clazzes = new Hashtable<>();
	private final Stack<String> classStack = new Stack<>();
	private boolean ignoreUnknownMethods = true;

	public static void main(String[] args) throws IOException {
		DecompiledCommandIdsExtractor ex = new DecompiledCommandIdsExtractor();
		ex.readRecurse(new File("/home/vincent/Decomp/projectnomads/decomp-dumps"));

		NOBClazz nroot = new NOBClazz();
		nroot.setName("nroot");
		ex.getClazzes().put(nroot.getName(), nroot);

		System.out.println("Knows " + ex.getClazzes().size() + " classes");
		for (NOBClazz c : ex.getClazzes().values()) {
			System.out.println(c.getName() + " : " + c.getSuperclass());
			for (Entry<String, CmdPrototype> e : c.getMethods().entrySet()) {
				System.out.println("\t" + e.getKey() + " " + e.getValue());
			}
		}

		LEDataInputStream stream = new LEDataInputStream(new FileInputStream(
				"/home/vincent/.wine/drive_c/Program Files (x86)/Nebula2 SDK/bin/win32/pack.npk/s_scoutgarage01.n/_main.n"));
		NOBScriptReader read = new NOBScriptReader(stream);
		read.setClazzes(ex.getClazzes());
		read.readHeader();
		while (stream.available() > 0) {
			read.readBlock();
		}
	}

	public NOBScriptReader(LEDataInputStream stream) {
		this.stream = stream;
	}

	public void readHeader() throws IOException {
		int magic = this.stream.readIntLE();
		if (magic != MAGIC_NUMBER) {
			throw new IOException("Invalid magic number");
		}

		String header = this.readString();
		System.out.println(header);
	}

	public void readBlock() throws IOException {
		int cmd = this.stream.readIntLE();

		if (cmd == FourccUtils.fourcc("_new")) {
			String clazz = this.readString();
			String name = this.readString();
			this.context.put(name, clazz);
			this.classStack.push(clazz); // _new automatically cds into created object
			System.out.println("new " + clazz + " " + name);
		} else if (cmd == FourccUtils.fourcc("_sel")) {
			String path = this.readString();
			if ("..".equals(path)) {
				this.classStack.pop();
			}
			System.out.println("sel " + path);
		} else {
			NOBClazz cls = this.clazzes.get(this.classStack.peek());
			String fourcc = FourccUtils.fourccToString(cmd);
			CmdPrototype method = this.recursiveGetMethod(cls, fourcc);
			short argLength = this.stream.readShortLE();
			if (method == null) {
				if (this.ignoreUnknownMethods) {
					System.err.println("Skipping " + Integer.toHexString(cmd) + " " + fourcc + " " + method);
					this.stream.skip(argLength);
					return;
				} else {
					throw new IllegalStateException("Couldn't find method " + Integer.toHexString(cmd) + " " + fourcc
							+ " in hiearchy of class " + cls.getName());
				}
			}
			System.out.println(Integer.toHexString(cmd) + " " + fourcc + " " + method);
			int argCount = method.getInArgs().size();
			for (int i = 0; i < argCount; i++) {
				NOBType arg = method.getInArgs().get(i);
				System.out.print("\t");
				switch (arg) {
				case INT:
					System.out.println(this.stream.readIntLE());
					break;
				case FLOAT:
					System.out.println(this.stream.readFloatLE());
					break;
				case STRING:
				case USTRING:
				case CODE:
					int strLength = this.stream.readUnsignedShortLE();
					System.out.println("\"" + new String(this.stream.readNBytes(strLength)) + "\"");
					break;
				case BOOL:
					System.out.println(this.stream.readCharLE() != 0);
					break;
				case VOID:
					break;
				default:
					throw new IllegalArgumentException("fuck " + arg);
				}
			}
		}
	}

	public CmdPrototype recursiveGetMethod(NOBClazz cls, String fourcc) {
		if (cls.containsMethod(fourcc)) {
			return cls.getMethod(fourcc);
		} else {
			if (cls.getSuperclass() != null) {
				NOBClazz sc = this.clazzes.get(cls.getSuperclass());
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
		int size = this.stream.readUnsignedShortLE();
		return new String(this.stream.readNBytes(size));
	}

	/**
	 * @return the clazzes
	 */
	public Map<String, NOBClazz> getClazzes() {
		return clazzes;
	}

	/**
	 * @param clazzes the clazzes to set
	 */
	public void setClazzes(Map<String, NOBClazz> clazzes) {
		this.clazzes = clazzes;
	}
}
