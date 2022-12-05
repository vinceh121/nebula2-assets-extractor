package me.vinceh121.n2ae.gltf;

public class Channel {
	private int sampler;
	private ChannelTarget target;

	public int getSampler() {
		return sampler;
	}

	public void setSampler(int sampler) {
		this.sampler = sampler;
	}

	public ChannelTarget getTarget() {
		return target;
	}

	public void setTarget(ChannelTarget target) {
		this.target = target;
	}
}
