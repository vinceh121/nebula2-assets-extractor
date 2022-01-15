package me.vinceh121.n2ae;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum VertexType {
	VOID(0), COORD(1 << 0), NORM(1 << 1), RGBA(1 << 2), UV0(1 << 3), UV1(1 << 4), UV2(1 << 5), UV3(1 << 6), JOINTS_WEIGHTS(1 << 7);

	private int mask;

	private VertexType(int mask) {
		this.mask = mask;
	}

	public int getMask() {
		return mask;
	}

	public static List<VertexType> getTypes(int types) {
		List<VertexType> t = new ArrayList<>(VertexType.values().length);
		for (VertexType v : VertexType.values()) {
			if ((types & v.getMask()) == v.getMask()) {
				t.add(v);
			}
		}
		return Collections.unmodifiableList(t);
	}
}
