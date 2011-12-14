#!/bin/bash  

clear

 echo  "Translates and compiles/runs all java files in src/xtc/oop/examples/"

 # 0. Make/clean output directory
if [ ! -d "output" ]
       then
           mkdir "output"
	   echo "--- Created directory output/"
      else
	   echo "Found existing directory output/"
           read -p "rm all files in output/ (y/n)?" choice
           case "$choice" in 
             y|Y ) echo "rm-ing all files in output/"
		   rm output/*;;
             n|N ) echo "please rename existing output/ and try again"
		   return 1;;
             * ) echo "invalid";;
           esac

           echo "Files will write to output/"
fi


 cd src/xtc/oop/examples

   for file in *.java ; do
	JavaFiles=("${JavaFiles[@]}" "$file")
	echo "$file: "

	# Compile and run Java file
	javac "$file" || {echo "ERR: Java compile time"}
	java "$file" > ../../../../../output/j.txt || {echo "ERR: Java run time"}

	# Translate the file
	cd ../../../..
	source setup.sh
	make > /dev/null || {echo "ERR: Translator compile time"}
	java xtc.oop.Translator -translate $file > /dev/null || {echo "ERR: Translator run time"}
	
	# Compile and run CC file, check output
	# May need to force overwriting
	cd output/

	cp ../src/xtc/oop/include/* .
	for file in *.cc ; do
		g++ "$file" java_lang.cc || {echo "ERR: CPP compile time"}
		./a.out > "c.txt" || {echo "ERR: CPP run time"}
 	done
	cd ..

	 if diff j.txt c.txt  > /dev/null ; then
	
	  echo "--- Pass"
	  # cat c.txt

   	 else 
 	  echo "--- ERR: Outputs were different."
	  # echo "--- See diff --side-by-side below:"
	  # diff j.txt c.txt --side-by-side

   done

# read -p "End of program - Press Enter to quit."


