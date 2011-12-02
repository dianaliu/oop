/*
 * xtc - The eXTensible Compiler
 * Copyright (C) 2011 Robert Grimm
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 * USA.
 */
package xtc.oop;
import xtc.oop.LeafTransplant;

import xtc.parser.ParseException;
import xtc.parser.Result;

import xtc.tree.Attribute;
import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;
import xtc.tree.Location;
import xtc.tree.Printer;

import xtc.util.SymbolTable;
import xtc.util.SymbolTable.Scope;

import xtc.lang.CParser;
import xtc.lang.CPrinter;
import xtc.lang.JavaFiveParser;
import xtc.lang.JavaPrinter;
import xtc.lang.JavaAnalyzer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java.util.ArrayList;




public class Translator extends xtc.util.Tool {
	
	
    public static boolean DEBUG = false;
	
	/** Create a new translator. */
    public Translator() {
	    // Nothing to do.
	}
    
    public String getName() {
	    return "\n*** Java to C++ Translator";
	}
    
    public String getCopy() {
	    return "Diana, Hernel, & Robert Kirim ***";
	}
	
    public String getVersion() {
		return "0.2";
    }
    
    public void init() {
	    super.init();  
	    runtime.
		bool("printAST", "printAST", false, "Print Java AST.").
		bool("debug", "debug", false, "Extra output for debugging").
		bool("inherit", "inherit", false, "Test inheritance analysis, includes DEBUG flag.").
		bool("translate", "translate", false, 
		     "Translate Java code to C++ without inheritance.");
    }
	
    public Node parse(Reader in, File file) throws IOException, ParseException {
	    JavaFiveParser parser =
		new JavaFiveParser(in, file.toString(), (int)file.length());
	    Result result = parser.pCompilationUnit(0);
	    return (Node)parser.value(result);
    }  
	
