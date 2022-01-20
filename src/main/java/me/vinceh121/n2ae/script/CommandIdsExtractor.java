package me.vinceh121.n2ae.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandIdsExtractor {
	private static final Pattern PATTERN_ADDCMD = Pattern
			.compile("AddCmd[ ]*\\([ ]*\"([_\\-0-9a-zA-Z]+)\",[ \t]*\\'([A-Z0-9_\\-]+)\\'[ ]*,");
	private final Map<String, NOBClazz> clazzes = new Hashtable<>();

	public static void main(String[] args) throws IOException {
		CommandIdsExtractor ex = new CommandIdsExtractor();
		ex.readRecurse(new File("/home/vincent/github-workspace/nebuladevice2/"));
		for (NOBClazz c : ex.getClazzes().values()) {
			System.out.println(c.getName());
			for (String key : c.getMethods().keySet()) {
				System.out.println("\t" + key + " = " + c.getMethod(key));
			}
		}
	}

	public void readRecurse(File file) throws IOException {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				readRecurse(f);
			}
		} else {
			String name = file.getName().replace("_cmds.cc", "");
			try (BufferedReader read = new BufferedReader(new FileReader(file))) {
				readFile(read, name);
			}
		}
	}

	public void readFile(BufferedReader read, String name) throws IOException {
		NOBClazz clazz = new NOBClazz();
		clazz.setName(name);
		boolean hasAnything = false;
		String line;
		while ((line = read.readLine()) != null) {
			Matcher match = PATTERN_ADDCMD.matcher(line);
			while (match.find()) {
				if (clazz.containsMethod(match.group(2))) {
					throw new RuntimeException("method already exists");
				}
				clazz.putMethod(match.group(2), match.group(1));
				hasAnything = true;
			}
		}
		if (hasAnything)
			this.clazzes.put(name, clazz);
	}

	public Map<String, NOBClazz> getClazzes() {
		return clazzes;
	}
}
