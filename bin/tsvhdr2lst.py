#!/usr/bin/env python

import sys

HELP="""

usage: tsvhdr2lst.py < infile.tsv > outfile.lst

converts a tab separated input file to a gazetteer list file
where the separator character is also a tab.
The input file must contain the field names in the first row.

Example input:
entry	field1	field2
asdf	12	xxx
jkl	13	yyy

Output:
asdf	field1=12	field2=xxx
jkl	field1=13	field2=yyy
"""


if __name__ == "__main__":
    if len(sys.argv) > 1:
        print(HELP)
    else:
        for idx, line in enumerate(sys.stdin):
            line = line.rstrip("\n\r")
            if idx == 0:
                cols = line.split("\t")
                if len(cols) < 2:
                    raise Exception("Not at least 2 columns")
                print("Columns:", ",".join(cols), file=sys.stderr)
            else:
                fields = line.split("\t")
                if len(fields) != len(cols):
                    raise Exception(f"Number of fields not {len(cols)} but {len(fields)} in row {idx+1}")
                print(fields[0], end="\t")
                print("\t".join([f"{cols[i+1]}={v}" for i, v in enumerate(fields[1:])]))
        print(f"Lines converted: {idx}", file=sys.stderr)
