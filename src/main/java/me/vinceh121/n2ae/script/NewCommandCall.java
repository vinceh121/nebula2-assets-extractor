package me.vinceh121.n2ae.script;

public class NewCommandCall implements ICommandCall {
	private NOBClazz clazz, newClazz;
	private String varName;

	public NewCommandCall() {
	}

	public NewCommandCall(NOBClazz clazz, NOBClazz newClazz, String varName) {
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
		return newClazz;
	}

	public void setNewClazz(NOBClazz newClazz) {
		this.newClazz = newClazz;
	}

	public String getVarName() {
		return varName;
	}

	public void setVarName(String varName) {
		this.varName = varName;
	}

	@Override
	public String toString() {
		return "new " + this.newClazz.getName() + " " + this.varName;
	}
}
