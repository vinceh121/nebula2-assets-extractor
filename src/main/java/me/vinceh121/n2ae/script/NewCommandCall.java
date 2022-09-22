package me.vinceh121.n2ae.script;

public class NewCommandCall implements ICommandCall {
	private NOBClazz clazz, newClazz;
	private String varName;

	public NewCommandCall() {
	}

	public NewCommandCall(final NOBClazz clazz, final NOBClazz newClazz, final String varName) {
		this.clazz = clazz;
		this.newClazz = newClazz;
		this.varName = varName;
	}

	@Override
	public NOBClazz getClazz() {
		return this.clazz;
	}

	public void setClazz(final NOBClazz clazz) {
		this.clazz = clazz;
	}

	public NOBClazz getNewClazz() {
		return this.newClazz;
	}

	public void setNewClazz(final NOBClazz newClazz) {
		this.newClazz = newClazz;
	}

	public String getVarName() {
		return this.varName;
	}

	public void setVarName(final String varName) {
		this.varName = varName;
	}

	@Override
	public String toString() {
		return "new " + this.newClazz.getName() + " " + this.varName;
	}
}
