package me.vinceh121.n2ae.gltf;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_NULL)
public class Material {
	private String name;
	private PbrMetallicRoughness pbrMetallicRoughness;
	private TextureInfo emissiveTexture;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PbrMetallicRoughness getPbrMetallicRoughness() {
		return pbrMetallicRoughness;
	}

	public void setPbrMetallicRoughness(PbrMetallicRoughness pbrMetallicRoughness) {
		this.pbrMetallicRoughness = pbrMetallicRoughness;
	}

	public TextureInfo getEmissiveTexture() {
		return emissiveTexture;
	}

	public void setEmissiveTexture(TextureInfo emissiveTexture) {
		this.emissiveTexture = emissiveTexture;
	}
}
