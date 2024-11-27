#!/bin/zsh
update-alternatives --set java /usr/lib/jvm/java-1.11.0-openjdk-arm64/bin/java
# if the previous command fails, try the following:
if [ $? -ne 0 ]; then
    update-alternatives --install /usr/bin/java java /usr/lib/jvm/java-1.8.0-openjdk-arm64/bin/java 1
    update-alternatives --install /usr/bin/java java /usr/lib/jvm/java-1.11.0-openjdk-arm64/bin/java 2
    update-alternatives --set java /usr/lib/jvm/java-1.11.0-openjdk-arm64/bin/java
    update-alternatives --set java /usr/lib/jvm/java-11-openjdk-amd64/bin/java
    if [ $? -ne 0 ]; then
      . ~/.sdkman/bin/sdkman-init.sh
      sdk use java 11.0.20.fx-zulu
      set -e
    fi
fi 


VISUALISE=$1
if [ -z "$VISUALISE" ]; then
  VISUALISE=none
fi

VISUALISE_INTRAJ=""
VISUALISE_EXTENDJ=""
VISUALISE_CFG=""
if [ "$VISUALISE" = "none" ]; then
  echo "No visualisation"
elif [ "$VISUALISE" = "intraj" ]; then
  VISUALISE_INTRAJ="-visualise"
elif [ "$VISUALISE" = "extendj" ]; then
  VISUALISE_EXTENDJ="-visualise"
elif [ "$VISUALISE" = "cfg" ]; then
  VISUALISE_CFG="-visualise"
else
  echo "Invalid visualisation option"
  exit 1
fi



# "$VISUALISE" = "intraj" or "$VISUALISE" = "none"
if [ "$VISUALISE" = "intraj" -o "$VISUALISE" = "none" ]; then
cd intraj
# cp tools/build_relaxedmonolithic.gradle build.gradle
./gradlew clean build -x test --no-build-cache --quiet 
rm *.jar

java -jar ../cat/cat.jar -classpath ./tools/magpiebridge-0.1.6-SNAPSHOT-jar-with-dependencies.jar -o CacheConfiguration.json -attributesOnly build/generated-src/**/*.java src/**/*.java -rta -entryPoint org.extendj.IntraJ main $VISUALISE_INTRAJ
cd ..
fi

if [ "$VISUALISE" = "extendj" -o "$VISUALISE" = "none" ]; then
cd extendj_eval
./gradlew clean java8:build -x test --no-build-cache -q
rm java8/extendj.jar
java -jar ../cat/cat.jar src/**/*.java java8/**/*.java -classpath ./java8/build/classes/java/main:./java8/build/resources/main  -o CacheConfiguration.json -entryPoint org.extendj.JavaChecker main $VISUALISE_EXTENDJ
cd ..
fi


if [ "$VISUALISE" = "cfg" -o "$VISUALISE" = "none" ]; then
cd CFG
rm -rf AST
mkdir AST
parser_name="CFGrammar"
package="AST"
tools="tools"

java -jar tools/jastadd2.jar --jjtree --grammar=${parser_name} --package="${package}" $(find . -name "*.ast" -o -name "*.jrag" -o -name "*.jadd")  --cache=all   --visitCheck=false 
java -classpath tools/javacc.jar jjtree -OUTPUT_DIRECTORY="${package}" -STATIC=false -VISITOR=true -NODE_PACKAGE=${package} -NODE_PREFIX='""' "${parser_name}.jjt"
java -classpath tools/javacc.jar javacc -OUTPUT_DIRECTORY="${package}" -STATIC=false "${package}/${parser_name}.jj"
javac -classpath tools/junit.jar:. **/*.java
java -jar ../cat/cat.jar AST/**/*.java src/**/*.java  -entryPoint src.java.AST.Main main -o CacheConfiguration.json -attributesOnly $VISUALISE_CFG

fi