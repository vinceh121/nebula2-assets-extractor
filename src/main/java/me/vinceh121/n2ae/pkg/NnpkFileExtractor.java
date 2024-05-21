package me.vinceh121.n2ae.pkg;

import java.io.File;
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

	public NnpkFileExtractor(final InputStream in) {
		this.in = in;
	}

	/**
	 * Extracts the table of contents to the file system
	 *
	 * @param toc
	 * @throws IOException
	 */
	public void extractAllFiles(final TableOfContents toc) throws IOException {
		this.extractAllFiles(toc, new ArrayDeque<>(6));
	}

	private void extractAllFiles(final TableOfContents toc, final Deque<String> path) throws IOException {
		if (toc.isDirectory()) {
			Path dirPath = this.output.toPath();
			for (final String p : path) {
				dirPath = dirPath.resolve(p);
			}
			dirPath.toFile().mkdirs();

			for (final TableOfContents t : toc.getEntries().values()) {
				path.add(t.getName());
				this.extractAllFiles(t, path);
				path.pollLast();
			}
		} else if (toc.isFile()) {
			Path outPath = this.output.toPath();
			for (final String p : path) {
				outPath = outPath.resolve(p);
			}
			try (FileOutputStream out = new FileOutputStream(outPath.toFile())) {
				this.writeFile(out, toc.getLength());
			}
		}
	}

	public void extractFile(final TableOfContents toc) throws IOException {
		if (!toc.isFile()) {
			throw new IllegalArgumentException("TOC entry isn't a file");
		}

		this.in.skip(toc.getOffset());

		try (FileOutputStream out = new FileOutputStream(this.output)) {
			this.writeFile(out, toc.getLength());
		}
	}

	private void writeFile(final OutputStream out, final int length) throws IOException {
		final byte[] buf = new byte[NnpkFileExtractor.BUFFER_SIZE];
		int totalRead = 0;
		while (totalRead < length) { // maybe <= ?
			final int read = this.in.read(buf,
					0,
					totalRead + NnpkFileExtractor.BUFFER_SIZE > length ? length - totalRead
							: NnpkFileExtractor.BUFFER_SIZE);
			totalRead += read;
			out.write(buf, 0, read);
		}
	}

	public File getOutput() {
		return this.output;
	}

	public void setOutput(final File output) {
		this.output = output;
	}
}
