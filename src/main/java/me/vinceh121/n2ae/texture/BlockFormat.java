package me.vinceh121.n2ae.texture;

import static me.vinceh121.n2ae.texture.GLConstants.*;

import java.awt.image.BufferedImage;

public enum BlockFormat {
	NONE(0, 0, 0, 0),
	RGB8(BufferedImage.TYPE_INT_RGB, GL_UNSIGNED_INT, GL_RGB, GL_RGB8UI),
	/**
	 * OpenGL: swap alpha
	 */
	ARGB8(BufferedImage.TYPE_INT_ARGB, GL_UNSIGNED_INT_8_8_8_8, GL_RGBA, GL_RGBA8UI),
	RGB565(BufferedImage.TYPE_USHORT_565_RGB, GL_UNSIGNED_SHORT_5_6_5, GL_RGB, GL_RGB565),
	/**
	 * JavaImage: 16-bit ARGB with 4 bits per channel, need to convert to ARGB8
	 * OpenGL: swap alpha
	 */
	ARGB4(BufferedImage.TYPE_INT_ARGB, GL_UNSIGNED_SHORT_4_4_4_4, GL_RGBA, GL_RGBA4);

	private final int javaFormat, glType, glFormat, glInternalFormat;

	BlockFormat(final int javaFormat, final int glType, final int glFormat, final int glInternalFormat) {
		this.javaFormat = javaFormat;
		this.glType = glType;
		this.glFormat = glFormat;
		this.glInternalFormat = glInternalFormat;
	}

	public int getJavaFormat() {
		return this.javaFormat;
	}

	public int getGlType() {
		return this.glType;
	}

	public int getGlFormat() {
		return this.glFormat;
	}

	public int getGlInternalFormat() {
		return this.glInternalFormat;
	}
}
