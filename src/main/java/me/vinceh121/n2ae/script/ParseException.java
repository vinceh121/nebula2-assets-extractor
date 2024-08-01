package me.vinceh121.n2ae.script;

public class ParseException extends Exception {
	private static final long serialVersionUID = 1L;
	private final int offset, line, column;

	public ParseException(final int offset, final int line, final int column) {
		this.offset = offset;
		this.line = line;
		this.column = column;
	}

	public ParseException(final String message, final Throwable cause, final int offset, final int line, final int column) {
		super(message, cause);
		this.offset = offset;
		this.line = line;
		this.column = column;
	}

	public ParseException(final String message, final int offset, final int line, final int column) {
		super(message);
		this.offset = offset;
		this.line = line;
		this.column = column;
	}

	public ParseException(final Throwable cause, final int offset, final int line, final int column) {
		super(cause);
		this.offset = offset;
		this.line = line;
		this.column = column;
	}

	public int getOffset() {
		return this.offset;
	}

	public int getLine() {
		return this.line;
	}

	public int getColumn() {
		return this.column;
	}

	@Override
	public String toString() {
		return super.toString() + " @ offset: " + this.offset + ", line: " + this.line + ", column" + this.column;
	}
}
