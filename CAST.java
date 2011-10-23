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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java.util.ArrayList;

import xtc.oop.LeafTransplant;


public class CAST extends xtc.util.Tool {
	
	/** Create a new translator. */
    public CAST() {
	    // Nothing to do.
	}
    
    public String getName() {
	    return "Print C AST";
	}
    
    public String getCopy() {
	    return "Diana, Hernel, Kirim, & Robert";
	}
	
    public String getVersion() {
		return "0.1";
    }
    
    public void init() {
	    super.init();  
	}
	
	public Node parse(Reader in, File file) throws IOException, ParseException {
	    CParser parser =
		new CParser(in, file.toString(), (int)file.length());
		Result result = parser.pTranslationUnit(0);
		return (Node)parser.value(result);
	}
    
    public void process(Node node) {
		runtime.console().format(node).pln().flush();
    } // end process
	
    public static void main(String[] args) {
		new CAST().run(args);
    }
    
}
