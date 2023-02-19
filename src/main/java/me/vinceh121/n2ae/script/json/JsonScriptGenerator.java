package me.vinceh121.n2ae.script.json;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import me.vinceh121.n2ae.script.ClassCommandCall;
import me.vinceh121.n2ae.script.ICommandCall;
import me.vinceh121.n2ae.script.NewCommandCall;
import me.vinceh121.n2ae.script.SelCommandCall;

public class JsonScriptGenerator {
	private final ObjectMapper mapper;

	public JsonScriptGenerator() {
		this(new ObjectMapper());
	}

	public JsonScriptGenerator(final ObjectMapper mapper) {
		this.mapper = mapper;
	}

	public ObjectNode generateJson(final List<ICommandCall> script) {
		final ObjectNode node = this.mapper.createObjectNode();
		this.generateJson(script, 0, node);
		return node;
	}

	private int generateJson(final List<ICommandCall> script, final int start, final ObjectNode node) {
		for (int i = start; i < script.size(); i++) {
			final ICommandCall call = script.get(i);
			if (call instanceof NewCommandCall) {
				final NewCommandCall newCall = (NewCommandCall) call;
				final ObjectNode newObject = this.mapper.createObjectNode();
				newObject.put("@class", call.getClazz().getName());
				i = this.generateJson(script, i + 1, newObject);
				node.set(newCall.getVarName(), newObject);
			} else if (call instanceof SelCommandCall) {
				final SelCommandCall sel = (SelCommandCall) call;
				if (!"..".equals(sel.getPath())) {
					throw new UnsupportedOperationException("Sel path other than .. not supported");
				}
				return i;
			} else if (call instanceof ClassCommandCall) {
				final ClassCommandCall clsCall = (ClassCommandCall) call;
				final String prop = clsCall.getPrototype().getName();
				final Object val = this.getValueForCall(clsCall);

				if (node.has(prop)) {
					final JsonNode oldVal = node.get(prop);
					if (oldVal.isArray() && ((ArrayNode) oldVal).get(0).isArray()) {
						this.addObject((ArrayNode) oldVal, val);
					} else {
						final ArrayNode arr = this.mapper.createArrayNode();
						arr.add(oldVal);
						this.addObject(arr, val);
						node.set(prop, arr);
					}
				} else {
					this.putObject(node, prop, val);
				}
			}
		}
		return -1;
	}

	private Object getValueForCall(final ClassCommandCall clsCall) {
		if (clsCall.getArguments().length == 0) {
			return true;
		} else if (clsCall.getArguments().length == 1) {
			return clsCall.getArguments()[0];
		} else {
			final ArrayNode arr = this.mapper.createArrayNode();
			for (final Object obj : clsCall.getArguments()) {
				this.addObject(arr, obj);
			}
			return arr;
		}
	}

	private void addObject(final ArrayNode node, final Object value) {
		if (value instanceof Integer) {
			node.add((int) value);
		} else if (value instanceof Float) {
			node.add((float) value);
		} else if (value instanceof String) {
			node.add((String) value);
		} else if (value instanceof Boolean) {
			node.add((boolean) value);
		} else if (value instanceof JsonNode) {
			node.add((JsonNode) value);
		} else {
			throw new IllegalArgumentException("Cannot add type " + value.getClass());
		}
	}

	private void putObject(final ObjectNode node, final String propertyName, final Object value) {
		if (value instanceof Integer) {
			node.put(propertyName, (int) value);
		} else if (value instanceof Float) {
			node.put(propertyName, (float) value);
		} else if (value instanceof String) {
			node.put(propertyName, (String) value);
		} else if (value instanceof Boolean) {
			node.put(propertyName, (boolean) value);
		} else if (value instanceof JsonNode) {
			node.set(propertyName, (JsonNode) value);
		} else {
			throw new IllegalArgumentException("Cannot put type " + value.getClass());
		}
	}
}
