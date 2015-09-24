package org.bimserver.serializers.json;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.bimserver.emf.IdEObject;
import org.bimserver.emf.IfcModelInterface;
import org.bimserver.emf.PackageMetaData;
import org.bimserver.models.geometry.GeometryData;
import org.bimserver.models.geometry.GeometryInfo;
import org.bimserver.models.ifc2x3tc1.IfcColumn;
import org.bimserver.models.ifc2x3tc1.IfcDoor;
import org.bimserver.models.ifc2x3tc1.IfcProduct;
import org.bimserver.models.ifc2x3tc1.IfcRoot;
import org.bimserver.models.ifc2x3tc1.IfcSlab;
import org.bimserver.models.ifc2x3tc1.IfcWall;
import org.bimserver.models.ifc2x3tc1.IfcWindow;
import org.bimserver.plugins.PluginManager;
import org.bimserver.plugins.renderengine.RenderEnginePlugin;
import org.bimserver.plugins.serializers.EmfSerializer;
import org.bimserver.plugins.serializers.ProgressReporter;
import org.bimserver.plugins.serializers.ProjectInfo;
import org.bimserver.plugins.serializers.SerializerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreeJsSerializer extends EmfSerializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ThreeJsSerializer.class);
	private PrintWriter out;

	public void init(IfcModelInterface model, ProjectInfo projectInfo, PluginManager pluginManager, RenderEnginePlugin renderEnginePlugin, PackageMetaData packageMetaData, boolean oids) throws SerializerException {
		super.init(model, projectInfo, pluginManager, renderEnginePlugin, packageMetaData, false);
	}

	@Override
    public void reset() {
		setMode(Mode.BODY);
	}

	@Override
	protected boolean write(OutputStream outputStream, ProgressReporter progressReporter) {
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

	private void writeGeometry(GeometryData geometryData, IfcRoot ifcRoot) {
		out.println("  \"id\" : \"" + ifcRoot.getGlobalId() + "\", ");
		out.println("  \"type\" : \"" + ifcRoot.eClass().getName().toUpperCase() + "\", ");
		out.println("  \"geometry\" : {");
		out.println("   \"metadata\" : { \"formatVersion\" : 3 }, ");
		out.println("	\"materials\": [],");
		out.print("	\"vertices\": [ ");

		List<Float> vertices = getFloatList(geometryData.getVertices());
		if (vertices != null && vertices.size() > 0) {
			for (int i = 0; i < vertices.size(); i++) {
				out.print(i == 0 ? "" : ",");
				out.print(i % 3 == 0 ? " " : "");
				out.print(vertices.get(i));
			}
		}

		out.println("	], ");
		out.print("	\"normals\":  [");

		List<Float> normals = getFloatList(geometryData.getNormals());
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

		List<Integer> indices = getIntegerList(geometryData.getIndices());
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
		Class<IdEObject>[] eClasses = new Class[] {
				IfcWall.class, IfcWindow.class, IfcDoor.class, IfcSlab.class, IfcColumn.class,
				org.bimserver.models.ifc4.IfcWall.class,org.bimserver.models.ifc4.IfcWindow.class,
				org.bimserver.models.ifc4.IfcDoor.class, org.bimserver.models.ifc4.IfcSlab.class,
				org.bimserver.models.ifc4.IfcColumn.class
		};

		boolean first = true;
		for (Class<? extends IdEObject> eClass : eClasses) {
            for (IdEObject object : model.getAllWithSubTypes(eClass)) {
				IfcProduct ifcRoot = (IfcProduct) object;
                GeometryInfo geometryInfo = ifcRoot.getGeometry();
                if (geometryInfo != null) {
                    out.println(first ? "  {" : " ,{");
                    first = false;
                    writeGeometry(geometryInfo.getData(), ifcRoot);
                    out.print("  }");
                }
            }
        }
	}

	private List<Float> getFloatList(byte[] byteArray) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		List<Float> floatList = new ArrayList<Float>();
		while(byteBuffer.hasRemaining()){
            floatList.add(byteBuffer.getFloat());
        }
		return floatList;
	}
	private List<Integer> getIntegerList(byte[] byteArray) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		List<Integer> integerList = new ArrayList<Integer>();
		while(byteBuffer.hasRemaining()){
			integerList.add(byteBuffer.getInt());
		}
		return integerList;
	}

}