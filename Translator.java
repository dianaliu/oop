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

import xtc.lang.JavaFiveParser;
import xtc.lang.JavaPrinter;

import xtc.lang.CParser;
import xtc.lang.CPrinter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
	    return "Java to C++ Translator";
	}
    
    public String getCopy() {
	    return "Diana, Hernel, & Robert Kirim";
	}
	
    public String getVersion() {
		return "0.1";
    }
    
    public void init() {
	    super.init();  
	    runtime.
		bool("printAST", "printAST", false, "Print AST.").
		bool("debug", "debug", false, "Extra output for debugging").
		bool("run", "run", false, 
		     "Runs Translator with latest features").
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

	if( runtime.test("run") ) {
	    GNode[] trees = new GNode[100];  //FIXME: hardcoded array size
	    trees[0] = (GNode)node;
	    
	    //
	    //Print out the current working directory - helps with file I/O
	    //
	    /*
	      System.out.println("\n------------------------------");
	      System.out.println("The Current Working Directory");
	      System.out.println("------------------------------");
	      System.out.println(System.getProperty("user.dir"));
	    */
	    
	    //*****************************************************
	    //Begin analyzing the AST to determine all dependencies
	    //*****************************************************
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

	    //print out the dependency addresses
	    if(DEBUG) {
		runtime.console().
		    p("The below ASTs are stored in trees[]:").flush();
		depResolver.printAddressArray();
	    }
	    
	    /*
	    //print out the ASTs stored in trees[]
	    for(int i = 0; i < trees.length; i++) {
	    if(trees[i] != null){
	    if(i == 0)	System.out.println("$$Main java test file:");
	    else	System.out.println("$$Dependency file # " + i + ":");
	    runtime.console().pln().format(trees[i]).pln().pln().flush();
	    }
	    }
	    */
	    
	    //print out the number of ASTs stored in trees
	    if(DEBUG) {
		runtime.console().pln("# Active ASTs: " + 
				    depResolver.getTreeCount(trees)).flush();
	    }
	    
	    if(DEBUG) {
		runtime.console().pln("--Process and Analyze dependencies: done"
				      ).flush();
	    }
	    

      	    final ClassLayoutParser clp = new ClassLayoutParser(trees);
	
	    if(DEBUG) {
		runtime.console().pln("--Create vtables & data layouts: done"
				      ).flush();
	    }

	    // ---------- Sample usage of clp
	 
	}
	
	if( runtime.test("translate") ) {
	    
	    //Initialize and array of AST trees to hold all dependency files
	    if(DEBUG) runtime.console().pln("Beginning Translation...");
	    GNode[] trees = new GNode[100];  //FIXME: hardcoded array size
	    trees[0] = (GNode)node;
	    if(DEBUG) runtime.console().pln("Initialized AST Tree array.");

	    //Begin analyzing the AST to determine all dependencies
	    if(DEBUG) runtime.console().pln("Starting dependency resolution process...");
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
	    if(DEBUG) runtime.console().pln("Dependency resolution process complete.");
	    //FIXME: add an optional verbose debug statement with more information

	    //Initialize and run a ClassLayoutParser on the array of trees
      	    if(DEBUG) runtime.console().pln("Starting class inheritance hierarchy analysis process...");
	    final ClassLayoutParser clp = new ClassLayoutParser(trees);
	    if(DEBUG) runtime.console().pln("Inheritance analysis process complete.");
	    //FIXME: add an optional verbose debug statement with more information
	    
	    //Initialize and run the Java AST to CPP AST Translator class
	    if(DEBUG) runtime.console().pln("Starting Java -> CPP AST translation process...");
	    LeafTransplant translator = 
		new LeafTransplant(clp, GNode.cast(trees[0])); //FIXME: only one tree translated
	    translator.translateJavaToCPP();
	    GNode returned = translator.getTranslatedTree();
	    if(DEBUG) runtime.console().pln("AST Translation process complete.");

	    // Run a new CPP printer on the translated AST Tree
	    if(DEBUG) runtime.console().pln("Starting CPP AST Pretty Printing process...");
	    new CPPPrinter( runtime.console() ).dispatch(returned); //FIXME: output to a file instead
	    if(DEBUG) runtime.console().pln("Pretty Printing process complete.");
	    if(DEBUG) runtime.console().pln("Translation process complete.");
	    runtime.console().flush(); 
	}
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
