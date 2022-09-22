package me.vinceh121.n2ae.script;

public enum NOBType {
	VOID('v'),
	INT('i'),
	FLOAT('f'),
	STRING('s'),
	USTRING('u'),
	BOOL('b'),
	OBJECT('o'),
	CODE('c'),
	LIST('l'),
	FLOAT4,
	MATRIX44;

	private char type;

	NOBType() {
		this('\0');
	}

	NOBType(final char type) {
		this.type = type;
	}

	public char getType() {
		return this.type;
	}

	public static NOBType getType(final char c) {
		for (final NOBType t : NOBType.values()) {
			if (t.getType() == c) {
				return t;
			}
		}
		throw new IllegalArgumentException("Don't know type '" + c + "'");
	}
}
