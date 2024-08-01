package me.vinceh121.n2ae.gltf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import me.vinceh121.n2ae.LEDataOutputStream;
import me.vinceh121.n2ae.animation.Curve;
import me.vinceh121.n2ae.animation.Interpolation;
import me.vinceh121.n2ae.animation.NaxFileReader;
import me.vinceh121.n2ae.gltf.Accessor.Type;
import me.vinceh121.n2ae.gltf.ChannelTarget.TargetPath;
import me.vinceh121.n2ae.gltf.Sampler.GltfInterpolation;
import me.vinceh121.n2ae.model.Vertex;
import me.vinceh121.n2ae.model.VertexType;
import me.vinceh121.n2ae.script.ClassCommandCall;
import me.vinceh121.n2ae.script.ICommandCall;

public class GLTFGenerator {
	private final GLTF gltf = new GLTF();
	private final LEDataOutputStream packedBinary;
	private int bufferSize;

	public GLTFGenerator(final OutputStream packedBinary) {
		this.packedBinary = new LEDataOutputStream(packedBinary);
	}

	public void addCurves(final List<Curve> curves) throws IOException {
		for (final Curve c : curves) {
			this.addCurve(c);
		}
	}

	public void addCurve(final Curve c) throws IOException {
		if (!c.isRotation() && !c.isTranslation()) {
			throw new IllegalArgumentException("Curve (named " + c.getName() + ") is neither rotation or translation");
		}
		final Animation anim = new Animation();
		anim.setName(c.getName());
		this.gltf.getAnimations().add(anim);

		// input buffer and accessor: array of floats for the timing of each key
		// NAX0 already guarantees uniform linear keys, so just write a key every delay
		this.prependPaddingComp(Accessor.FLOAT);
		final BufferView bufInput = new BufferView();
		bufInput.setByteOffset(this.bufferSize);
		bufInput.setByteLength(c.getNumKeys() * 4);
		bufInput.setBuffer(0);
		this.gltf.getBufferViews().add(bufInput);
		for (int i = 0; i < c.getNumKeys(); i++) {
			this.packedBinary.writeFloatLE(1f / c.getKeysPerSec() * i);
		}
		this.bufferSize += bufInput.getByteLength();
		this.checkBufferSize();

		final Accessor accessorInput = new Accessor();
		accessorInput.setBufferView(this.gltf.getBufferViews().indexOf(bufInput));
		accessorInput.setComponentType(Accessor.FLOAT);
		accessorInput.setCount(c.getNumKeys());
		accessorInput.setType(Type.SCALAR);
		accessorInput.setMax(new float[] { 1 / c.getKeysPerSec() * (c.getNumKeys() - 1) });
		accessorInput.setMin(new float[] { 0 });
		this.gltf.getAccessors().add(accessorInput);

		// output buffer and accessor: value of keys; vec3 for translation, quaternions
		// for rotation
		this.prependPaddingComp(Accessor.FLOAT);
		final BufferView bufOutput = new BufferView();
		bufOutput.setByteOffset(this.bufferSize);
		bufOutput.setBuffer(0);
		if (c.isRotation()) {
			bufOutput.setByteLength(c.getNumKeys() * 4 * 4); // 4 32-bit float
		} else if (c.isTranslation()) {
			bufOutput.setByteLength(c.getNumKeys() * 3 * 4); // 3 32-bit floats
		}
		this.gltf.getBufferViews().add(bufOutput);

		final Accessor accessorOutput = new Accessor();
		accessorOutput.setBufferView(this.gltf.getBufferViews().indexOf(bufOutput));
		accessorOutput.setCount(c.getNumKeys());
		if (c.isRotation()) {
			accessorOutput.setComponentType(Accessor.FLOAT);
			accessorOutput.setType(Type.VEC4);
		} else if (c.isTranslation()) {
			accessorOutput.setComponentType(Accessor.FLOAT);
			accessorOutput.setType(Type.VEC3);
		}
		this.gltf.getAccessors().add(accessorOutput);

		this.bufferSize += bufOutput.getByteLength();

		if (c.isRotation()) {
			for (int i = 0; i < c.getPackedCurve().length; i += 4) {
				final short[] quatPack = new short[4];
				System.arraycopy(c.getPackedCurve(), i, quatPack, 0, 4);
				final float[] quat = new float[4];
				NaxFileReader.unpackCurve(quatPack, quat);

				// conjugate quaternion
				quat[0] = -quat[0];
				quat[1] = -quat[1];
				quat[2] = -quat[2];

				for (final float f : quat) {
					this.packedBinary.writeFloatLE(f);
				}
			}
		} else if (c.isTranslation()) {
			// NAX0 stores translations as VEC4 with 4th component being always 0
			final float[] v = c.getVanillaCurve();
			for (int i = 0; i < c.getVanillaCurve().length; i += 4) {
				this.packedBinary.writeFloatLE(v[i + 0]);
				this.packedBinary.writeFloatLE(v[i + 1]);
				this.packedBinary.writeFloatLE(v[i + 2]);
			}
		}
		this.checkBufferSize();

		// now that buffers are ready, make the animation sampler
		final Sampler sampler = new Sampler();
		sampler.setInput(this.gltf.getAccessors().indexOf(accessorInput));
		sampler.setOutput(this.gltf.getAccessors().indexOf(accessorOutput));
		if (c.isRotation() || c.isTranslation() && c.getInterpolation() == Interpolation.LINEAR) {
			sampler.setInterpolation(GltfInterpolation.LINEAR);
		} else if (c.isTranslation() && c.getInterpolation() == Interpolation.STEP) {
			sampler.setInterpolation(GltfInterpolation.STEP);
		}
		anim.getSamplers().add(sampler);

		// then the channel
		final Channel chan = new Channel();
		chan.setSampler(anim.getSamplers().indexOf(sampler));
		final ChannelTarget target = new ChannelTarget();
		target.setNode(this.getNodeIdxByName(c.getBoneName()));
		if (c.isRotation()) {
			target.setPath(TargetPath.rotation);
		} else if (c.isTranslation()) {
			target.setPath(TargetPath.translation);
		}
		chan.setTarget(target);
		anim.getChannels().add(chan);
	}

