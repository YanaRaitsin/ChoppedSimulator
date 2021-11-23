package guis;

import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import math.Matrix4f;

public class GuiShader {
    
   private static final String VERTEX_FILE = "src/main/java/Guis/guiVertexShader.txt";
   private static final String FRAGMENT_FILE = "src/main/java/Guis/guiFragmentShader.txt";
   
   private int programID, vertexShaderID, fragmentShaderID;
   private int location_transformationMatrix;
   private static FloatBuffer buffer = BufferUtils.createFloatBuffer(16);

   public GuiShader() {
		vertexShaderID = loadShader(VERTEX_FILE, GL20.GL_VERTEX_SHADER);
		fragmentShaderID = loadShader(FRAGMENT_FILE, GL20.GL_FRAGMENT_SHADER);
		programID = GL20.glCreateProgram();
		GL20.glAttachShader(programID, vertexShaderID);
		GL20.glAttachShader(programID, fragmentShaderID);
		bindAttributes();
		GL20.glLinkProgram(programID);
		GL20.glValidateProgram(programID);
		getAllUniformLocations();
   }
    
   public void loadTransformation(Matrix4f matrix){
      loadMatrix4f(location_transformationMatrix, matrix);
   }
   
	protected void loadMatrix4f(int location, Matrix4f mat) {
		mat.store(buffer);
		buffer.flip();
		glUniformMatrix4fv(location, false, buffer);
	}


   protected void getAllUniformLocations() {
       location_transformationMatrix = GL20.glGetUniformLocation(programID, "transformationMatrix");
   }

   protected void bindAttributes() {
	   GL20.glBindAttribLocation(programID, 0, "position");
   }
   
	private static int loadShader(String file, int type) {
		StringBuilder shaderSource = new StringBuilder();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null)
				shaderSource.append(line).append("\n");
			reader.close();
		} catch (IOException exception) {
			System.err.println("Could not read file!");
			exception.printStackTrace();
			System.exit(-1);
		}

		int shaderID = GL20.glCreateShader(type);
		GL20.glShaderSource(shaderID, shaderSource);
		GL20.glCompileShader(shaderID);
		if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			System.out.print(GL20.glGetShaderInfoLog(shaderID, 500));
			System.err.println("Could not compile shader!");
			System.exit(-1);
		}

		return shaderID;
	}
	
	public void stop() {
		GL20.glUseProgram(0);
	}
	
	public void cleanUp() {
		stop();
		GL20.glDetachShader(programID, vertexShaderID);
		GL20.glDetachShader(programID, fragmentShaderID);
		GL20.glDeleteShader(vertexShaderID);
		GL20.glDeleteShader(fragmentShaderID);
		GL20.glDeleteProgram(programID);
	}
	
	public void start() {
		GL20.glUseProgram(programID);
	}
}
