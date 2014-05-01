package net.sf.roborumbleathome.util.codesize.classfile;

final class CodeAttribute extends AttributeInfo {

    private final int codeLength;

    CodeAttribute(final int codeLength) {
	this.codeLength = codeLength;
    }

    @Override
    int getCodesize() {
	return codeLength;
    }

}