package me.vinceh121.n2ae.texture;

public class Block {
	private BlockFormat format;
	private BlockType type;
	private int width, height, depth, mipmapLevel, dataOffset, dataLength;

	public BlockFormat getFormat() {
		return this.format;
	}

	public void setFormat(final BlockFormat format) {
		this.format = format;
	}

	public BlockType getType() {
		return this.type;
	}

	public void setType(final BlockType type) {
		this.type = type;
	}

	public int getWidth() {
		return this.width;
	}

	public void setWidth(final int width) {
		this.width = width;
	}

	public int getHeight() {
		return this.height;
	}

	public void setHeight(final int height) {
		this.height = height;
	}

	public int getDepth() {
		return this.depth;
	}

	public void setDepth(final int depth) {
		this.depth = depth;
	}

	public int getMipmapLevel() {
		return this.mipmapLevel;
	}

	public void setMipmapLevel(final int mipmapLevel) {
		this.mipmapLevel = mipmapLevel;
	}

	public int getDataOffset() {
		return this.dataOffset;
	}

	public void setDataOffset(final int dataOffset) {
		this.dataOffset = dataOffset;
	}

	public int getDataLength() {
		return this.dataLength;
	}

	public void setDataLength(final int dataLength) {
		this.dataLength = dataLength;
	}

	@Override
	public String toString() {
		return "Block [format=" + this.format + ", type=" + this.type + ", width=" + this.width + ", height="
				+ this.height + ", depth=" + this.depth + ", mipmapLevel=" + this.mipmapLevel + ", dataOffset="
				+ this.dataOffset + ", dataLength=" + this.dataLength + "]";
	}
}
