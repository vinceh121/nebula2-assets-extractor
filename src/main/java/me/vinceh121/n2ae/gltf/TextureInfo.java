package me.vinceh121.n2ae.gltf;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class TextureInfo {
	private int index = -1;
	@JsonInclude(value = Include.CUSTOM, valueFilter = NotMinusOne.class)
	private int texCoord = -1;

	public int getIndex() {
		return this.index;
	}

	public void setIndex(final int index) {
		this.index = index;
	}

	public int getTexCoord() {
		return this.texCoord;
	}

	public void setTexCoord(final int texCoord) {
		this.texCoord = texCoord;
	}

}