    public void process(Node node) {
	if (runtime.test("printAST")) {
	    runtime.console().format(node).pln().flush();
	}
	
	if(runtime.test("debug")) {
	    DEBUG = true;
	}
	
	if( runtime.test("translate") ) {
	    
	    runtime.console().pln("--- Begin translation").flush();
	    
	    // Create an array of AST trees to hold each dependency file
	    // FIXME: Do not hardcode size
	    GNode[] trees = new GNode[100];  
	    trees[0] = (GNode)node;
	    
	    runtime.console().pln("--- Begin dependency analysis").flush();
	    
	    // Analyze the main Java AST to find & resolve dependencies
	    // TODO: Only create ASTs for those Classes used & imported
	    DependencyResolver depResolver = new DependencyResolver();
	    depResolver.processDependencies(trees);
	    try {
		trees = depResolver.parseDependencies();
		trees[0] = (GNode)node;
	    } 
	    catch (IOException e) {
		trees = null;
		runtime.console().pln("IOException: " + e).flush();
	    }
	    catch (ParseException e) {
		trees = null;
		runtime.console().pln("ParseException: " + e).flush();
	    }	
	    
	    runtime.console().pln("--- Finish dependency analysis").flush();
	    
	    runtime.console().pln("--- Begin inheritance analysis").flush();
	    
	    // Parse all classes to create vtables and data layouts
	    final ClassLayoutParser clp = new ClassLayoutParser(trees, DEBUG);
	    if(DEBUG) 
			runtime.console().pln("--- Finish inheritance analysis").flush();
	   	//----------------------------------------------------------------------
	   	

	    if(DEBUG) 
			runtime.console().pln("--- Begin trimming dependencies").flush();
		// Mark only the dependency files which are actually invoked
		// trees = depResolver.trimDependencies(clp, trees);
	    if(DEBUG) 
			runtime.console().pln("--- Finished trimming dependencies").flush();
	   	//----------------------------------------------------------------------
	   	
	   	
	   	
	   	
	   	//FIXME: SymTable test; remove later
	   	System.out.println("\nMESSING WITH THE SYMBOL TABLE\n");
	   	SymbolTable symTable = new SymbolTable();
	   	JavaAnalyzer myAnalyzer = new JavaAnalyzer(runtime, symTable);
	   	
	   	
	   	
	   	/*
	   	TranslatorSymbolTable tst = new TranslatorSymbolTable("Global");
	    //System.out.println("Symbol identifier value : " + tst.getType("intGlobal"));
 		//find all variable names and their types
 		
 			tst.addSymbols(trees[0]);
	 		tst.symTable.root();
	 		tst.symTable.enter("depScope");
	 		tst.addSymbols(trees[1]);
 		
 		
 		tst.addSymbols(trees[0]);
 		String depScope = "dep";
 		for(int i = 1; i < trees.length; i++) {
 			if(trees[i] != null){
		 		
		 		tst.symTable.root();
		 		tst.symTable.enter(depScope + i);
		 		tst.addSymbols(trees[i]);
	 		}
	 	}
 		
 		tst.symTable.root();
 		//System.out.println("Get type intGlobal: " + tst.getType("intGlobal"));
 		System.out.println("Get type intMain: " + tst.getType("intGlobal"));
 		//System.out.println("Get type intMeth: " + tst.getType("intMeth"));
 		*/
 		
 		System.out.println("\nDONE MESSING WITH THE SYMBOL TABLE\n");
	   	
	   	
	   	
	   	
	    if(DEBUG) 
			runtime.console().pln("--- Begin cpp translation").flush();
	    // Create a translator to output a cpp tree for each java ast
	    // FIXME: Do not hardcode size
	    GNode[] returned = new GNode[500];
	    LeafTransplant translator = 
		new LeafTransplant(clp, GNode.cast(trees[0]), DEBUG);
	    
	    for(int i = 0; i < trees.length; i++) {
		if(trees[i] != null) {
		    translator = 
			new LeafTransplant(clp, GNode.cast(trees[i]), DEBUG); 
		    returned[i] = translator.getCPPTree();
		    
		    if(DEBUG) runtime.console().pln("--- CPP AST #" + (i+1));
		    if(DEBUG) runtime.console().format(returned[i]).pln().flush();
		    if(DEBUG) 
			runtime.console().pln("\t-----------------------").flush();
		}
	    }
	    
	    runtime.console().pln("--- Finish cpp translation").flush();
	    
	    runtime.console().pln("--- Begin writing CPP file(s)").flush();
	    
	    // Run CPP printer on each CPP Tree and output to Code.cpp
	    try {		
		// Create an output directory
		boolean status = new File("output/").mkdir();
		
		// TODO: Clear directory if not empty
		
		for(int i = 0; i < returned.length; i++) {
		    if(returned[i] != null)
			{
			    // Name our file
			    GNode root = GNode.cast(returned[i]);
			    String fileName = "output/";
			    fileName += root.getString(root.size() - 1);
			    fileName += ".cpp";
			    
			    PrintWriter fstream = new PrintWriter(fileName);
			    Printer cppCode = new Printer(fstream);
			    
			    new CPPPrinter( cppCode ).dispatch(returned[i]); 
			    cppCode.flush();
			    if(DEBUG) runtime.console().pln("--- Wrote " 
							    + fileName);
			}
		}
		
		runtime.console().pln("--- Finish writing CPP file(s)").flush();
		// Also print cpp output to console?
		runtime.console().pln("--- Finish translation. See output/").flush();
		
	    }
	    catch(Exception e) {
		System.err.println(e.getMessage());
	    }
	    

	    
	} // end -translate
	
	
	if(runtime.test("inherit")) {
	    DEBUG = true;
	    
	    runtime.console().pln("--- Begin translation").flush();
	    
	    // Create an array of AST trees to hold each dependency file
	    // FIXME: Do not hardcode
	    GNode[] trees = new GNode[100];
	    trees[0] = (GNode)node;
	    
	    runtime.console().pln("--- Begin dependency analysis").flush();
	    
	    // Analyze the main Java AST to find & resolve dependencies
	    DependencyResolver depResolver = new DependencyResolver();
	    depResolver.processDependencies(trees);
	    try {
		trees = depResolver.parseDependencies();
		trees[0] = (GNode)node;
	    }
	    catch (IOException e) {
		trees = null;
		System.out.println("IOException: " + e);
	    }
	    catch (ParseException e) {
		trees = null;
		System.out.println("ParseException: " + e);
	    }
	    
	    runtime.console().pln("--- Finish dependency analysis").flush();
	    
	    runtime.console().pln("--- Begin inheritance analysis").flush();
	    
	    // Parse all classes to create vtables and data layouts
	    final ClassLayoutParser clp = new ClassLayoutParser(trees, DEBUG);
	    
	    runtime.console().pln("--- Finish inheritance analysis").flush();
	} // end -inherit
	
	
    } // end process
    
    /**
     * Run the translator with the specified command line arguments.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
		new Translator().run(args);
    }
    
}
