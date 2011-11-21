#!/bin/bash  

clear

 echo  Test Script: Compares Java output to translated CPP output.

 # 0. Make/clean output directory
if [ ! -d "output" ]
       then
           mkdir "output"
	   echo "Created directory output/"
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

  
  # 1. Get Java file(s) from command line and add to array javaInput
 javaInput=()

 # 2. Compile and run Java file.
 for var in "$@"
         do
         cp "$var" "output/"$var""

	 echo "--- Received input file: $var"
         javac "output/$var" ||echo "--- ERR: Java compile time error" return 1;
	 echo "--- Compiled $var"
         # 3. Save output from Java files
	 j=java
	 fileName=${var%.$j} # remove .java 


	 # FIXME: For some programs, exits without printing err below
	 java -cp output/ "$fileName" > "output/j.txt" || 
	   echo "--- ERR: Java runtime error" return 1;

	 echo "--- Ran $var and saved output to output/j.txt"

         javaInput=("${javaInput[@]}" "$var")   
done 

 # 4. Translate Java files

 source setup.sh
 make

 for ((a=0; a < ${#javaInput[@]}; a++)) 
	do
         java xtc.oop.Translator -translate ${javaInput[a]}
 #	 java -jar midterm-translator.jar -translate -debug ${javaFiles[0]}
 done
 
 # 5. Get .cpp files from output/
 cd output/

 cppOutput=()
 for file in *.cpp ; do
	cppOutput=("${cppOutput[@]}" "$file")
 done

# 5.5 Copy Grimm's java_lang etc. files to output/
 cp ../src/xtc/oop/include/* .

 # 6. Compile CPP files. Form string for cpp arguments
 allCPP=""
 for((i=0; i < ${#cppOutput[@]}; i++))
 do
     allCPP="${allCPP} ${cppOutput[i]}"
 done
  g++ $allCPP java_lang.cc || echo"--- ERR: CPP compile time error" cd .. return 1;
  echo "--- Compiled $allCPP java_lang.cc"

 # 7. Run and save output from CPP files
  ./a.out > "c.txt" || echo "--- ERR: CPP runtime error" cd .. return 1;
    echo "--- Ran $allCPP java_lang.cc Output is saved to output/c.txt"


 # 8. Compare Java and CPP output
    echo "--------------------------------------------------------------------"
 if diff j.txt c.txt  > /dev/null ; then
	echo "--- Pass! Outputs were the same."
 else 
 	echo "--- Fail. Outputs were different."
	echo "--- See diff --side-by-side below::"
	diff j.txt c.txt --side-by-side
 fi
    echo "--------------------------------------------------------------------"

 #  Return to xtc/  
 cd ..

 read -p "End of program - Press Enter to quit."


