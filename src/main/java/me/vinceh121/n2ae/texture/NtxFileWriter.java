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

	public static byte[] imageToRaw(BufferedImage img, int width, int height, BlockFormat fmt) {
		int[] arr = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
		return imageToRaw(arr, width, height, fmt);
	}

	public static byte[] imageToRaw(int[] argb, int width, int height, BlockFormat fmt) {
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
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					int idx = x * width + y;
					int i = argb[idx];
					// original values
					final short oalpha = (short) (i >> 24);
					final short ored = (short) ((i >> 16) & 0xFF);
					final short ogreen = (short) ((i >> 8) & 0xFF);
					final short oblue = (short) (i & 0xFF);

					// error values
//					final short ealpha = (short) (oalpha & 0xF);
//					final short ered = (short) (ored & 0xF);
//					final short egreen = (short) (ogreen & 0xF);
//					final short eblue = (short) (oblue & 0xF);

					// reduced values
					final short salpha = (short) (oalpha >> 4);
					final short sred = (short) (ored >> 4);
					final short sgreen = (short) (ogreen >> 4);
					final short sblue = (short) (oblue >> 4);

					final short pixel = (short) (salpha << 12 | sred << 8 | sgreen << 4 | sblue);
					bytes.putShort(idx * 2, Short.reverseBytes(pixel));
				}
			}
			return bytes.array();
		} else if (fmt == BlockFormat.RGB565) { // remove alpha and lower 3-2-3 bits, LE
			ByteBuffer sbuf = ByteBuffer.allocate(2 * argb.length);
			for (int i = 0; i < argb.length; i++) {
				final int p = argb[i];
				// original RGB values
				final short ored = (short) ((p >> 16) & 0xFF);
				final short ogreen = (short) ((p >> 8) & 0xFF);
				final short oblue = (short) (p & 0xFF);

				// error values
//				final short ered = (short) (ored & 7);
//				final short egreen = (short) (ogreen & 3);
//				final short eblue = (short) (oblue & 7);

				// shortened values
				short sred = (short) (ored >> 3);
				short sgreen = (short) (ogreen >> 2);
				short sblue = (short) (oblue >> 3);

				final short pixel = (short) (sred << 11 | sgreen << 5 | sblue);
				sbuf.putShort(Short.reverseBytes(pixel));
			}
			return sbuf.array();
		} else {
			throw new UnsupportedOperationException("Cannot convert Java image to raw format " + fmt);
		}
	}
}
