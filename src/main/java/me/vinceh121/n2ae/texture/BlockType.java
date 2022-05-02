package me.vinceh121.n2ae.texture;

public enum BlockType {
	NONE, TEXTURE_1D, TEXTURE_2D, TEXTURE_3D, TEXTURE_CUBE(8);

	private final int numberOfFaces;

	BlockType() {
		this(1);
	}

	BlockType(final int numberOfFaces) {
		this.numberOfFaces = numberOfFaces;
	}

	public int getNumberOfFaces() {
		return this.numberOfFaces;
	}
}
