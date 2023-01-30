package me.vinceh121.n2ae.script;

public class ParseException extends Exception {
	private static final long serialVersionUID = 1L;
	private final int offset, line, column;

	public ParseException(int offset, int line, int column) {
		super();
		this.offset = offset;
		this.line = line;
		this.column = column;
	}

	public ParseException(String message, Throwable cause, int offset, int line, int column) {
		super(message, cause);
		this.offset = offset;
		this.line = line;
		this.column = column;
	}

	public ParseException(String message, int offset, int line, int column) {
		super(message);
		this.offset = offset;
		this.line = line;
		this.column = column;
	}

	public ParseException(Throwable cause, int offset, int line, int column) {
		super(cause);
		this.offset = offset;
		this.line = line;
		this.column = column;
	}

	public int getOffset() {
		return offset;
	}

	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}

	@Override
	public String toString() {
		return super.toString() + " @ offset: " + this.offset + ", line: " + this.line + ", column" + this.column;
	}
}
