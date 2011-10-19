
package xtc.oop;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import xtc.parser.ParseException;
import xtc.parser.Result;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;
import xtc.tree.Location;

import xtc.lang.JavaFiveParser;
import xtc.lang.JavaPrinter;

import xtc.lang.CParser;
import xtc.lang.CPrinter;


public class DependencyParser extends xtc.util.Tool {

	public DependencyParser() {
	}
	
	public String getName() {
		return "Dependency Parser";
	}
	
	public String getCopy() {
		return "Group";
	}
	
	public Node parseDependencies(String[] addressArray, int addressIndex) throws IOException, ParseException {
		//Parse the dependency files - todo append to the main Java AST
		Node depTree;
		String curDir = System.getProperty("user.dir");
		
		try {
			//Locate the file
			File file = locate(curDir + addressArray[1]);
			//Open the file
			Reader in = runtime.getReader(file);
			//DependencyParser depParser = new DependencyParser();
			//depTree = depParser.parse(in, file);
			depTree = parse(in, file);
		} 
		catch (IOException e) {
			depTree = null;
			System.out.println("IOException");
		}
		catch (ParseException e) {
			depTree = null;
			System.out.println("ParseException");
		} 
		//runtime.console().pln().format(depTree).pln().flush();
		return depTree;
	}

	public Node parse(Reader in, File file) throws IOException, ParseException {
		
		JavaFiveParser parser =
				new JavaFiveParser(in, file.toString(), (int)file.length());
		Result result = parser.pCompilationUnit(0);
		
		return (Node)parser.value(result);
	}  
	
	public void appendDependency(Node baseAST) {
	
	}

}
