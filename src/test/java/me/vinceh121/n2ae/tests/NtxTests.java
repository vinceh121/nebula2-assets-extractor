package me.vinceh121.n2ae.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import me.vinceh121.n2ae.texture.Block;
import me.vinceh121.n2ae.texture.NtxFileReader;
import me.vinceh121.n2ae.texture.NtxFileWriter;

class NtxTests {

	@Test
	void readerWriterConsistency() throws IOException {
		byte[] arr = NtxTests.class.getClassLoader().getResourceAsStream("radonlabsnone.ntx").readAllBytes();

		NtxFileReader reader = new NtxFileReader(new ByteArrayInputStream(arr));
		reader.readHeader();
		reader.readAllRaws();

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		NtxFileWriter writer = new NtxFileWriter(output);
		writer.writeHeader(reader.getCountBlocks());
		for (Block b : reader.getBlocks()) {
			writer.writeBlock(b);
		}
		for (byte[] mipmaps : reader.getRaws()) {
			output.write(mipmaps);
		}

		assertTrue(Arrays.equals(arr, output.toByteArray()));
	}
}
