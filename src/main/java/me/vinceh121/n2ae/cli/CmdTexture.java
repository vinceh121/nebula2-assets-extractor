package me.vinceh121.n2ae.cli;

import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;

import me.vinceh121.n2ae.texture.Block;
import me.vinceh121.n2ae.texture.NtxFileReader;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "texture", description = { "Convert an NTX file to an image" })
public class CmdTexture implements Callable<Integer> {
	@Option(names = { "-o", "--output" })
	private File outputFile;

	@Option(names = { "-i", "--input" }, required = true)
	private File inputFile;

	@Option(names = { "-l", "--list-blocks" }, description = { "list blocks in the texture file" })
	private boolean listBlocks;

	@Option(names = { "-b", "--block" }, description = { "select block to extract",
			"defaults to higest resolution" }, defaultValue = "-1")
	private int block;

	@Option(names = { "-f", "--format" }, description = { "output image format" }, defaultValue = "png")
	private String format;

	@Override
	public Integer call() throws Exception {
		if (this.outputFile == null) {
			this.outputFile = new File("./" + (this.inputFile.getName().endsWith(".nvx")
					? this.inputFile.getName().substring(0, this.inputFile.getName().length() - 4)
					: this.inputFile.getName()) + "." + this.format);
		}

		try (FileInputStream is = new FileInputStream(this.inputFile);
				FileOutputStream os = new FileOutputStream(this.outputFile)) {
			final NtxFileReader reader = new NtxFileReader(is);
			reader.readHeader();

			if (this.listBlocks) {
				for (int i = 0; i < reader.getCountBlocks(); i++) {
					System.out.println(i + ": " + reader.getBlocks().get(i));
				}
				return 0;
			}

			if ("ktx".equals(this.format)) {
				if (this.block != -1) {
					System.err.println("-b is incompatible with ktx format");
					return -1;
				}

				reader.readAllRaws();
				reader.writeKtx(new DataOutputStream(os));

				return 0;
			}

			reader.readAllTextures(); // TODO read only concerned block

			BufferedImage img;
			if (this.block == -1) {
				int maxRes = 0;
				int maxIdx = 0;
				for (int i = 0; i < reader.getCountBlocks(); i++) {
					final Block b = reader.getBlocks().get(i);
					final int res = b.getHeight() * b.getWidth();
					if (res > maxRes) {
						maxRes = res;
						maxIdx = i;
					}
				}
				img = reader.getTextures().get(maxIdx);
			} else {
				img = reader.getTextures().get(this.block);
			}

			ImageIO.write(img, this.format, this.outputFile);
		} catch (final Exception e) {
			e.printStackTrace();
			return -1;
		}
		return 0;
	}
}
