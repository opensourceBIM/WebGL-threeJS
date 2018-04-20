package org.bimserver.serializers.json;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bimserver.emf.IdEObject;
import org.bimserver.emf.IfcModelInterface;
import org.bimserver.emf.PackageMetaData;
import org.bimserver.models.geometry.GeometryData;
import org.bimserver.models.geometry.GeometryInfo;
import org.bimserver.models.ifc2x3tc1.IfcColumn;
import org.bimserver.models.ifc2x3tc1.IfcDoor;
import org.bimserver.models.ifc2x3tc1.IfcProduct;
import org.bimserver.models.ifc2x3tc1.IfcSlab;
import org.bimserver.models.ifc2x3tc1.IfcWall;
import org.bimserver.models.ifc2x3tc1.IfcWindow;
import org.bimserver.plugins.PluginManagerInterface;
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

	public void init(IfcModelInterface model, ProjectInfo projectInfo, PluginManagerInterface pluginManager, RenderEnginePlugin renderEnginePlugin, PackageMetaData packageMetaData, boolean oids) throws SerializerException {
		super.init(model, projectInfo, pluginManager, false);
	}

	@Override
	protected boolean write(OutputStream outputStream, ProgressReporter progressReporter) {
		if (getMode() == Mode.BODY) {
			out = new PrintWriter(outputStream);
			out.println("{");
			out.println("  \"metadata\" : { \"formatVersion\" : 4.3, \"type\" : \"object\", \"generator\" : \"BIMserver three.js serializer\"  }, ");
			out.println("  \"geometries\" : [");
			Map<String, GeometryInfo> geometryData = collectGeometryData();
			writeGeometries(geometryData);
			out.println("  ],");
			out.println("  \"object\" : {");
			out.println("  \"uuid\" : \"root\",");
			out.println("  \"type\" : \"Scene\",");
			out.println("  \"matrix\" : [1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1],");
			out.println("  \"children\" : [");
			writeObjects(geometryData);
			out.println("  ]");
			out.println("  }");
			out.println("}");
			out.flush();

			setMode(Mode.FINISHED);
			return true;
		} else {
			return false;
		}
	}

	private void writeGeometry(GeometryData geometryData) {
		out.println("  \"uuid\" : \"" + geometryData.getOid() + "\", ");
		out.println("  \"type\" : \"Geometry\", ");
		out.println("  \"data\" : {");
		out.print("	   \"vertices\": [ ");

		List<Float> vertices = getFloatList(geometryData.getVertices());
		if (vertices != null && vertices.size() > 0) {
			for (int i = 0; i < vertices.size(); i++) {
				out.print(i == 0 ? "" : ",");
				out.print(i % 3 == 0 ? " " : "");
				out.print(vertices.get(i));
			}
		}

		out.println("], ");
		out.print("	\"normals\":  [");

		List<Float> normals = getFloatList(geometryData.getNormals());
		if (normals != null && normals.size() > 0) {
			for (int i = 0; i < normals.size(); i++) {
				out.print(i == 0 ? "" : ",");
				out.print(i % 3 == 0 ? " " : "");
				out.print(normals.get(i));
			}
		}

		out.println("],");
		out.println("    \"uvs\": [ ],");
		out.print("	   \"faces\": [ ");

		List<Integer> indices = getIntegerList(geometryData.getIndices());
		if (indices != null && indices.size() > 0) {
			for (int i = 0; i < indices.size(); i += 3) {
				out.print(i == 0 ? "" : ",");
				out.print(" 32, ");
				out.print((indices.get(i)) + "," + (indices.get(i + 1)) + "," + (indices.get(i + 2)) + ",");
				out.print((indices.get(i)) + "," + (indices.get(i + 1)) + "," + (indices.get(i + 2)));
			}
		}

		out.println("]}");
	}

	@SuppressWarnings("unchecked")
	private Map<String, GeometryInfo> collectGeometryData() {
		Map<String, GeometryInfo> geometryData = new HashMap<String, GeometryInfo>();
		Class<IdEObject>[] eClasses = new Class[] {
				IfcWall.class, IfcWindow.class, IfcDoor.class, IfcSlab.class, IfcColumn.class,
				org.bimserver.models.ifc4.IfcWall.class,org.bimserver.models.ifc4.IfcWindow.class,
				org.bimserver.models.ifc4.IfcDoor.class, org.bimserver.models.ifc4.IfcSlab.class,
				org.bimserver.models.ifc4.IfcColumn.class
		};
		for (Class<? extends IdEObject> eClass : eClasses) {
			for (IdEObject object : model.getAllWithSubTypes(eClass)) {
				IfcProduct ifcRoot = (IfcProduct) object;
				GeometryInfo geometryInfo = ifcRoot.getGeometry();
				if (geometryInfo != null) {
					geometryData.put(ifcRoot.getGlobalId(), geometryInfo);
				}
			}
		}
		return geometryData;
	}


	private void writeGeometries(Map<String, GeometryInfo> geometryInfos) {
		boolean first = true;
		Set<Long> writtenGeometries = new HashSet<Long>();
		for(GeometryInfo geometryInfo: geometryInfos.values()){
			if(!writtenGeometries.contains(geometryInfo.getData().getOid())){
				out.println(first ? "  {" : " ,{");
				first = false;
				writeGeometry(geometryInfo.getData());
				out.print("  }");
				writtenGeometries.add(geometryInfo.getData().getOid());
            }
        }
		out.println();
	}

	private void writeObjects(Map<String, GeometryInfo> geometryInfos) {
		boolean first = true;
		for (Map.Entry<String, GeometryInfo> geometryEntry: geometryInfos.entrySet()) {
			String guid = geometryEntry.getKey();
			GeometryInfo geometryInfo = geometryEntry.getValue();
			out.println(first ? "  {" : "  , {");
			writeObject(guid, geometryInfo);
			out.print("  }");
			first = false;
		}
		out.println();
	}

	private void writeObject(String guid, GeometryInfo geometryInfo) {
		out.println("  \"uuid\" : \"" + guid + "\", ");
		out.println("  \"type\" : \"Mesh\", ");
		out.println("  \"geometry\" : \"" + geometryInfo.getData().getOid() + "\", ");
		out.print(  "  \"matrix\" : [");
		boolean first = true;
		for(float i: getFloatList(geometryInfo.getTransformation())){
			out.print(first ? "" : ",");
			out.print(i);
			first=false;
		}
		out.println("]");
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