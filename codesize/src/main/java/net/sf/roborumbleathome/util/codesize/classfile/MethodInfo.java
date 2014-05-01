package net.sf.roborumbleathome.util.codesize.classfile;

final class MethodInfo {

    private final AttributeInfo[] attributes;

    MethodInfo(final AttributeInfo[] attributes) {
	this.attributes = attributes;
    }

    int getCodesize() {
	int codesize = 0;
	for (final AttributeInfo attribute : attributes) {
	    codesize += attribute.getCodesize();
	}
	return codesize;
    }
}