package me.vinceh121.n2ae.model;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public enum VertexType {
	VOID(0),
	COORD(1 << 0),
	NORM(1 << 1),
	RGBA(1 << 2),
	UV0(1 << 3),
	UV1(1 << 4),
	UV2(1 << 5),
	UV3(1 << 6),
	JOINTS_WEIGHTS(1 << 7);

	private int mask;

	VertexType(final int mask) {
		this.mask = mask;
	}

	public int getMask() {
		return this.mask;
	}

	public static Set<VertexType> getTypes(final int types) {
		final List<VertexType> t = new ArrayList<>(VertexType.values().length);

		for (final VertexType v : VertexType.values()) {
			if ((types & v.getMask()) == v.getMask()) {
				t.add(v);
			}
		}

		return EnumSet.copyOf(t);
	}

	public static int getTypes(final Set<VertexType> types) {
		int mask = 0;

		for (final VertexType t : types) {
			mask |= t.getMask();
		}

		return mask;
	}
}
