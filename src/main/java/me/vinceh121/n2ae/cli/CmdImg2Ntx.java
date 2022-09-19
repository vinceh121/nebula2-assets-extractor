package me.vinceh121.n2ae.cli;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;

import me.vinceh121.n2ae.texture.Block;
import me.vinceh121.n2ae.texture.BlockFormat;
import me.vinceh121.n2ae.texture.BlockType;
import me.vinceh121.n2ae.texture.NtxFileWriter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "img2ntx", description = { "Convert an image to an NTX1 texture" })
public class CmdImg2Ntx implements Callable<Integer> {
	@Option(names = { "-o", "--output" })
	private File outputFile;

	@Option(names = { "-i", "--input" }, required = true)
	private File inputFile;

	@Option(names = { "-f", "--format" }, defaultValue = "ARGB4")
	private BlockFormat format;

	@Option(names = { "-t", "--type" }, defaultValue = "TEXTURE_2D")
	private BlockType type;

	@Option(names = { "-m", "--mipmap" }, defaultValue = "1")
	private int mipmaps;

	@Override
	public Integer call() throws Exception {
		if (this.outputFile == null) {
			this.outputFile = new File("./" + (this.inputFile.getName().contains(".")
					? this.inputFile.getName().substring(0, this.inputFile.getName().indexOf("."))
					: this.inputFile.getName()) + ".ntx");
		}
		try (FileOutputStream out = new FileOutputStream(this.outputFile)) {
			BufferedImage img = ImageIO.read(this.inputFile);
			final BlockFormat fmt = img.getAlphaRaster() == null ? BlockFormat.ARGB4 : BlockFormat.ARGB4;
			byte[] data = NtxFileWriter.imageToRaw(img, img.getWidth(), img.getHeight(), fmt);
			final ByteArrayOutputStream mipmaps = new ByteArrayOutputStream(img.getHeight() * img.getWidth() * 2);
			mipmaps.write(data);

			int offset = 328; // 10 block headers of 8 ints

			Block b = new Block();
			b.setWidth(img.getWidth());
			b.setHeight(img.getHeight());
			b.setDataOffset(offset);
			b.setDataLength(data.length);
			b.setFormat(fmt);
			b.setMipmapLevel(0);
			b.setDepth(1);
			b.setType(BlockType.TEXTURE_2D);

			offset += data.length;

			final NtxFileWriter writer = new NtxFileWriter(out);
			writer.writeHeader(this.mipmaps);
			writer.writeBlock(b);

			for (int i = 1; i < this.mipmaps; i++) {
				if (img.getWidth() != 1 && img.getHeight() != 1) {
					final AffineTransform ts = AffineTransform.getScaleInstance(0.5, 0.5);
					final AffineTransformOp op = new AffineTransformOp(ts, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
					img = op.filter(img, null);
				}
				data = NtxFileWriter.imageToRaw(img, img.getWidth(), img.getHeight(), fmt);
				mipmaps.write(data);

				b = new Block();
				b.setWidth(img.getWidth());
				b.setHeight(img.getHeight());
				b.setDataOffset(offset);
				b.setDataLength(data.length);
				b.setFormat(fmt);
				b.setMipmapLevel(i);
				b.setDepth(1);
				b.setType(BlockType.TEXTURE_2D);
				writer.writeBlock(b);

				offset += data.length;
			}
			out.write(mipmaps.toByteArray());
			out.flush();
		}
		return 0;
	}

}