	private int getNodeIdxByName(final String name) {
		for (int i = 0; i < this.gltf.getNodes().size(); i++) {
			final Node node = this.gltf.getNodes().get(i);
			if (name.equals(node.getName())) {
				return i;
			}
		}
		throw new IllegalStateException();
	}

	public Node addMesh(final String name, final List<VertexType> types, final List<Vertex> vertices,
			final List<int[]> triangles) throws IOException {
		return this.addMesh(name, types, vertices, triangles, -1);
	}

	public Node addMesh(final String name, final List<VertexType> types, final List<Vertex> vertices,
			final List<int[]> triangles, final int skinIdx) throws IOException {
		final Mesh mesh = new Mesh();
		mesh.setName(name);
		final Primitive prim = new Primitive();
		mesh.getPrimitives().add(prim);

		float[] maxCoord = vertices.get(0).getCoord();
		float[] minCoord = vertices.get(0).getCoord();

		int positionCount = 0;
		int normalCount = 0;
		int jointWeightsCount = 0;
		int uv0Count = 0;
		int uv1Count = 0;
		int uv2Count = 0;
		int uv3Count = 0;
		int colorCount = 0;

		final LEDataOutputStream positionBuf = new LEDataOutputStream(new ByteArrayOutputStream());
		final LEDataOutputStream normalBuf = new LEDataOutputStream(new ByteArrayOutputStream());
		final LEDataOutputStream jointBuf = new LEDataOutputStream(new ByteArrayOutputStream());
		final LEDataOutputStream weightsBuf = new LEDataOutputStream(new ByteArrayOutputStream());
		final List<LEDataOutputStream> uvBuf = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			uvBuf.add(new LEDataOutputStream(new ByteArrayOutputStream()));
		}
		final LEDataOutputStream colorBuf = new LEDataOutputStream(new ByteArrayOutputStream());

