#!/bin/bash  

clear

# This script and Test.java and Rest.java should be in xtc/

 echo  "Test Script: Runs Grimm's test files."

  # 1. Get Java file(s) from command line and add to array javaInput
 javaInput=()

 # 2. Compile and run Java file.
 for var in "$@"
         do
         cp "$var" "src/xtc/oop/output/"$var""

	 echo "--- Received input file: $var"
         javac "src/xtc/oop/output/$var" || { echo "--- ERR: Java compile time error"; return 1; }
	 echo "--- Compiled $var"
         # 3. Save output from Java files
	 j=java
	 fileName=${var%.$j} # remove .java 

         javaInput=("${javaInput[@]}" "$var")   
done 


# 3. Run the java files
 java xtc.oop.Test > "src/xtc/oop/output/j.txt" ||  { echo "--- ERR: Java runtime error"; return 1; }
 echo "---  Ran Test.java and saved output to src/xtc/oop/output/j.txt"

 # 4. Translate Java file
 source setup.sh
 make
 java xtc.oop.Translator -translate "Test.java"

 
 # 5. Get .cpp files from xtc/output/ created by Translator
 cd output/

 cppOutput=()
 for file in *.cc ; do
     cp "$file" "../src/xtc/oop/output/$file"
     cppOutput=("${cppOutput[@]}" "$file")
 done

 cd ..

# 5.5 Copy Grimm's java_lang etc. files
 cp src/xtc/oop/include/* src/xtc/oop/output/

 # 6. Compile CPP files.
 g++ src/xtc/oop/output/Test.cc src/xtc/oop/output/Rest.cc src/xtc/oop/output/java_lang.cc || { echo "--- ERR: CPP compile time error";  return 1; }
  echo "--- Compiled CC files"

 # 7. Run and save output from CPP files
  cd src/xtc/oop/output
  ./a.out > "c.txt" || { echo "--- ERR: CPP runtime error"; 
      cd ../../../..; return 1; }
    echo "--- Ran CC files and saved output to c.txt"
    cd ../../../..

 # 8. Compare Java and CPP output
    echo "--------------------------------------------------------------------"

	echo "--- Output"
	cat src/xtc/oop/output/c.txt

    echo "--------------------------------------------------------------------"

 #  Return to xtc/  

 return 0;

# read -p "End of program - Press Enter to quit."


