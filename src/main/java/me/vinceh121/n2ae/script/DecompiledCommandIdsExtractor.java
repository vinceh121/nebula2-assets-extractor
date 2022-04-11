package me.vinceh121.n2ae.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import me.vinceh121.n2ae.FourccUtils;

public class DecompiledCommandIdsExtractor {
	public static final Pattern PAT_CLASS_INIT = Pattern.compile("__cdecl n_init_([a-z]+)\\("),
			PAT_INIT_CMDS_CALL = Pattern.compile("uVar1 = FUN_([0-9]+)\\(param_1\\)"),
			PAT_ADDCMD = Pattern.compile("nClass::AddCmd\\(param_1,\"([_a-zA-Z0-9]+)\",0x([0-9a-z]+),");
	private final Map<String, NOBClazz> clazzes = new Hashtable<>();

	public void readRecurse(File file) throws IOException {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				readRecurse(f);
			}
		} else {
			readFile(file);
		}
	}

	public void readFile(File file) throws FileNotFoundException, IOException {
		try (final BufferedReader br = new BufferedReader(new FileReader(file))) {
			readFile(br.lines().collect(Collectors.toList()));
		}
	}

	public void readFile(List<String> lines) {
		for (int i = 0; i < lines.size(); i++) {
			final String line = lines.get(i);
			final Matcher initMatcher = PAT_CLASS_INIT.matcher(line);
			if (initMatcher.find()) {
				final String className = initMatcher.group(1);

				String callLine;
				while (!(callLine = lines.get(i++)).contains("return")) { // if we reach a return, this init doesn't
																			// have n_initcmds
					final Matcher callMatcher = PAT_INIT_CMDS_CALL.matcher(callLine);
					if (callMatcher.find()) { // we've found our n_initcmds
						final int funPos = findNInitCmds(lines, callMatcher.group(1));
						if (funPos != -1)
							readCmds(lines, className, funPos);
						break; // no need to search more
					}
				}
			}
		}
	}

	private void readCmds(List<String> lines, String className, int funPos) {
		final NOBClazz clazz = new NOBClazz();
		clazz.setName(className);
		for (int i = funPos; i < lines.size(); i++) {
			final String line = lines.get(i);
			if (line.contains("nClass::EndCmds")) {
				break;
			}

			final Matcher addCmdMatcher = PAT_ADDCMD.matcher(line);
			if (addCmdMatcher.find()) {
				final int iFourcc = Integer.parseInt(addCmdMatcher.group(2), 16);
				final String sFourcc = FourccUtils.fourccToString(iFourcc);
				final CmdPrototype proto = new CmdPrototype(addCmdMatcher.group(1));
				clazz.putMethod(sFourcc, proto);
			}
		}
		this.clazzes.put(className, clazz);
	}

	private int findNInitCmds(List<String> lines, String funPtr) {
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.contains("__cdecl FUN_" + funPtr + "(nClass")) {
				return i;
			}
		}
		return -1;
	}

	public Map<String, NOBClazz> getClazzes() {
		return clazzes;
	}
}
