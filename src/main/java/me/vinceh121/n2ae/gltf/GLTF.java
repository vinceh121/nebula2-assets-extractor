package me.vinceh121.n2ae.gltf;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_EMPTY)
public class GLTF {
	private final Asset asset = new Asset();
	private int scene;
	private final List<Scene> scenes = new ArrayList<>();
	private final List<Node> nodes = new ArrayList<>();
	private final List<Mesh> meshes = new ArrayList<>();
	private final List<Animation> animations = new ArrayList<>();
	private final List<Skin> skins = new ArrayList<>();
	private final List<Accessor> accessors = new ArrayList<>();
	private final List<BufferView> bufferViews = new ArrayList<>();
	private final List<Buffer> buffers = new ArrayList<>();

	public int getScene() {
		return scene;
	}

	public void setScene(int scene) {
		this.scene = scene;
	}

	public Asset getAsset() {
		return asset;
	}

	public List<Scene> getScenes() {
		return scenes;
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public List<Mesh> getMeshes() {
		return meshes;
	}

	public List<Animation> getAnimations() {
		return animations;
	}

	public List<Skin> getSkins() {
		return skins;
	}

	public List<Accessor> getAccessors() {
		return accessors;
	}

	public List<BufferView> getBufferViews() {
		return bufferViews;
	}

	public List<Buffer> getBuffers() {
		return buffers;
	}
}
