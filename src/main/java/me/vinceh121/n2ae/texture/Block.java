package me.vinceh121.n2ae.texture;

public class Block {
	private BlockFormat format;
	private BlockType type;
	private int width, height, depth, mipmapLevel, dataOffset, dataLength;

	public BlockFormat getFormat() {
		return format;
	}

	public void setFormat(BlockFormat format) {
		this.format = format;
	}

	public BlockType getType() {
		return type;
	}

	public void setType(BlockType type) {
		this.type = type;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public int getMipmapLevel() {
		return mipmapLevel;
	}

	public void setMipmapLevel(int mipmapLevel) {
		this.mipmapLevel = mipmapLevel;
	}

	public int getDataOffset() {
		return dataOffset;
	}

	public void setDataOffset(int dataOffset) {
		this.dataOffset = dataOffset;
	}

	public int getDataLength() {
		return dataLength;
	}

	public void setDataLength(int dataLength) {
		this.dataLength = dataLength;
	}

	@Override
	public String toString() {
		return "Block [format=" + format + ", type=" + type + ", width=" + width + ", height=" + height + ", depth="
				+ depth + ", mipmapLevel=" + mipmapLevel + ", dataOffset=" + dataOffset + ", dataLength=" + dataLength
				+ "]";
	}
}
