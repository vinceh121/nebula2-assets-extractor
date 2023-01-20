package me.vinceh121.n2ae.pkg;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class TableOfContents {
	private final Map<String, TableOfContents> entries = new LinkedHashMap<>();
	private boolean directory, file;
	private int offset, length, blockLen;
	private String name;
	private byte[] data;

	public TableOfContents() {
	}

	public TableOfContents(TableOfContents from) {
		this.entries.putAll(from.entries);
		this.directory = from.directory;
		this.file = from.file;
		this.offset = from.offset;
		this.length = from.length;
		this.blockLen = from.blockLen;
		this.name = from.name;
		this.data = new byte[from.data.length];
		System.arraycopy(from.data, 0, this.data, 0, from.data.length);
	}

	@Override
	public TableOfContents clone() {
		return new TableOfContents(this);
	}

	public TableOfContents deepClone() {
		TableOfContents clone = this.clone();
		for (String key : clone.entries.keySet()) {
			clone.entries.put(key, clone.entries.get(key).deepClone());
		}
		return clone;
	}

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

	public int getBlockLen() {
		return blockLen;
	}

	public void setBlockLen(int blockLen) {
		this.blockLen = blockLen;
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

	public byte[] getData() {
		return data;
	}

	public void setData(final byte[] data) {
		this.data = data;
	}

	public TableOfContents get(final String key) {
		return this.entries.get(key);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(data);
		result = prime * result + Objects.hash(blockLen, directory, entries, file, length, name, offset);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TableOfContents other = (TableOfContents) obj;
		return blockLen == other.blockLen && Arrays.equals(data, other.data) && directory == other.directory
				&& Objects.equals(entries, other.entries) && file == other.file && length == other.length
				&& Objects.equals(name, other.name) && offset == other.offset;
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
		return "TableOfContents [directory=" + directory + ", file=" + file + ", offset="
				+ offset + ", length=" + length + ", blockLen=" + blockLen + ", name=" + name + ", data="
				+ Arrays.toString(data) + "]";
	}
}
