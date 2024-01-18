package me.vinceh121.n2ae.texture;

import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Vector;

import me.vinceh121.n2ae.FourccUtils;
import me.vinceh121.n2ae.LEDataInputStream;

public class NtxFileReader {
	public static final String MAGIC_STRING = "NTX1";
	public static final int MAGIC_NUMBER = FourccUtils.fourcc(NtxFileReader.MAGIC_STRING);
	public static final byte[] KTX_MAGIC =
			{ (byte) 0xAB, 0x4B, 0x54, 0x58, 0x20, 0x31, 0x31, (byte) 0xBB, 0x0D, 0x0A, 0x1A, 0x0A };

	private final LEDataInputStream in;

	private int countBlocks;
	private final Vector<Block> blocks = new Vector<>();
	private int headerSize;
	private final Vector<BufferedImage> textures = new Vector<>();
	private final Vector<byte[]> raws = new Vector<>();

	public NtxFileReader(final InputStream in) {
		this.in = new LEDataInputStream(in);
	}

	public void readHeader() throws IOException {
		final int magic = this.in.readIntLE();
		if (magic != NtxFileReader.MAGIC_NUMBER) {
			throw new IOException("Invalid magic number");
		}

		this.countBlocks = this.in.readIntLE();
		for (int i = 0; i < this.countBlocks; i++) {
			final Block b = new Block();
			b.setFormat(BlockFormat.values()[this.in.readIntLE()]);
			b.setType(BlockType.values()[this.in.readIntLE()]);
			b.setWidth(this.in.readIntLE());
			b.setHeight(this.in.readIntLE());
			b.setDepth(this.in.readIntLE());
			b.setMipmapLevel(this.in.readIntLE());
			b.setDataOffset(this.in.readIntLE());
			b.setDataLength(this.in.readIntLE());
			this.blocks.add(b);
		}
		this.headerSize = /* magic number */4 + /* 8 ints in each block */4 * 8 * this.countBlocks;
	}

	public void readAllRaws() throws IOException {
		for (final Block block : this.blocks) {
			final byte[] buf = this.in.readNBytes(block.getDataLength());
			this.raws.add(buf);
		}
	}

	/**
	 * Call {@link #readHeader()} and {@link #readAllRaws()} beforehand
	 *
	 * @throws IOException
	 */
	public void writeKtx(final DataOutputStream out) throws IOException {
		final Block level0 = this.blocks.get(0);
		final BlockFormat format = level0.getFormat(); // assume all blocks have same type/format
		final BlockType type = level0.getType();

		out.write(NtxFileReader.KTX_MAGIC); // identifier
		out.writeInt(0x04030201); // endiannes
		out.writeInt(format.getGlType()); // glType
		out.writeInt(1); // glTypeSize XXX should be glType size
		out.writeInt(format.getGlFormat()); // glFormat
		out.writeInt(format.getGlInternalFormat()); // glInternalFormat
		out.writeInt(format.getGlFormat()); // glBaseInternalFormat
		out.writeInt(level0.getWidth()); // pixelWidth
		if (type == BlockType.TEXTURE_1D) {
			out.writeInt(0); // pixelHeight
			out.writeInt(0); // pixelDepth
		} else if (type == BlockType.TEXTURE_2D) {
			out.writeInt(level0.getHeight()); // pixelHeight
			out.writeInt(0); // pixelDepth
		} else {
			out.writeInt(level0.getHeight()); // pixelHeight
			out.writeInt(level0.getDepth()); // pixelDepth
		}
		out.writeInt(0); // numberOfArrayElements
		out.writeInt(type.getNumberOfFaces()); // numberOfFaces
		out.writeInt(this.blocks.size()); // numberOfMipmapLevels
		out.writeInt(0); // bytesOfKeyValueData

		for (int i = 0; i < this.blocks.size(); i++) {

			byte[] raw;
			if (format == BlockFormat.ARGB4) { // move alpha to last nibble
				final byte[] origRaw = this.raws.get(i);
				raw = new byte[origRaw.length];
				System.arraycopy(origRaw, 0, raw, 0, origRaw.length);

				for (int j = 0; j < raw.length; j += 2) {
					final byte alpha = (byte) (raw[j] >> 4 & 0xF);
					final byte red = (byte) (raw[j] & 0xF);
					final byte green = (byte) (raw[j + 1] >> 4 & 0xF);
					final byte blue = (byte) (raw[j + 1] & 0xF);

					raw[j] = (byte) (red << 4 | green);
					raw[j + 1] = (byte) (blue << 4 | alpha);

				}
			} else if (format == BlockFormat.ARGB8) { // move alpha to last byte
				final byte[] origRaw = this.raws.get(i);
				raw = new byte[origRaw.length];
				System.arraycopy(origRaw, 0, raw, 0, origRaw.length);

				for (int j = 0; j < raw.length; j += 4) {
					final byte alpha = raw[j];
					final byte red = raw[j + 1];
					final byte green = raw[j + 2];
					final byte blue = raw[j + 3];

					raw[j] = red;
					raw[j + 1] = green;
					raw[j + 2] = blue;
					raw[j + 3] = alpha;
				}
			} else { // use raw directly
				raw = this.raws.get(i);
			}

			out.writeInt(raw.length); // imageSize
			// for each numberOfArrayElements
			// for each numberOfFaces
			// for each pixelDepth
			out.write(raw);

			// KTX textures need to be aligned to 4 bytes
			final int alignmentPadding = ((raw.length + 3) & ~3) - raw.length;
			out.write(new byte[alignmentPadding]);
		}
	}

