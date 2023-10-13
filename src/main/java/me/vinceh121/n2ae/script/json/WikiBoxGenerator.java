package me.vinceh121.n2ae.script.json;

import java.io.PrintStream;
import java.text.DecimalFormat;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import me.vinceh121.n2ae.script.json.WikiBox.Entry;

public class WikiBoxGenerator {
	public void write(final PrintStream out, final WikiBox box, final ObjectNode node) {
		out.print("{{");
		out.println(box.getName());
		for (final Entry e : box.getEntries()) {
			final JsonPointer ptr = e.getPointer();
			final JsonNode jsonVal = node.at(ptr);

			final Object val;

			if (jsonVal.isMissingNode()) {
				if (e.getDefolt() != null) {
					val = e.getDefolt();
				} else {
					throw new IllegalStateException("Pointer " + ptr + " missing and has no default");
				}
			} else {
				val = jsonVal;
			}

			out.print("| ");
			out.print(e.getName());
			out.print("=");
			if (jsonVal.isFloat()) {
				// use default decimal format to print decimal part only when required
				out.println(new DecimalFormat().format(jsonVal.asDouble()));
			} else {
				out.println(val);
			}
		}
		out.println("}}");
	}
}
