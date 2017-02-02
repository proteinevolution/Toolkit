
# TODO: parse SEQNAME and KNOWNS in a proper way

# create python file
touch modeller.py
pir_converter.pl -i %alignment.path -o alignment.pir -fas -tmp .
KNOWNS=`cat alignment.pir | grep 'structure' | cut -d':' -f 2 | sed "s/\(.*\)/'\1'/"  | paste -sd',' -`
SEQNAME=`cat alignment.pir | grep sequence | cut -d':' -f 2`
echo "# Homology modeling by the automodel class" >> modeller.py
echo "from modeller import *               # Load standard Modeller classes" >> modeller.py
echo "from modeller.automodel import *     # Load the automodel class" >> modeller.py
echo "log.verbose()" >> modeller.py
echo "env = environ()                      # create a new MODELLER environment to build this model" >> modeller.py
echo "# directories for input atom files" >> modeller.py
echo "env.io.atom_files_directory = '%PDB:%PDBALL:%HHOMP'" >> modeller.py
echo "a = automodel(env," >> modeller.py
echo "             alnfile  = 'alignment.pir',    # alignment filename" >> modeller.py
echo "             knowns   = ($KNOWNS),     #codes of the templates" >> modeller.py
echo "             sequence = '$SEQNAME') #code of the target" >> modeller.py
echo "a.starting_model= 1                       # index of the first model" >> modeller.py
echo "a.ending_model = 1                        # index of the last model" >> modeller.py
echo "a.make()                                  # do the actual homology modeling" >> modeller.py
tr -d $'\r' < modeller.py >> modeller_script.py
mv modeller.py modeller_script.py
chmod 0777 modeller_script.py
chmod 0777 alignment.pir
# run modeller
modeller modeller_script.py >> modeller.log
mv modeller.log ../logs/
mv $SEQNAME* ../results/