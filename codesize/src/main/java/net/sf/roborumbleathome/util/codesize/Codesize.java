package net.sf.roborumbleathome.util.codesize;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.sf.roborumbleathome.util.codesize.classfile.ClassFile;
import net.sf.roborumbleathome.util.codesize.classfile.ClassFileBuilder;

public final class Codesize {

    private static final String CLASS_EXTENSION = ".class";
    private static final String JAR_EXTENSION = ".jar";

    public static int processJar(final InputStream inputStream) {
	int codesize = 0;
	final ZipInputStream zipInputStream = new ZipInputStream(inputStream);
	try {
	    ZipEntry zipEntry;
	    do {
		zipEntry = zipInputStream.getNextEntry();
		if (zipEntry != null) {
		    final String name = zipEntry.getName().toLowerCase();
		    if (name.endsWith(CLASS_EXTENSION)) {
			codesize += processClass(zipInputStream);
		    } else if (name.endsWith(JAR_EXTENSION)) {
			codesize += processJar(zipInputStream);
		    }
		}
	    } while (zipEntry != null);
	    return codesize;

	} catch (final IOException e) {
	    throw new RuntimeException(e);
	}
    }

    public static int processClass(final InputStream inputStream) {
	final ClassFileBuilder classFileBuilder = new ClassFileBuilder(inputStream);
	final ClassFile classFile = classFileBuilder.buildClassFile();
	return classFile.getCodesize();
    }

}