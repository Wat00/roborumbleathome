package net.sf.roborumbleathome.util.codesize.classfile;

public final class InvalidClassFormatException extends RuntimeException {

    InvalidClassFormatException(final int message) {
	super(Integer.toString(message));
    }

    InvalidClassFormatException(final Class<?> message) {
	super(message.getName());
    }

}