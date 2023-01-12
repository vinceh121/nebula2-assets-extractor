package me.vinceh121.n2ae.animation;

import java.util.Arrays;
import java.util.regex.Pattern;

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
		return this.startKey;
	}

	public void setStartKey(final int startKey) {
		this.startKey = startKey;
	}

	public int getNumKeys() {
		return this.numKeys;
	}

	public void setNumKeys(final int numKeys) {
		this.numKeys = numKeys;
	}

	public float getKeysPerSec() {
		return this.keysPerSec;
	}

	public void setKeysPerSec(final float keysPerSec) {
		this.keysPerSec = keysPerSec;
	}

	public Interpolation getInterpolation() {
		return this.interpolation;
	}

	public void setInterpolation(final Interpolation interpolation) {
		this.interpolation = interpolation;
	}

	public RepeatType getRepeat() {
		return this.repeat;
	}

	public void setRepeat(final RepeatType repeat) {
		this.repeat = repeat;
	}

	public KeyType getType() {
		return this.type;
	}

	public void setType(final KeyType type) {
		this.type = type;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public float[] getVanillaCurve() {
		return this.vanillaCurve;
	}

	public void setVanillaCurve(final float[] vanillaCurve) {
		this.vanillaCurve = vanillaCurve;
	}

	public short[] getPackedCurve() {
		return this.packedCurve;
	}

	public void setPackedCurve(final short[] packedCurve) {
		this.packedCurve = packedCurve;
	}

	public boolean isRotation() {
		return this.name.contains("_rot_");
	}

	public boolean isTranslation() {
		return this.name.contains("_trans_");
	}

	public String getBoneName() {
		final String nameSplit;
		if (this.isRotation()) {
			nameSplit = "_rot_";
		} else if (this.isTranslation()) {
			nameSplit = "_trans_";
		} else {
			throw new IllegalStateException("Curve neither rotation nor translation");
		}
		final String[] split = this.getName().split(Pattern.quote(nameSplit));
		return split[split.length - 1];
	}

	@Override
	public String toString() {
		return "Curve [startKey=" + this.startKey + ", numKeys=" + this.numKeys + ", keysPerSec=" + this.keysPerSec
				+ ", interpolation=" + this.interpolation + ", repeat=" + this.repeat + ", type=" + this.type + ", name=" + this.name
				+ ", vanillaCurve=" + Arrays.toString(this.vanillaCurve) + ", packedCurve=" + Arrays.toString(this.packedCurve)
				+ "]";
	}
}
