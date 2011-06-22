package org.bimserver.serializers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nl.tue.buildingsmart.express.dictionary.SchemaDefinition;

import org.bimserver.emf.IdEObject;
import org.bimserver.ifc.BimModelSerializer;
import org.bimserver.ifc.EmfSerializer;
import org.bimserver.ifc.FieldIgnoreMap;
import org.bimserver.ifc.IfcModel;
import org.bimserver.ifc.SerializerException;
import org.bimserver.ifc.file.writer.IfcStepSerializer;
import org.bimserver.ifcengine.FailSafeIfcEngine;
import org.bimserver.ifcengine.Geometry;
import org.bimserver.ifcengine.IfcEngineException;
import org.bimserver.ifcengine.IfcEngineFactory;
import org.bimserver.ifcengine.IfcEngineModel;
import org.bimserver.ifcengine.Instance;
import org.bimserver.ifcengine.SurfaceProperties;
import org.bimserver.ifcengine.jvm.IfcEngine.InstanceVisualisationProperties;
import org.bimserver.models.ifc2x3.IfcColumn;
import org.bimserver.models.ifc2x3.IfcDoor;
import org.bimserver.models.ifc2x3.IfcRailing;
import org.bimserver.models.ifc2x3.IfcRamp;
import org.bimserver.models.ifc2x3.IfcRoof;
import org.bimserver.models.ifc2x3.IfcRoot;
import org.bimserver.models.ifc2x3.IfcSlab;
import org.bimserver.models.ifc2x3.IfcStair;
import org.bimserver.models.ifc2x3.IfcStairFlight;
import org.bimserver.models.ifc2x3.IfcWall;
import org.bimserver.models.ifc2x3.IfcWallStandardCase;
import org.bimserver.models.ifc2x3.IfcWindow;
import org.bimserver.models.store.Project;
import org.bimserver.models.store.User;
import org.bimserver.o3d.BinaryIndexBuffer;
import org.bimserver.o3d.BinaryVertexBuffer;
import org.bimserver.o3d.SetGeometryResult;
import org.eclipse.emf.ecore.EObject;
import org.mangosdk.spi.ProviderFor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ProviderFor(value=EmfSerializer.class)
public class JSONModelFormat2Serializer extends BimModelSerializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(JSONModelFormat2Serializer.class);
	private Project project;
	private User user;
	private SchemaDefinition schemaDefinition;
	private FailSafeIfcEngine ifcEngine;	
	
	public void init(Project project, User user, String fileName, IfcModel model, FieldIgnoreMap fieldIgnoreMap, SchemaDefinition schemaDefinition, IfcEngineFactory ifcEngineFactory) throws SerializerException {
		super.init(fileName, model, fieldIgnoreMap);
		this.project = project;
		this.user = user;
		this.schemaDefinition = schemaDefinition;
		try {
			this.ifcEngine = ifcEngineFactory.createFailSafeIfcEngine();
		} catch (IfcEngineException e) {
			throw new SerializerException(e);
		}
	}
	
	@Override
	protected void reset() {
		setMode(Mode.BODY);
	}
	
	@Override
	public boolean write(OutputStream outputStream) throws SerializerException {
		//System.out.println("...");
		if (getMode() == Mode.BODY) {
			List<SetGeometryResult> geometries = getGeometries();
			
			PrintWriter out = new PrintWriter(outputStream);
			out.println("var model = [");
			
			for(int i=0; i<geometries.size(); i++) {
				SetGeometryResult geometry = geometries.get(i);
				out.println(i == 0 ? "{": ",{");
				writeGeometry(out, geometry);
				out.println("}");
			}
			
			out.println("");
			out.println("];");
			out.println("");		
			out.println("  postMessage( model );");
			out.flush();
			
			setMode(Mode.FINISHED);
			return true;
		} else if (getMode() == Mode.FINISHED) {
			return false;
		}
		return false;
	}
	
	private void writeGeometry(PrintWriter out, SetGeometryResult geometry) {
		out.println("id : '" + geometry.getGuid() +  "', ");
		out.println("type : '" + geometry.getEntityType() +  "', ");
		out.println("geometry : {");
		out.println(" 'version' : 2, ");
		out.println("  'materials': [],");
		out.print("  'vertices': [ ");

		List<Float> vertices = geometry.getBinaryVertexBuffer().getVertices();
		if (vertices != null && vertices.size() > 0) {		
			for (int i = 0; i < vertices.size(); i++) {	
				out.print(i==0 ? "" : ",");
				out.print(i % 3 == 0 ? " " : "");
				out.print(vertices.get(i));
			}
		}
		
		out.println(" ], ");
		out.print("  'normals':  [");
		
		List<Float> normals = geometry.getBinaryVertexBuffer().getNormals();
		if (normals != null && normals.size() > 0) {
			for (int i = 0; i < normals.size(); i++) {
				out.print(i==0 ? "" : ",");
				out.print(i % 3 == 0 ? " " : "");
				out.print(normals.get(i));
			}
		}
		
		out.println("  ],");
		out.println("  'colors':   [ ],");
		out.println("  'uvs':      [ ],");
		out.print("  'faces': [ ");
		
		List<Integer> indices = geometry.getBinaryIndexBuffer().getIndices();
		if (indices != null && indices.size() > 0) {		
			for (int i = 0; i < indices.size(); i+=3) {
				out.print(i==0 ? "" : ",");
				out.print(" 32, ");
				out.print((indices.get(i)) + "," + (indices.get(i+1)) + "," + (indices.get(i+2)) + ",");
				out.print((indices.get(i)) + "," + (indices.get(i+1)) + "," + (indices.get(i+2)));
			}
		}
		
		
		out.println("  ]");
		out.println("  }");
		out.println("");
	}
	
	
