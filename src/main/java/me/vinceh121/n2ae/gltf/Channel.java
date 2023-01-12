package me.vinceh121.n2ae.gltf;

public class Channel {
	private int sampler;
	private ChannelTarget target;

	public int getSampler() {
		return this.sampler;
	}

	public void setSampler(final int sampler) {
		this.sampler = sampler;
	}

	public ChannelTarget getTarget() {
		return this.target;
	}

	public void setTarget(final ChannelTarget target) {
		this.target = target;
	}
}
