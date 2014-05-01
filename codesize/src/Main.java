import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;

import codesize.Codesize;


public class Main {

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
	File file=new File("C:\\Java\\rumble-1.7.3.2-1\\robots\\abc.ShadowTeam_3.83.jar");
	ZipInputStream in=new ZipInputStream(new FileInputStream(file));
	System.out.println(Codesize.processZipFile(file, in).getCodeSize());

    }

}