//	private void writeFaces(PrintWriter out, List<SetGeometryResult> geometries) {
//		int offset = 0;
//		for (int i = 0; i < geometries.size(); i++) {
//			SetGeometryResult geometry = geometries.get(i);
//			if (geometry == null) { continue; }
//			List<Integer> indices = geometry.getBinaryIndexBuffer().getIndices();
//			if (indices == null || indices.size() == 0) { continue; }
//			
//			for (int j = 0; j < indices.size(); j+=3) {
//				out.print((i==0 && j==0) ? "" : ",");
//				out.print(" 32, ");
//				out.print((offset + indices.get(j)) + "," + (offset + indices.get(j+1)) + "," + (offset + indices.get(j+2)) + ",");
//				out.print((offset + indices.get(j)) + "," + (offset + indices.get(j+1)) + "," + (offset + indices.get(j+2)));
//			}
//			offset += (geometry.getBinaryVertexBuffer().getVertices().size() / 3);
//			System.out.println("Indices: " + indices.size());
//		}
//	}

//	private void writeVertices(PrintWriter out, List<SetGeometryResult> geometries) {
//		for (int i = 0; i < geometries.size(); i++) {
//			SetGeometryResult geometry = geometries.get(i);
//			if (geometry == null) { continue; }
//			List<Float> vertices = geometry.getBinaryVertexBuffer().getVertices();
//			if (vertices == null || vertices.size() == 0) { continue; }
//			
//			for (int j = 0; j < vertices.size(); j++) {
//				out.print((i==0 && j==0) ? "" : ",");
//				out.print(j % 3 == 0 ? " " : "");
//				out.print(vertices.get(j));
//			}
//			System.out.println("Vertices: " + vertices.size());
//
//		}
//	}
	
