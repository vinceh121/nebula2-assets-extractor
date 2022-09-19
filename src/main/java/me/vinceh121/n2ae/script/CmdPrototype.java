package me.vinceh121.n2ae.script;

import java.util.List;
import java.util.Vector;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CmdPrototype {
	private final List<NOBType> inArgs = new Vector<>(), returnArgs = new Vector<>();
	private String name, fourcc;

	public CmdPrototype() {
	}

	public CmdPrototype(final String proto) {
		char c;
		int idx = 0;
		while (idx < proto.length() && (c = proto.charAt(idx++)) != '_') {
			this.returnArgs.add(NOBType.getType(c));
		}

		final StringBuilder sb = new StringBuilder();
		while ((c = proto.charAt(idx++)) != '_') {
			sb.append(c);
		}
		this.name = sb.toString();

		while (idx < proto.length() && (c = proto.charAt(idx++)) != '_') {
			this.inArgs.add(NOBType.getType(c));
		}
	}

	public List<NOBType> getInArgs() {
		return this.inArgs;
	}

	public List<NOBType> getReturnArgs() {
		return this.returnArgs;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}
	@JsonIgnore
	public String getFourcc() {
		return fourcc;
	}
@JsonIgnore
	public void setFourcc(String fourcc) {
		this.fourcc = fourcc;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for (final NOBType rt : this.returnArgs) {
			sb.append(rt.getType());
		}
		sb.append('_');
		sb.append(this.name);
		sb.append('_');
		for (final NOBType it : this.inArgs) {
			sb.append(it.getType());
		}
		return sb.toString();
	}
}
