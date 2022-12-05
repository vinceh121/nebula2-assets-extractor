package me.vinceh121.n2ae.gltf;

public class Sampler {
	private int input, output;
	private GltfInterpolation interpolation;

	public int getInput() {
		return input;
	}

	public void setInput(int input) {
		this.input = input;
	}

	public int getOutput() {
		return output;
	}

	public void setOutput(int output) {
		this.output = output;
	}

	public GltfInterpolation getInterpolation() {
		return interpolation;
	}

	public void setInterpolation(GltfInterpolation interpolation) {
		this.interpolation = interpolation;
	}

	public enum GltfInterpolation {
		LINEAR, STEP, CUBICSPLINE;
	}
}
