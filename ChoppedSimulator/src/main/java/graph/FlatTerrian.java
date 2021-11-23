package graph;
public class FlatTerrian {

	public static final int TERRAIN_VERTEX_COUNT = 128;
	public static final float TERRAIN_SIZE = 50;

	@SuppressWarnings("unused")
	private float x;
	@SuppressWarnings("unused")
	private float z;
    private final Mesh mesh;
    
    public FlatTerrian(int gridX, int gridZ, String textureFile, int textInc) throws Exception {
		this.x = gridX * TERRAIN_SIZE;
		this.z = gridZ * TERRAIN_SIZE;
    	
        Texture texture = new Texture(textureFile);
		int count = TERRAIN_VERTEX_COUNT * TERRAIN_VERTEX_COUNT;
		float[] positions = new float[count * 3];
		float[] normals = new float[count * 3];
		float[] textureCoords = new float[count*2];
		int[] indices = new int[6*(TERRAIN_VERTEX_COUNT-1)*(TERRAIN_VERTEX_COUNT-1)];
		int vertexPointer = 0;
		for(int i=0;i<TERRAIN_VERTEX_COUNT;i++){
			for(int j=0;j<TERRAIN_VERTEX_COUNT;j++){
				positions[vertexPointer*3] = (float)j/((float)TERRAIN_VERTEX_COUNT - 1) * TERRAIN_SIZE;
				positions[vertexPointer*3+1] = 0;
				positions[vertexPointer*3+2] = (float)i/((float)TERRAIN_VERTEX_COUNT - 1) * TERRAIN_SIZE;
				normals[vertexPointer*3] = 0;
				normals[vertexPointer*3+1] = 1;
				normals[vertexPointer*3+2] = 0;
				textureCoords[vertexPointer*2] = (float)j/((float)TERRAIN_VERTEX_COUNT - 1);
				textureCoords[vertexPointer*2+1] = (float)i/((float)TERRAIN_VERTEX_COUNT - 1);
				vertexPointer++;
			}
		}
		int pointer = 0;
		for(int gz=0;gz<TERRAIN_VERTEX_COUNT-1;gz++){
			for(int gx=0;gx<TERRAIN_VERTEX_COUNT-1;gx++){
				int topLeft = (gz*TERRAIN_VERTEX_COUNT)+gx;
				int topRight = topLeft + 1;
				int bottomLeft = ((gz+1)*TERRAIN_VERTEX_COUNT)+gx;
				int bottomRight = bottomLeft + 1;
				indices[pointer++] = topLeft;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = topRight;
				indices[pointer++] = topRight;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = bottomRight;
			}
		}
        this.mesh = new Mesh(positions, textureCoords, normals, indices);
        Material material = new Material(texture, 0.0f);
        mesh.setMaterial(material);
    }

    public Mesh getMesh() {
        return mesh;
    }

}
