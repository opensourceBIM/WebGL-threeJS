package org.bimserver.serializers.json;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.List;

import org.bimserver.emf.IdEObject;
import org.bimserver.emf.IfcModelInterface;
import org.bimserver.models.ifc2x3tc1.GeometryData;
import org.bimserver.models.ifc2x3tc1.GeometryInfo;
import org.bimserver.models.ifc2x3tc1.IfcColumn;
import org.bimserver.models.ifc2x3tc1.IfcDoor;
import org.bimserver.models.ifc2x3tc1.IfcProduct;
import org.bimserver.models.ifc2x3tc1.IfcRoot;
import org.bimserver.models.ifc2x3tc1.IfcSlab;
import org.bimserver.models.ifc2x3tc1.IfcWall;
import org.bimserver.models.ifc2x3tc1.IfcWallStandardCase;
import org.bimserver.models.ifc2x3tc1.IfcWindow;
import org.bimserver.plugins.PluginManager;
import org.bimserver.plugins.renderengine.RenderEnginePlugin;
import org.bimserver.plugins.serializers.EmfSerializer;
import org.bimserver.plugins.serializers.ProjectInfo;
import org.bimserver.plugins.serializers.SerializerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSONModelFormat2Serializer extends EmfSerializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(JSONModelFormat2Serializer.class);
	private PrintWriter out;

	public void init(IfcModelInterface model, ProjectInfo projectInfo, PluginManager pluginManager, RenderEnginePlugin renderEnginePlugin, boolean oids) throws SerializerException {
		super.init(model, projectInfo, pluginManager, renderEnginePlugin, false);
	}

	@Override
    public void reset() {
		setMode(Mode.BODY);
	}

	@Override
	public boolean write(OutputStream outputStream) throws SerializerException {
		if (getMode() == Mode.BODY) {
			out = new PrintWriter(outputStream);
			out.println("[");
			writeGeometries();
			out.println();
			out.println("]");
			out.flush();

			setMode(Mode.FINISHED);
			return true;
		} else {
			return false;
		}
	}

	private void writeGeometry(SetGeometryResult geometry, IfcRoot ifcRoot) {
		out.println("  \"id\" : \"" + ifcRoot.getGlobalId() + "\", ");
		out.println("  \"type\" : \"" + ifcRoot.eClass().getName().toUpperCase() + "\", ");
		out.println("  \"geometry\" : {");
		out.println("   \"metadata\" : { \"formatVersion\" : 3 }, ");
		out.println("	\"materials\": [],");
		out.print("	\"vertices\": [ ");

		List<Float> vertices = geometry.getBinaryVertexBuffer().getVertices();
		if (vertices != null && vertices.size() > 0) {
			for (int i = 0; i < vertices.size(); i++) {
				out.print(i == 0 ? "" : ",");
				out.print(i % 3 == 0 ? " " : "");
				out.print(vertices.get(i));
			}
		}

		out.println("	], ");
		out.print("	\"normals\":  [");

		List<Float> normals = geometry.getBinaryVertexBuffer().getNormals();
		if (normals != null && normals.size() > 0) {
			for (int i = 0; i < normals.size(); i++) {
				out.print(i == 0 ? "" : ",");
				out.print(i % 3 == 0 ? " " : "");
				out.print(normals.get(i));
			}
		}

		out.println("	],");
		out.println("	\"colors\":   [ ],");
		out.println("	\"uvs\":	  [ ],");
		out.print("	\"faces\": [ ");

		List<Integer> indices = geometry.getBinaryIndexBuffer().getIndices();
		if (indices != null && indices.size() > 0) {
			for (int i = 0; i < indices.size(); i += 3) {
				out.print(i == 0 ? "" : ",");
				out.print(" 32, ");
				out.print((indices.get(i)) + "," + (indices.get(i + 1)) + "," + (indices.get(i + 2)) + ",");
				out.print((indices.get(i)) + "," + (indices.get(i + 1)) + "," + (indices.get(i + 2)));
			}
		}

		out.println(" ]");
		out.println("	 }");
		out.println();
	}

	@SuppressWarnings("unchecked")
	private void writeGeometries() {
		// Class[] eClasses = new Class[] { IfcSlab.class, IfcRoof.class,
		// IfcWall.class, IfcWallStandardCase.class, IfcWindow.class,
		// IfcDoor.class, IfcColumn.class, IfcRamp.class,
		// IfcStair.class, IfcStairFlight.class, IfcRailing.class };
		Class<IdEObject>[] eClasses = new Class[] { IfcWallStandardCase.class, IfcWall.class, IfcWindow.class, IfcDoor.class, IfcSlab.class, IfcColumn.class };

		try {
			boolean first = true;
			for (Class<? extends IdEObject> eClass : eClasses) {
				for (IdEObject object : model.getAll(eClass)) {
                    IfcProduct ifcRoot = (IfcProduct) object;
					SetGeometryResult geometry = getGeometry(ifcRoot);
					if (geometry != null) {
						out.println(first ? "  {" : " ,{");
						first = false;
						writeGeometry(geometry, ifcRoot);
						out.print("  }");
					}
				}
			}
		} catch (SerializerException e) {
			LOGGER.error("", e);
		}
	}

	private SetGeometryResult getGeometry(IfcProduct ifcRootObject) throws SerializerException {
		GeometryInfo geometryInfo = ifcRootObject.getGeometry();
		if (geometryInfo != null) {
			GeometryData geometryData = geometryInfo.getData();
			ByteBuffer verticesBuffer = ByteBuffer.wrap(geometryData.getVertices());
			ByteBuffer normalsBuffer = ByteBuffer.wrap(geometryData.getNormals());
			
			int t = geometryInfo.getPrimitiveCount() * 3 * 3;
			
			BinaryIndexBuffer binaryIndexBuffer = new BinaryIndexBuffer();
			BinaryVertexBuffer binaryVertexBuffer = new BinaryVertexBuffer();
			
			for (int i = 0; i < t; i++) {
				binaryVertexBuffer.addVertex(verticesBuffer.getFloat());
				binaryVertexBuffer.addNormal(normalsBuffer.getFloat());
			}
			for (int i = 0; i < geometryInfo.getPrimitiveCount() * 3; i++) {
				binaryIndexBuffer.addIndex(i);
			}
			return new SetGeometryResult(geometryInfo.getPrimitiveCount() * 3, geometryInfo.getData().getVertices().length, binaryIndexBuffer, binaryVertexBuffer);
		}
		return null;
	}
}