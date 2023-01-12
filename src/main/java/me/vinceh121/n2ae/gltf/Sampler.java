package me.vinceh121.n2ae.gltf;

public class Sampler {
	private int input, output;
	private GltfInterpolation interpolation;

	public int getInput() {
		return this.input;
	}

	public void setInput(final int input) {
		this.input = input;
	}

	public int getOutput() {
		return this.output;
	}

	public void setOutput(final int output) {
		this.output = output;
	}

	public GltfInterpolation getInterpolation() {
		return this.interpolation;
	}

	public void setInterpolation(final GltfInterpolation interpolation) {
		this.interpolation = interpolation;
	}

	public enum GltfInterpolation {
		LINEAR, STEP, CUBICSPLINE;
	}
}
