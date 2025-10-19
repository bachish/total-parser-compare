#!/usr/bin/env bash
# run-progs.sh DIR TESTCASE
# Example: ./collect_statistics.sh /home/olga/Documents/grigra/recs
if [[ $# -lt 1 ]]; then
  echo "Usage: $0 DIR" >&2
  exit 1
fi

dir="$1"

if [[ ! -d "$dir" ]]; then
  echo "Error: '$dir' is not a directory." >&2
  exit 1
fi

for path in "$dir"/*; do
  [[ -f "$path" ]] || continue
  echo "run for $path"
  ./gradlew runners:run --args="-i $path -o /home/olga/Documents/grigra/parsing_results"
done

