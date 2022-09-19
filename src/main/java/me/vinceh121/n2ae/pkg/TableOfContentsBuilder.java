package me.vinceh121.n2ae.pkg;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

public class TableOfContentsBuilder {
	private final TableOfContents toc = new TableOfContents();
	private int fileOffset;

	public void buildTableOfContents(final File root) throws IOException {
		if (!root.isDirectory()) {
			throw new IllegalArgumentException("root isn't a directory");
		}
		this.toc.setName(root.getName());
		this.toc.setDirectory(true);

		final File[] list = root.listFiles();
		Arrays.sort(list, (f1, f2) -> f1.getName().compareTo(f2.getName()));
		for (final File child : list) {
			this.buildTableOfContents(child, this.toc);
		}
	}

	private void buildTableOfContents(final File file, final TableOfContents toc) throws IOException {
		if (file.isDirectory()) {
			final TableOfContents dtoc = new TableOfContents();
			dtoc.setDirectory(true);
			dtoc.setName(file.getName());
			final File[] list = file.listFiles();
			Arrays.sort(list, (f1, f2) -> f1.getName().compareTo(f2.getName()));
			for (final File child : list) {
				this.buildTableOfContents(child, dtoc);
			}
			toc.put(dtoc.getName(), dtoc);
		} else if (file.isFile()) {
			final TableOfContents ftoc = new TableOfContents();
			ftoc.setFile(true);
			ftoc.setName(file.getName());
			ftoc.setLength((int) Files.size(file.toPath()));
			ftoc.setOffset(this.fileOffset);
			this.fileOffset += ftoc.getLength();
			toc.put(file.getName(), ftoc);
		} else {
			System.err.println("wtf " + file);
		}
	}

	public int getOffset() {
		return this.fileOffset;
	}

	public void setOffset(final int offset) {
		this.fileOffset = offset;
	}

	public TableOfContents getTableOfContents() {
		return this.toc;
	}
}
