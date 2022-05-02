package me.vinceh121.n2ae.pkg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;

public class NnpkFileExtractor {
	private static final int BUFFER_SIZE = 512;
	private final InputStream in;
	private File output = new File(".");

	public NnpkFileExtractor(InputStream in) {
		this.in = in;
	}

	public void extractAllFiles(TableOfContents toc) throws IOException {
		if (!this.output.isDirectory()) {
			throw new IllegalStateException("output must be a folder");
		}

		this.extractAllFiles(toc, new ArrayDeque<>(6));
	}

	private void extractAllFiles(TableOfContents toc, Deque<String> path) throws IOException {
		if (toc.isDirectory()) {
			Path dirPath = output.toPath();
			for (String p : path) {
				dirPath = dirPath.resolve(p);
			}
			dirPath.toFile().mkdirs();
			
			for (TableOfContents t : toc.getEntries().values()) {
				path.add(t.getName());
				extractAllFiles(t, path);
				path.pollLast();
			}
		} else if (toc.isFile()) {
			Path outPath = output.toPath();
			for (String p : path) {
				outPath = outPath.resolve(p);
			}
			try (FileOutputStream out = new FileOutputStream(outPath.toFile())) {
				this.writeFile(out, toc.getLength());
			}
		}
	}

	public void extractFile(TableOfContents toc) throws IOException {
		if (!toc.isFile()) {
			throw new IllegalArgumentException("TOC entry isn't a file");
		}

		in.skip(toc.getOffset());

		try (FileOutputStream out = new FileOutputStream(output)) {
			this.writeFile(out, toc.getLength());
		}
	}

	private void writeFile(OutputStream out, int length) throws IOException {
		byte[] buf = new byte[BUFFER_SIZE];
		int totalRead = 0;
		while (totalRead < length) { // maybe <= ?
			int read = in.read(buf, 0, totalRead + BUFFER_SIZE > length ? length - totalRead : BUFFER_SIZE);
			totalRead += read;
			out.write(buf, 0, read);
		}
	}

	public File getOutput() {
		return output;
	}

	public void setOutput(File output) {
		this.output = output;
	}
}
