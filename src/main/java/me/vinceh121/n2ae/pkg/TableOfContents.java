package me.vinceh121.n2ae.pkg;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class TableOfContents {
	private boolean directory, file;
	private int offset, length;
	private String name;
	private final Map<String, TableOfContents> entries = new LinkedHashMap<>();

	public boolean isDirectory() {
		return this.directory;
	}

	public void setDirectory(final boolean directory) {
		this.directory = directory;
	}

	public boolean isFile() {
		return this.file;
	}

	public void setFile(final boolean file) {
		this.file = file;
	}

	public int getOffset() {
		return this.offset;
	}

	public void setOffset(final int offset) {
		this.offset = offset;
	}

	public int getLength() {
		return this.length;
	}

	public void setLength(final int length) {
		this.length = length;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public Map<String, TableOfContents> getEntries() {
		return this.entries;
	}

	public TableOfContents get(final String key) {
		return this.entries.get(key);
	}

	public TableOfContents get(final Iterable<String> path) {
		TableOfContents toc = this;
		for (final String s : path) {
			toc = toc.get(s);
		}
		return toc;
	}

	public void put(final String key, final TableOfContents value) {
		this.entries.put(key, value);
	}

	public void put(final Iterable<String> path, final TableOfContents value) {
		TableOfContents toc = this;
		final Iterator<String> it = path.iterator();
		while (it.hasNext()) {
			final String key = it.next();
			if (!it.hasNext()) {
				toc.put(key, value);
			} else {
				toc = toc.get(key);
			}
		}
	}

	@Override
	public String toString() {
		return "TableOfContents [directory=" + this.directory + ", file=" + this.file + ", offset=" + this.offset
				+ ", length=" + this.length + ", name=" + this.name + ", entries=" + this.entries + "]";
	}
}
