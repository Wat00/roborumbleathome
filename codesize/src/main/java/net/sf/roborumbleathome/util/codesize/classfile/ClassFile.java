package net.sf.roborumbleathome.util.codesize.classfile;

public final class ClassFile {

    private final MethodInfo[] methods;

    ClassFile(final MethodInfo[] methods) {
	this.methods = methods;
    }

    public int getCodesize() {
	int codesize = 0;
	for (final MethodInfo method : methods) {
	    codesize += method.getCodesize();
	}
	return codesize;
    }
}