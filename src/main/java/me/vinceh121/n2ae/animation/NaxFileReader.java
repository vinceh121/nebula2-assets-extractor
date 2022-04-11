package me.vinceh121.n2ae.animation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import me.vinceh121.n2ae.FourccUtils;
import me.vinceh121.n2ae.LEDataInputStream;

public class NaxFileReader {
	public static final String MAGIC_STRING = "NAX0";
	public static final int MAGIC_NUMBER = FourccUtils.fourcc(MAGIC_STRING);
	public static final String CURVE_MAGIC_STRING = "CHDR";
	public static final int CURVE_MAGIC_NUMBER = FourccUtils.fourcc(CURVE_MAGIC_STRING);

	private final LEDataInputStream input;

	private int blockLength, curvesCount;

	public static void main(String[] args) throws IOException {
		NaxFileReader r = new NaxFileReader(new FileInputStream(
				"/home/vincent/wanderer-workspace/wanderer/android/assets/orig/char_john.n/character.nax"));
		r.readHeader();
		for (int i = 0; i < r.getCurvesCount(); i++)
			r.readCurve();
	}

	public NaxFileReader(final InputStream stream) {
		this.input = new LEDataInputStream(stream);
	}

	public void readHeader() throws IOException {
		int magic = this.input.readIntLE();
		if (magic != MAGIC_NUMBER) {
			throw new IOException("Invalid file magic number");
		}

		this.blockLength = this.input.readIntLE();
		this.curvesCount = this.input.readIntLE();
	}

	public void readCurve() throws IOException {
		int magic = this.input.readIntLE();
		if (magic != CURVE_MAGIC_NUMBER) {
			System.out.println(FourccUtils.fourccToString(magic));
			throw new IOException("Invalid curve magic number");
		}

		this.blockLength = this.input.readIntLE(); // why is the header overwritten?
		int startKey = this.input.readIntLE();
		int numKeys = this.input.readIntLE();
		float keysPerSec = this.input.readFloatLE();

		// the version of nebula's code that i have has those as shorts, but
		// Teivaz's extractor seems to work reading them as bytes
		byte ipolType = this.input.readByte();
		byte repType = this.input.readByte();
		byte keyType = this.input.readByte();
		byte padding = this.input.readByte();

		short curveNameLength = this.input.readShortLE();
		String curveName = new String(this.input.readNBytes(curveNameLength));
		System.out.println(curveName);
		// build curve object

		int curveType = this.input.readIntLE(); // either 'CDTV' for VANILLA or 'CDTP' for PACKED
		System.out.println(FourccUtils.fourccToString(curveType));
		this.blockLength = this.input.readIntLE(); // again, why is the header overwritten?
		int dataSize;
		if (curveType == FourccUtils.fourcc("CDTV")) {
			dataSize = numKeys * 4 * 4; // vector4 = float x, y, z, w
		} else {
			dataSize = numKeys * 2 * 4; // ushort x, y, z, w
		}
		byte[] data = this.input.readNBytes(dataSize);
	}

	public int getCurvesCount() {
		return curvesCount;
	}

	public enum Interpolation {
		STEP, LINEAR, QUATERNION;
	}
}
