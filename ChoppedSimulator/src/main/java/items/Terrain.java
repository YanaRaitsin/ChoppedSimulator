package items;

import graph.FlatTerrian;

public class Terrain {
    private final GameItem[] gameItems;

    @SuppressWarnings("unused")
	private FlatTerrian terrain;

    /**
     * A Terrain is composed by blocks, each block is a GameItem constructed
     * from a HeightMap.
     *
     * @param scale The scale to be applied to each terrain block
     * @param textureFile
     * @param textInc
     * @throws Exception
     */
    public Terrain(int blocksPerRow,float scale, String textureFile, int textInc) throws Exception {
    	gameItems = new GameItem[blocksPerRow * blocksPerRow];
        FlatTerrian heightMapMesh = new FlatTerrian(0,-1,textureFile, textInc);
        for (int row = 0; row < blocksPerRow; row++) {
            for (int col = 0; col < blocksPerRow; col++) {
                float xDisplacement = (col - ((float) blocksPerRow - 1) / (float) 2) * 0;
                float zDisplacement = (row - ((float) blocksPerRow - 1) / (float) 2) * -1;

                GameItem terrainBlock = new GameItem(heightMapMesh.getMesh());
                terrainBlock.setScale(scale);
                terrainBlock.setPosition(xDisplacement, 0, zDisplacement);
                gameItems[row * blocksPerRow + col] = terrainBlock;
            }
        }
    }

    public GameItem[] getGameItems() {
        return gameItems;
    }

}
