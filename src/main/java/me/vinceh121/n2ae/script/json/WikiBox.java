package me.vinceh121.n2ae.script.json;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.JsonPointer;

public class WikiBox {
	private final String name;
	private final List<Entry> entries = new LinkedList<>();

	public WikiBox(final String name) {
		this.name = name;
	}

	public WikiBox(final String name, final Collection<Entry> entries) {
		this.name = name;
		this.entries.addAll(entries);
	}

	public String getName() {
		return this.name;
	}

	public WikiBox addEntry(final Entry entry) {
		this.entries.add(entry);
		return this;
	}

	public WikiBox addEntry(final String name, final String pointer) {
		this.entries.add(new Entry(name, JsonPointer.compile(pointer), null));
		return this;
	}

	public WikiBox addEntry(final String name, final String pointer, final Object defolt) {
		this.entries.add(new Entry(name, JsonPointer.compile(pointer), defolt));
		return this;
	}

	public List<Entry> getEntries() {
		return this.entries;
	}

	public static class Entry {
		private String name;
		private JsonPointer pointer;
		private Object defolt; // damn reserved keywords

		public Entry(final String name, final JsonPointer pointer, final Object defolt) {
			this.name = name;
			this.pointer = pointer;
			this.defolt = defolt;
		}

		public String getName() {
			return this.name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public JsonPointer getPointer() {
			return this.pointer;
		}

		public void setPointer(final JsonPointer pointer) {
			this.pointer = pointer;
		}

		public Object getDefolt() {
			return this.defolt;
		}

		public void setDefolt(final Object defolt) {
			this.defolt = defolt;
		}

		@Override
		public String toString() {
			return "Entry [name=" + this.name + ", pointer=" + this.pointer + ", defolt=" + this.defolt + "]";
		}
	}
}
