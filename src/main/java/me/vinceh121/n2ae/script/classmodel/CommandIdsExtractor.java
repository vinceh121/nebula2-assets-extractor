package me.vinceh121.n2ae.script.classmodel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.vinceh121.n2ae.script.CmdPrototype;
import me.vinceh121.n2ae.script.NOBClazz;

public class CommandIdsExtractor {
	private static final Pattern PATTERN_ADDCMD =
			Pattern.compile("AddCmd[ ]*\\([ ]*\"([_\\-0-9a-zA-Z]+)\",[ \t]*\\'([A-Z0-9_\\-]+)\\'[ ]*,");
	private final Map<String, NOBClazz> clazzes = new Hashtable<>();

	public void readRecurse(final File file) throws IOException {
		if (file.isDirectory()) {
			for (final File f : file.listFiles()) {
				this.readRecurse(f);
			}
		} else {
			final String name = file.getName().replace("_cmds.cc", "");
			try (BufferedReader read = new BufferedReader(new FileReader(file))) {
				this.readFile(read, name);
			}
		}
	}

	public void readFile(final BufferedReader read, final String name) throws IOException {
		final NOBClazz clazz = new NOBClazz();
		clazz.setName(name);
		boolean hasAnything = false;
		String line;
		while ((line = read.readLine()) != null) {
			final Matcher match = CommandIdsExtractor.PATTERN_ADDCMD.matcher(line);
			while (match.find()) {
				if (clazz.containsMethodByFourcc(match.group(2))) {
					throw new RuntimeException("method already exists");
				}
				final CmdPrototype proto = new CmdPrototype(match.group(1));
				proto.setFourcc(match.group(2));
				clazz.putMethod(proto);
				hasAnything = true;
			}
		}
		if (hasAnything) {
			this.clazzes.put(name, clazz);
		}
	}

	public Map<String, NOBClazz> getClazzes() {
		return this.clazzes;
	}
}
