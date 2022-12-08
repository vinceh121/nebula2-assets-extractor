package me.vinceh121.n2ae.gltf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import me.vinceh121.n2ae.LEDataOutputStream;
import me.vinceh121.n2ae.animation.Curve;
import me.vinceh121.n2ae.animation.Interpolation;
import me.vinceh121.n2ae.animation.NaxFileReader;
import me.vinceh121.n2ae.gltf.Accessor.Type;
import me.vinceh121.n2ae.gltf.ChannelTarget.TargetPath;
import me.vinceh121.n2ae.gltf.Sampler.GltfInterpolation;
import me.vinceh121.n2ae.model.NvxFileReader;
import me.vinceh121.n2ae.model.Vertex;
import me.vinceh121.n2ae.model.VertexType;
import me.vinceh121.n2ae.script.ClassCommandCall;
import me.vinceh121.n2ae.script.ICommandCall;
import me.vinceh121.n2ae.script.NOBClazz;
import me.vinceh121.n2ae.script.tcl.TCLParser;

public class GLTFGenerator {
	private final GLTF gltf = new GLTF();
	private final LEDataOutputStream packedBinary;
	private int bufferSize;

	public static void main(String[] args) throws StreamReadException, DatabindException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		FileOutputStream out = new FileOutputStream("/tmp/owo.bin");
		GLTFGenerator gen = new GLTFGenerator(out);

		TCLParser parser = new TCLParser();
		parser.setClassModel(mapper.readValue(new File("./project-nomads.classmodel.json"),
				new TypeReference<Map<String, NOBClazz>>() {
				}));
		parser.read(new FileInputStream(
				"/home/vincent/wanderer-workspace/wanderer/android/assets/orig/char_goliath.n/_main.tcl"));

		gen.addBones(parser.getCalls());
		gen.buildBasicScene("owo", 0);

		NvxFileReader modelReader = new NvxFileReader(new FileInputStream(
				"/home/vincent/wanderer-workspace/wanderer/android/assets/orig/char_goliath.n/skin.nvx"));
		modelReader.readAll();

		gen.addMesh("skin", modelReader.getTypes(), modelReader.getVertices(), modelReader.getTriangles(), 0);

		NaxFileReader animReader = new NaxFileReader(new FileInputStream(
				"/home/vincent/wanderer-workspace/wanderer/android/assets/orig/char_goliath.n/character.nax"));

		List<Curve> curves = animReader.readAll();
		curves.removeIf(c -> c.isTranslation() || !c.getName().startsWith("landen"));
		gen.addCurves(curves);

		gen.buildBuffer("owo.bin");
		out.flush();
		out.close();

