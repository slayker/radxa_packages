#!/bin/bash

cd PharaohOTAClient

if [ "$1" == "--target" ] 
then
	#GENERATE THE R.java FILES
	aapt package -v -f -m -S res/ -J src/ -M AndroidManifest.xml -I $ANDROID_HOME/platforms/$2/android.jar
	# COLLECT THE LIST OF .JAVA FILES TO BE 'COMPILED'
	find ./src -name "*.java" > sources_list.txt
	# COLLECT THE LIST OF .JAR LIBRARIES TO BE USED AS 'CLASSPATH'
	# GENERATE THE .class FILES
	mkdir -p bin/classes
	javac -encoding ascii -d bin/classes -bootclasspath external_library/classes-full-debug.jar:$ANDROID_HOME/platforms/$2/android.jar @sources_list.txt -cp "libs/*"  -verbose
	rm ./src/hudl/ota/R.java
fi

if [ "$1" != "--target" ] 
then
	echo "Wrong parameters, e.g: --target android-19"
fi

