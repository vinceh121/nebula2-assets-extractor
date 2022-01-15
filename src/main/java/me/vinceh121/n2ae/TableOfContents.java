package me.vinceh121.n2ae;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class TableOfContents {
	private boolean directory, file;
	private int offset, length;
	private String name;
	private final Map<String, TableOfContents> entries = new LinkedHashMap<>();

	public boolean isDirectory() {
		return directory;
	}

	public void setDirectory(boolean directory) {
		this.directory = directory;
	}

	public boolean isFile() {
		return file;
	}

	public void setFile(boolean file) {
		this.file = file;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, TableOfContents> getEntries() {
		return entries;
	}

	public TableOfContents get(String key) {
		return entries.get(key);
	}

	public TableOfContents get(Iterable<String> path) {
		TableOfContents toc = this;
		for (String s : path) {
			toc = toc.get(s);
		}
		return toc;
	}

	public void put(String key, TableOfContents value) {
		entries.put(key, value);
	}

	public void put(Iterable<String> path, TableOfContents value) {
		TableOfContents toc = this;
		Iterator<String> it = path.iterator();
		while (it.hasNext()) {
			String key = it.next();
			if (!it.hasNext()) {
				toc.put(key, value);
			} else {
				toc = toc.get(key);
			}
		}
	}

	@Override
	public String toString() {
		return "TableOfContents [directory=" + directory + ", file=" + file + ", offset=" + offset + ", length="
				+ length + ", name=" + name + ", entries=" + entries + "]";
	}
}
