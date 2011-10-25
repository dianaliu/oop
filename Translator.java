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

import xtc.parser.ParseException;
import xtc.parser.Result;

import xtc.tree.Attribute;
import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;
import xtc.tree.Location;
import xtc.tree.Printer;

import xtc.lang.CParser;
import xtc.lang.CPrinter;
import xtc.lang.JavaFiveParser;
import xtc.lang.JavaPrinter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java.util.ArrayList;

import xtc.oop.LeafTransplant;


public class Translator extends xtc.util.Tool {
	

    public static boolean DEBUG = false;

	/** Create a new translator. */
    public Translator() {
	    // Nothing to do.
	}
    
    public String getName() {
	    return "\n********** Java to C++ Translator";
	}
    
    public String getCopy() {
	    return "Diana, Hernel, & Robert Kirim **********";
	}
	
    public String getVersion() {
		return "0.1";
    }
    
    public void init() {
	    super.init();  
	    runtime.
		bool("printAST", "printAST", false, "Print Java AST.").
		bool("debug", "debug", false, "Extra output for debugging").
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
	  
	    if(DEBUG) 
		runtime.console().pln("--- Begin translation").flush();

	    // Create an array of AST trees to hold each dependency file
	    // FIXME: Do not hardcode
	    GNode[] trees = new GNode[100];  
	    trees[0] = (GNode)node;

	    if(DEBUG) 
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
    
	    if(DEBUG) 
		runtime.console().pln("--- Finish dependency analysis").flush();


	    if(DEBUG) 
		runtime.console().pln("--- Begin inheritance analysis").flush();

	    // Parse all classes to create vtables and data layouts
	    final ClassLayoutParser clp = new ClassLayoutParser(trees, DEBUG);
	    
	    if(DEBUG) 
		runtime.console().pln("--- Finish inheritance analysis").flush();
	   	 

	    if(DEBUG) 
		runtime.console().pln("--- Begin cpp translation").flush();
   
	    // Create a translator to output a cpp tree for each java ast
	    // FIXME: Do not hardcode size
	    GNode[] returned = new GNode[100];
	    LeafTransplant translator = 
		new LeafTransplant(clp, GNode.cast(trees[0]));

	    for(int i = 0; i < trees.length; i++) {
	    	if(trees[i] != null)
		    {
	    		translator = 
			    new LeafTransplant(clp, GNode.cast(trees[i])); 
			returned[i] = translator.getTranslatedTree();
		    }
	    }

	    if(DEBUG) 
		runtime.console().pln("--- Finish cpp translation").flush();
	   

	    if(DEBUG) 
		runtime.console().pln("--- Begin printing cpp tree(s)").flush();
 
	    // Run CPP printer on each CPP Tree and output to Code.cpp
	    // FIXME: Support multiple outputs
	    try{
			PrintWriter fstream = new PrintWriter("Code.cpp");
			Printer cppCode = new Printer(fstream);
		
			for(int i = 0; i < returned.length; i++) {
				if(returned[i] != null)
				{
		   		 	new CPPPrinter( cppCode ).dispatch(returned[i]); 
		   		}
		   	}

			if(DEBUG) 
			    runtime.console().pln("--- Finish printing cpp tree(s)").flush();

			if(DEBUG) 
			    runtime.console().pln("--- Finish translation").flush();
			cppCode.flush();
	    }
	    catch(Exception e) {
	    	System.err.println(e.getMessage());
	    }
	}
    } // end translate
    
    /**
     * Run the translator with the specified command line arguments.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
	new Translator().run(args);
    }
    
}
