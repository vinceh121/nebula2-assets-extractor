package me.vinceh121.n2ae.tests;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import me.vinceh121.n2ae.script.NOBClazz;
import me.vinceh121.n2ae.script.NOBParser;
import me.vinceh121.n2ae.script.TCLWriter;

class NobTests {
	private ObjectMapper mapper = new ObjectMapper();

	@Test
	void test() throws IOException {
		final Map<String, NOBClazz> model = mapper.readValue(new File("./project-nomads.classmodel.json"),
				new TypeReference<Map<String, NOBClazz>>() {
				});

		NOBParser parse = new NOBParser();
		parse.setClassModel(model);
		parse.read(NobTests.class.getClassLoader().getResourceAsStream("if_hilfe.n"));

		TCLWriter writer = new TCLWriter();
		writer.setHeader(parse.getHeader());
		writer.setCalls(parse.getCalls());
		writer.write(System.out);
		System.out.flush();
	}
}
