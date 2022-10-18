package me.vinceh121.n2ae.script.tcl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Matcher;

import me.vinceh121.n2ae.script.ClassCommandCall;
import me.vinceh121.n2ae.script.CmdPrototype;
import me.vinceh121.n2ae.script.ICommandCall;
import me.vinceh121.n2ae.script.IParser;
import me.vinceh121.n2ae.script.NOBClazz;
import me.vinceh121.n2ae.script.NOBType;
import me.vinceh121.n2ae.script.NewCommandCall;
import me.vinceh121.n2ae.script.ScriptHeader;
import me.vinceh121.n2ae.script.SelCommandCall;
import me.vinceh121.n2ae.script.nob.NOBParser;

public class TCLParser implements IParser {
	public static String HEADER_SEPARATOR = "# ---";
	private final Stack<String> classStack = new Stack<>();
	private ScriptHeader header;
	private LinkedList<ICommandCall> commandCalls = new LinkedList<>();
	private Map<String, NOBClazz> clazzes = new Hashtable<>();

	@Override
	public void read(final InputStream in) throws IOException {
		this.read(new BufferedReader(new InputStreamReader(in)));
	}

	public void read(final BufferedReader br) throws IOException {
		String line = br.readLine();
		if (TCLParser.HEADER_SEPARATOR.equals(line)) {
			this.readHeader(br);
		}

		do {
			line = line.strip();
			// comment
			if (line.startsWith("#") || line.isEmpty()) {
				continue;
			}

			try (final Scanner scan = new Scanner(line)) {
				final ICommandCall call = this.readCommand(scan);
				this.commandCalls.add(call);
			}
		} while ((line = br.readLine()) != null);
	}

	private ICommandCall readCommand(final Scanner scan) throws IOException {
		final String cmdName = scan.next();
		if ("new".equals(cmdName)) {
			final String className = scan.next();
			final String varName = scan.next();
			final NOBClazz clazz = this.getClazzByName(className);
			this.classStack.push(className); // _new automatically cds into created object

			final NewCommandCall call = new NewCommandCall(this.getLastClazz(), clazz, varName);
			return call;
		} else if ("sel".equals(cmdName)) {
			final String path = scan.next();
			final SelCommandCall call = new SelCommandCall(this.getLastClazz(), path);
			if ("..".equals(path)) {
				this.classStack.pop();
			}
			return call;
		} else {
			if (!cmdName.startsWith(".")) {
				throw new IOException("Invalid command call");
			}

			final CmdPrototype cmd = this.recursiveGetMethod(this.getLastClazz(), cmdName.substring(1));

			final ClassCommandCall call = new ClassCommandCall();
			call.setClazz(this.getLastClazz());
			call.setPrototype(cmd);

			final int argCount = cmd.getInArgs().size();
			final List<Object> args = new ArrayList<>();
			for (int i = 0; i < argCount; i++) {
				final NOBType arg = cmd.getInArgs().get(i);
				switch (arg) {
				case INT:
					args.add(scan.nextInt());
					break;
				case FLOAT:
					args.add(scan.nextFloat());
					break;
				case STRING:
				case USTRING:
				case CODE:
					// ASCII between quotes, but doesn't contain quotes either
					final String rawStr = scan.findInLine("\"[\\p{ASCII}&&[^\"]]*\"");
					if (!rawStr.startsWith("\"") || !rawStr.endsWith("\"")) {
						throw new IOException("Missing String quotes");
					}
					args.add(rawStr.substring(1, rawStr.length() - 1));
					break;
				case BOOL:
					final boolean b;
					final String read = scan.next();
					if ("true".equals(read)) {
						b = true;
					} else if ("false".equals(read)) {
						b = false;
					} else {
						throw new IOException("Expected boolean");
					}
					args.add(b);
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

	private void readHeader(final BufferedReader br) throws IOException {
		final String headerLine = br.readLine();
		final Matcher match = NOBParser.PAT_HEADER.matcher(headerLine);
		if (!match.find()) {
			throw new IOException("Invalid header");
		}

		this.header = new ScriptHeader(match.group(1), match.group(2));

		if (!TCLParser.HEADER_SEPARATOR.equals(br.readLine())) {
			throw new IOException("Invalid header");
		}
	}

	private NOBClazz getLastClazz() {
		return this.getClazzByName(this.classStack.peek());
	}

	private NOBClazz getClazzByName(final String name) {
		for (final NOBClazz cls : this.clazzes.values()) {
			if (name.equals(cls.getName())) {
				return cls;
			}
		}
		return null;
	}

	public CmdPrototype recursiveGetMethod(final NOBClazz cls, final String name) {
		if (cls.containsMethodByName(name)) {
			return cls.getMethodByName(name);
		} else {
			if (cls.getSuperclass() != null) {
				final NOBClazz sc = this.clazzes.get(cls.getSuperclass());
				if (sc == null) {
					throw new IllegalStateException(cls.getName() + " has unknown superclass " + cls.getSuperclass());
				}
				return this.recursiveGetMethod(sc, name);
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
		return this.commandCalls;
	}

	@Override
	public void setClassModel(final Map<String, NOBClazz> classModel) {
		this.clazzes = classModel;
	}

	@Override
	public void setKeepUnknownCommands(final boolean keepUnknownCommands) {
		throw new UnsupportedOperationException(
				"TCLParser#setKeepUnknownCommands(keepUnknownCommands) is not implemented");
	}

	@Override
	public boolean isKeepUnknownCommands() {
		throw new UnsupportedOperationException("TCLParser#isKeepUnknownCommands() is not implemented");
	}

}
