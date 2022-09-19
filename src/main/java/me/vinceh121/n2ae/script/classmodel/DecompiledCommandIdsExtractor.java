package me.vinceh121.n2ae.script.classmodel;

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
import me.vinceh121.n2ae.script.CmdPrototype;
import me.vinceh121.n2ae.script.NOBClazz;

public class DecompiledCommandIdsExtractor {
	public static final Pattern PAT_CLASS_INIT = Pattern.compile("__cdecl n_init_([a-zA-Z0-9]+)\\("),
			PAT_SUPERCLASS = Pattern.compile(Pattern.quote("(**(code **)(*param_2 + ") + "[0-9a-fx]+"
					+ Pattern.quote("))(\"") + "([a-zA-Z0-9]+)\"[,\\)]"),
			PAT_ADDCMD = Pattern.compile("nClass::AddCmd\\(param_1,\"([_a-zA-Z0-9]+)\",0x([0-9a-z]+),"),
			PAT_CLASS_MANUAL_INIT = Pattern.compile("__cdecl ([a-zA-Z0-9]+)_init\\(");
	public static final Pattern[] PAT_INIT_CMDS = { Pattern.compile("uVar1 = FUN_([0-9a-zA-Z]+)\\(param_1\\)"),
			Pattern.compile("FUN_([0-9a-zA-Z]+)\\(param_1\\)") };
	private final Map<String, NOBClazz> clazzes = new Hashtable<>();

	public void readRecurse(final File file) throws IOException {
		if (file.isDirectory()) {
			for (final File f : file.listFiles()) {
				this.readRecurse(f);
			}
		} else {
			this.readFile(file);
		}
	}

	public void readFile(final File file) throws FileNotFoundException, IOException {
		try (final BufferedReader br = new BufferedReader(new FileReader(file))) {
			this.readFile(br.lines().collect(Collectors.toList()));
		}
	}

	public void readFile(final List<String> lines) {
		for (int i = 0; i < lines.size(); i++) {
			final String line = lines.get(i);
			final Matcher initMatcher;
			if ((initMatcher = this
				.tryMatchers(new Matcher[] { DecompiledCommandIdsExtractor.PAT_CLASS_INIT.matcher(line),
						DecompiledCommandIdsExtractor.PAT_CLASS_MANUAL_INIT.matcher(line) })) != null) {
				final String className = initMatcher.group(1);

				final NOBClazz clazz = new NOBClazz(); // we register classes even if they don't have methods
				clazz.setName(className);
				this.clazzes.put(className, clazz);
				String callLine;
				while (!(callLine = lines.get(i++)).contains("return")) { // if we reach a return, this init doesn't
																			// have n_initcmds nor superclass
					final Matcher superClassMatcher = DecompiledCommandIdsExtractor.PAT_SUPERCLASS.matcher(callLine);

					if (superClassMatcher.find()) {
						final String superClass = superClassMatcher.group(1);
						clazz.setSuperclass(superClass);
					}

					if (this.findNInitCmdsCall(callLine, lines, clazz)) {
						break;
					}
				}
			}
		}
	}

	private Matcher tryMatchers(final Matcher[] matchers) {
		for (final Matcher m : matchers) {
			if (m.find()) {
				return m;
			}
		}
		return null;
	}

	private boolean findNInitCmdsCall(CharSequence callLine, List<String> lines, NOBClazz clazz) {
		for (Pattern patInit : DecompiledCommandIdsExtractor.PAT_INIT_CMDS) {
			final Matcher callMatcher = patInit.matcher(callLine);
			if (callMatcher.find()) { // we've found our n_initcmds
				final int funPos = this.findNInitCmds(lines, callMatcher.group(1));

				if (funPos != -1) {
					this.readCmds(lines, clazz, funPos);
					return true;
				}
			}
		}
		return false;
	}
	
	private void readCmds(final List<String> lines, final NOBClazz clazz, final int funPos) {
		for (int i = funPos; i < lines.size(); i++) {
			final String line = lines.get(i);
			if (line.contains("nClass::EndCmds")) {
				break;
			}

			final Matcher addCmdMatcher = DecompiledCommandIdsExtractor.PAT_ADDCMD.matcher(line);
			if (addCmdMatcher.find()) {
				final int iFourcc = Integer.parseInt(addCmdMatcher.group(2), 16);
				final String sFourcc = FourccUtils.fourccToString(iFourcc);
				final CmdPrototype proto = new CmdPrototype(addCmdMatcher.group(1));
				proto.setFourcc(sFourcc);
				clazz.putMethod(proto);
			}
		}
	}

	private int findNInitCmds(final List<String> lines, final String funPtr) {
		for (int i = 0; i < lines.size(); i++) {
			final String line = lines.get(i);
			if (line.contains("__cdecl FUN_" + funPtr + "(nClass")) {
				return i;
			}
		}
		return -1;
	}

	public Map<String, NOBClazz> getClazzes() {
		return this.clazzes;
	}
}
