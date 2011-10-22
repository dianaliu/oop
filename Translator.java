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
	
	/** Create a new translator. */
    public Translator() {
	    // Nothing to do.
	}
    
    public String getName() {
	    return "Java to C++ Translator";
	}
    
    public String getCopy() {
	    return "Diana, Hernel, Kirim, & Robert";
	}
	
    public String getVersion() {
		return "0.1";
    }
    
    public void init() {
	    super.init();  
		runtime.
		bool("printAST", "printAST", false, "Print AST.").
		bool("testDependencies", "testDependencies", false, "Test dependency resolution.").
		bool("testVtable", "testVtable", false, "Test the creation of data structures for vtable and data layout").
		bool("testCPPPrinter", "testCPPPrinter", false, "Test the functionality of the CPPPrinter class").
		bool("translateToCPP", "translateToCPP", false, "Translate Java code to C++ without inheritance.");
	}
    ///*
    public Node parse(Reader in, File file) throws IOException, ParseException {
	    JavaFiveParser parser =
		new JavaFiveParser(in, file.toString(), (int)file.length());
		Result result = parser.pCompilationUnit(0);
		return (Node)parser.value(result);
	}  //*/
	/*
	public Node parse(Reader in, File file) throws IOException, ParseException {
	    CParser parser =
		new CParser(in, file.toString(), (int)file.length());
		Result result = parser.pTranslationUnit(0);
		return (Node)parser.value(result);
	} //*/ 
    
    public void process(Node node) {
	    if (runtime.test("printAST")) {
		    runtime.console().format(node).pln().flush();
		}
		
	    if( runtime.test("testDependencies") ) {
			Node depTree;
			
			//
			//Print out the current working directory - helps with file I/O
			//
			System.out.println("\n------------------------------");
			System.out.println("The Current Working Directory");
			System.out.println("------------------------------");
			String curDir = System.getProperty("user.dir");
			System.out.println(curDir);
			
			//
			//Begin analyzing the AST to determine all dependencies
			//
			System.out.println("\n-------------------------------");
			System.out.println("Initial Dependency Analysis Stage");
			System.out.println("-------------------------------");
			DependencyVisitor depVisitor = new DependencyVisitor();
			
			//call DependencyVisitor on node and store the addresses inside 
			//DependencyVisitor.addressArray
			depVisitor.dispatch(node);
			depVisitor.printAddressArray();
			//parse the dependencies listed in addressArray and append to node
			try {
				depTree = depVisitor.parseArray(node);
			} 
			catch (IOException e) {
				depTree = null;
				System.out.println("IOException");
			}
			catch (ParseException e) {
				depTree = null;
				System.out.println("ParseException");
			} 
			
			runtime.console().pln().format(depTree).pln().flush();
			
			
			//
			//Begin analyzing the AST to create the VTable
			//
			/* 			System.out.println("\n-----------------------------");
			 System.out.println("Analyzing Virtual Table Stage");
			 System.out.println("-----------------------------");
			 
			 AnalyzeVisitor myVisitor = new AnalyzeVisitor();
			 myVisitor.dispatch(node);
			 new Visitor() {
			 public void visit(Node n){
			 System.out.println( n.getName() + " " + ((GNode)n).getProperty("VTable") );
			 for( Object o : n) {
			 if (o instanceof Node) dispatch((Node)o);
			 }
			 }
			 }.dispatch(myVisitor.getVtableTree());
			 runtime.console().format(myVisitor.getVtableTree()).pln().flush();  
			 */
			
			//Translate to C++ - to do
			//Print out C++ code - to do
		}
	    
	    if(runtime.test("testVtable")) {
			
			final ClassLayoutParser clp = new ClassLayoutParser(node);
			
			new Visitor() {
				// When we encounter a class in the AST, send it to 
				// ClassLayoutParser 
				public void visitClassDeclaration(GNode n) {
					clp.addClass(n);
					visit(n);
				} // end visitClassDeclaration
				
				public void visit(Node n) {
					for( Object o : n) {
						if (o instanceof Node) dispatch((Node)o);
					}
				}
			}.dispatch(node);
			runtime.console().format(clp.getClassTree()).pln().flush();
			runtime.console().pln("------------");
			//		     runtime.console().format(clp.getDataLayoutTree("Object")).pln().flush();
	    }// end transform	
		
		if( runtime.test("testCPPPrinter") ) {
			//This simple visitor is just used to place a dummy vtable declaration node into each classes class body
			//for testing purposes.  It also tests and implements mutability of the AST tree.
			new Visitor() {
				public void visit(Node n) {
					for( Object o : n ) if( o instanceof Node ) dispatch((Node)o);
				}
				
				public void visitClassDeclaration(GNode n) {
					n.set(5, GNode.ensureVariable(GNode.cast(n.getNode(5)))); //make sure the class body is mutable (num of nodes can be changed).  If not, it is made mutable.
					n.getNode(5).add(0, GNode.create("VirtualTableDeclaration")); //insert a vtabledecl node, currently has no functionality, just for testing
					visit(n);
				}
			}.dispatch(node);
			
			//The real CPPPrinter, initalized and dispatched...
			new CPPPrinter( runtime.console() ).dispatch(node); 
		}	
			
		
		if( runtime.test("translateToCPP") ) {
			// Where the MAGIC happens!
			
			LeafTransplant trsltr = new LeafTransplant(GNode.cast(node), GNode.cast(node));
			trsltr.translateJavaToCPP();
			GNode returned = trsltr.getTranslatedTree();
			//runtime.console().format(returned).pln().flush();
			new CPPPrinter( runtime.console() ).dispatch(returned);
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
