package me.vinceh121.n2ae.script;

public class SelCommandCall implements ICommandCall {
	private NOBClazz clazz;
	private String path;

	public SelCommandCall() {
	}

	public SelCommandCall(NOBClazz clazz, String path) {
		this.clazz = clazz;
		this.path = path;
	}

	@Override
	public NOBClazz getClazz() {
		return clazz;
	}

	public void setClazz(NOBClazz clazz) {
		this.clazz = clazz;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return "sel " + this.path;
	}
}