		for (final Vertex v : vertices) {
			if (types.contains(VertexType.COORD)) {
				positionCount++;
				minCoord = this.min(v.getCoord(), minCoord);
				maxCoord = this.max(v.getCoord(), maxCoord);
				for (final float f : v.getCoord()) {
					positionBuf.writeFloatLE(f);
				}
			}

			if (types.contains(VertexType.NORM)) {
				normalCount++;
				for (final float f : v.getNormal()) {
					normalBuf.writeFloatLE(f);
				}
			}

			if (types.contains(VertexType.UV0)) {
				uv0Count++;
				for (final float f : v.getUv().get(0)) {
					uvBuf.get(0).writeFloatLE(f);
				}
			}
			if (types.contains(VertexType.UV1)) {
				uv1Count++;
				for (final float f : v.getUv().get(1)) {
					uvBuf.get(1).writeFloatLE(f);
				}
			}
			if (types.contains(VertexType.UV2)) {
				uv2Count++;
				for (final float f : v.getUv().get(2)) {
					uvBuf.get(2).writeFloatLE(f);
				}
			}
			if (types.contains(VertexType.UV3)) {
				uv3Count++;
				for (final float f : v.getUv().get(3)) {
					uvBuf.get(3).writeFloatLE(f);
				}
			}

			if (types.contains(VertexType.JOINTS_WEIGHTS)) {
				jointWeightsCount++;
				final float[] w = this.normalize(v.getWeights());
				final short[] j = v.getJointIndices();

				for (int i = 0; i < 4; i++) {
					// 1. NVX1 uses -1 for unused indices, glTF uses 0
					// 2. useless joints that don't have weights should use joint 0
					if (j[i] == -1 || w[i] == 0) {
						jointBuf.writeUnsignedShortLE(0);
						weightsBuf.writeFloatLE(0);
					} else {
						jointBuf.writeUnsignedShortLE(j[i]);
						weightsBuf.writeFloatLE(w[i]);
					}
				}
			}

			if (types.contains(VertexType.RGBA)) {
				colorCount++;
				final int bgra = v.getColor();

				final float normB = (bgra & 0xFF000000) >>> 24;
				final float normG = (bgra & 0x00FF0000) >>> 16;
				final float normR = (bgra & 0x0000FF00) >>> 8;
				final float normA = bgra & 0x000000FF;

				final float b = normB / 255;
				final float g = normG / 255;
				final float r = normR / 255;
				final float a = normA / 255;

				colorBuf.writeFloatLE(r);
				colorBuf.writeFloatLE(g);
				colorBuf.writeFloatLE(b);
				colorBuf.writeFloatLE(a);
			}
		}

		// write arrays to packed stream and build accessors
		final Map<String, Integer> attributes = prim.getAttributes();

		if (types.contains(VertexType.COORD)) {
			final byte[] buf = ((ByteArrayOutputStream) positionBuf.getUnderlyingOutputStream()).toByteArray();

			this.prependPaddingComp(Accessor.FLOAT);
			final BufferView view = new BufferView();
			view.setBuffer(0);
			view.setByteOffset(this.bufferSize);
			view.setByteLength(buf.length);
			this.gltf.getBufferViews().add(view);

			final Accessor accessor = new Accessor();
			accessor.setBufferView(this.gltf.getBufferViews().indexOf(view));
			accessor.setType(Type.VEC3);
			accessor.setComponentType(Accessor.FLOAT);
			accessor.setCount(positionCount);
			accessor.setMax(maxCoord);
			accessor.setMin(minCoord);
			this.gltf.getAccessors().add(accessor);
			attributes.put("POSITION", this.gltf.getAccessors().indexOf(accessor));

			this.bufferSize += buf.length;
			this.packedBinary.write(buf);
			this.checkBufferSize();
		}

		if (types.contains(VertexType.NORM)) {
			final byte[] buf = ((ByteArrayOutputStream) normalBuf.getUnderlyingOutputStream()).toByteArray();

			this.prependPaddingComp(Accessor.FLOAT);
			final BufferView view = new BufferView();
			view.setBuffer(0);
			view.setByteOffset(this.bufferSize);
			view.setByteLength(buf.length);
			this.gltf.getBufferViews().add(view);

			final Accessor accessor = new Accessor();
			accessor.setBufferView(this.gltf.getBufferViews().indexOf(view));
			accessor.setType(Type.VEC3);
			accessor.setComponentType(Accessor.FLOAT);
			accessor.setCount(normalCount);
			this.gltf.getAccessors().add(accessor);
			attributes.put("NORMAL", this.gltf.getAccessors().indexOf(accessor));

			this.bufferSize += buf.length;
			this.packedBinary.write(buf);
			this.checkBufferSize();
		}

