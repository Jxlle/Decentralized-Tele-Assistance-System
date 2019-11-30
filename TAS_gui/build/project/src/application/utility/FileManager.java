/*******************************************************************************
 * Copyright Yifan Ruan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package application.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Responsible for reading and writing between string and file
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class FileManager {

	private String filePath;
	private PrintWriter writer;
	private BufferedReader reader;
	private int mode=-1;
	
	// the mode
	public static final int WRITING=0;
	public static final int READING=1;
	
	/**
	 * Constructor
	 * @param filePath the file path
	 */
	public FileManager(String filePath){
		this.filePath=filePath;
	}
	
	/**
	 * Set the mode, WRITING or READING
	 * @param mode the specific mode
	 */
	public void setMode(int mode){
		if(mode==FileManager.WRITING || mode ==FileManager.READING)
			this.mode=mode;
		else
			System.out.println("Wrong mode!!!!");
	}
	
	/**
	 * Reset the file
	 */
	public void reset(){
	   	File file = new File(filePath);
    	if(file.exists() && !file.isDirectory()) {
    		file.delete();
    	}
	}
	
	/**
	 * Open 
	 */
	public void open(){
		try {
			if(mode==FileManager.WRITING)
				writer=new PrintWriter(new BufferedWriter(new FileWriter(filePath,true)));
			else
				reader = new BufferedReader(new FileReader(filePath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Close
	 */
	public void close(){
		try{	
			if(mode==FileManager.WRITING)
				writer.close();
			else
				reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	/**
	 * Write to file
	 * @param args the object to be written
	 */
	public void writeLine(Object... args){
		StringBuilder build=new StringBuilder();
		for(int i=0;i<args.length;i++)
			build.append(args[i]+",");
		writer.println(build.toString());
		writer.flush();
	}
	
	
	public void write(String str){
		writer.println(str);
		writer.flush();
	}
	
	
	/**
	 * Read from file
	 * @return the read string
	 */
	public String readLine(){
		try {
			return reader.readLine();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
