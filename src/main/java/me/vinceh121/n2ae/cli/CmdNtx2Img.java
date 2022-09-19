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
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

@Command(name = "ntx2img", description = { "Convert an NTX1 file to an image" })
public class CmdNtx2Img implements Callable<Integer> {
	@Spec
	private CommandSpec spec;

	@Option(names = { "-o", "--output" })
	private File outputFile;

	@Option(names = { "-i", "--input" })
	private File inputFile;

	@Option(names = { "-l", "--list-blocks" }, description = { "list blocks in the texture file" })
	private boolean listBlocks;

	@Option(names = { "-b", "--block" }, description = { "select block to extract",
			"defaults to higest resolution" }, defaultValue = "-1")
	private int block;

	@Option(names = { "-f", "--format" }, description = { "output image format" }, defaultValue = "png")
	private String format;

	@Option(names = { "--list-formats" }, description = { "lists supported output image formats" })
	private boolean listFormats;

	@Override
	public Integer call() throws Exception {
		if (this.listFormats) {
			System.out.println("Specially handled formats:");
			System.out.println();
			System.out.println("\tktx  -  KTX 1 textures for OpenGL");
			System.out.println("\traw  -  Raw texture block");
			System.out.println();
			System.out.println("Common image formats:");
			System.out.println();
			for (final String n : ImageIO.getWriterFormatNames()) {
				System.out.println("\t" + n);
			}
			return 0;
		}

		if (this.inputFile == null) {
			throw new ParameterException(this.spec.commandLine(), "Missing required argument -i, --input=<inputFile>");
		}

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
			} else if ("raw".equals(this.format)) {
				reader.readAllRaws();
				final byte[] raw = reader.getRaws().get(this.block == -1 ? 0 : this.block);
				os.write(raw);
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
