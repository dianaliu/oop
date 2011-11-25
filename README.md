#About#
This is the final project for [Object Oriented Programming](http://cs.nyu.edu/rgrimm/teaching/fa11-oop/) - a Java to CPP translator coded by Diana, Hernel, Kirim, and Rob.

##How to run##
First off, you need [xtc](http://cs.nyu.edu/rgrimm/xtc/).  Then,

    cd xtc/
    cp src/xtc/oop/magic.sh magic.sh
    source magic.sh JAVAFILE
    
or

    cd xtc/
    java xtc.oop.Translator -translate JAVAFILE

##What does it do?##
- Resolve and translate all dependencies in the same package
- Inheritance using vtables and data layouts
- Translate new classes and methods
- (Method overloading)
- (Memory management using smart pointers)

##What does it not do?##
Our translator only supports a subset of the Java language. For more details, see the [specifications] (http://cs.nyu.edu/rgrimm/teaching/fa11-oop/#languages).