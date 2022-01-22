package me.vinceh121.n2ae.script;

public enum NOBType {
	VOID('v'), INT('i'), FLOAT('f'), STRING('s'), BOOL('b'), OBJECT('o'), LIST('l'), FLOAT4, MATRIX44;

	private char type;

	private NOBType() {
		this('\0');
	}

	private NOBType(char type) {
		this.type = type;
	}

	public char getType() {
		return type;
	}

	public static NOBType getType(char c) {
		for (NOBType t : NOBType.values()) {
			if (t.getType() == c) {
				return t;
			}
		}
		return null;
	}
}
