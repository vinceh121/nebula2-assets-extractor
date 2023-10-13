package me.vinceh121.n2ae.script.json;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.JsonPointer;

public class WikiBox {
	private final String name;
	private final List<Entry> entries = new LinkedList<>();

	public WikiBox(String name) {
		this.name = name;
	}

	public WikiBox(String name, Collection<Entry> entries) {
		this.name = name;
		this.entries.addAll(entries);
	}

	public String getName() {
		return name;
	}

	public WikiBox addEntry(Entry entry) {
		this.entries.add(entry);
		return this;
	}

	public WikiBox addEntry(String name, String pointer) {
		this.entries.add(new Entry(name, JsonPointer.compile(pointer), null));
		return this;
	}

	public WikiBox addEntry(String name, String pointer, Object defolt) {
		this.entries.add(new Entry(name, JsonPointer.compile(pointer), defolt));
		return this;
	}

	public List<Entry> getEntries() {
		return entries;
	}

	public static class Entry {
		private String name;
		private JsonPointer pointer;
		private Object defolt; // damn reserved keywords

		public Entry(String name, JsonPointer pointer, Object defolt) {
			this.name = name;
			this.pointer = pointer;
			this.defolt = defolt;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public JsonPointer getPointer() {
			return pointer;
		}

		public void setPointer(JsonPointer pointer) {
			this.pointer = pointer;
		}

		public Object getDefolt() {
			return defolt;
		}

		public void setDefolt(Object defolt) {
			this.defolt = defolt;
		}

		@Override
		public String toString() {
			return "Entry [name=" + name + ", pointer=" + pointer + ", defolt=" + defolt + "]";
		}
	}
}
