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
		    bool("backToJava", "backToJava", false, 
			 "Print Java AST to Java code.").
		    bool("backToC", "backToC", false, "Print C AST to C code.").
		    bool("test", "test", false, "Run our translator project").
		    bool("transform", "transform", false, "Transforming nodes");
	}
    
    public Node parse(Reader in, File file) throws IOException, ParseException {
	    JavaFiveParser parser =
		    new JavaFiveParser(in, file.toString(), (int)file.length());
		Result result = parser.pCompilationUnit(0);
		return (Node)parser.value(result);
	}  
    
    //Parse C file
	/*    public Node parse(Reader in, File file) throws IOException, ParseException {
	      CParser parser =
	 new CParser(in, file.toString(), (int)file.length());
	 Result result = parser.pTranslationUnit(0);
	 return (Node)parser.value(result);
	 }   */
    
    public void process(Node node) {
	    if (runtime.test("printAST")) {
		    runtime.console().format(node).pln().flush();
		}
		
		if( runtime.test("backToJava") ) {
		    new JavaPrinter( runtime.console() ).dispatch(node);
			runtime.console().flush();
		}  
		
		if( runtime.test("backToC") ) {
		    new CPrinter( runtime.console() ).dispatch(node);
			runtime.console().flush();
		}
		
		//overwrites info in nodes
		//NOTE: make >> output.txt writes terminal output to file
		if( runtime.test("test") ) {
		    
		    //Returns Class Hiearchy Tree
		    ClassParser  cp  = new ClassParser();
		    cp.dispatch(node);
		    new Visitor() {
			public void visit(Node n) {
			    if (n.hasProperty("Methods")) {
			
			    }
		       			    
			    for( Object o : n) {
				if (o instanceof Node) dispatch((Node)o);
			    }
			}
		    }.dispatch(cp.getClassTree());

		    //print class hierarchy tree
		    runtime.console().format(cp.getClassTree()).pln().flush();
		} // end test
		
		if(runtime.test("transform")) {
		    
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
		     
		     
		     

		} // end transform		      
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
