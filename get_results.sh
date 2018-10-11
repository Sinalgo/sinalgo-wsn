#!/bin/bash

for filename in sim*.txt; do
	prefix=$(echo "$filename" | sed -E 's/\.\w+$//')
	length=$(cat "$filename" | wc -l)
	result_line=$(cat "$filename" | grep -n "Final Simulation Results" | cut -d : -f 1)
	tail -n -$(($length - $result_line)) "$filename" > temp.txt
	last_line=$(cat temp.txt | grep -nE "^\s*$" | cut -d : -f 1)
	head -n $(($last_line - 1)) temp.txt > "$prefix".json
	rm temp.txt
done
