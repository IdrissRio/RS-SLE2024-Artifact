#!/bin/zsh
# Define variables for colors
CYAN_BOLD="\u001b[36;1m"
YELLOW_BOLD="\u001b[33;1m"
RESET="\u001b[0m"

# Function that logs a message
function log_message() {
  local message="$1"
  local log_type="$2"

  case $log_type in
    info)
      echo -e "\n${CYAN_BOLD}[INFO] ${message}${RESET}"
      ;;
    warning)
      echo -e "${YELLOW_BOLD}[WARNING] ${message}${RESET}"
      ;;
    *)
      echo "Invalid log type"
      exit 1
      ;;
  esac
}

# update-alternatives --set java /usr/lib/jvm/java-1.8.0-openjdk-arm64/bin/java

update-alternatives --install /usr/bin/java java /usr/lib/jvm/java-1.8.0-openjdk-arm64/bin/java 1
update-alternatives --install /usr/bin/java java /usr/lib/jvm/java-1.11.0-openjdk-arm64/bin/java 2


# Creating aliases for date and timeout so that the script can run on both Linux and MacOS.
if [[ "$OSTYPE" == "darwin"* ]]; then
  log_message "Running on MacOS" "info"
  alias gdate="gdate"
  alias gtimeout="gtimeout"
else
  alias gdate="date"
  alias gtimeout="timeout"
fi


# Checking if the first argument is "fast"
if [ "$1" = "fast" ]; then
  log_message "Running in fast mode. Meaning that a smaller number of iterations will be run on one single project." "info"
  FAST=true
else
  FAST=false
fi

#Checking if the correct number of arguments are passed (2) or FAST = true
if [ "$FAST" = false ] && [ "$#" -ne 2 ]; then
  log_message "Not enough arguments passed. Please pass the following arguments:\
   \n\t1. Iteration of the outer loop (warm-up).\
   \n\t2. Iteration of the inner loop (steady-state)." "error"
  exit 1
fi

if [ "$FAST" = false ] && [ "$1" -le 0 ]; then
  log_message "The first argument must be a positive integer." "error"
  exit 1
fi

if [ "$FAST" = false ] && [ "$2" -le 0 ]; then
  log_message "The second argument must be a positive integer." "error"
  exit 1
fi




# If FAST is true, assign the value 2 to the outer loop and 2 to the inner loop.
if [ "$FAST" = true ]; then
  OUTER=2
  INNER=2
  prj_json="projects_fast.json"
else
  OUTER=$1
  INNER=$2
  prj_json="projects.json"
fi




# Static array with all JARS.

declare -a JARS=(
   extendj_relaxedmonolithic.jar
   extendj_relaxedstacked.jar
   intraj_daa_relaxedmonolithic.jar
   intraj_daa_relaxedstacked.jar
   intraj_npa_relaxedmonolithic.jar
   intraj_npa_relaxedstacked.jar
   intraj_ondemand_npa_relaxedstacked.jar
   intraj_ondemand_npa_relaxedmonolithic.jar
   intraj_ondemand_daa_relaxedmonolithic.jar
   intraj_ondemand_daa_relaxedstacked.jar
   )

declare -a JARS_TRACE=(
   intraj_ondemand_npa_relaxedstacked_trace.jar
   intraj_ondemand_npa_relaxedmonolithic_trace.jar
   intraj_ondemand_daa_relaxedmonolithic_trace.jar
   intraj_ondemand_daa_relaxedstacked_trace.jar
   )







