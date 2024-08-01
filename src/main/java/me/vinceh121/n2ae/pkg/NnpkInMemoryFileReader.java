package me.vinceh121.n2ae.pkg;

import java.io.IOException;
import java.io.RandomAccessFile;

public class NnpkInMemoryFileReader {
	private final RandomAccessFile file;
	private int dataOffset;

	public NnpkInMemoryFileReader(final RandomAccessFile file) {
		this.file = file;
	}

	public void readTableOfContents(final TableOfContents toc) throws IOException {
		if (toc.isDirectory()) {
			for (final TableOfContents child : toc.getEntries().values()) {
				this.readTableOfContents(child);
			}
		} else if (toc.isFile()) {
			this.file.seek(toc.getOffset() + this.dataOffset);
			final byte[] data = new byte[toc.getLength()];
			this.file.read(data);
			toc.setData(data);
		}
	}

	public int getDataOffset() {
		return this.dataOffset;
	}

	public void setDataOffset(final int dataOffset) {
		this.dataOffset = dataOffset;
	}
}