		if (types.contains(VertexType.UV0)) {
			this.writeUV(uvBuf.get(0), uv0Count, 0, attributes);
		}
		if (types.contains(VertexType.UV1)) {
			this.writeUV(uvBuf.get(1), uv1Count, 1, attributes);
		}
		if (types.contains(VertexType.UV2)) {
			this.writeUV(uvBuf.get(2), uv2Count, 2, attributes);
		}
		if (types.contains(VertexType.UV3)) {
			this.writeUV(uvBuf.get(3), uv3Count, 3, attributes);
		}

		if (types.contains(VertexType.JOINTS_WEIGHTS)) {
			final byte[] bufJoints = ((ByteArrayOutputStream) jointBuf.getUnderlyingOutputStream()).toByteArray();

			this.prependPaddingComp(Accessor.UNSIGNED_SHORT);
			BufferView view = new BufferView();
			view.setBuffer(0);
			view.setByteOffset(this.bufferSize);
			view.setByteLength(bufJoints.length);
			this.gltf.getBufferViews().add(view);

			Accessor accessor = new Accessor();
			accessor.setBufferView(this.gltf.getBufferViews().indexOf(view));
			accessor.setType(Type.VEC4);
			accessor.setComponentType(Accessor.UNSIGNED_SHORT);
			accessor.setCount(jointWeightsCount);
			this.gltf.getAccessors().add(accessor);
			attributes.put("JOINTS_0", this.gltf.getAccessors().indexOf(accessor));

			this.bufferSize += bufJoints.length;
			this.packedBinary.write(bufJoints);
			this.checkBufferSize();

			final byte[] bufWeights = ((ByteArrayOutputStream) weightsBuf.getUnderlyingOutputStream()).toByteArray();

			this.prependPaddingComp(Accessor.FLOAT);
			view = new BufferView();
			view.setBuffer(0);
			view.setByteOffset(this.bufferSize);
			view.setByteLength(bufWeights.length);
			this.gltf.getBufferViews().add(view);

			accessor = new Accessor();
			accessor.setBufferView(this.gltf.getBufferViews().indexOf(view));
			accessor.setType(Type.VEC4);
			accessor.setComponentType(Accessor.FLOAT);
			accessor.setCount(jointWeightsCount);
			this.gltf.getAccessors().add(accessor);
			attributes.put("WEIGHTS_0", this.gltf.getAccessors().indexOf(accessor));

			this.bufferSize += bufWeights.length;
			this.packedBinary.write(bufWeights);
			this.checkBufferSize();
		}

		if (types.contains(VertexType.RGBA)) {
			final byte[] buf = ((ByteArrayOutputStream) colorBuf.getUnderlyingOutputStream()).toByteArray();

			this.prependPaddingComp(Accessor.FLOAT);
			final BufferView view = new BufferView();
			view.setBuffer(0);
			view.setByteOffset(this.bufferSize);
			view.setByteLength(buf.length);
			this.gltf.getBufferViews().add(view);

			final Accessor accessor = new Accessor();
			accessor.setBufferView(this.gltf.getBufferViews().indexOf(view));
			accessor.setType(Type.VEC4);
			accessor.setComponentType(Accessor.FLOAT);
			accessor.setCount(colorCount);
			this.gltf.getAccessors().add(accessor);
			attributes.put("COLOR_0", this.gltf.getAccessors().indexOf(accessor));

			this.bufferSize += buf.length;
			this.packedBinary.write(buf);
			this.checkBufferSize();
		}

