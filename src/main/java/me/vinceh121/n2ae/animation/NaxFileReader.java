package me.vinceh121.n2ae.animation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import me.vinceh121.n2ae.FourccUtils;
import me.vinceh121.n2ae.LEDataInputStream;

public class NaxFileReader {
	public static final String MAGIC_STRING = "NAX0", CURVE_MAGIC_STRING = "CHDR";
	public static final int MAGIC_NUMBER = FourccUtils.fourcc(NaxFileReader.MAGIC_STRING),
			CURVE_MAGIC_NUMBER = FourccUtils.fourcc(NaxFileReader.CURVE_MAGIC_STRING);

	private final LEDataInputStream input;

	private int blockLength, curvesCount;

	public NaxFileReader(final InputStream stream) {
		this.input = new LEDataInputStream(stream);
	}

	public List<Curve> readAll() throws IOException {
		this.readHeader();
		return this.readCurves();
	}

	public void readHeader() throws IOException {
		final int magic = this.input.readIntLE();
		if (magic != NaxFileReader.MAGIC_NUMBER) {
			throw new IOException("Invalid file magic number");
		}

		this.blockLength = this.input.readIntLE();
		this.curvesCount = this.input.readIntLE();
	}

	public List<Curve> readCurves() throws IOException {
		final List<Curve> curves = new ArrayList<>(this.curvesCount);
		for (int i = 0; i < this.curvesCount; i++) {
			curves.add(this.readCurve());
		}
		return curves;
	}

	public Curve readCurve() throws IOException {
		final int magic = this.input.readIntLE();
		if (magic != NaxFileReader.CURVE_MAGIC_NUMBER) {
			throw new IOException("Invalid curve magic number");
		}

		final Curve c = new Curve();

		this.blockLength = this.input.readIntLE(); // why is the header overwritten?
		final int startKey = this.input.readIntLE();
		c.setStartKey(startKey);
		final int numKeys = this.input.readIntLE();
		c.setNumKeys(numKeys);
		final float keysPerSec = this.input.readFloatLE();
		c.setKeysPerSec(keysPerSec);

		// the version of nebula's code that i have has those as shorts, but
		// Teivaz's extractor seems to work reading them as bytes
		final Interpolation ipolType = Interpolation.values()[this.input.readByte()];
		c.setInterpolation(ipolType);
		final RepeatType repType = RepeatType.values()[this.input.readByte()];
		c.setRepeat(repType);
		final KeyType keyType = KeyType.values()[this.input.readByte()];
		c.setType(keyType);
		/* final byte padding = */this.input.readByte();

		final short curveNameLength = this.input.readShortLE();
		final String curveName = new String(this.input.readNBytes(curveNameLength));
		c.setName(curveName);
		// build curve object

		final KeyType keyTypeMagic = KeyType.get(this.input.readIntLE()); // either 'CDTV' for VANILLA or 'CDTP' for
																			// PACKED
		if (keyType != keyTypeMagic) {
			throw new IllegalStateException(
					"Animation file inconsistency: key type in file header and key type in curve magic differ ("
							+ keyType + " vs " + keyTypeMagic + ")");
		}
		this.blockLength = this.input.readIntLE(); // again, why is the header overwritten?
		int dataSize;
		if (keyTypeMagic == KeyType.VANILLA) {
			dataSize = numKeys * 4 * 4; // vector4 = float x, y, z, w
		} else if (keyTypeMagic == KeyType.PACKED) {
			dataSize = numKeys * 2 * 4; // ushort x, y, z, w
		} else {
			// shouldn't happen
			throw new RuntimeException();
		}

		final byte[] data = this.input.readNBytes(dataSize);

		if (keyTypeMagic == KeyType.VANILLA) {
			final FloatBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
			final float[] vanillaCurve = new float[buf.remaining()];
			buf.get(vanillaCurve);
			c.setVanillaCurve(vanillaCurve);
		} else if (keyTypeMagic == KeyType.PACKED) {
			final ShortBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
			final short[] packedCurve = new short[buf.remaining()];
			buf.get(packedCurve);
			c.setPackedCurve(packedCurve);
		}

		return c;
	}

	public int getCurvesCount() {
		return this.curvesCount;
	}

	// https://github.com/dgiunchi/m-nebula/blob/master/code/inc/anim/nanimcurve.h#L181
	public static void unpackCurve(final short[] packed, final float[] dst) {
		if (packed.length != dst.length) {
			throw new IllegalArgumentException("packed source and unpacked destination must be the same length");
		}

		final float fact = 1f / 32767.5f;

		for (int i = 0; i < packed.length; i++) {
			dst[i] = Short.toUnsignedInt(packed[i]) * fact - 1f;
		}
	}
}
