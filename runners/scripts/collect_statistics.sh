#!/usr/bin/env bash
# run-progs.sh DIR TESTCASE
# Example: ./collect_statistics.sh /home/olga/Documents/grigra/recs
#if [[ $# -lt 1 ]]; then
#  echo "Usage: $0 DIR" >&2
#  exit 1
#fi
#dir="$1"

cd ../..
dataset="junit4-r4.12"
path_to_data="/home/olga/src/dataset/grigra"
dir="$path_to_data/mutated/$dataset"

if [[ ! -d "$dir" ]]; then
  echo "Error: '$dir' is not a directory." >&2
  exit 1
fi

for path in "$dir"/*; do
  [[ -f "$path" ]] || continue
  echo "run for $path"
  ./gradlew runners:run --args="-i $path -o $path_to_data/parsing_results/$dataset"
done

