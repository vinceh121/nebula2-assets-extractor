package me.vinceh121.n2ae.gltf;

public class Asset {
	private String version = "2.0";
	private String generator = "nebula2-assets-extractor";

	public String getVersion() {
		return this.version;
	}

	public void setVersion(final String version) {
		this.version = version;
	}

	public String getGenerator() {
		return this.generator;
	}

	public void setGenerator(final String generator) {
		this.generator = generator;
	}
}
