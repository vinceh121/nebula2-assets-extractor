package me.vinceh121.n2ae.tests;

import java.io.File;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import me.vinceh121.n2ae.script.IParser;
import me.vinceh121.n2ae.script.NOBClazz;
import me.vinceh121.n2ae.script.json.JsonScriptGenerator;
import me.vinceh121.n2ae.script.nob.NOBParser;

class JsonScriptTests {
	private ObjectMapper mapper = new ObjectMapper();

	@Test
	void test() throws Exception {
		final Map<String, NOBClazz> model = this.mapper.readValue(new File("./project-nomads.classmodel.json"),
				new TypeReference<Map<String, NOBClazz>>() {
				});

		final IParser parser = new NOBParser();
		parser.setClassModel(model);
		parser.read(NobTests.class.getClassLoader().getResourceAsStream("if_hilfe.n"));

		final JsonScriptGenerator gen = new JsonScriptGenerator();

		final String actual = this.mapper.writeValueAsString(gen.generateJson(parser.getCalls()));

		Assertions.assertEquals(
				"{\"visual\":{\"@class\":\"n3dnode\",\"plane\":" + "{\"@class\":\"n3dnode\",\"txyz\":[1.0,-0.5,-1.5],"
						+ "\"mesh\":{\"@class\":\"nmeshnode\",\"setfilename\":"
						+ "\"data:if_hilfe.n/plane.nvx\"},\"shader\":{\"@class\":"
						+ "\"nlinknode\",\"settarget\":\"/data/shaders/if_hilfe\"},"
						+ "\"tex\":{\"@class\":\"ntexarraynode\",\"settexture\":"
						+ "[0,\"data:if_hilfe.n/texturenone.ntx\",\"none\"]}}}}",
				actual);
	}
}
