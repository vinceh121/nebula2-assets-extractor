package me.vinceh121.n2ae.cli;

import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import me.vinceh121.n2ae.model.NvxFileReader;
import me.vinceh121.n2ae.pkg.NnpkFileExtractor;
import me.vinceh121.n2ae.pkg.NnpkFileReader;
import me.vinceh121.n2ae.script.NOBClazz;
import me.vinceh121.n2ae.script.NOBScriptDecompiler;
import me.vinceh121.n2ae.texture.Block;
import me.vinceh121.n2ae.texture.NtxFileReader;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "extract", description = { "Unpacks an NPK archive and converts all assets" })
public class CmdFullExtract implements Callable<Integer> {
	public static final List<String> FILES_TO_NOT_DELETE = List.of("npk", "wav", "obj", "png", "ktx", "tcl");

	@Option(names = { "-o", "--output" }, required = true)
	private File outputFolder;

	@Option(names = { "-i", "--input" }, required = true)
	private File inputFile;

	@Option(names = { "-d", "--delete-old" }, description = { "Deletes original files and unprocessable files" })
	private boolean delete;

	@Option(names = { "-f", "--format" }, description = { "texture output image format" }, defaultValue = "png")
	private String format;

	@Option(names = { "-m",
			"--model" }, description = "Json file containing a class model generated using `extract-classes`")
	private File clazzModel;

	@Override
	public Integer call() throws Exception {
		if (!this.outputFolder.isDirectory()) {
			System.err.println("Output must be a directory");
			return -1;
		}

		try (FileInputStream is = new FileInputStream(this.inputFile)) {
			final NnpkFileReader r = new NnpkFileReader(is);
			r.readAll();

			final NnpkFileExtractor ex = new NnpkFileExtractor(is);
			ex.setOutput(this.outputFolder);
			ex.extractAllFiles(r.getTableOfContents());
		}

		this.recurse(this.outputFolder);

		return 0;
	}

	private void recurse(final File file) {
		if (file.isDirectory()) {
			for (final File f : file.listFiles()) {
				this.recurse(f);
			}
		} else {
			try {
				this.processFile(file);
			} catch (final IOException e) {
				System.err.println("Failed to process " + file.getPath());
				e.printStackTrace();
			}
		}
	}

	private void processFile(final File file) throws IOException {
		if (!file.getName().contains(".")) {
			this.unprocessableFile(file);
		}
		final String extension = file.getName().substring(file.getName().lastIndexOf('.') + 1);

		final String outPath = file.toPath()
			.resolveSibling(file.getName().substring(0, file.getName().length() - extension.length() - 1))
			.toString();

		switch (extension) {
		case "nvx":
			this.processModel(file, new File(outPath + ".obj"));
			break;
		case "ntx":
			this.processTexture(file, new File(outPath + "." + this.format));
			break;
		case "n":
			this.processScript(file, new File(outPath + ".tcl"));
			break;
		default:
			if (!CmdFullExtract.FILES_TO_NOT_DELETE.contains(extension)) {
				this.unprocessableFile(file);
			}
		}

		if (this.delete && !CmdFullExtract.FILES_TO_NOT_DELETE.contains(extension)) {
			file.delete();
		}
	}

	private void processTexture(final File fileIn, final File fileOut) throws IOException {
		if ("ktx".equals(this.format)) {
			this.processTextureKtx(fileIn, fileOut);
		} else {
			this.processTextureJava(fileIn, fileOut);
		}
	}

	private void processTextureKtx(final File fileIn, final File fileOut) throws IOException {
		try (FileInputStream is = new FileInputStream(fileIn); FileOutputStream os = new FileOutputStream(fileOut)) {
			final NtxFileReader r = new NtxFileReader(is);
			r.readHeader();
			r.readAllRaws();

			r.writeKtx(new DataOutputStream(os));
		}
	}

	private void processTextureJava(final File fileIn, final File fileOut) throws IOException {
		try (FileInputStream is = new FileInputStream(fileIn); FileOutputStream os = new FileOutputStream(fileOut)) {
			final NtxFileReader r = new NtxFileReader(is);
			r.readHeader();
			r.readAllTextures();

			int maxRes = 0;
			int maxIdx = 0;
			for (int i = 0; i < r.getCountBlocks(); i++) {
				final Block b = r.getBlocks().get(i);
				final int res = b.getHeight() * b.getWidth();
				if (res > maxRes) {
					maxRes = res;
					maxIdx = i;
				}
			}

			final BufferedImage img = r.getTextures().get(maxIdx);

			ImageIO.write(img, this.format, os);
		}
	}

	private void processModel(final File fileIn, final File fileOut) throws IOException {
		try (FileInputStream is = new FileInputStream(fileIn); FileOutputStream os = new FileOutputStream(fileOut)) {
			final NvxFileReader r = new NvxFileReader(is);
			r.readAll();
			r.writeObj(os);
		}
	}

	private void processScript(final File fileIn, final File fileOut) throws IOException {
		try (FileInputStream is = new FileInputStream(fileIn); FileOutputStream os = new FileOutputStream(fileOut)) {
			final NOBScriptDecompiler r = new NOBScriptDecompiler(is);

			if (this.clazzModel != null) {
				final ObjectMapper mapper = new ObjectMapper();
				final Map<String, NOBClazz> model = mapper.readValue(this.clazzModel,
						new TypeReference<Map<String, NOBClazz>>() {
						});
				r.setClazzes(model);

				final NOBClazz nroot = new NOBClazz();
				nroot.setName("nroot");
				model.put(nroot.getName(), nroot);
			}

			os.write(r.readHeader().getBytes());
			while (is.available() > 0) {
				os.write(r.readBlock().getBytes());
			}
		}
	}

	private void unprocessableFile(final File file) {
		System.err.println("Don't know how to process file " + file.getPath());
		if (this.delete) {
			file.delete();
		}
	}
}
