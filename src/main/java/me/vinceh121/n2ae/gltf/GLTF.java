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
		return this.scene;
	}

	public void setScene(final int scene) {
		this.scene = scene;
	}

	public Asset getAsset() {
		return this.asset;
	}

	public List<Scene> getScenes() {
		return this.scenes;
	}

	public List<Node> getNodes() {
		return this.nodes;
	}

	public List<Mesh> getMeshes() {
		return this.meshes;
	}

	public List<Animation> getAnimations() {
		return this.animations;
	}

	public List<Skin> getSkins() {
		return this.skins;
	}

	public List<Accessor> getAccessors() {
		return this.accessors;
	}

	public List<BufferView> getBufferViews() {
		return this.bufferViews;
	}

	public List<Buffer> getBuffers() {
		return this.buffers;
	}
}