//	private void writeNormals(PrintWriter out, List<SetGeometryResult> geometries) {
//		for (int i = 0; i < geometries.size(); i++) {
//			SetGeometryResult geometry = geometries.get(i);
//			if (geometry == null) { continue; }
//			List<Float> normals = geometry.getBinaryVertexBuffer().getNormals();
//			if (normals == null || normals.size() == 0) { continue; }
//			
//			for (int j = 0; j < normals.size(); j++) {
//				out.print((i==0 && j==0) ? "" : ",");
//				out.print(j % 3 == 0 ? " " : "");
//				out.print(normals.get(j));
//			}
//			System.out.println("Normals: " + normals.size());
//		}
//
//	}
	
	private List<SetGeometryResult> getGeometries() {
		List<SetGeometryResult> geometryList = new ArrayList<SetGeometryResult>();
		//Class[] eClasses = new Class[] { IfcSlab.class, IfcRoof.class, IfcWall.class, IfcWallStandardCase.class, IfcWindow.class, IfcDoor.class, IfcColumn.class, IfcRamp.class,
		//				IfcStair.class, IfcStairFlight.class, IfcRailing.class };
		Class[] eClasses = new Class[] { IfcWallStandardCase.class, IfcWall.class, IfcWindow.class, IfcDoor.class, IfcSlab.class, IfcColumn.class };

		try {
			for (Class<? extends EObject> eClass : eClasses) {
				for (Object object : model.getAll(eClass)) {
					IfcRoot ifcRoot = (IfcRoot) object;
					SetGeometryResult geometry = getGeometry(ifcRoot);
					if (geometry != null) { geometryList.add(geometry); }					
				}
			}
		}  catch (SerializerException e) {
			LOGGER.error("", e);
		} catch (IfcEngineException e) {
			LOGGER.error("", e);
		}
		return geometryList;
	}

	
	private SetGeometryResult getGeometry(IdEObject ifcRootObject) throws SerializerException, IfcEngineException {
		IfcModel IfcModel = new IfcModel();
		convertToSubset(ifcRootObject.eClass(), ifcRootObject, IfcModel, new HashMap<EObject, EObject>());
		IfcStepSerializer ifcSerializer = new IfcStepSerializer(project, user, "", IfcModel, schemaDefinition);
		BinaryIndexBuffer binaryIndexBuffer = new BinaryIndexBuffer();
		BinaryVertexBuffer binaryVertexBuffer = new BinaryVertexBuffer();
		IfcEngineModel model = ifcEngine.openModel(ifcSerializer.getBytes());
		try {
			SurfaceProperties sp = model.initializeModelling();
			model.setPostProcessing(true);
			Geometry geometry = model.finalizeModelling(sp);
			int nrIndices = 0;
			if (geometry != null) {
				for (Instance instance : model.getInstances(ifcRootObject.eClass().getName().toUpperCase())) {
					InstanceVisualisationProperties instanceInModelling = instance.getVisualisationProperties();
					for (int i = instanceInModelling.getStartIndex(); i < instanceInModelling.getPrimitiveCount() * 3 + instanceInModelling.getStartIndex(); i += 3) {
						binaryIndexBuffer.addIndex(geometry.getIndex(i));
						binaryIndexBuffer.addIndex(geometry.getIndex(i + 2));
						binaryIndexBuffer.addIndex(geometry.getIndex(i + 1));
						nrIndices++;
					}
				}
				for (int i = 0; i < geometry.getNrVertices(); i += 3) {
					binaryVertexBuffer.addVertex(geometry.getVertex(i));
					binaryVertexBuffer.addVertex(geometry.getVertex(i + 1));
					binaryVertexBuffer.addVertex(geometry.getVertex(i + 2));
				}
				for (int i = 0; i < geometry.getNrNormals(); i += 3) {
					binaryVertexBuffer.addNormal(geometry.getNormal(i));
					binaryVertexBuffer.addNormal(geometry.getNormal(i + 1));
					binaryVertexBuffer.addNormal(geometry.getNormal(i + 2));
				}
				String guid =  ((IfcRoot)ifcRootObject).getGlobalId().getWrappedValue(); // "TODO" + Long.toString(ifcRootObject.getOid()); // TODO
				String entityType = ifcRootObject.eClass().getName().toUpperCase();
				return new SetGeometryResult(entityType, guid, nrIndices * 3, geometry.getNrVertices(), binaryIndexBuffer, binaryVertexBuffer);
			}
		} finally {
			model.close();
		}
		return null;
	}


}
