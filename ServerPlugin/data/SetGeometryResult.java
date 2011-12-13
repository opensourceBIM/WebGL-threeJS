package org.bimserver.serializers.json.data;

import org.bimserver.models.ifc2x3.IfcElement;


public class SetGeometryResult {
//	private final String guid;
	private final int addedIndices;
	private final int addedVertices;
	private final BinaryIndexBuffer binaryIndexBuffer;
	private final BinaryVertexBuffer binaryVertexBuffer;
	private Class ifcClass = null;




	public SetGeometryResult(int addedIndices, int addedVertices, BinaryIndexBuffer binaryIndexBuffer, BinaryVertexBuffer binaryVertexBuffer) {
//		this.guid = guid;
		this.addedIndices = addedIndices;
		this.addedVertices = addedVertices;
		this.binaryIndexBuffer = binaryIndexBuffer;
		this.binaryVertexBuffer = binaryVertexBuffer;
	}
	
	

	public int getAddedIndices() {
		return addedIndices;
	}

	public int getAddedVertices() {
		return addedVertices;
	}

	public BinaryIndexBuffer getBinaryIndexBuffer() {
		return binaryIndexBuffer;
	}

	public BinaryVertexBuffer getBinaryVertexBuffer() {
		return binaryVertexBuffer;
	}	
	
//	public String getGuid() {
//		return guid;
//	}
	
	public Class getIfcClass() {
		return ifcClass;
	}
	public void setIfcClass(Class ifcClass) {
		this.ifcClass = ifcClass;
	}



}