		mapper.writerWithDefaultPrettyPrinter().writeValue(new File("/tmp/owo.gltf"), gen.getGltf());
		mapper.writerWithDefaultPrettyPrinter().writeValue(new File("/tmp/owo.json"), gen.getGltf());
	}

	public GLTFGenerator(OutputStream packedBinary) {
		this.packedBinary = new LEDataOutputStream(packedBinary);
	}

	public void addCurves(List<Curve> curves) throws IOException {
		for (Curve c : curves) {
			this.addCurve(c);
		}
	}

	public void addCurve(Curve c) throws IOException {
		if (!c.isRotation() && !c.isTranslation()) {
			throw new IllegalArgumentException("Curve (named " + c.getName() + ") is neither rotation or translation");
		}
		Animation anim = new Animation();
		anim.setName(c.getName());
		this.gltf.getAnimations().add(anim);

		// input buffer and accessor: array of floats for the timing of each key
		// NAX0 already guarantees uniform linear keys, so just write a key every delay
		BufferView bufInput = new BufferView();
		bufInput.setByteOffset(this.bufferSize);
		bufInput.setByteLength(c.getNumKeys() * 4);
		bufInput.setBuffer(0);
		this.gltf.getBufferViews().add(bufInput);
		for (int i = 0; i < c.getNumKeys(); i++) {
			this.packedBinary.writeFloatLE(c.getKeysPerSec() * i);
		}
		this.bufferSize += bufInput.getByteLength();

		Accessor accessorInput = new Accessor();
		accessorInput.setBufferView(this.gltf.getBufferViews().indexOf(bufInput));
		accessorInput.setComponentType(Accessor.FLOAT);
		accessorInput.setCount(c.getNumKeys());
		accessorInput.setType(Type.SCALAR);
		accessorInput.setMax(new float[] { c.getKeysPerSec() * (c.getNumKeys() - 1) });
		accessorInput.setMin(new float[] { 0 });
		this.gltf.getAccessors().add(accessorInput);

		// output buffer and accessor: value of keys; vec3 for translation, quaternions
		// for rotation
		BufferView bufOutput = new BufferView();
		bufOutput.setByteOffset(this.bufferSize);
		bufOutput.setBuffer(0);
		if (c.isRotation()) {
			bufOutput.setByteLength(c.getNumKeys() * 4 * 4); // 4 32-bit float
		} else if (c.isTranslation()) {
			bufOutput.setByteLength(c.getNumKeys() * 3 * 4); // 3 32-bit floats
		}
		this.gltf.getBufferViews().add(bufOutput);

		Accessor accessorOutput = new Accessor();
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
				short[] quat = new short[4];
				System.arraycopy(c.getPackedCurve(), i, quat, 0, 4);
				float[] quatNorm = this.normalizeShort(quat);
				for (float comp : quatNorm) {
					this.packedBinary.writeFloatLE(comp);
				}
			}
		} else if (c.isTranslation()) {
			// NAX0 stores translations as VEC4 with 4th component being always 0
			final float[] v = c.getVanillaCurve();
			for (int i = 0; i < c.getVanillaCurve().length; i += 4) {
				this.packedBinary.writeFloatLE(v[0]);
				this.packedBinary.writeFloatLE(v[1]);
				this.packedBinary.writeFloatLE(v[2]);
			}
		}

		// now that buffers are ready, make the animation sampler
		Sampler sampler = new Sampler();
		sampler.setInput(this.gltf.getAccessors().indexOf(accessorInput));
		sampler.setOutput(this.gltf.getAccessors().indexOf(accessorOutput));
		if (c.isRotation()) {
			sampler.setInterpolation(GltfInterpolation.LINEAR);
		} else if (c.isTranslation() && c.getInterpolation() == Interpolation.LINEAR) {
			sampler.setInterpolation(GltfInterpolation.LINEAR);
		} else if (c.isTranslation() && c.getInterpolation() == Interpolation.STEP) {
			sampler.setInterpolation(GltfInterpolation.STEP);
		}
		anim.getSamplers().add(sampler);

		// then the channel
		Channel chan = new Channel();
		chan.setSampler(anim.getSamplers().indexOf(sampler));
		ChannelTarget target = new ChannelTarget();
		target.setNode(this.getNodeIdxByName(c.getBoneName()));
		if (c.isRotation()) {
			target.setPath(TargetPath.rotation);
		} else if (c.isTranslation()) {
			target.setPath(TargetPath.translation);
		}
		chan.setTarget(target);
		anim.getChannels().add(chan);
	}

	private float[] normalizeShort(short[] a) {
		final float shortMax = (int) (Math.pow(2, 16) - 1);
		int sum = 0;
		for (short s : a) {
			sum += s;
		}

		float[] norm = new float[a.length];
		for (int i = 0; i < a.length; i++) {
			norm[i] = (float) (a[i] * shortMax / sum) / shortMax;
		}

		return norm;
	}

	private int getNodeIdxByName(String name) {
		for (int i = 0; i < this.gltf.getNodes().size(); i++) {
			Node node = this.gltf.getNodes().get(i);
			if (name.equals(node.getName())) {
				return i;
			}
		}
		throw new IllegalStateException();
	}

	public void addMesh(String name, List<VertexType> types, List<Vertex> vertices, List<int[]> triangles, int skinIdx)
			throws IOException {
		Mesh mesh = new Mesh();
		mesh.setName(name);
		Primitive prim = new Primitive();
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

		LEDataOutputStream positionBuf = new LEDataOutputStream(new ByteArrayOutputStream());
		LEDataOutputStream normalBuf = new LEDataOutputStream(new ByteArrayOutputStream());
		LEDataOutputStream jointBuf = new LEDataOutputStream(new ByteArrayOutputStream());
		LEDataOutputStream weightsBuf = new LEDataOutputStream(new ByteArrayOutputStream());
		List<LEDataOutputStream> uvBuf = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			uvBuf.add(new LEDataOutputStream(new ByteArrayOutputStream()));
		}

		for (Vertex v : vertices) {
			if (types.contains(VertexType.COORD)) {
				positionCount++;
				minCoord = min(v.getCoord(), minCoord);
				maxCoord = max(v.getCoord(), maxCoord);
				for (float f : v.getCoord()) {
					positionBuf.writeFloatLE(f);
				}
			}

			if (types.contains(VertexType.NORM)) {
				normalCount++;
				for (float f : v.getNormal()) {
					normalBuf.writeFloatLE(f);
				}
			}

			if (types.contains(VertexType.UV0)) {
				uv0Count++;
				for (float f : v.getUv().get(0)) {
					uvBuf.get(0).writeFloatLE(f);
				}
			}
			if (types.contains(VertexType.UV1)) {
				uv1Count++;
				for (float f : v.getUv().get(1)) {
					uvBuf.get(1).writeFloatLE(f);
				}
				// XXX won't do those now, and probably never will
				throw new IllegalArgumentException("UV1 not supported");
			}
			if (types.contains(VertexType.UV2)) {
				uv2Count++;
				for (float f : v.getUv().get(2)) {
					uvBuf.get(2).writeFloatLE(f);
				}
				throw new IllegalArgumentException("UV2 not supported");
			}
			if (types.contains(VertexType.UV3)) {
				uv3Count++;
				for (float f : v.getUv().get(3)) {
					uvBuf.get(3).writeFloatLE(f);
				}
				throw new IllegalArgumentException("UV3 not supported");
			}

			if (types.contains(VertexType.JOINTS_WEIGHTS)) {
				jointWeightsCount++;
				float[] w = normalize(v.getWeights());
				for (int i = 0; i < 4; i++) {
					// useless joints that don't have weights should use joint 0
					if (w[i] == 0) {
						jointBuf.writeUnsignedShortLE(0);
					} else {
						// NVX1 uses -1 for unused indices, glTF uses 0
						jointBuf.writeUnsignedShortLE(v.getJointIndices()[i] == -1 ? 0 : v.getJointIndices()[i]);
					}
					weightsBuf.writeFloatLE(w[i]);
				}
			}
		}

		// write arrays to packed stream and build accessors
		Map<String, Integer> attributes = prim.getAttributes();

		if (types.contains(VertexType.COORD)) {
			byte[] buf = ((ByteArrayOutputStream) positionBuf.getUnderlyingOutputStream()).toByteArray();

			BufferView view = new BufferView();
			view.setBuffer(0);
			view.setByteOffset(this.bufferSize);
			view.setByteLength(buf.length);
			this.gltf.getBufferViews().add(view);

			Accessor accessor = new Accessor();
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
		}

		if (types.contains(VertexType.NORM)) {
			byte[] buf = ((ByteArrayOutputStream) normalBuf.getUnderlyingOutputStream()).toByteArray();

			BufferView view = new BufferView();
			view.setBuffer(0);
			view.setByteOffset(this.bufferSize);
			view.setByteLength(buf.length);
			this.gltf.getBufferViews().add(view);

			Accessor accessor = new Accessor();
			accessor.setBufferView(this.gltf.getBufferViews().indexOf(view));
			accessor.setType(Type.VEC3);
			accessor.setComponentType(Accessor.FLOAT);
			accessor.setCount(normalCount);
			this.gltf.getAccessors().add(accessor);
			attributes.put("NORMAL", this.gltf.getAccessors().indexOf(accessor));

			this.bufferSize += buf.length;
			this.packedBinary.write(buf);
		}

		if (types.contains(VertexType.UV0)) {
			byte[] buf = ((ByteArrayOutputStream) uvBuf.get(0).getUnderlyingOutputStream()).toByteArray();

			BufferView view = new BufferView();
			view.setBuffer(0);
			view.setByteOffset(this.bufferSize);
			view.setByteLength(buf.length);
			this.gltf.getBufferViews().add(view);

			Accessor accessor = new Accessor();
			accessor.setBufferView(this.gltf.getBufferViews().indexOf(view));
			accessor.setType(Type.VEC2);
			accessor.setComponentType(Accessor.FLOAT);
			accessor.setCount(uv0Count);
			this.gltf.getAccessors().add(accessor);
			attributes.put("TEXCOORD_0", this.gltf.getAccessors().indexOf(accessor));

			this.bufferSize += buf.length;
			this.packedBinary.write(buf);
		}

		if (types.contains(VertexType.JOINTS_WEIGHTS)) {
			byte[] bufJoints = ((ByteArrayOutputStream) jointBuf.getUnderlyingOutputStream()).toByteArray();

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

			byte[] bufWeights = ((ByteArrayOutputStream) weightsBuf.getUnderlyingOutputStream()).toByteArray();

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
		}

		// indices
		{
			int indicesCount = 0;
			ByteArrayOutputStream indicesBuf = new ByteArrayOutputStream();
			LEDataOutputStream indicesOut = new LEDataOutputStream(indicesBuf);

			for (int[] ints : triangles) {
				for (int i : ints) {
					indicesCount++;
					indicesOut.writeUnsignedShortLE(i);
				}
			}

			byte[] buf = indicesBuf.toByteArray();

			BufferView view = new BufferView();
			view.setBuffer(0);
			view.setByteOffset(this.bufferSize);
			view.setByteLength(buf.length);
			this.gltf.getBufferViews().add(view);

			Accessor accessor = new Accessor();
			accessor.setBufferView(this.gltf.getBufferViews().indexOf(view));
			accessor.setType(Type.SCALAR);
			accessor.setComponentType(Accessor.UNSIGNED_SHORT);
			accessor.setCount(indicesCount);
			this.gltf.getAccessors().add(accessor);
			prim.setIndices(this.gltf.getAccessors().indexOf(accessor));

			this.bufferSize += buf.length;
			this.packedBinary.write(buf);
		}

		this.gltf.getMeshes().add(mesh);

		Node node = new Node();
		node.setMesh(this.gltf.getMeshes().indexOf(mesh));
		node.setName(name);
		node.setSkin(skinIdx);
		this.gltf.getNodes().add(node);
	}

	private float[] normalize(float[] a) {
		float sum = 0;
		for (float f : a) {
			sum += f;
		}

		if (sum == 1) {
			return a;
		}

		float[] out = new float[a.length];
		for (int i = 0; i < a.length; i++) {
			out[i] = a[i] * 1 / sum;
		}

		return out;
	}

	private float[] max(float[] a, float[] b) {
		if (a.length != b.length) {
			throw new IllegalArgumentException("a.length != b.length");
		}
		float[] m = new float[a.length];
		for (int i = 0; i < a.length; i++) {
			m[i] = Math.max(a[i], b[i]);
		}
		return m;
	}

	private float[] min(float[] a, float[] b) {
		if (a.length != b.length) {
			throw new IllegalArgumentException("a.length != b.length");
		}
		float[] m = new float[a.length];
		for (int i = 0; i < a.length; i++) {
			m[i] = Math.min(a[i], b[i]);
		}
		return m;
	}

	public void buildBasicScene(String name, int rootNode) {
		final Scene sc = new Scene();
		sc.setName(name);
		sc.getNodes().add(rootNode);
		gltf.getScenes().add(sc);
	}

	public void addBones(List<ICommandCall> calls) {
		for (ICommandCall cmd : calls) {
			if (!(cmd instanceof ClassCommandCall)) {
				continue;
			}
			ClassCommandCall cmdCls = (ClassCommandCall) cmd;
			if ("addjoint".equals(cmdCls.getPrototype().getName())) {
				this.addBones(cmdCls);
			}
		}

		// reverse tree referencing
		// Nebula: child -> parent
		// glTF: parent -> child
		for (ICommandCall cmd : calls) {
			if (!(cmd instanceof ClassCommandCall)
					|| !"addjoint".equals(((ClassCommandCall) cmd).getPrototype().getName())) {
				continue;
			}
			Object[] args = ((ClassCommandCall) cmd).getArguments();
			int idx = (int) args[0];
			int parent = (int) args[2];
			if (parent == -1) {
				continue;
			}
			this.gltf.getNodes().get(parent).getChildren().add(idx);
		}

		Skin skin = new Skin();
		skin.setName("rig");
		skin.setSkeleton(0);
		for (int i = 0; i < this.gltf.getNodes().size(); i++) {
			skin.getJoints().add(i);
		}
		this.gltf.getSkins().add(skin);
	}

	public void addBones(ClassCommandCall cmd) {
		Object[] args = cmd.getArguments();
		Node bone = new Node();
		bone.setName((String) args[1]);
		bone.setTranslation(new float[] { (float) args[3], (float) args[4], (float) args[5] });
		bone.setRotation(new float[] { (float) args[6], (float) args[7], (float) args[8], (float) args[9] });
		gltf.getNodes().add(bone);
	}

	/**
	 * Call only when everything relying on a buffer is done
	 */
	public void buildBuffer(String uri) {
		final Buffer buf = new Buffer();
		buf.setUri(uri);
		buf.setByteLength(this.bufferSize);
		this.gltf.getBuffers().add(buf);
	}

	public GLTF getGltf() {
		return this.gltf;
	}
}
