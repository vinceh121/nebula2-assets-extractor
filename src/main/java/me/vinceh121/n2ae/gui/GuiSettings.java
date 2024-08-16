package me.vinceh121.n2ae.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;

import org.apache.commons.lang3.SystemUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GuiSettings {
	private static final ObjectMapper CONFIG_MAPPER = new ObjectMapper();
	private final LinkedList<String> recentlyOpened = new LinkedList<>();
	private String classModelUrl;

	public String getClassModelUrl() {
		return this.classModelUrl;
	}

	public void setClassModelUrl(final String classModelUrl) {
		this.classModelUrl = classModelUrl;
	}

	public void addRecentlyOpened(final String path) {
		this.recentlyOpened.add(path);

		if (this.recentlyOpened.size() > 5) {
			this.recentlyOpened.pop();
		}
	}

	public LinkedList<String> getRecentlyOpened() {
		return recentlyOpened;
	}

	public void save() throws IOException {
		try (FileOutputStream out = new FileOutputStream(GuiSettings.getConfigFile())) {
			GuiSettings.CONFIG_MAPPER.writerWithDefaultPrettyPrinter().writeValue(out, this);
		}
	}

	public static GuiSettings load() throws IOException {
		try (FileInputStream in = new FileInputStream(GuiSettings.getConfigFile())) {
			return GuiSettings.CONFIG_MAPPER.readValue(in, GuiSettings.class);
		}
	}

	public static File getConfigFile() {
		if (SystemUtils.IS_OS_WINDOWS) {
			return Path.of(System.getenv("APPDATA"), "nebula2-assets-extractor.json").toFile();
		} else if (SystemUtils.IS_OS_MAC) {
			return Path
				.of(System.getProperty("user.home"), "Library", "Application Support", "nebula2-assets-extractor.json")
				.toFile();
		} else if (SystemUtils.IS_OS_LINUX) {
			return Path.of(System.getProperty("user.home"), ".config", "nebula2-assets-extractor.json").toFile();
		} else {
			try {
				return File.createTempFile("n2ae", "config");
			} catch (final IOException e) {
				throw new RuntimeException("Couldn't create temp config", e);
			}
		}
	}
}
