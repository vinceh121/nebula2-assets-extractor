package me.vinceh121.n2ae.gltf;

public class ChannelTarget {
	private int node;
	private TargetPath path;

	public int getNode() {
		return this.node;
	}

	public void setNode(final int node) {
		this.node = node;
	}

	public TargetPath getPath() {
		return this.path;
	}

	public void setPath(final TargetPath path) {
		this.path = path;
	}

	public enum TargetPath {
		// undercase on purpose
		translation, rotation, scale;
	}
}
