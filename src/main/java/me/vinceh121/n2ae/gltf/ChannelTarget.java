package me.vinceh121.n2ae.gltf;

public class ChannelTarget {
	private int node;
	private TargetPath path;

	public int getNode() {
		return node;
	}

	public void setNode(int node) {
		this.node = node;
	}

	public TargetPath getPath() {
		return path;
	}

	public void setPath(TargetPath path) {
		this.path = path;
	}

	public enum TargetPath {
		// undercase on purpose
		translation, rotation, scale;
	}
}
