package me.vinceh121.n2ae.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HexFormat;

import org.junit.jupiter.api.Test;

import me.vinceh121.n2ae.model.Mesh;
import me.vinceh121.n2ae.model.NvxFileReader;
import me.vinceh121.n2ae.model.NvxFileWriter;
import me.vinceh121.n2ae.model.ObjFileReader;
import me.vinceh121.n2ae.model.ObjFileWriter;

class NvxTests {
	@Test
	void testIdempotency() throws IOException {
		final byte[] nvx;

		try (final InputStream in = NvxTests.class.getClassLoader().getResourceAsStream("skin.nvx")) {
			nvx = in.readAllBytes();
		}

		final Mesh nvxMesh;

		try (final ByteArrayInputStream in = new ByteArrayInputStream(nvx);
				final NvxFileReader reader = new NvxFileReader(in)) {
			nvxMesh = reader.readMesh();
		}

		final byte[] obj;

		try (final ByteArrayOutputStream out = new ByteArrayOutputStream();
				final ObjFileWriter writer = new ObjFileWriter(out)) {
			writer.writeMesh(nvxMesh);
			obj = out.toByteArray();
		}

		final Mesh objMesh;

		try (final ByteArrayInputStream in = new ByteArrayInputStream(obj);
				final ObjFileReader reader = new ObjFileReader(in)) {
			objMesh = reader.readMesh();
		}

		final byte[] newNvxMesh;

		try (final ByteArrayOutputStream out = new ByteArrayOutputStream();
				final NvxFileWriter writer = new NvxFileWriter(out)) {
			writer.writeMesh(objMesh);
			newNvxMesh = out.toByteArray();
		}

		final HexFormat hex = HexFormat.of().withDelimiter(" ").withUpperCase();

		assertEquals(hex.formatHex(nvx), hex.formatHex(newNvxMesh));
	}
}
