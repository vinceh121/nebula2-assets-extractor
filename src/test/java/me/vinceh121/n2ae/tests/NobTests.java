package me.vinceh121.n2ae.tests;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import me.vinceh121.n2ae.script.IParser;
import me.vinceh121.n2ae.script.IWriter;
import me.vinceh121.n2ae.script.NOBClazz;
import me.vinceh121.n2ae.script.nob.NOBParser;
import me.vinceh121.n2ae.script.tcl.NOBWriter;
import me.vinceh121.n2ae.script.tcl.TCLWriter;

class NobTests {
	private ObjectMapper mapper = new ObjectMapper();

	@Test
	void test() throws IOException {
		final Map<String, NOBClazz> model = this.mapper.readValue(new File("./project-nomads.classmodel.json"),
				new TypeReference<Map<String, NOBClazz>>() {
				});

		final IParser parse = new NOBParser();
		parse.setClassModel(model);
		parse.read(NobTests.class.getClassLoader().getResourceAsStream("if_hilfe.n"));

		final TCLWriter writer = new TCLWriter();
		writer.setHeader(parse.getHeader());
		writer.setCalls(parse.getCalls());
		writer.write(System.out);
		System.out.flush();
	}

	@Test
	void nobSymetry() throws IOException {
		final Map<String, NOBClazz> model = this.mapper.readValue(new File("./project-nomads.classmodel.json"),
				new TypeReference<Map<String, NOBClazz>>() {
				});

		final byte[] orig = NobTests.class.getClassLoader().getResourceAsStream("if_hilfe.n").readAllBytes();

		final IParser parse = new NOBParser();
		parse.setClassModel(model);
		parse.read(orig);

		final ByteArrayOutputStream out = new ByteArrayOutputStream();

		final IWriter writer = new NOBWriter();
		writer.setHeader(parse.getHeader());
		writer.setCalls(parse.getCalls());
		writer.write(out);

		Assertions.assertArrayEquals(orig, out.toByteArray());
	}
}
