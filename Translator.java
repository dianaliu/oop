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
import java.io.IOException;
import java.io.Reader;

import java.util.ArrayList;



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
		bool("printAST", "printAST", false, "Print Java AST.").
		bool("testDependencies", "testDependencies", false, "Test dependency resolution.").
		bool("testVtable", "testVtable", false, "Test the creation of data structures for vtable and data layout").
		bool("testCPPPrinter", "testCPPPrinter", false, "Test the functionality of the CPPPrinter class").
		bool("translateToCPP", "translateToCPP", false, "Translate Java code to C++ without inheritance.");
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
		
	    if( runtime.test("testDependencies") ) {
			/*
					INSERT CODE TO TEST DEPENDENCY RESOLUTION
			 */
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
			
			
		
		if( runtime.test("translateToCPP") ) {
			// Where the MAGIC happens!
			
			/*
				Rough idea of what should go here:
				1. dependencyResolver dispatch on node
				2. makeVtablesAndDataLayout for node and return a new formed import node or null
				3. if a new import node is returned, add to tree and goto step 1.   (this means that more dependencies are needed)
				4. more tree analysis to format properly for cppprinter?
				5. send a structurally sound 'C++' AST to CPPPrinter
				6. handle the output (to files, I assume)
			 */
			
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
