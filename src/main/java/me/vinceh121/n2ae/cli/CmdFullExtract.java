package me.vinceh121.n2ae.cli;

import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;

import me.vinceh121.n2ae.model.NvxFileReader;
import me.vinceh121.n2ae.pkg.NnpkFileExtractor;
import me.vinceh121.n2ae.pkg.NnpkFileReader;
import me.vinceh121.n2ae.texture.Block;
import me.vinceh121.n2ae.texture.NtxFileReader;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "extract", description = { "Unpacks an NPK archive and converts all assets" })
public class CmdFullExtract implements Callable<Integer> {
	public static final List<String> FILES_TO_NOT_DELETE = List.of("npk", "wav", "obj", "png", "ktx");

	@Option(names = { "-o", "--output" }, required = true)
	private File outputFolder;

	@Option(names = { "-i", "--input" }, required = true)
	private File inputFile;

	@Option(names = { "-d", "--delete-old" }, description = { "Deletes original files and unprocessable files" })
	private boolean delete;

	@Option(names = { "-f", "--format" }, description = { "texture output image format" }, defaultValue = "png")
	private String format;

	@Override
	public Integer call() throws Exception {
		if (!outputFolder.isDirectory()) {
			System.err.println("Output must be a directory");
			return -1;
		}

		try (FileInputStream is = new FileInputStream(inputFile)) {
			NnpkFileReader r = new NnpkFileReader(is);
			r.readAll();

			NnpkFileExtractor ex = new NnpkFileExtractor(is);
			ex.setOutput(outputFolder);
			ex.extractAllFiles(r.getTableOfContents());
		}

		recurse(outputFolder);

		return 0;
	}

	private void recurse(File file) {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				recurse(f);
			}
		} else {
			try {
				this.processFile(file);
			} catch (IOException e) {
				System.err.println("Failed to process " + file.getPath());
				e.printStackTrace();
			}
		}
	}

	private void processFile(File file) throws IOException {
		if (!file.getName().contains(".")) {
			this.unprocessableFile(file);
		}
		String extension = file.getName().substring(file.getName().lastIndexOf('.') + 1);

		String outPath = file.toPath().resolveSibling(file.getName().substring(0, file.getName().length() - 4))
				.toString();

		switch (extension) {
		case "nvx":
			this.processModel(file, new File(outPath + ".obj"));
			break;
		case "ntx":
			this.processTexture(file, new File(outPath + "." + this.format));
			break;
		default:
			if (!FILES_TO_NOT_DELETE.contains(extension)) {
				this.unprocessableFile(file);
			}
		}

		if (this.delete && !FILES_TO_NOT_DELETE.contains(extension)) {
			file.delete();
		}
	}

	private void processTexture(File fileIn, File fileOut) throws IOException {
		if ("ktx".equals(this.format)) {
			this.processTextureKtx(fileIn, fileOut);
		} else {
			this.processTextureJava(fileIn, fileOut);
		}
	}

	private void processTextureKtx(File fileIn, File fileOut) throws IOException {
		try (FileInputStream is = new FileInputStream(fileIn); FileOutputStream os = new FileOutputStream(fileOut)) {
			NtxFileReader r = new NtxFileReader(is);
			r.readHeader();
			r.readAllRaws();

			r.writeKtx(new DataOutputStream(os));
		}
	}

	private void processTextureJava(File fileIn, File fileOut) throws IOException {
		try (FileInputStream is = new FileInputStream(fileIn); FileOutputStream os = new FileOutputStream(fileOut)) {
			NtxFileReader r = new NtxFileReader(is);
			r.readHeader();
			r.readAllTextures();

			int maxRes = 0;
			int maxIdx = 0;
			for (int i = 0; i < r.getCountBlocks(); i++) {
				Block b = r.getBlocks().get(i);
				int res = b.getHeight() * b.getWidth();
				if (res > maxRes) {
					maxRes = res;
					maxIdx = i;
				}
			}

			BufferedImage img = r.getTextures().get(maxIdx);

			ImageIO.write(img, this.format, os);
		}
	}

	private void processModel(File fileIn, File fileOut) throws IOException {
		try (FileInputStream is = new FileInputStream(fileIn); FileOutputStream os = new FileOutputStream(fileOut)) {
			NvxFileReader r = new NvxFileReader(is);
			r.readAll();
			r.writeObj(os);
		}
	}

	private void unprocessableFile(File file) {
		System.err.println("Don't know how to process file " + file.getPath());
		if (this.delete) {
			file.delete();
		}
	}
}
