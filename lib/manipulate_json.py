#! /usr/bin/env python
"""
Script for parsing HMMER3 output text format into JSON format.
"""

import json
import argparse
import sys


def main(argv):

    parser = argparse.ArgumentParser()
    parser.add_argument("IN", type=str)
    parser.add_argument("-k", type=str, required=True)
    parser.add_argument("-v", type=str, required=True)

    args = parser.parse_args(argv[1:])

    with open(args.IN, 'rw') as json_file:
        json_value = json.load(json_file)
        json_value[args.k] = args.v
    with open(args.IN, 'w') as outfile:
        json.dump(json_value, outfile)

if __name__ == '__main__':
    main(sys.argv)

