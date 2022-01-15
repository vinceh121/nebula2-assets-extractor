package me.vinceh121.n2ae.texture;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Vector;

import javax.imageio.ImageIO;

import me.vinceh121.n2ae.LEDataInputStream;

public class NtvFileReader {
	public static final String MAGIC_STRING = "NTX1";
	public static final int MAGIC_NUMBER = 1314150449; // ByteBuffer.wrap("NTX1".getBytes("US-ASCII")).getInt();

	private final LEDataInputStream in;

	private int countBlocks;
	private Vector<Block> blocks = new Vector<>();
	private int headerSize;
	private Vector<BufferedImage> textures = new Vector<>();

	public static void main(String[] args) throws IOException {
		try (FileInputStream in = new FileInputStream(args[0])) {
			NtvFileReader r = new NtvFileReader(in);
			r.readHeader();
			r.readAllTextures();
			for (int i = 0; i < r.getCountBlocks(); i++) {
				BufferedImage img = r.getTextures().get(i);
				System.out.println(r.getBlocks().get(i));
				System.out.println(img);
				System.out.println();
				ImageIO.write(img, "png", new File("/tmp/" + i + ".png"));
			}
		}
	}

	public NtvFileReader(InputStream in) {
		this.in = new LEDataInputStream(in);
	}

	public void readHeader() throws IOException {
		int magic = in.readIntLE();
		if (magic != MAGIC_NUMBER) {
			throw new IOException("Invalid magic number");
		}

		this.countBlocks = this.in.readIntLE();
		for (int i = 0; i < this.countBlocks; i++) {
			Block b = new Block();
			b.setFormat(BlockFormat.values()[in.readIntLE()]);
			b.setType(BlockType.values()[in.readIntLE()]);
			b.setWidth(in.readIntLE());
			b.setHeight(in.readIntLE());
			b.setDepth(in.readIntLE());
			b.setMipmapLevel(in.readIntLE());
			b.setDataOffset(in.readIntLE());
			b.setDataLength(in.readIntLE());
			this.blocks.add(b);
		}
		this.headerSize = /* magic number */4 + /* 8 ints in each block */4 * 8 * this.countBlocks;
	}

	public void readAllTextures() throws IOException {
		for (Block block : this.blocks) {
			int width = block.getWidth();
			int height = block.getHeight();
			byte[] buf = this.in.readNBytes(block.getDataLength());
			BufferedImage img = new BufferedImage(width, height, block.getFormat().getNativeFormat());

			if (block.getFormat() == BlockFormat.RGB565) {
				ShortBuffer sbuf = ByteBuffer.wrap(buf).asShortBuffer();
				short[] sarr = new short[sbuf.remaining()];
				sbuf.get(sarr);
				for (int i = 0; i < sarr.length; i++) {
					img.getRaster().getDataBuffer().setElem(i, Short.reverseBytes(sarr[i]));
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
		return countBlocks;
	}

	public Vector<Block> getBlocks() {
		return blocks;
	}

	public int getHeaderSize() {
		return headerSize;
	}

	public Vector<BufferedImage> getTextures() {
		return textures;
	}
}