		// indices
		{
			int indicesCount = 0;
			final ByteArrayOutputStream indicesBuf = new ByteArrayOutputStream();
			final LEDataOutputStream indicesOut = new LEDataOutputStream(indicesBuf);

			for (final int[] ints : triangles) {
				for (final int i : ints) {
					indicesCount++;
					indicesOut.writeUnsignedShortLE(i);
				}
			}

			final byte[] buf = indicesBuf.toByteArray();

			this.prependPaddingComp(Accessor.FLOAT);
			final BufferView view = new BufferView();
			view.setBuffer(0);
			view.setByteOffset(this.bufferSize);
			view.setByteLength(buf.length);
			this.gltf.getBufferViews().add(view);

			final Accessor accessor = new Accessor();
			accessor.setBufferView(this.gltf.getBufferViews().indexOf(view));
			accessor.setType(Type.SCALAR);
			accessor.setComponentType(Accessor.UNSIGNED_SHORT);
			accessor.setCount(indicesCount);
			this.gltf.getAccessors().add(accessor);
			prim.setIndices(this.gltf.getAccessors().indexOf(accessor));

			this.bufferSize += buf.length;
			this.packedBinary.write(buf);
			this.checkBufferSize();
		}

		this.gltf.getMeshes().add(mesh);

		final Node node = new Node();
		node.setMesh(this.gltf.getMeshes().indexOf(mesh));
		node.setName(name);
		node.setSkin(skinIdx);
		this.gltf.getNodes().add(node);

