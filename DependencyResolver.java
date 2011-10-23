package xtc.oop;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.io.FileReader;

import xtc.parser.ParseException;
import xtc.parser.Result;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;
import xtc.tree.Location;
import xtc.tree.Printer;

import xtc.lang.JavaFiveParser;
import xtc.lang.JavaPrinter;

//create a filenamefilter that only accepts files ending in .java
class JavaFilter implements FilenameFilter {
    public boolean accept(File dir, String name) {
        return (name.endsWith(".java"));
    }
}

public class DependencyResolver extends xtc.util.Tool {
	
	String[] addressArray = new String[100];
	String[] directoryArray = new String[100];
	int addressIndex = 1;
	int directoryIndex = 0;
	
	GNode[] treeArray = new GNode[100];
	
	//default constructor
   	public DependencyResolver() {
   		addressArray[0] = "/Test.java";
	}
	
	//about this file
	public String getName() {
		return "Dependency Visitor";
	}
	
	//about this file
	public String getCopy() {
		return "Robert, Diana, Hernel, Kirim";
	}
	
	//puts imported java files into the address array
	//@param trees: array of ASTs we want to search for dependencies
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
							//the directory and add them to addressArray
							resolveCatchAll(currentAddress);
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
							addressIndex++;
						}
						
					}
			
					//put addresses of packaged java files into addressArray[] 
					//NOT WORKING AS EXPECTED - TO BE FIXED
					public void visitPackageDeclaration(GNode n) {	
						String packAddress = System.getProperty("user.dir");
						for (int i = 0; i < n.getNode(1).size(); i++){
							packAddress += "/";
							if(((String)(n.getNode(1).get(i))) != null)
								packAddress += ((String)(n.getNode(1).get(i)));
							System.out.println("Package at: " + packAddress);
						}
						//addressArray[addressIndex] = currentAddress;
						//addressIndex++;
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
	//@param currentAddress: the address of the directory holding the dependencies
	public void resolveCatchAll(String currentAddress) {
		//filename filter allows us to only read in java files
		FilenameFilter javaOnly = new JavaFilter(); 
		File folder = new File((System.getProperty("user.dir")).concat(currentAddress));
		File[] listOfFiles = folder.listFiles(javaOnly);
		boolean duplicate = false;
		String filename = "";

		for (int i = 0; i < listOfFiles.length; i++) {
		  if (listOfFiles[i].isFile()) {
			filename = currentAddress.concat("/").concat(listOfFiles[i].getName());
			
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
				filename = "";
				addressIndex++;
			}
			
		  } 
		}
	}
	
	
	//uses the address array to return a Node array 
	//the main java test file's AST is at index 0
	//dependency files AST are at index >= 1
	//@return: the updated array of ASTs
	public GNode[] parseDependencies() throws IOException, ParseException {
		GNode depTree[] = new GNode[100];
		String curDir = System.getProperty("user.dir");
		for(int i = 1; i < addressArray.length; i++) {
			//only do the following for elements of addressArray that has actual file info
			if(addressArray[i] != null) {
				try {
					//Locate and open the file
					File file = locate(curDir + addressArray[i]);
					Reader in = runtime.getReader(file);
					depTree[i] = parse(in, file);
				} 
				catch (IOException e) {
					depTree[i] = null;
					System.out.println("IOException parseDependencies");
				}
				catch (ParseException e) {
					depTree[i] = null;
					System.out.println("ParseException parseDependencies");
				} 
			}
		}
		return depTree;
	} 
	
	//return a Java AST from the file directory passed into the method
	//@param in: Java Reader object
	//@param file: contains address of the dependency
	public GNode parse(Reader in, File file) throws IOException, ParseException {
		
		JavaFiveParser parser =
				new JavaFiveParser(in, file.toString(), (int)file.length());
		Result result = parser.pCompilationUnit(0);
		
		return (GNode)parser.value(result);
	}  
	
	//@return: the String array containing the addresses of dependencies
	public String[] getAddressArray() {
		return addressArray;
	}
	
	//@return: the number of AST trees
	public int getTreeCount(GNode[] trees) {
		int count = 0;
		for(int i = 0; i < trees.length; i++) {
			if(trees[i] != null)
				count++;
		}
		return count;
	}
	
	//print out the contents of the addressArray which contains the
	//addresses of the dependency java files
	public void printAddressArray() {
		for(int i = 0; i < addressArray.length; i++) {
			if(addressArray[i] != null)
				System.out.println(addressArray[i]);
		}
		System.out.println();
	}
	
}


