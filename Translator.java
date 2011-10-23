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
		bool("dependency", "dependency", false, "Test dependency resolution.").
		bool("vtable", "vtable", false, "Test the creation of data structures for vtable and data layout").
		bool("testCPPPrinter", "testCPPPrinter", false, "Test the functionality of the CPPPrinter class").
		bool("translateToCPP", "translateToCPP", false, "Translate Java code to C++ without inheritance.");
	}
    ///*
    public Node parse(Reader in, File file) throws IOException, ParseException {
	    JavaFiveParser parser =
		new JavaFiveParser(in, file.toString(), (int)file.length());
		Result result = parser.pCompilationUnit(0);
		return (Node)parser.value(result);
    }  
	
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
		
	      if( runtime.test("dependency") ) {
	      	GNode[] trees = new GNode[100];
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
			System.out.println("\n-------------------------------");
			System.out.println("START Dependency Analysis Stage");
			System.out.println("-------------------------------");
			
			DependencyResolver depResolver = new DependencyResolver();
			depResolver.processDependencies(trees);
			try {
				trees = depResolver.parseDependencies();
				trees[0] = (GNode)node;
			} 
			catch (IOException e) {
				trees = null;
				System.out.println("IOException Translator->ParseDependencies()");
			}
			catch (ParseException e) {
				trees = null;
				System.out.println("ParseException Translator->ParseDependencies()");
			}
			
			//print out the dependency addresses
			System.out.println("The following ASTs are stored in trees[]:");
			depResolver.printAddressArray();
			//print out the ASTs stored in trees[]
			/*
			for(int i = 0; i < trees.length; i++) {
				if(trees[i] != null){
					if(i == 0)	System.out.println("$$Main java test file:");
					else	System.out.println("$$Dependency file # " + i + ":");
					runtime.console().pln().format(trees[i]).pln().pln().flush();
				}
			}
			*/
			
			//print out the number of ASTs stored in trees
			//int numtrees = depResolver.getTreeCount(trees);
			//System.out.println(numtrees);
			
			System.out.println("\n-------------------------------");
			System.out.println("END Dependency Analysis Stage");
			System.out.println("-------------------------------\n");
			
			final ClassLayoutParser clp = new ClassLayoutParser(trees);
			clp.beginCLP(trees);
			clp.pClassTree();
	    }
	    

	    if(runtime.test("vtable")) {
	    	GNode[] trees = new GNode[100];
	      	trees[0] = (GNode)node;
			
			final ClassLayoutParser clp = new ClassLayoutParser(trees);
		
			clp.beginCLP(trees);
			clp.pClassTree();

			//		System.out.println("Looking for String, found " + 
			//				   clp.getName(clp.getClass("String")) );

	

			//		runtime.console().format(clp.getClassTree()).pln().flush();
	
	    }	
	    



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
