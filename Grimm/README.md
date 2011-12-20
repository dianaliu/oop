#About#
This is the final project for [Object Oriented Programming](http://cs.nyu.edu/rgrimm/teaching/fa11-oop/) - a Java to CPP translator coded by Diana Liu, Robert Huebner, Hernel Guerrero, and Kirim Kirimli.

Using our modified Test.java, we output: 

--------------------------------------------------------------------
PASS Test.main()
PASS Object.<init>()
PASS short[0].length
PASS short[1].length
PASS short[2].length
PASS short[i] == 0
PASS short[0] = (short)32768

7 out of 7 tests have passed.
--------------------------------------------------------------------


##How to run##

    cd xtc/
    cp PATH/TO/final.jar PATH/TO/Final.sh PATH/TO/Test.java .
    source Final.sh Test.java
    
or

    cd xtc/
    java xtc.oop.Translator -translate Test.java

##What does it do?##
- Resolve and translate all dependencies in the same package
- Inheritance using vtables and data layouts
- Translate new classes and methods
- Method overloading
- Method overriding
- Memory management using smart pointers

##What does it not do?##
Our translator only supports a subset of the Java language. For more details, see the [specifications] (http://cs.nyu.edu/rgrimm/teaching/fa11-oop/#languages).