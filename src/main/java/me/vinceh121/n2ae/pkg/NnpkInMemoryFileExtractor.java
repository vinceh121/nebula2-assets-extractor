package me.vinceh121.n2ae.pkg;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class NnpkInMemoryFileExtractor {
	private final File output;

	public NnpkInMemoryFileExtractor(final File output) {
		this.output = output;
	}

	public void write(final TableOfContents toc) throws IOException {
		this.write(toc, this.output);
	}

	private void write(final TableOfContents toc, final File out) throws IOException {
		if (toc.isDirectory()) {
			out.mkdir();
			for (final TableOfContents child : toc.getEntries().values()) {
				this.write(child, out.toPath().resolve(child.getName()).toFile());
			}
		} else if (toc.isFile()) {
			try (FileOutputStream os = new FileOutputStream(out)) {
				os.write(toc.getData());
			}
		}
	}
}
