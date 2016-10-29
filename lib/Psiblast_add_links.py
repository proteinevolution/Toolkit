import argparse
import sys
import re

def main(argv):

   parser = argparse.ArgumentParser()
   parser.add_argument("-i", type=str, required=True)
   args = parser.parse_args(argv[1:])
   pattern = re.compile("\s.*<a name=")
   start_parsing = False
   with open(args.i, "r") as f:
      for line in f:
        if line.startswith( '>' ) and start_parsing:
            break
        if start_parsing:
            access_version = line.split(' ', 1)[0]
            replace = '<a title="Show report for '+access_version+'" href="https://www.ncbi.nlm.nih.gov/protein/'+access_version+'?report=genbank&log$=prottop&blast_rank=1&RID==" >'+access_version+'</a>'
            sys.stdout.write(line.replace(access_version, replace,1))
        else:
            sys.stdout.write(line)
        if line.startswith( 'Sequences producing significant alignments:' ):
            start_parsing = True
   start_parsing = False

   with open(args.i, "r") as f:
       for line in f:
           if line.startswith( '>' ):
               start_parsing = True
           if line.startswith( '>' ) and start_parsing:
               access_version = line.split('<', 1)[0]
               access_version = access_version[1:]
               replace = '<a title="Show report for '+access_version+'" href="https://www.ncbi.nlm.nih.gov/protein/'+access_version+'?report=genbank&log$=protalign&blast_rank=4&RID=0" >'+access_version+'</a>'
               sys.stdout.write(line.replace(access_version, replace,1))
               for line in f:
                    if line.startswith( 'Length=' ):
                       sys.stdout.write(line)
                       break
                    if pattern.match(line):
                        access_version = line.split('<', 1)[0]
                        # remove leading whitespaces
                        access_version = access_version[1:]
                        replace = '<a title="Show report for '+access_version+'" href="https://www.ncbi.nlm.nih.gov/protein/'+access_version+'?report=genbank&log$=protalign&blast_rank=1&RID=0" >'+access_version+'</a>'
                        sys.stdout.write(line.replace(access_version, replace,1))
           elif start_parsing:
               sys.stdout.write(line)










   sys.stdout.close()


if __name__ == "__main__":
    main(sys.argv)