package me.vinceh121.n2ae.pkg;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TableOfContents implements Serializable {
	private static final long serialVersionUID = -3528403199997622881L;
	private final Map<String, TableOfContents> entries = new LinkedHashMap<>();
	private boolean directory, file;
	private int offset, length;
	private String name;
	private byte[] data;

	public TableOfContents() {
	}

	public TableOfContents(final TableOfContents from) {
		this.entries.putAll(from.entries);
		this.directory = from.directory;
		this.file = from.file;
		this.offset = from.offset;
		this.length = from.length;
		this.name = from.name;

		if (from.data != null) {
			this.data = new byte[from.data.length];
			System.arraycopy(from.data, 0, this.data, 0, from.data.length);
		}
	}

	@Override
	public TableOfContents clone() {
		return new TableOfContents(this);
	}

	public TableOfContents deepClone() {
		final TableOfContents clone = this.clone();

		for (final String key : clone.entries.keySet()) {
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
		return this.data;
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
		result = prime * result + Arrays.hashCode(this.data);
		result = prime * result
				+ Objects.hash(this.directory, this.entries, this.file, this.length, this.name, this.offset);
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final TableOfContents other = (TableOfContents) obj;
		return Arrays.equals(this.data, other.data) && this.directory == other.directory
				&& Objects.equals(this.entries, other.entries) && this.file == other.file && this.length == other.length
				&& Objects.equals(this.name, other.name) && this.offset == other.offset;
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

	public int calculateBlockLen() {
		if (this.directory) {
			return 2 + this.name.length();
		} else if (this.file) {
			return 2 * 4 + 2 + this.getName().length();
		} else {
			throw new IllegalStateException("TOC is neither directory nor file");
		}
	}

	public List<TableOfContents> flatten() {
		final List<TableOfContents> flat = new LinkedList<>();

		if (this.isDirectory()) {
			flat.add(this);

			for (final TableOfContents child : this.entries.values()) {
				flat.addAll(child.flatten());
			}
		} else if (this.isFile()) {
			flat.add(this);
		} else {
			throw new IllegalStateException("TOC is neither directory nor file");
		}

		return flat;
	}

	@Override
	public String toString() {
		return "TableOfContents [directory=" + this.directory + ", file=" + this.file + ", offset=" + this.offset
				+ ", length=" + this.length + ", name=" + this.name + ", data=" + Arrays.toString(this.data) + "]";
	}
}
