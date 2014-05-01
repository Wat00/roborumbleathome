package net.sf.roborumbleathome.util.codesize.classfile;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public final class ClassFileBuilder {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    private static final int CONSTANT_CLASS_TAG = 7;
    private static final int CONSTANT_FIELDREF_TAG = 9;
    private static final int CONSTANT_METHODREF_TAG = 10;
    private static final int CONSTANT_INTERFACE_METHODREF_TAG = 11;
    private static final int CONSTANT_STRING_TAG = 8;
    private static final int CONSTANT_INTEGER_TAG = 3;
    private static final int CONSTANT_FLOAT_TAG = 4;
    private static final int CONSTANT_LONG_TAG = 5;
    private static final int CONSTANT_DOUBLE_TAG = 6;
    private static final int CONSTANT_NAME_AND_TYPE_TAG = 12;
    private static final int CONSTANT_UTF8_TAG = 1;

    private static final String CODE_ATTRIBUTE_NAME = "Code";

    private final DataInput dataInput;

    private CPInfo[] constantPool;

    public ClassFileBuilder(final InputStream inputStream) {
	this.dataInput = new DataInputStream(inputStream);
    }

    public ClassFile buildClassFile() {
	try {

	    final int magicNumber = readU4();
	    if (magicNumber != MAGIC_NUMBER) {
		throw new InvalidClassFormatException(magicNumber);
	    }

	    readU2(); // minor_version
	    readU2(); // major_version

	    buildConstantPool();

	    readU2(); // access_flags
	    readU2(); // this_class
	    readU2(); // super_class

	    final int interfacesCount = readU2();
	    for (int i = 0; i < interfacesCount; ++i) {
		readU2(); // interfaces
	    }

	    final int fieldsCount = readU2();
	    for (int i = 0; i < fieldsCount; ++i) {
		readFieldInfo();
	    }

	    final int methodsCount = readU2();
	    final MethodInfo[] methods = new MethodInfo[methodsCount];
	    for (int i = 0; i < methodsCount; ++i) {
		methods[i] = buildMethodInfo();
	    }

	    buildAttributes();

	    return new ClassFile(methods);

	} catch (final IOException e) {
	    throw new RuntimeException(e);
	}
    }

    private void buildConstantPool() throws IOException {
	final int constantPoolCount = readU2();
	constantPool = new CPInfo[constantPoolCount];
	for (int i = 1; i < constantPool.length; ++i) {
	    final int tag = readU1();
	    switch (tag) {
	    case CONSTANT_CLASS_TAG:
		constantPool[i] = buildConstantClassInfo();
		break;
	    case CONSTANT_FIELDREF_TAG:
		constantPool[i] = buildConstantFieldref();
		break;
	    case CONSTANT_METHODREF_TAG:
		constantPool[i] = buildConstantMethodref();
		break;
	    case CONSTANT_INTERFACE_METHODREF_TAG:
		constantPool[i] = buildConstantInterfaceMethodrefInfo();
		break;
	    case CONSTANT_STRING_TAG:
		constantPool[i] = buildConstantString();
		break;
	    case CONSTANT_INTEGER_TAG:
		constantPool[i] = buildConstantInteger();
		break;
	    case CONSTANT_FLOAT_TAG:
		constantPool[i] = buildConstantFloat();
		break;
	    case CONSTANT_LONG_TAG:
		constantPool[i] = buildConstantLong();
		++i;
		break;
	    case CONSTANT_DOUBLE_TAG:
		constantPool[i] = buildConstantDouble();
		++i;
		break;
	    case CONSTANT_NAME_AND_TYPE_TAG:
		constantPool[i] = buildConstantNameAndTypeInfo();
		break;
	    case CONSTANT_UTF8_TAG:
		constantPool[i] = buildConstantUTF8Info();
		break;
	    default:
		throw new InvalidClassFormatException(tag);
	    }
	}
    }

    private CPInfo buildConstantClassInfo() throws IOException {
	readU2(); // name_index
	return new CPInfo();
    }

    private CPInfo buildConstantFieldref() throws IOException {
	readU2(); // class_index
	readU2(); // name_and_type_index
	return new CPInfo();
    }

    private CPInfo buildConstantMethodref() throws IOException {
	return buildConstantFieldref();
    }

    private CPInfo buildConstantInterfaceMethodrefInfo() throws IOException {
	return buildConstantFieldref();
    }

    private CPInfo buildConstantString() throws IOException {
	readU2(); // string_index
	return new CPInfo();
    }

    private CPInfo buildConstantInteger() throws IOException {
	readU4(); // bytes
	return new CPInfo();
    }

    private CPInfo buildConstantFloat() throws IOException {
	return buildConstantInteger();
    }

    private CPInfo buildConstantLong() throws IOException {
	readU4(); // high_bytes
	readU4(); // low_bytes
	return new CPInfo();
    }

    private CPInfo buildConstantDouble() throws IOException {
	return buildConstantLong();
    }

    private CPInfo buildConstantNameAndTypeInfo() throws IOException {
	readU2(); // name_index
	readU2(); // descriptor_index
	return new CPInfo();
    }

    private CPInfo buildConstantUTF8Info() throws IOException {
	final int length = readU2();
	final byte[] bytes = readU1(length);
	return new ConstantUTF8Info(new String(bytes, UTF_8));
    }

    private void readFieldInfo() throws IOException {
	readU2(); // access_flags
	readU2(); // name_index
	readU2(); // descriptor_index
	buildAttributes();
    }

    private MethodInfo buildMethodInfo() throws IOException {
	readU2(); // access_flags
	readU2(); // name_index
	readU2(); // descriptor_index
	final AttributeInfo[] attributes = buildAttributes();
	return new MethodInfo(attributes);
    }

    private AttributeInfo[] buildAttributes() throws IOException {
	final int attributesCount = readU2();
	final AttributeInfo[] attributes = new AttributeInfo[attributesCount];
	for (int i = 0; i < attributesCount; ++i) {
	    attributes[i] = buildAttributeInfo();
	}
	return attributes;
    }

    private AttributeInfo buildAttributeInfo() throws IOException {
	final int attributeNameIndex = readU2();
	final String attributeName = constantPool[attributeNameIndex].getAttributeName();

	if (CODE_ATTRIBUTE_NAME.equals(attributeName)) {
	    return buildCodeAttribute();
	} else {
	    final int attributeLength = readU4();
	    readU1(attributeLength); // info
	    return new AttributeInfo();
	}
    }

    private AttributeInfo buildCodeAttribute() throws IOException {
	readU4(); // attribute_length
	readU2(); // max_stack
	readU2(); // max_locals

	final int codeLength = readU4();
	readU1(codeLength); // code

	final int exceptionTableLength = readU2();
	for (int i = 0; i < exceptionTableLength; ++i) {
	    readU2(); // start_pc;
	    readU2(); // end_pc;
	    readU2(); // handler_pc;
	    readU2(); // catch_type;
	}

	buildAttributes();

	return new CodeAttribute(codeLength);
    }

    private int readU1() throws IOException {
	return dataInput.readUnsignedByte();
    }

    private byte[] readU1(final int length) throws IOException {
	final byte[] bytes = new byte[length];
	dataInput.readFully(bytes);
	return bytes;
    }

    private int readU2() throws IOException {
	return dataInput.readUnsignedShort();
    }

    private int readU4() throws IOException {
	return dataInput.readInt();
    }
}