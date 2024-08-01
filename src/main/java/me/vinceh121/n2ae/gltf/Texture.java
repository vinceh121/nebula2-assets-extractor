package me.vinceh121.n2ae.gltf;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class Texture {
	@JsonInclude(value = Include.NON_NULL)
	private String name;

	@JsonInclude(value = Include.CUSTOM, valueFilter = NotMinusOne.class)
	private int sampler = -1;

	@JsonInclude(value = Include.CUSTOM, valueFilter = NotMinusOne.class)
	private int source = -1;

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public int getSampler() {
		return this.sampler;
	}

	public void setSampler(final int sampler) {
		this.sampler = sampler;
	}

	public int getSource() {
		return this.source;
	}

	public void setSource(final int source) {
		this.source = source;
	}
}
