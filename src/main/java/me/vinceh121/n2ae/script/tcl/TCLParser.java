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
import java.util.NoSuchElementException;
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
import me.vinceh121.n2ae.script.ParseException;
import me.vinceh121.n2ae.script.ScriptHeader;
import me.vinceh121.n2ae.script.SelCommandCall;
import me.vinceh121.n2ae.script.nob.NOBParser;

public class TCLParser implements IParser {
	public static final String HEADER_SEPARATOR = "# ---";
	private final Stack<String> classStack = new Stack<>();
	private ScriptHeader header;
	private LinkedList<ICommandCall> commandCalls = new LinkedList<>();
	private Map<String, NOBClazz> clazzes = new Hashtable<>();

	@Override
	public void read(final InputStream in) throws IOException, ParseException {
		this.read(new BufferedReader(new InputStreamReader(in)));
	}

	public void read(final BufferedReader br) throws IOException, ParseException {
		String line = br.readLine();
		int lineNo = 0;
		if (TCLParser.HEADER_SEPARATOR.equals(line)) {
			this.readHeader(br);
			lineNo += 3;
		}

		do {
			line = line.strip();
			// comment
			if (line.startsWith("#") || line.isEmpty()) {
				continue;
			}

			try (final Scanner scan = new Scanner(line)) {
				final ICommandCall call = this.readCommand(scan, lineNo);
				this.commandCalls.add(call);
			}
			lineNo++;
		} while ((line = br.readLine()) != null);
	}

	private ICommandCall readCommand(final Scanner scan, final int lineNo) throws IOException, ParseException {
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
				throw new ParseException("Invalid command call", 0, lineNo, 0);
			}

			final CmdPrototype cmd = this.recursiveGetMethod(this.getLastClazz(), cmdName.substring(1), lineNo);

			if (cmd == null) {
				throw new ParseException("Unknown command " + cmdName.substring(1), 0, lineNo, 0);
			}

			final ClassCommandCall call = new ClassCommandCall();
			call.setClazz(this.getLastClazz());
			call.setPrototype(cmd);

			final int argCount = cmd.getInArgs().size();
			final List<Object> args = new ArrayList<>();
			for (int i = 0; i < argCount; i++) {
				final NOBType arg = cmd.getInArgs().get(i);
				switch (arg) {
				case INT:
					try {
						args.add(scan.nextInt());
						break;
					} catch (final NoSuchElementException e) {
						throw new ParseException("Expected int in argument " + i, 0, lineNo, 0);
					}
				case FLOAT:
					try {
						args.add(scan.nextFloat());
						break;
					} catch (final NoSuchElementException e) {
						throw new ParseException("Expected float in argument " + i, 0, lineNo, 0);
					}
				case STRING:
				case USTRING:
				case CODE:
					// ASCII between quotes, but doesn't contain quotes either
					final String rawStr = scan.findInLine("\"[\\p{ASCII}&&[^\"]]*\"");
					if (rawStr == null || !rawStr.startsWith("\"") || !rawStr.endsWith("\"")) {
						throw new ParseException("Missing String quotes", 0, lineNo, 0);
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
						throw new ParseException("Expected boolean in argument " + i, 0, lineNo, 0);
					}
					args.add(b);
					break;
				case VOID:
					break;
				default:
					throw new ParseException("Unknown arg type" + arg, 0, lineNo, 0);
				}
			}

			if (scan.hasNext()) {
				throw new ParseException("Too many arguments, expected " + cmd.getInArgs().size(), 0, lineNo, 0);
			}

			call.setArguments(args.toArray());
			return call;
		}
	}

	private void readHeader(final BufferedReader br) throws IOException, ParseException {
		final String headerLine = br.readLine();
		final Matcher match = NOBParser.PAT_HEADER.matcher(headerLine);
		if (!match.find()) {
			throw new ParseException("Invalid header", 0, 1, 0);
		}

		this.header = new ScriptHeader(match.group(1), match.group(2));

		if (!TCLParser.HEADER_SEPARATOR.equals(br.readLine())) {
			throw new ParseException("Invalid header", 0, 2, 0);
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

	public CmdPrototype recursiveGetMethod(final NOBClazz cls, final String name, final int lineNo)
			throws ParseException {
		if (cls.containsMethodByName(name)) {
			return cls.getMethodByName(name);
		} else {
			if (cls.getSuperclass() != null) {
				final NOBClazz sc = this.clazzes.get(cls.getSuperclass());
				if (sc == null) {
					throw new ParseException(cls.getName() + " has unknown superclass " + cls.getSuperclass(),
							0,
							lineNo,
							0);
				}
				return this.recursiveGetMethod(sc, name, lineNo);
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
