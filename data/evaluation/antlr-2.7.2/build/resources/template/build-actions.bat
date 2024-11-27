@echo off
cd antlr\actions\csharp


java -cp ..\..\..\ antlr.Tool action.g

javac -deprecation -verbose  -classpath ..\..\..\ *.java


cd ..\..\..\

