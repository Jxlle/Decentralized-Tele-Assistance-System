package application.utility;

import java.io.File;
import java.io.IOException;

public class Utility {

    public static void createFile(String filePath){
	File yourFile = new File(filePath);
	if(!yourFile.exists()) {
	    try {
		yourFile.createNewFile();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	} 
    }
}
