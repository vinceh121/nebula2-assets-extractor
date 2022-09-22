package me.vinceh121.n2ae.script;

public class SelCommandCall implements ICommandCall {
	private NOBClazz clazz;
	private String path;

	public SelCommandCall() {
	}

	public SelCommandCall(final NOBClazz clazz, final String path) {
		this.clazz = clazz;
		this.path = path;
	}

	@Override
	public NOBClazz getClazz() {
		return this.clazz;
	}

	public void setClazz(final NOBClazz clazz) {
		this.clazz = clazz;
	}

	public String getPath() {
		return this.path;
	}

	public void setPath(final String path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return "sel " + this.path;
	}
}
