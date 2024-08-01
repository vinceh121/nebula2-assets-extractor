package me.vinceh121.n2ae.tests;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;

import me.vinceh121.n2ae.pkg.NnpkFileReader;
import me.vinceh121.n2ae.pkg.NnpkFileWriter;
import me.vinceh121.n2ae.pkg.NnpkInMemoryFileReader;

class NpkTests {
	// @Test do not use this test. It's not possible to use RandomAccessFile to
	// point to JAR resources, so we'll keep a local file for now
	void testFromMemorySymetry() throws IOException {
		final String testFile = "/home/vincent/Games/ProjectNomads/Project Nomads/Run/data-orig.npk";
		final byte[] dataExpected = Files.readAllBytes(Path.of(testFile));

		final NnpkFileReader read;
		try (FileInputStream in = new FileInputStream(testFile)) {
			read = new NnpkFileReader(in);
			read.readAll();
		}

		final NnpkInMemoryFileReader memRead;
		try (RandomAccessFile rand = new RandomAccessFile(testFile, "r")) {
			memRead = new NnpkInMemoryFileReader(rand);
			memRead.setDataOffset(read.getDataOffset());
			memRead.readTableOfContents(read.getTableOfContents());
		}

		final NnpkFileWriter writer;
		final byte[] dataActual;
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			NnpkFileWriter.updateTableOfContentsOffsets(read.getTableOfContents());
			writer = new NnpkFileWriter(out);
			writer.setTableOfContents(read.getTableOfContents());
			writer.writeFromMemory();
			dataActual = out.toByteArray();
		}

		Assertions.assertArrayEquals(dataExpected, dataActual);
	}
}
