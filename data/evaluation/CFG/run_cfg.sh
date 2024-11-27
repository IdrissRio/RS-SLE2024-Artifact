#!/bin/zsh

# Targets for working from terminal window:
#   build (default) - generates java files and compiles them
#   test            - runs junit test cases
#   clean           - removes all generated files and class files
# Targets for working from Eclipse:
#   gen             - generates java files
#   genClean        - removes all generated files and their class files


parser_name="CFGrammar"
package="AST"
tools="tools"
testdir="TestGrammars"

# TARGET build
build() {
  gen
for test in TestJavaGrammarSorted
  do
    runTest ${test}
  done
  rm -rf AST
}

# TARGET runTest
runTest() {
  for i in {1..25}
  do
  java -Xss1m -Xmx8g src.java.AST.Main ${testdir}/$1 10 >> $EVAL_DIR/$kind.txt
  done
}

# TARGET gen
gen() {
  # if kind=old
  if [ "$kind" = "bs-old" ]; then
      java -jar tools/jastadd2_old.jar --jjtree --grammar=${parser_name} --package="${package}" $(find . -name "*.ast" -o -name "*.jrag" -o -name "*.jadd")  
  elif [ "$kind" = "bs" ]; then
      java -jar tools/jastadd2.jar --jjtree --grammar=${parser_name} --package="${package}" $(find . -name "*.ast" -o -name "*.jrag" -o -name "*.jadd")  --cache=all   --visitCheck=false 
  elif [ "$kind" = "rs" ]; then
      java -jar tools/jastadd2.jar --jjtree --grammar=${parser_name} --package="${package}" $(find . -name "*.ast" -o -name "*.jrag" -o -name "*.jadd")  --cache=all   --visitCheck=false --safelazy --dnc --optimisation-config=CacheConfig.json
  else # RelaxMonolithic
      java -jar tools/jastadd2.jar --jjtree --grammar=${parser_name} --package="${package}" $(find . -name "*.ast" -o -name "*.jrag" -o -name "*.jadd")  --cache=all --cacheCycle=true --visitCheck=false --safeLazy 
  fi

  
  java -classpath tools/javacc.jar jjtree -OUTPUT_DIRECTORY="${package}" -STATIC=false -VISITOR=true -NODE_PACKAGE=${package} -NODE_PREFIX='""' "${parser_name}.jjt"
  java -classpath tools/javacc.jar javacc -OUTPUT_DIRECTORY="${package}" -STATIC=false "${package}/${parser_name}.jj"
  javac -classpath tools/junit.jar:. **/*.java
}

# TARGET clean
clean() {
  cleanGen
  sudo find . -name "*.class" -exec rm {} \;
}

# TARGET cleanGen
cleanGen() {
  sudo rm -rf "${package}"
}

# TARGET test
test() {
  clean
  build
  java -cp ".:${tools}/junit.jar" testframework.TestAll
}

catbuild() {
  java -jar cat.jar AST/**/*.java src/**/*.java  -entryPoint src.java.AST.Main main -o CacheConfig.json -attributesOnly
}


# Set the default target to build

EVAL_DIR=../$1

# catbuild
kind=bs-old
build
kind=bs
build
kind=rs
build
kind=rm
build