count=$(jq '[.benchmarks[] | select(.enable == true)] | length' $prj_json)
count_total=$(jq '[.benchmarks[]] | length' $prj_json)
TOTAL_ITERATIONS=$(( ( (${#JARS[@]} - 4) * $OUTER * 2 + ($OUTER * 5 * 4) ) * $count ))
CURRENT_ITERATION=0

function progress_bar() {
  local current=$1
  local percentage=$((current * 100 / $TOTAL_ITERATIONS))
  local color=$'\e[32m'
  local completed=$(printf "%.0f" $(echo "scale=2; $percentage / 2" | bc -l))
  local remaining=$((50 - $completed))
  printf "\rProgress: [\e[42m%${completed}s\e[0m%${remaining}s] %s%d%%$RESET (Iteration %d/%d)" \
         '' '' $color $percentage $current $TOTAL_ITERATIONS
}

function eval_warmup() {
   # Run the evaluation
   export results=()
   for a in {1..$OUTER}; do
      CURRENT_ITERATION=$((CURRENT_ITERATION + 1))
      START=$(gdate +%s.%N)
      # Check if intraj contains the name extendj
      if [[ $intraj == *"extendj"* ]]; then
         java -Xmx8g -jar ../$intraj 1 -classpath $classpath $all_files
      else
         gtimeout 3m java  -Xmx8g -jar ../$intraj -classpath $classpath $all_files || true
      #   java  -Xmx8g -jar ../$intraj -classpath $classpath $all_files
      fi
      END=$(gdate +%s.%N)
      progress_bar $CURRENT_ITERATION
      PROC_TIME=$(echo "$END - $START" | bc)
      results+=($PROC_TIME)
   done
}

function eval_steady() {
   # Run the evaluation
   export results=()
   for a in {1..$OUTER}; do
      CURRENT_ITERATION=$((CURRENT_ITERATION + 1))
      # Store the result printed on standard output by the process in a local variable 'res'
      if [[ $intraj == *"extendj"* ]]; then
         res=$(java -Xmx8g -jar ../$intraj $INNER -classpath $classpath $all_files)
      else
         res=$(java -Xmx8g  -jar ../$intraj -classpath $classpath $all_files -niter=$INNER)
      fi
      progress_bar $CURRENT_ITERATION
      # If res is empty, set it to 0
      if [ -z "$res" ]; then
         res=0
      fi
      results+=($res)
      # Add new line to result
      results+=("\n")
   done
}

eval_steady_ondemand() {
    local ondemand_value=$1
      # Run the evaluation
   export results=()
      for a in {1..$OUTER}; do
      CURRENT_ITERATION=$((CURRENT_ITERATION + 1))
      # Store the result printed on standard output by the process in a local variable 'res'
      res=$(java -Xmx8g -jar ../$intraj -classpath $classpath $all_files -niter=$INNER -setSize $ondemand_value)
      progress_bar $CURRENT_ITERATION
      # If res is empty, set it to 0
      if [ -z "$res" ]; then
         res=0
      fi
      results+=($res)
      # Add new line to result
      results+=("\n")
   done
}


eval_trace(){
   echo $(pwd)
   progress_bar $CURRENT_ITERATION
   # For each alias in JARS_TRACE
   for jar in "${JARS_TRACE[@]}"; do
   readable_name=${jar%.*}
   # CURRENT_ITERATION=$((CURRENT_ITERATION + 1))
   for value in 10 20 50 100 200; do
   # remove "trace" from readable_name
   readable_name=${readable_name%_trace}
   java -Xmx8g -jar ../$jar -classpath $classpath $all_files -niter=1 -projectName $EVAL_DIR/$name"_"$readable_name"_" -count -setSize $value
   done
   # progress_bar $CURRENT_ITERATION
   done
}


compile(){
   # Cleaning
   log_message "Cleaning" "info"
   cd ..
   rm *.jar  | true
   cd evaluation
   rm -rf intraj/build | true
   rm -rf extend_eval/build | true
   rm -rf CFG/AST | true

   # zsh run_cat.sh none #No visualisation
   update-alternatives --set java /usr/lib/jvm/java-1.8.0-openjdk-arm64/bin/java

   # Compiling the IntraJ tool
   log_message "Compiling IntraJ RS" "info"
   cd intraj
   ./gradlew clean build -x test --no-build-cache -PenableOptimization -q
   rm -rf build
   mv intraj_daa.jar ../../intraj_daa_relaxedstacked.jar
   mv intraj_npa.jar ../../intraj_npa_relaxedstacked.jar
   mv intraj_ondemand_npa.jar ../../intraj_ondemand_npa_relaxedstacked.jar
   mv intraj_ondemand_daa.jar ../../intraj_ondemand_daa_relaxedstacked.jar
   rm *.jar

   log_message "Compiling IntraJ RS with tracing information" "info"
   ./gradlew clean build -x test --no-build-cache  -PenableTracingAndOptimisation -q
   rm -rf build
   mv intraj_ondemand_npa.jar ../../intraj_ondemand_npa_relaxedstacked_trace.jar
   mv intraj_ondemand_daa.jar ../../intraj_ondemand_daa_relaxedstacked_trace.jar
   rm *.jar

   log_message "Compiling IntraJ RM" "info"
   ./gradlew clean build -x test --no-build-cache -q
   mv intraj_daa.jar ../../intraj_daa_relaxedmonolithic.jar
   mv intraj_npa.jar ../../intraj_npa_relaxedmonolithic.jar
   mv intraj_ondemand_npa.jar ../../intraj_ondemand_npa_relaxedmonolithic.jar
   mv intraj_ondemand_daa.jar ../../intraj_ondemand_daa_relaxedmonolithic.jar
   rm *.jar

   log_message "Compiling IntraJ RM with tracing information" "info"
   ./gradlew clean build -x test --no-build-cache -PenableTracing -q
   rm -rf build
   mv intraj_ondemand_npa.jar ../../intraj_ondemand_npa_relaxedmonolithic_trace.jar
   mv intraj_ondemand_daa.jar ../../intraj_ondemand_daa_relaxedmonolithic_trace.jar
   rm *.jar
   cd ../


   cd extendj_eval
   log_message "Compiling ExtendJ" "info"
   ./gradlew clean java8:build -x test --no-build-cache -PenableOptimization -q
   mv java8/extendj.jar ../../extendj_relaxedstacked.jar

   rm -rf build
   ./gradlew clean java8:build -x test --no-build-cache -PenableOptimization -q
   mv java8/extendj.jar ../../extendj_relaxedmonolithic.jar
   cd ../
}



# The results of the evaluation are stored in a directory named with a timestamp.
# The timestamp is used to avoid overwriting previous results.
TIMESTAMP=$(date +%Y%m%d%H%M%S)
EVAL_DIR=results/$TIMESTAMP
mkdir -p $EVAL_DIR

compile



run_cfg(){
   cd CFG
   zsh run_cfg.sh $EVAL_DIR
   cd ..
}








log_message "The results will be stored in $EVAL_DIR" "info"
update-alternatives --set java /usr/lib/jvm/java-1.11.0-openjdk-arm64/bin/java
run_cfg
update-alternatives --set java /usr/lib/jvm/java-1.8.0-openjdk-arm64/bin/java


log_message "Number of benchmarks: $count" "info"
for ((i = 0; i < $count_total; i++)); do
   enable=$(jq -r ".benchmarks[$i].enable" $prj_json)

   if [ "$enable" = "false" ]; then
      #Skipping the banchmark if not enabled
      continue
   fi
   name=$(jq -r '.benchmarks['$i'].name' $prj_json)
   dir_to_analyze=$(jq -r '.benchmarks['$i'].dir_to_analyze' $prj_json)
   # if dir_to_analyze is @COMPILE_ARGS, then open $name/COMPILE_ARGS and each line is a file to analyze
   if [ "$dir_to_analyze" = "@COMPILE_ARGS" ]; then
      all_files=()
      while IFS= read -r line; do
         all_files+=($name/$line)
      done <"$name/COMPILE_ARGS"
   else
   all_files=($name/$dir_to_analyze**/*.java)
   all_files_no_dir=()
   for file in "${all_files[@]}"; do
      if [ -d "$file" ]; then
         continue
      else
         all_files_no_dir+=($file)
      fi
   done
   all_files=("${all_files_no_dir[@]}")
   fi
   exclude_dirs=$(jq -r '.benchmarks['$i'].exclude_dirs' $prj_json)
   if [ "$exclude_dirs" != "null" ]; then
      count_dirs=$(jq -r '.benchmarks['$i'].exclude_dirs | length' $prj_json)
      # exclude_dirs is a JSON array containg pairs of directory to exclude and the reason for excluding it.
      # e.g., [["/tmp", "For some reason"], ["/home", "For some other reason"]]
      for ((j = 0; j < $count_dirs; j++)); do
         dir=$(jq -r '.benchmarks['$i'].exclude_dirs['$j'].path' $prj_json)
         reason=$(jq -r '.benchmarks['$i'].exclude_dirs['$j'].motivation' $prj_json)
         # info="Excluding '$dir' because: $reason"
         # log_info
         log_message "Excluding '$dir' because: $reason" "info"
         # removing files from $all_files that starts with $dir
         for file in ${all_files[@]}; do
            if [[ $file == $name/$dir* ]]; then
               all_files=("${all_files[@]/$file/}")

            fi
         done
      done
   fi
   # Run the evaluation process.
   classpath=$(jq -r '.benchmarks['$i'].classpath' $prj_json)
   entryPackage=$(jq -r '.benchmarks['$i'].entryPackage' $prj_json)
   entryMethod=$(jq -r '.benchmarks['$i'].entryMethod' $prj_json)

   folder=$name
   iter=0

   elapsed=1
   # Run the trace evaluation
   log_message "COMPUTING HOW MANY TIME EACH ATTRIBUTE IS COMPUTED" "info"
   eval_trace
   # iterate over all JARS and run the evaluation
   for jar in "${JARS[@]}"; do
      intraj=$jar
      # Removes the .jar at the end
      readable_name=${intraj%.*}
      log_message "RUNNING BENCHMARK ON PROJECT $name WITH $readable_name" "info"
      if [[ $jar == *ondemand* ]]; then
         # If the jar has "ondemand" in its name, iterate over X random methods
         for value in 10 20 50 100 200; do
            eval_steady_ondemand $value
            echo "$results" >>$EVAL_DIR/$name"_"$readable_name"_ondemand_"$value"_results.new"
         done
      else
         # Run the warmup evaluation and save the results to file
         eval_warmup
         echo "$results" >>$EVAL_DIR/$name"_"$readable_name"_warmup_results.new"

         # If the jar does not have "ondemand" in its name, run eval_steady once
         eval_steady
         echo "$results" >>$EVAL_DIR/$name"_"$readable_name"_steady_results.new"
      fi

   done
done







log_message "Evaluation finished" "info"

python3 genlatex.py $EVAL_DIR $FAST
cp latex/Results.pdf $EVAL_DIR/results.pdf
cp latex/Results.pdf ../results.pdf