	public void readAllTextures() throws IOException {
		for (final Block block : this.blocks) {
			final int width = block.getWidth();
			final int height = block.getHeight();
			final byte[] buf = this.in.readNBytes(block.getDataLength());
			final BufferedImage img = new BufferedImage(width, height, block.getFormat().getJavaFormat());

			if (block.getFormat() == BlockFormat.RGB565) {
				final ShortBuffer sbuf = ByteBuffer.wrap(buf).asShortBuffer();
				final short[] sarr = new short[sbuf.remaining()];
				sbuf.get(sarr);
				for (int i = 0; i < sarr.length; i++) {
					img.getRaster().getDataBuffer().setElem(i, Short.reverseBytes(sarr[i]));
				}
			} else if (block.getFormat() == BlockFormat.ARGB4) {
				for (int i = 0; i < buf.length; i += 2) {
					// this one is little endian for some reason
					final int pixel4 = Short.reverseBytes((short) ((buf[i] & 0xFF) << 8 | buf[i + 1] & 0xFF));
					int alpha = pixel4 >> 12 & 0xF;
					int red = pixel4 >> 8 & 0xF;
					int green = pixel4 >> 4 & 0xF;
					int blue = pixel4 & 0xF;

					alpha <<= 4;
					red <<= 4;
					green <<= 4;
					blue <<= 4;

					final int pixel = alpha << 24 | red << 16 | green << 8 | blue;
					img.getRaster().getDataBuffer().setElem(i / 2, pixel);
				}
			} else if (block.getFormat() == BlockFormat.ARGB8) { // BufferedImage will want pixels packed as ints, not
																	// bytes
				final IntBuffer ibuf = ByteBuffer.wrap(buf).asIntBuffer();
				for (int i = 0; i < ibuf.capacity(); i++) {
					img.getRaster().getDataBuffer().setElem(i, ibuf.get(i));
				}
			} else {
				for (int i = 0; i < buf.length; i++) {
					img.getRaster().getDataBuffer().setElem(i, buf[i]);
				}
			}

			this.textures.add(img);
		}
	}

	public int getCountBlocks() {
		return this.countBlocks;
	}

	public Vector<Block> getBlocks() {
		return this.blocks;
	}

	public int getHeaderSize() {
		return this.headerSize;
	}

	public Vector<BufferedImage> getTextures() {
		return this.textures;
	}

	public Vector<byte[]> getRaws() {
		return this.raws;
	}
}
