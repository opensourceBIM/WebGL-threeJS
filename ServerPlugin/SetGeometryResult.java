package ServerPlugin;

public class SetGeometryResult {
	private final int addedIndices;
	private final int addedVertices;
	private final BinaryIndexBuffer binaryIndexBuffer;
	private final BinaryVertexBuffer binaryVertexBuffer;
	private final String guid;
	private final String entityType;
	
	
	public SetGeometryResult(String entityType, String guid, int addedIndices, int addedVertices, BinaryIndexBuffer binaryIndexBuffer, BinaryVertexBuffer binaryVertexBuffer) {
		this.entityType = entityType;
		this.guid = guid;
		this.addedIndices = addedIndices;
		this.addedVertices = addedVertices;
		this.binaryIndexBuffer = binaryIndexBuffer;
		this.binaryVertexBuffer = binaryVertexBuffer;
	}
	
	public SetGeometryResult(int addedIndices, int addedVertices, BinaryIndexBuffer binaryIndexBuffer, BinaryVertexBuffer binaryVertexBuffer) {
		this(null, null, addedIndices, addedVertices, binaryIndexBuffer, binaryVertexBuffer);
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
	
	public String getGuid() {
		return guid;
	}
	
	public String getEntityType() {
		return entityType;
	}
	
}