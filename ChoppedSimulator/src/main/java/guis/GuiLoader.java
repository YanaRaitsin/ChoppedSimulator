package guis;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameterf;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class GuiLoader {

	private List<Integer> vaos = new ArrayList<Integer>();
	private List<Integer> vbos = new ArrayList<Integer>();
	private List<Integer> textures = new ArrayList<Integer>();

	private int textureID;
	private int width;
	private int height;

	public RawModel loadToVAO(float positions[], int dimensions) {
		int vaoID = createVAO();
		this.storeDataInAttributeList(0, dimensions, positions);
		unbindVAO();
		return new RawModel(vaoID, positions.length / dimensions);
	}

	public void cleanUp() {
		for (int vao : vaos)
			GL30.glDeleteVertexArrays(vao);

		for (int vbo : vbos)
			GL15.glDeleteBuffers(vbo);

		for (int texture : textures)
			GL11.glDeleteTextures(texture);
	}

	private int createVAO() {
		int vaoID = GL30.glGenVertexArrays();
		vaos.add(vaoID);
		GL30.glBindVertexArray(vaoID);
		return vaoID;
	}

	private void storeDataInAttributeList(int attributeNumber, int coordinateSize, float data[]) {
		int vboID = GL15.glGenBuffers();
		vbos.add(vboID);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		FloatBuffer buffer = storeDataInFloatBuffer(data);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(attributeNumber, coordinateSize, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}

	private void unbindVAO() {
		GL30.glBindVertexArray(0);
	}

	public void bind() {
		glBindTexture(GL_TEXTURE_2D, textureID);
	}

	private FloatBuffer storeDataInFloatBuffer(float data[]) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}
	
	public int loadTexture(String fileName) {
		BufferedImage texture;
		try {
			texture = ImageIO.read(new File(fileName));
			width = texture.getWidth();
			height = texture.getHeight();
			int rawPixels[] = new int[width * height * 4];
			rawPixels = texture.getRGB(0, 0, width, height, null, 0, width);

			ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * 4);

			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					int pixel = rawPixels[j * width + i];
					// red
					pixels.put((byte) ((pixel >> 16) & 0xFF));
					// green
					pixels.put((byte) ((pixel >> 8) & 0xFF));
					// blue
					pixels.put((byte) (pixel & 0xFF));
					// alpha
					pixels.put((byte) ((pixel >> 24) & 0xFF));
				}
			}

			pixels.flip();

			textureID = glGenTextures();
			glBindTexture(GL_TEXTURE_2D, textureID);
			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
			GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, -0.4f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		textures.add(textureID);
		return textureID;

	}

}
