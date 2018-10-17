#!/bin/bash

arq_1=()
sizes_1=()
max1=0
for filename in sim\ 36\ 1\ *.csv; do
	arq_1+=("$filename")
	cur=$(cat "$filename" | wc -l)
	sizes_1+=("$cur")
	if (( $cur > $max1 )); then
		max1=$cur
	fi
done

arq_neg1=()
sizes_neg1=()
maxneg1=0
for filename in sim\ 36\ -1\ *.csv; do
        arq_neg1+=("$filename")
        cur=$(cat "$filename" | wc -l)
        sizes_neg1+=("$cur")
	if (( $cur > $maxneg1 )); then
                maxneg1=$cur
        fi
done

arq_095=()
sizes_095=()
max095=0
for filename in sim\ 36\ 0.95\ *.csv; do
        arq_095+=("$filename")
        cur=$(cat "$filename" | wc -l)
        sizes_095+=("$cur")
	if (( $cur > $max095 )); then
                max095=$cur
        fi
done

min_maxes=$(($max1>$maxneg1?$maxneg1:$max1))
min_maxes=$(($min_maxes>$max095?$max095:$min_maxes))

selected_095=0
selected_1=0
selected_neg1=0
for ((i=0;i<${#arq_1[@]};i++)); do
	if ((${sizes_095[$i]}==$min_maxes)); then
		selected_095=$i
	fi
	if ((${sizes_1[$i]}==$min_maxes)); then
                selected_1=$i
        fi
	if ((${sizes_neg1[$i]}==$min_maxes)); then
                selected_neg1=$i
        fi
done

cp "${arq_1[$selected_1]}" "sim 036 1 selected.csv"
cp "${arq_neg1[$selected_neg1]}" "sim 036 -1 selected.csv"
cp "${arq_095[$selected_095]}" "sim 036 0.95 selected.csv"
