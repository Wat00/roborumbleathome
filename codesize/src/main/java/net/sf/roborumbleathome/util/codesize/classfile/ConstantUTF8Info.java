package net.sf.roborumbleathome.util.codesize.classfile;

final class ConstantUTF8Info extends CPInfo {

    private final String bytes;

    ConstantUTF8Info(final String bytes) {
	this.bytes = bytes;
    }

    @Override
    String getAttributeName() {
	return bytes;
    }

}