#!/usr/bin/env python3
import sys
import os

def print_args(args):
    print(args[-1] + ' '+  '_'.join(args[:-1]))


def main(argv):
    path = "%STANDARD/DB"
    # If directory does not exist, print random stuff
    if not os.path.exists(path):
        print("foo bar")
        return
    args = []
    with open(path, 'r') as infile:
        for line in infile:
            line = line.strip()
            if not line or line.startswith('#'):
                continue
            if line.startswith('$'):
                print_args(args)
                args = []
            else:
                args.append(line)

if __name__ == '__main__':
    main(sys.argv)