		return node;
	}

	private void writeUV(final LEDataOutputStream out, final int count, final int uvIdx, final Map<String, Integer> attributes)
			throws IOException {
		final byte[] buf = ((ByteArrayOutputStream) out.getUnderlyingOutputStream()).toByteArray();

		this.prependPaddingComp(Accessor.FLOAT);
		final BufferView view = new BufferView();
		view.setBuffer(0);
		view.setByteOffset(this.bufferSize);
		view.setByteLength(buf.length);
		this.gltf.getBufferViews().add(view);

		final Accessor accessor = new Accessor();
		accessor.setBufferView(this.gltf.getBufferViews().indexOf(view));
		accessor.setType(Type.VEC2);
		accessor.setComponentType(Accessor.FLOAT);
		accessor.setCount(count);
		this.gltf.getAccessors().add(accessor);
		attributes.put("TEXCOORD_" + uvIdx, this.gltf.getAccessors().indexOf(accessor));

		this.bufferSize += buf.length;
		this.packedBinary.write(buf);
		this.checkBufferSize();
	}

	private float[] normalize(final float[] a) {
		float sum = 0;
		for (final float f : a) {
			sum += f;
		}

		if (sum == 1) {
			return a;
		}

		final float[] out = new float[a.length];
		for (int i = 0; i < a.length; i++) {
			out[i] = a[i] * 1 / sum;
		}

		return out;
	}

	private float[] max(final float[] a, final float[] b) {
		if (a.length != b.length) {
			throw new IllegalArgumentException("a.length != b.length");
		}
		final float[] m = new float[a.length];
		for (int i = 0; i < a.length; i++) {
			m[i] = Math.max(a[i], b[i]);
		}
		return m;
	}

	private float[] min(final float[] a, final float[] b) {
		if (a.length != b.length) {
			throw new IllegalArgumentException("a.length != b.length");
		}
		final float[] m = new float[a.length];
		for (int i = 0; i < a.length; i++) {
			m[i] = Math.min(a[i], b[i]);
		}
		return m;
	}

	public void buildBasicScene(final String name) {
		this.buildBasicScene(name, -1);
	}

	public void buildBasicScene(final String name, final int rootNode) {
		final Scene sc = new Scene();
		sc.setName(name);
		if (rootNode != -1) {
			sc.getNodes().add(0);
			sc.getNodes().add(rootNode);
		}
		this.gltf.getScenes().add(sc);
	}

	public void addBones(final List<ICommandCall> calls) throws IOException {
		final Skin skin = new Skin();
		skin.setName("rig");
		skin.setSkeleton(0);

		final List<Quaternion> rots = new Vector<>();
		final List<Matrix4> invBindMats = new Vector<>();

		for (final ICommandCall cmd : calls) {
			if (!(cmd instanceof final ClassCommandCall cmdCls)) {
				continue;
			}
			if ("addjoint".equals(cmdCls.getPrototype().getName())) {
				invBindMats.add(null);
				rots.add(null);
			}
		}

		for (final ICommandCall cmd : calls) {
			if (!(cmd instanceof final ClassCommandCall cmdCls)) {
				continue;
			}
			if ("addjoint".equals(cmdCls.getPrototype().getName())) {
				final Node bone = this.buildBones(cmdCls);
				this.gltf.getNodes().add(bone);

				final int idx = (int) cmdCls.getArguments()[0];
				final int parentIdx = (int) cmdCls.getArguments()[2];

				final Quaternion rot = new Quaternion(bone.getRotation()[0],
						bone.getRotation()[1],
						bone.getRotation()[2],
						bone.getRotation()[3]);

				// calculate inverse bind matrix
				final Matrix4 mat = new Matrix4(new Vector3(bone.getTranslation()), rot, new Vector3(1, 1, 1));

				if (parentIdx != -1) {
					this.mult_simple(mat, invBindMats.get(parentIdx));
				}

				if (invBindMats.get(idx) != null) {
					throw new IllegalStateException("Inverse bind matrix " + idx + " is already assigned");
				}
				invBindMats.set(idx, mat.cpy());
			}
		}

		// reverse tree referencing
		// Nebula: child -> parent
		// glTF: parent -> child
		for (final ICommandCall cmd : calls) {
			if (!(cmd instanceof ClassCommandCall)
					|| !"addjoint".equals(((ClassCommandCall) cmd).getPrototype().getName())) {
				continue;
			}

			final Object[] args = ((ClassCommandCall) cmd).getArguments();
			final int idx = (int) args[0];
			final int parent = (int) args[2];

			skin.getJoints().add(idx);
			if (parent == -1) {
				continue;
			}
			this.gltf.getNodes().get(parent).getChildren().add(idx);
		}

		this.gltf.getSkins().add(skin);

		// build inverse bind matrices buffer view
		final LEDataOutputStream inv = new LEDataOutputStream(new ByteArrayOutputStream());
		for (final Matrix4 mat : invBindMats) {
			mat.inv();

			for (int j = 0; j < 4 * 4; j++) {
				inv.writeFloatLE(mat.val[j]);
			}
		}

		final byte[] invBuf = ((ByteArrayOutputStream) inv.getUnderlyingOutputStream()).toByteArray();

		this.prependPaddingComp(Accessor.FLOAT);
		final BufferView invView = new BufferView();
		invView.setBuffer(0);
		invView.setByteOffset(this.bufferSize);
		invView.setByteLength(invBuf.length);
		this.gltf.getBufferViews().add(invView);

		final Accessor invAcc = new Accessor();
		invAcc.setBufferView(this.gltf.getBufferViews().indexOf(invView));
		invAcc.setComponentType(Accessor.FLOAT);
		invAcc.setCount(skin.getJoints().size());
		invAcc.setType(Type.MAT4);
		this.gltf.getAccessors().add(invAcc);

		this.packedBinary.write(invBuf);
		this.bufferSize += invView.getByteLength();
		this.checkBufferSize();

		skin.setInverseBindMatrices(this.gltf.getAccessors().indexOf(invAcc));
	}

	/**
	 * https://github.com/dgiunchi/m-nebula/blob/master/code/inc/mathlib/matrix.h#L836
	 *
	 * @param m  Matrix to be multiplied, results stored in place
	 * @param m1 Matrix to multiply with
	 */
	private void mult_simple(final Matrix4 m, final Matrix4 m1) {
		// It seems this assertion is never true. Which was the source of this bug that
		// took me a while to fix.
		// Using a normal matrix multiplication instead of this one, results in an
		// incorrect bind pose for all Nomads main characters, except Susie.
		// Goliath only has the 3 right breast bones broken, and John is all mangled.
		// Is this algorithm just flawed and Nebula made the flaw consistent through the
		// engine?
//		assert isAllEqual(1E-4f, 0f, m.val[M03], m.val[M13], m.val[M23], m1.val[M03], m1.val[M13], m1.val[M23])
//				&& isAllEqual(1E-4f, 1f, m.val[M33], m1.val[M33]);

		for (int i = 0; i < 4; i++) {
			final float mi0 = m.val[0 + 4 * i];
			final float mi1 = m.val[1 + 4 * i];
			final float mi2 = m.val[2 + 4 * i];
			m.val[0 + 4 * i] = mi0 * m1.val[Matrix4.M00] + mi1 * m1.val[Matrix4.M01] + mi2 * m1.val[Matrix4.M02];
			m.val[1 + 4 * i] = mi0 * m1.val[Matrix4.M10] + mi1 * m1.val[Matrix4.M11] + mi2 * m1.val[Matrix4.M12];
			m.val[2 + 4 * i] = mi0 * m1.val[Matrix4.M20] + mi1 * m1.val[Matrix4.M21] + mi2 * m1.val[Matrix4.M22];
		}
		m.val[Matrix4.M03] += m1.val[Matrix4.M03];
		m.val[Matrix4.M13] += m1.val[Matrix4.M13];
		m.val[Matrix4.M23] += m1.val[Matrix4.M23];
		m.val[Matrix4.M30] = 0;
		m.val[Matrix4.M31] = 0;
		m.val[Matrix4.M32] = 0;
		m.val[Matrix4.M33] = 1;
	}

	private boolean isAllEqual(final float tolerance, final float... vals) {
		for (int i = 0; i < vals.length - 1; i++) {
			if (!MathUtils.isEqual(vals[i], vals[i + 1], tolerance)) {
				return false;
			}
		}
		return true;
	}

	public Node buildBones(final ClassCommandCall cmd) {
		final Object[] args = cmd.getArguments();
		final Node bone = new Node();
		bone.setName((String) args[1]);
		bone.setTranslation(new float[] { (float) args[3], (float) args[4], (float) args[5] });
		bone.setRotation(new float[] { (float) args[6], (float) args[7], (float) args[8], (float) args[9] });
		bone.setScale(new float[] { 1, 1, 1 });
		return bone;
	}

	/**
	 * Call only when everything relying on a buffer is done
	 */
	public void buildBuffer(final String uri) {
		final Buffer buf = new Buffer();
		buf.setUri(uri);
		buf.setByteLength(this.bufferSize);
		this.gltf.getBuffers().add(buf);
	}

	public void updateRootNodes() {
		final Set<Integer> noParent = new HashSet<>();

		for (int i = 0; i < this.gltf.getNodes().size(); i++) {
			noParent.add(i);
		}

		for (final Node n : this.gltf.getNodes()) {
			for (final Integer c : n.getChildren()) {
				noParent.remove(c);
			}
		}

		this.gltf.getScenes().get(0).getNodes().addAll(noParent);
	}

	private void prependPaddingComp(final int compType) throws IOException {
		this.prependPadding(Accessor.getComponentTypeLength(compType));
	}

	/**
	 * glTF 2.0 requires bufferViews and accessors byte offsets to be multiples of
	 * their component type. C.f. 3.6.2.4. Data Alignment This function prepends the
	 * necessary padding to make sure this condition is met.
	 *
	 * @param remainderSize size in bytes of the componentType that's going to be
	 *                      written
	 * @throws IOException
	 */
	private void prependPadding(final int remainderSize) throws IOException {
		final int remaining = this.bufferSize % remainderSize;
		for (int i = 0; i < remaining; i++) {
			this.packedBinary.write(0);
		}
		this.bufferSize += remaining;
		this.checkBufferSize();
	}

	private void checkBufferSize() {
		if (this.bufferSize != this.packedBinary.getWrittenBytes()) {
			throw new IllegalStateException("Expected buffer size and real buffer size are off");
		}

		for (int i = 0; i < this.gltf.getAccessors().size(); i++) {
			final Accessor a = this.gltf.getAccessors().get(i);
			final int compLength = Accessor.getComponentTypeLength(a.getComponentType());

			final BufferView view = this.gltf.getBufferViews().get(a.getBufferView());
			if (view.getByteOffset() % compLength != 0) {
				throw new IllegalStateException("Accessor's (" + i + ") total byteOffset " + view.getByteOffset()
						+ " isn't a multiple of componentType length " + compLength);
			}
		}
	}

	public GLTF getGltf() {
		return this.gltf;
	}
}
