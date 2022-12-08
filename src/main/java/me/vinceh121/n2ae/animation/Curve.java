package me.vinceh121.n2ae.animation;

import java.util.Arrays;

public class Curve {
	private int startKey, numKeys;
	private float keysPerSec;
	private Interpolation interpolation;
	private RepeatType repeat;
	private KeyType type;
	private String name;
	private float[] vanillaCurve;
	private short[] packedCurve;

	public int getStartKey() {
		return startKey;
	}

	public void setStartKey(int startKey) {
		this.startKey = startKey;
	}

	public int getNumKeys() {
		return numKeys;
	}

	public void setNumKeys(int numKeys) {
		this.numKeys = numKeys;
	}

	public float getKeysPerSec() {
		return keysPerSec;
	}

	public void setKeysPerSec(float keysPerSec) {
		this.keysPerSec = keysPerSec;
	}

	public Interpolation getInterpolation() {
		return interpolation;
	}

	public void setInterpolation(Interpolation interpolation) {
		this.interpolation = interpolation;
	}

	public RepeatType getRepeat() {
		return repeat;
	}

	public void setRepeat(RepeatType repeat) {
		this.repeat = repeat;
	}

	public KeyType getType() {
		return type;
	}

	public void setType(KeyType type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float[] getVanillaCurve() {
		return vanillaCurve;
	}

	public void setVanillaCurve(float[] vanillaCurve) {
		this.vanillaCurve = vanillaCurve;
	}

	public short[] getPackedCurve() {
		return packedCurve;
	}

	public void setPackedCurve(short[] packedCurve) {
		this.packedCurve = packedCurve;
	}
	
	public boolean isRotation() {
		return this.name.contains("_rot_");
	}
	
	public boolean isTranslation() {
		return this.name.contains("_trans_");
	}

	@Override
	public String toString() {
		return "Curve [startKey=" + startKey + ", numKeys=" + numKeys + ", keysPerSec=" + keysPerSec
				+ ", interpolation=" + interpolation + ", repeat=" + repeat + ", type=" + type + ", name=" + name
				+ ", vanillaCurve=" + Arrays.toString(vanillaCurve) + ", packedCurve=" + Arrays.toString(packedCurve)
				+ "]";
	}
}
