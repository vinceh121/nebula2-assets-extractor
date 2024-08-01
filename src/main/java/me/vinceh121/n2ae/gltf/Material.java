package me.vinceh121.n2ae.gltf;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_NULL)
public class Material {
	private String name;
	private PbrMetallicRoughness pbrMetallicRoughness;
	private TextureInfo emissiveTexture;

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public PbrMetallicRoughness getPbrMetallicRoughness() {
		return this.pbrMetallicRoughness;
	}

	public void setPbrMetallicRoughness(final PbrMetallicRoughness pbrMetallicRoughness) {
		this.pbrMetallicRoughness = pbrMetallicRoughness;
	}

	public TextureInfo getEmissiveTexture() {
		return this.emissiveTexture;
	}

	public void setEmissiveTexture(final TextureInfo emissiveTexture) {
		this.emissiveTexture = emissiveTexture;
	}
}
