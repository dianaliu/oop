package xtc.oop;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.io.FileReader;

import java.util.List;
import java.util.*;
import java.io.*;

import xtc.parser.ParseException;
import xtc.parser.Result;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;
import xtc.tree.Location;
import xtc.tree.Printer;

import xtc.lang.JavaFiveParser;
import xtc.lang.JavaPrinter;

/* 
 * Traces dependencies for all Java files and returns an array of ASTs
 */

// Only accepts .java files
class JavaFilter implements FilenameFilter {
    public boolean accept(File dir, String name) {
        return (name.endsWith(".java"));
    }
}

public class DependencyResolver extends xtc.util.Tool {
	
	String[] addressArray = new String[500];
	int addressIndex = 1;
	GNode[] treeArray = new GNode[500];
	
	//default constructor
   	public DependencyResolver() {
	}
	
	//about this file
	public String getName() {
		return "Dependency Visitor";
	}
	
	//about this file
	public String getCopy() {
		return "Robert, Diana, Hernel, Kirim";
	}
	
	// Puts imported java files into the address array
    public void processDependencies(GNode[] trees) {
		int additionalTrees = 0;
		for(int i = 0; i < trees.length; i++) {
			//check if the node array position is not empty
			if(trees[i] != null) {
			new Visitor() {
		    
				//put addresses of imported java files  into addressArray[]
				public void visitImportDeclaration(GNode n) {
					boolean duplicate = false;
					String currentAddress = "";
					//read the import statements from the AST
					for (int i = 0; i < n.getNode(1).size(); i++){
						currentAddress += "/";
						currentAddress += ((String)(n.getNode(1).get(i)));
					}
					//if the import is a .*
					if(((String)(n.get(2))) != null) { 
						//call a function that takes the address of all java files in 
						//the directory(currentAddress) recursively to include subfolders
						//and add them to addressArray
						try{
							resolveCatchAll(currentAddress);
						}
						catch (FileNotFoundException e) {
							System.out.println("FileException resolveCatchAll");
						}
						//do not put wildcard directory into addressArray
						return;
					}
					//if the import is specific
					else {
						currentAddress += ".java";
					}
					//check is the dependency file has already been added
					for(int i = 0; i < addressArray.length; i++) {
						if(addressArray[i] != null) {
						if(addressArray[i].equals(currentAddress))
							duplicate = true;
						}
					}
					//if the dependency file is not a duplicate add to list
					if(duplicate != true) {
						addressArray[addressIndex] = currentAddress;
						System.out.println("Added specific dependency to list: " + currentAddress);
						addressIndex++;
					}
			
				}
				
				//put addresses of packaged java files into addressArray[] 
				public void visitPackageDeclaration(GNode n) {	
					String currentAddress = (System.getProperty("user.dir")) + "/src";
					//					System.out.println("Package declared.");
					//				        System.out.println("Running directory: " + currentAddress);
					for (int i = 0; i < n.getNode(1).size(); i++){
						currentAddress += "/";
						currentAddress += ((String)(n.getNode(1).get(i)));
						//System.out.println("Current package directory: " + currentAddress);
					}
					//addressArray[addressIndex] = currentAddress + "/Rest.java";
					addressArray[addressIndex] = "/Rest.java";
					addressIndex++;
					//process all java files in the current directory
				}
				
				public void visit(GNode n) {
					for (Object o : n) if (o instanceof GNode) dispatch((GNode)o);
				}
				
			}.dispatch(trees[i]);
			additionalTrees++;
			}
		}
    }

    
    //called when importing .* or a package is declared
    //reads in all java files in the directory currentAddress
    public void resolveCatchAll(String currentAddress) throws FileNotFoundException{
		boolean duplicate = false;
		String filename = "";
		//filename filter allows us to only read in java files
		FilenameFilter javaOnly = new JavaFilter(); 
		System.out.println("Catchall address: " + (System.getProperty("user.dir")).concat("/src/xtc/oop" + currentAddress));
		File folder = new File((String)(System.getProperty("user.dir")).concat("/src/xtc/oop" + currentAddress));
		File listOfFiles[] = folder.listFiles(javaOnly);
		
	 	File startingDirectory = new File((System.getProperty("user.dir")).concat("/src/xtc/oop" + currentAddress));
		List<File> files = getFileListing(startingDirectory);


		//print out all file names, in the the order of File.compareTo()
		//System.out.println("Printing catchall folder heirarchy.");
		for(File file : files ){
		  //System.out.println(file.getPath());
		  if (file.isFile() && file.getPath().endsWith(".java")) {
				//System.out.println("Found a java catchall");
				StringTokenizer toke = new StringTokenizer(file.getPath(), "/", true);
				while(toke.hasMoreTokens()){
					String currentToken = toke.nextToken();
					if(currentToken.equals("oop")){
						filename.concat(currentToken);
						while(toke.hasMoreTokens()){
							filename+=(toke.nextToken());
						}
					}
				}
				//System.out.println("Imported via catchall: " + filename);
				//check is the dependency file has already been added
				for(int j = 0; j < addressArray.length; j++) {
					if(addressArray[j] != null) {
					if(addressArray[j].equals(filename))
						duplicate = true;
					}
				}
				//if the dependency file is not a duplicate add to list
				if(duplicate != true) {
					addressArray[addressIndex] = filename;
					System.out.println("Added catchall dependency to list: " + addressArray[addressIndex]);
					filename = "";
					addressIndex++;
				}
			} 
		}

    }
    
    
    //uses the address array to return a GNode array 
    //the main java test file's AST is at index 0
    //dependency files AST are at index >= 1
    public GNode[] parseDependencies() throws IOException, ParseException {
		GNode depTree[] = new GNode[500];
		String curDir = System.getProperty("user.dir");

		for(int i = 1; i < addressArray.length; i++) {
			//only do the following for elements of addressArray that has actual file info
			if(addressArray[i] != null) {
				try {
					//Locate and open the file
				    //				    System.out.print("Adding dependency node: " + curDir + "/src/xtc/oop" + addressArray[i]);
					File file = locate(curDir + "/src/xtc/oop" + addressArray[i]);
					Reader in = runtime.getReader(file);
					depTree[i] = parse(in, file);
					//					System.out.println(" -> DONE");
				} 
				catch (IOException e) {
					depTree[i] = null;
					System.out.println("IOException parseDependency");
				}
				catch (ParseException e) {
					depTree[i] = null;
					System.out.println("ParseException parseDependency");
				} 
			}
		}
		return depTree;
    } 



