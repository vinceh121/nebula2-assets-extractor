package me.vinceh121.n2ae.gltf;

public final class NotMinusOne {
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Number) {
			return ((Number) obj).intValue() == -1;
		}
		return false;
	}
}
