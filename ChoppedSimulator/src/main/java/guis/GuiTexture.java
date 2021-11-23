package guis;


import math.Vector2;

public class GuiTexture {
	private int texture;
	private Vector2 pos;
	private Vector2 scale;
	private boolean isRendered;
	
	public GuiTexture(int texture, Vector2 pos, Vector2 scale) {
		this.texture = texture;
		this.pos = pos;
		this.scale = scale;
		this.isRendered = false;
	}

	public int getTexture() {
		return texture;
	}

	public void setTexture(int texture) {
		this.texture = texture;
	}

	public Vector2 getPos() {
		return pos;
	}

	public void setPos(Vector2 pos) {
		this.pos = pos;
	}

	public Vector2 getScale() {
		return scale;
	}

	public void setScale(Vector2 scale) {
		this.scale = scale;
	}

	public boolean isRendered() {
		return isRendered;
	}

	public void setRendered(boolean isRendered) {
		this.isRendered = isRendered;
	}
}
