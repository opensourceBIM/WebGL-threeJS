package org.bimserver.serializers.json;

import org.bimserver.emf.IdEObject;
import org.bimserver.ifc.IfcModel;
import org.bimserver.models.ifc2x3.*;
import org.bimserver.plugins.PluginException;
import org.bimserver.plugins.PluginManager;
import org.bimserver.plugins.ifcengine.*;
import org.bimserver.plugins.serializers.*;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JSONModelFormat2Serializer extends BimModelSerializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(JSONModelFormat2Serializer.class);
	private IfcEngine ifcEngine;
	private PrintWriter out;

	public void init(IfcModelInterface model, ProjectInfo projectInfo, PluginManager pluginManager) throws SerializerException {
		super.init(model, projectInfo, pluginManager);
	}

	@Override
	protected void reset() {
		setMode(Mode.BODY);
	}

	@Override
	public boolean write(OutputStream outputStream) throws SerializerException {
		if (getMode() == Mode.BODY) {
			try {
				ifcEngine = getPluginManager().requireIfcEngine().createIfcEngine();
			} catch (PluginException e) {
				throw new SerializerException(e);
			}
			out = new PrintWriter(outputStream);
			out.println("var model = [");
			writeGeometries();
			out.println();
			out.println("];");
			out.println();
			out.println("postMessage( model );");
			out.flush();

			setMode(Mode.FINISHED);
			return true;
		} else {
			return false;
		}
	}
	
	private String colorFromClass(IfcRoot ifcClass) {

		if (ifcClass instanceof IfcSlab) {
			return "0xCCCDFF";
		} else if (ifcClass instanceof IfcRoof) {
			return "0x1F1F1F";
		} else if (ifcClass instanceof IfcPlate) {
			return "0x999999";
		} else if (ifcClass instanceof IfcWall) {
			return "0xB8B8B8";
		} else if (ifcClass instanceof IfcWallStandardCase) {
			return "0x999999";
		} else if (ifcClass instanceof IfcCurtainWall) {
			return "0x888888";
		} else if (ifcClass instanceof IfcWindow) {
			return "0xCCFFFF";
		} else if (ifcClass instanceof IfcDoor) {
			return "0x331A00";	
		} else if (ifcClass instanceof IfcColumn) {
			return "0x3D3D3D";
		} else if (ifcClass instanceof IfcMember) {
			return "0x7A7A7A";
		} else if (ifcClass instanceof IfcBeam) {
			return "0xFFCC33";
		} else if (ifcClass instanceof IfcStair) {
			return "0xFFE47A";
		} else if (ifcClass instanceof IfcStairFlight) {
			return "0xFFD83D";
		} else if (ifcClass instanceof IfcRailing) {
			return "0x0027C2";
		} else if (ifcClass instanceof IfcRamp) {
			return "0xC20027";
		} else if (ifcClass instanceof IfcFlowSegment) {
			return "0x6699CC";
		} else if (ifcClass instanceof IfcFlowTreatmentDevice) {
			return "0x6666CC";
		} else if (ifcClass instanceof IfcFlowTerminal) {
			return "0x2E2E8A";
		} else if (ifcClass instanceof IfcFlowFitting) {
			return "0x2E5C8A";
		} else if (ifcClass instanceof IfcFlowController) {
			return "0x2E2E8A";
		} else if (ifcClass instanceof IfcFurnishingElement) {
			return "0x00EB00";
		} else if (ifcClass instanceof IfcEnergyConversionDevice) {
			return "0xCC0033";
		} else if (ifcClass instanceof IfcDistributionControlElement) {
			return "0xFF0A47";
		} else if (ifcClass instanceof IfcBuildingElementProxy) {
			return "0xEB00EB";
		} else if (ifcClass instanceof IfcOpeningElement) {
			return "0x086CA2";
		} else {
			return "0x000000";
		}
	}

	private void writeGeometry(SetGeometryResult geometry, IfcRoot ifcRoot) {

		out.println("  'id' : '" + ifcRoot.getGlobalId().getWrappedValue() + "', ");
		out.println("  'type' : '" + ifcRoot.eClass().getName().toUpperCase() + "', ");
		out.println("  'geometry' : {");
		out.println("   'version' : 2, ");
		out.println("    'materials': [],");
		out.print("    'vertices': [ ");

		List<Float> vertices = geometry.getBinaryVertexBuffer().getVertices();
		if (vertices != null && vertices.size() > 0) {
			for (int i = 0; i < vertices.size(); i++) {
				out.print(i == 0 ? "" : ",");
				out.print(i % 3 == 0 ? " " : "");
				out.print(vertices.get(i));
			}
		}

		out.println("    ], ");
		out.print("    'normals':  [");

		List<Float> normals = geometry.getBinaryVertexBuffer().getNormals();
		if (normals != null && normals.size() > 0) {
			for (int i = 0; i < normals.size(); i++) {
				out.print(i == 0 ? "" : ",");
				out.print(i % 3 == 0 ? " " : "");
				out.print(normals.get(i));
			}
		}

		out.println("    ],");
		out.println("  'color':    " + colorFromClass(ifcRoot) + " ,");
		out.println("    'uvs':      [ ],");
		out.print("    'faces': [ ");

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
		out.println("    }");
		out.println();
	}

	private void writeGeometries() {
		List<SetGeometryResult> geometryList = new ArrayList<SetGeometryResult>();
		//More complete list
		Class[] eClasses = new Class[] { IfcSlab.class, IfcRoof.class, IfcPlate.class,
				                         IfcWall.class, IfcWallStandardCase.class, IfcCurtainWall.class, 
				                         IfcWindow.class, IfcDoor.class, IfcBeam.class, IfcColumn.class, IfcMember.class,
				                         IfcStair.class, IfcStairFlight.class, IfcRailing.class,IfcRamp.class, 
				                         IfcFlowSegment.class, IfcFlowTreatmentDevice.class, IfcFlowTerminal.class, IfcFlowFitting.class, IfcFlowController.class,
				                         IfcFurnishingElement.class, IfcBuildingElementProxy.class,
				                         IfcEnergyConversionDevice.class, IfcDistributionControlElement.class};
		//Simplified list
		//Class[] eClasses = new Class[] {IfcWallStandardCase.class, IfcWall.class, IfcWindow.class, IfcDoor.class, IfcSlab.class, IfcColumn.class};

		try {
			boolean first = true;
			for (Class<? extends EObject> eClass : eClasses) {
				for (Object object : model.getAll(eClass)) {
					IfcRoot ifcRoot = (IfcRoot) object;
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
		} catch (IfcEngineException e) {
			LOGGER.error("", e);
		}
	}


	private SetGeometryResult getGeometry(IdEObject ifcRootObject) throws SerializerException, IfcEngineException {
		IfcModel ifcModel = new IfcModel();
		convertToSubset(ifcRootObject.eClass(), ifcRootObject, ifcModel, new HashMap<EObject, EObject>());
		EmfSerializer serializer = getPluginManager().requireIfcStepSerializer();
		serializer.init(ifcModel, null, getPluginManager());
		BinaryIndexBuffer binaryIndexBuffer = new BinaryIndexBuffer();
		BinaryVertexBuffer binaryVertexBuffer = new BinaryVertexBuffer();
		IfcEngineModel model = ifcEngine.openModel(serializer.getBytes());
		try {
			IfcEngineSurfaceProperties sp = model.initializeModelling();
			model.setPostProcessing(true);
			IfcEngineGeometry geometry = model.finalizeModelling(sp);
			int nrIndices = 0;
			if (geometry != null) {
				for (IfcEngineInstance instance : model.getInstances(ifcRootObject.eClass().getName().toUpperCase())) {
					IfcEngineInstanceVisualisationProperties instanceInModelling = instance.getVisualisationProperties();
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
				return new SetGeometryResult(nrIndices * 3, geometry.getNrVertices(), binaryIndexBuffer, binaryVertexBuffer);
			}
		} finally {
			model.close();
		}
		return null;
	}


}
