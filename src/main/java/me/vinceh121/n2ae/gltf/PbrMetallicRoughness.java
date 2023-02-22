package me.vinceh121.n2ae.gltf;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_NULL)
public class PbrMetallicRoughness {
	private TextureInfo baseColorTexture, metallicRoughnessTexture;

	public TextureInfo getBaseColorTexture() {
		return baseColorTexture;
	}

	public void setBaseColorTexture(TextureInfo baseColorTexture) {
		this.baseColorTexture = baseColorTexture;
	}

	public TextureInfo getMetallicRoughnessTexture() {
		return metallicRoughnessTexture;
	}

	public void setMetallicRoughnessTexture(TextureInfo metallicRoughnessTexture) {
		this.metallicRoughnessTexture = metallicRoughnessTexture;
	}
}
