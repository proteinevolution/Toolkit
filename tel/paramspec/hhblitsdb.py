#!/usr/bin/env python3
import sys
import os

def main(argv):
    path = "%HHBLITS/DB"

    # If directory does not exist, print random stuff
    if not os.path.exists(path): #todo: implement fallback mechanism
        print("foo bar")
        return

    with open(path, 'r') as infile:
        for line in infile:
            line = line.strip()
            if line and not line.startswith('#') and not line.startswith('$'):
#todo: delete two lines
                to_print = 'hhblits/%s %s' % (line, line)
                print(to_print)
#                print(line)

if __name__ == '__main__':
    main(sys.argv)
