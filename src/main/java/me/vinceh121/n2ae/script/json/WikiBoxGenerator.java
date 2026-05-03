package me.vinceh121.n2ae.script.json;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class WikiBoxGenerator {
	public static Map<String, Object> extractValues(final WikiBox box, final ObjectNode node) {
		final Map<String, Object> values = new HashMap<>();

		for (final me.vinceh121.n2ae.script.json.WikiBox.Entry e : box.getEntries()) {
			final JsonPointer ptr = e.getPointer();
			final JsonNode jsonVal = node.at(ptr);

			final Object val;

			if (jsonVal.isMissingNode()) {
				if (e.getDefolt() != null) {
					val = e.getDefolt();
				} else {
					throw new IllegalStateException("Pointer " + ptr + " missing and has no default");
				}
			} else if (!jsonVal.isValueNode()) {
				throw new IllegalStateException("Pointer " + ptr + " is not pointing to a scalar, but a " + jsonVal);
			} else if (jsonVal.isFloat()) {
				val = jsonVal.floatValue();
			} else if (jsonVal.isTextual()) {
				val = jsonVal.asText();
			} else {
				throw new IllegalStateException("Unhandled value " + jsonVal);
			}

			values.put(e.getName(), val);
		}

		return values;
	}

	public static void write(final PrintStream out, final WikiBox box, final Map<String, Object> values) {
		out.print("{{");
		out.println(box.getName());

		for (final Entry<String, Object> e : values.entrySet()) {
			out.print("| ");
			out.print(e.getKey());
			out.print("=");

			final Object val = e.getValue();

			if (val instanceof Float) {
				// use default decimal format to print decimal part only when required
				out.println(new DecimalFormat().format(val));
			} else {
				out.println(val);
			}
		}
		out.println("}}");
	}
}
