package me.vinceh121.n2ae.tests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.KTXTextureData;

import me.vinceh121.n2ae.texture.Block;
import me.vinceh121.n2ae.texture.NtxFileReader;
import me.vinceh121.n2ae.texture.NtxFileWriter;

class NtxTests {

	@Test
	void readerWriterConsistency() throws IOException {
		final byte[] arr = NtxTests.class.getClassLoader().getResourceAsStream("radonlabsnone.ntx").readAllBytes();

		final NtxFileReader reader = new NtxFileReader(new ByteArrayInputStream(arr));
		reader.readHeader();
		reader.readAllRaws();

		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		final NtxFileWriter writer = new NtxFileWriter(output);
		writer.writeHeader(reader.getCountBlocks());
		for (final Block b : reader.getBlocks()) {
			writer.writeBlock(b);
		}
		for (final byte[] mipmaps : reader.getRaws()) {
			output.write(mipmaps);
		}

		Assertions.assertTrue(Arrays.equals(arr, output.toByteArray()));
	}

	@Test
	void ktxWriter2Gdx() throws IOException {
		final byte[] arr = NtxTests.class.getClassLoader().getResourceAsStream("radonlabsnone.ntx").readAllBytes();

		final NtxFileReader reader = new NtxFileReader(new ByteArrayInputStream(arr));
		reader.readHeader();
		reader.readAllRaws();

		final File output = File.createTempFile("ktxWriter2Gdx", ".ktx");

		try (FileOutputStream out = new FileOutputStream(output)) {
			reader.writeKtx(new DataOutputStream(out));
		}

		final KTXTextureData texData = new KTXTextureData(new FileHandle(output), false);
		try {
			texData.prepare();
		} catch (final UnsatisfiedLinkError e) {
			// because natives aren't loaded
		}
	}
}
