package me.vinceh121.n2ae.texture;

import java.awt.image.BufferedImage;

public enum BlockFormat {
	NONE(0), RGB8(BufferedImage.TYPE_INT_RGB), ARGB8(BufferedImage.TYPE_INT_ARGB),
	RGB565(BufferedImage.TYPE_USHORT_565_RGB), ARGB4(BufferedImage.TYPE_4BYTE_ABGR);

	private int nativeFormat;

	private BlockFormat(int nativeFormat) {
		this.nativeFormat = nativeFormat;
	}

	public int getNativeFormat() {
		return nativeFormat;
	}
}
