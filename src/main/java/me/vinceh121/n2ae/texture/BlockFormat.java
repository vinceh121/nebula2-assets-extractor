package me.vinceh121.n2ae.texture;

import java.awt.image.BufferedImage;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL41;

public enum BlockFormat {
	NONE(0, 0, 0, 0), RGB8(BufferedImage.TYPE_INT_RGB, GL20.GL_UNSIGNED_INT, GL20.GL_RGB, GL30.GL_RGB8UI),
	/**
	 * OpenGL: swap alpha
	 */
	ARGB8(BufferedImage.TYPE_INT_ARGB, GL20.GL_UNSIGNED_INT_8_8_8_8, GL20.GL_RGBA, GL30.GL_RGBA8UI),
	RGB565(BufferedImage.TYPE_USHORT_565_RGB, GL20.GL_UNSIGNED_SHORT_5_6_5, GL20.GL_RGB, GL41.GL_RGB565),
	/**
	 * JavaImage: 16-bit ARGB with 4 bits per channel, need to convert to ARGB8
	 * OpenGL: swap alpha
	 */
	ARGB4(BufferedImage.TYPE_INT_ARGB, GL20.GL_UNSIGNED_SHORT_4_4_4_4, GL20.GL_RGBA, GL11.GL_RGBA4);

	private final int javaFormat, glType, glFormat, glInternalFormat;

	private BlockFormat(int javaFormat, int glType, int glFormat, int glInternalFormat) {
		this.javaFormat = javaFormat;
		this.glType = glType;
		this.glFormat = glFormat;
		this.glInternalFormat = glInternalFormat;
	}

	public int getJavaFormat() {
		return javaFormat;
	}

	public int getGlType() {
		return glType;
	}

	public int getGlFormat() {
		return glFormat;
	}

	public int getGlInternalFormat() {
		return glInternalFormat;
	}
}