  static public List<File> getFileListing(
    File aStartingDir
  ) throws FileNotFoundException {
    validateDirectory(aStartingDir);
    List<File> result = getFileListingNoSort(aStartingDir);
    Collections.sort(result);
    return result;
  }

  // PRIVATE //
  static private List<File> getFileListingNoSort(
    File aStartingDir
  ) throws FileNotFoundException {
	FilenameFilter javaOnly = new JavaFilter(); 
    List<File> result = new ArrayList<File>();
    File[] filesAndDirs = aStartingDir.listFiles();
    List<File> filesDirs = Arrays.asList(filesAndDirs);
    for(File file : filesDirs) {
      result.add(file); //always add, even if directory
      if ( ! file.isFile() ) {
        //must be a directory
        //recursive call!
        List<File> deeperList = getFileListingNoSort(file);
        result.addAll(deeperList);
      }
    }
    return result;
  }

  /**
  * Directory is valid if it exists, does not represent a file, and can be read.
  */
  static private void validateDirectory (
    File aDirectory
  ) throws FileNotFoundException {
    if (aDirectory == null) {
      throw new IllegalArgumentException("Directory should not be null.");
    }
    if (!aDirectory.exists()) {
      throw new FileNotFoundException("Directory does not exist: " + aDirectory);
    }
    if (!aDirectory.isDirectory()) {
      throw new IllegalArgumentException("Is not a directory: " + aDirectory);
    }
    if (!aDirectory.canRead()) {
      throw new IllegalArgumentException("Directory cannot be read: " + aDirectory);
    }
  }


    
    //mark only the dependency files which are invoked 
    public GNode[] trimDependencies(ClassLayoutParser clp, GNode[] depTree) {
    	new Visitor() {
		    	public void visitClass(GNode n) {
					System.out.println("CLASS: " + n.getProperty("name"));
					visit(n);
				} 
		    
				public void visit(GNode n) {
					for (Object o : n) if (o instanceof GNode) dispatch((GNode)o);
				}
		}.dispatch(clp.classTree);
		return depTree;
    } 
    
    //return a Java AST from the file directory passed into the method
    public GNode parse(Reader in, File file) throws IOException, ParseException {
		JavaFiveParser parser =
	   	 	new JavaFiveParser(in, file.toString(), (int)file.length());
		Result result = parser.pCompilationUnit(0);
	
		return (GNode)parser.value(result);
    }  
    
    //return the String array containing the addresses of dependencies
    public String[] getAddressArray() {
		return addressArray;
    }
    
    //return the number of AST trees
    public int getTreeCount(GNode[] trees) {
		int count = 0;
		for(int i = 0; i < trees.length; i++) {
			if(trees[i] != null)
			count++;
		}
		return count;
    }
    
    //print out the contents of the addressArray which contains the
    //directories of the dependency java files
    public void printAddressArray() {
		for(int i = 0; i < addressArray.length; i++) {
			if(addressArray[i] != null)
			System.out.println(addressArray[i]);
		}
		System.out.println();
    }
    
}


