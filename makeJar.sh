#!/bin/bash  

clear

# This script should be in xtc/

 echo  "Makes a final.jar of our translator."

 cd classes/
 jar -cfm final.jar Manifest.txt xtc/*/*.class
 echo "--- Created final.jar"
 
 mv final.jar ../final.jar
 echo "--- Moved final.jar to xtc/"

 cd ..

 echo "--- Run final.jar using:"
 echo "java -jar final.jar -translate Test.java"
 

 return 0;

# read -p "End of program - Press Enter to quit."


