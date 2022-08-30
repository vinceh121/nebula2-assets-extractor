package me.vinceh121.n2ae.texture;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import me.vinceh121.n2ae.LEDataOutputStream;

public class NtxFileWriter {
	private final LEDataOutputStream out;

	public NtxFileWriter(OutputStream out) {
		this.out = new LEDataOutputStream(out);
	}

	public void writeHeader(int countBlocks) throws IOException {
		this.out.writeIntLE(NtxFileReader.MAGIC_NUMBER);
		this.out.writeIntLE(countBlocks);
	}

	public void writeBlock(Block b) throws IOException {
		this.out.writeIntLE(b.getFormat().ordinal());
		this.out.writeIntLE(b.getType().ordinal());
		this.out.writeIntLE(b.getWidth());
		this.out.writeIntLE(b.getHeight());
		this.out.writeIntLE(b.getDepth());
		this.out.writeIntLE(b.getMipmapLevel());
		this.out.writeIntLE(b.getDataOffset());
		this.out.writeIntLE(b.getDataLength());
	}

	public static byte[] imageToRaw(BufferedImage img, BlockFormat fmt) {
		int[] arr = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
		System.out.println(arr.length);
//		for (int x = 0; x < img.getWidth(); x++) {
//			for (int y = 0; y < img.getWidth(); y++) {
//				arr[x + y] = img.getRGB(x, y);
//			}
//		}
		return imageToRaw(arr, fmt);
	}

	public static byte[] imageToRaw(int[] argb, BlockFormat fmt) {
		if (fmt == BlockFormat.ARGB8) { // nothing, just convert to a byte array
			ByteBuffer bytes = ByteBuffer.allocate(4 * argb.length);
			for (int i : argb) {
				bytes.putInt(i);
			}
			return bytes.array();
		} else if (fmt == BlockFormat.RGB8) { // remove alpha
			ByteBuffer bytes = ByteBuffer.allocate(3 * argb.length);
			for (int i : argb) {
				final byte red = (byte) ((i >> 16) & 0xFF);
				final byte green = (byte) ((i >> 8) & 0xFF);
				final byte blue = (byte) (i & 0xFF);
				bytes.put(red);
				bytes.put(green);
				bytes.put(blue);
			}
			return bytes.array();
		} else if (fmt == BlockFormat.ARGB4) { // remove lower nibble of each component, LE
			ByteBuffer bytes = ByteBuffer.allocate(2 * argb.length);
			for (int i : argb) {
				final short alpha = (short) (i >> 24 + 4);
				final short red = (short) ((i >> 16 + 4) & 0xF);
				final short green = (short) ((i >> 8 + 4) & 0xF);
				final short blue = (short) ((i & 0xF) >> 4);

				final short pixel = (short) (alpha << 12 | red << 8 | green << 4 | blue);
				bytes.putShort(Short.reverseBytes(pixel));
			}
			return bytes.array();
		} else if (fmt == BlockFormat.RGB565) { // remove alpha and lower 3-2-3 bits, LE
			ByteBuffer bytes = ByteBuffer.allocate(2 * argb.length);
			for (int i : argb) {
				final short red = (short) ((i >> 16 + 3) & 0xF);
				final short green = (short) ((i >> 8 + 2) & 0xF);
				final short blue = (short) ((i & 0xF) >> 3);

				final short pixel = (short) (red << 11 | green << 5 | blue);
				bytes.putShort(Short.reverseBytes(pixel));
			}
			return bytes.array();
		} else {
			throw new UnsupportedOperationException("Cannot convert Java image to raw format " + fmt);
		}
	}
}
