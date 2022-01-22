package me.vinceh121.n2ae.script;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import me.vinceh121.n2ae.FourccUtils;
import me.vinceh121.n2ae.LEDataInputStream;

public class NOBScriptReader {
	public static final String MAGIC_STRING = "NOB0";
	public static final int MAGIC_NUMBER = 1313817136;
	private final LEDataInputStream stream;
	/**
	 * Stores context of created vars with `_new`. Key: var name Value: var class
	 */
	private Map<String, String> context = new Hashtable<>();
	private Map<String, NOBClazz> clazzes = new Hashtable<>();

	private String currentClass = "nroot";

	public static void main(String[] args) throws IOException {
		CommandIdsExtractor ex = new CommandIdsExtractor();
		ex.readRecurse(new File("/home/vincent/github-workspace/nebuladevice2/"));

		LEDataInputStream stream = new LEDataInputStream(new FileInputStream(
				"/home/vincent/.wine/drive_c/Program Files (x86)/Nebula2 SDK/bin/win32/pack.npk/bomber03.n/_main.n"));
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
			this.currentClass = clazz; // _new automatically cds into created object
			System.out.println("new " + clazz + " " + name);
		} else if (cmd == FourccUtils.fourcc("_sel")) {
			String path = this.readString();
			System.out.println("sel " + path);
		} else {
			NOBClazz cls = this.clazzes.get(this.currentClass);
			String fourcc = FourccUtils.fourccToString(cmd);
			/* short somethingToSkipIdkWhat = */this.stream.readShortLE();
			if (cls != null)
				System.out.println(cls.getMethod(fourcc));
			else
				System.out.println(fourcc);
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
