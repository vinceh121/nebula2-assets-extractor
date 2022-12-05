package me.vinceh121.n2ae.gltf;

import java.util.ArrayList;
import java.util.List;

public class Animation {
	private final List<Channel> channels = new ArrayList<>();
	private final List<Sampler> samplers = new ArrayList<>();
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Channel> getChannels() {
		return channels;
	}

	public List<Sampler> getSamplers() {
		return samplers;
	}

}
