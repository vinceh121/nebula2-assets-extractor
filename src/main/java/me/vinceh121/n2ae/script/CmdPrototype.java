package me.vinceh121.n2ae.script;

import java.util.List;
import java.util.Vector;

public class CmdPrototype {
	private final List<NOBType> inArgs = new Vector<>(), returnArgs = new Vector<>();
	private String name;

	public CmdPrototype() {
	}

	public CmdPrototype(String proto) {
		char c;
		int idx = 0;
		while (idx < proto.length() && (c = proto.charAt(idx++)) != '_') {
			this.returnArgs.add(NOBType.getType(c));
		}

		StringBuilder sb = new StringBuilder();
		while ((c = proto.charAt(idx++)) != '_') {
			sb.append(c);
		}
		this.name = sb.toString();

		while (idx < proto.length() && (c = proto.charAt(idx++)) != '_') {
			this.inArgs.add(NOBType.getType(c));
		}
	}

	public List<NOBType> getInArgs() {
		return inArgs;
	}

	public List<NOBType> getReturnArgs() {
		return returnArgs;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (NOBType rt : this.returnArgs) {
			sb.append(rt.getType());
		}
		sb.append('_');
		sb.append(this.name);
		sb.append('_');
		for (NOBType it : this.inArgs) {
			sb.append(it.getType());
		}
		return sb.toString();
	}
}
