import sys

def add_link(acc):
    return "<a href=\"https://www.ncbi.nlm.nih.gov/protein/{}\">{}</a>".format(acc, acc)


def main(argv):
    with open(argv[1], 'r') as f:

        state = -1
        for line in f:
            # Need to change to overview
            if line.startswith("Sequences producing"):
                sys.stdout.write(next(f))
                counter = 0
                sys.stdout.write("<table>\n<thead>\n<tr><td></td><td></td> <td>Accession</td><td>Description</td> <td>Score (Bits)</td> <td>E-Value</td></tr>\n</thead>\n<tbody>\n")
                state = 1
            # State for parsing the overview
            elif state == 1:
                if line.strip() == '':
                    state = 2
                    sys.stdout.write("</tbody>\n</table>\n<pre>\n")
                else:
                    counter +=1

                    table_datas = [counter , add_link(line[0:12].rstrip()), line[12:70].rstrip(), line[70:102].rstrip(), line[102:].rstrip()]
                    sys.stdout.write("<tr>" + "<td class=\"centered\"><input class=\"inp\"  type=\"checkbox\" /></td>" + "".join(["<td>{}</td>".format(field) for field in table_datas]) + "</tr>\n")
            # Need to change to reference
            elif line.startswith("href") and state == -1:
                sys.stdout.write("<div class=\"reveal\" id=\"referenceModal\" data-reveal>\n")
                sys.stdout.write("<pre>\n")
                sys.stdout.write("<b><a\n" +line)
                state = 0
            # State for parsing the references
            elif state == 0:
                if line.startswith("Database"):
                    sys.stdout.write("</pre>\n")
                    sys.stdout.write("</div>\n")
                    state = -1
                    continue
                else:
                    sys.stdout.write(line)
            elif line.lstrip().startswith("Effective") and state == 2:
                sys.stdout.write("</pre>\n")
                state = 3
                sys.stdout.write("<table>\n<tbody>\n")
                sys.stdout.write("<tr><td>Effective search space used</td><td>{}</td></tr>\n".format(line.split(':')[1].rstrip()))

            elif state == 2:
                sys.stdout.write(line)
            elif state == 3:
                if line.strip() == '':
                    continue

                if line.startswith("</PRE>"):
                    sys.stdout.write("</tbody>\n</table>\n")
                    state = -1
                    continue
                spt = line.split(':')
                if line.startswith("Gap"):
                    sys.stdout.write("<tr><td>Gap Existence Penalty</td><td>{}</td></tr>\n".format(spt[2].split(',')[0]))
                    sys.stdout.write("<tr><td>Gap Extension Penalty</td><td>{}</td></tr>\n".format(spt[3].rstrip()))
                else:
                   sys.stdout.write("<tr><td>{}</td><td>{}</td></tr>\n".format(spt[0], spt[1].rstrip()))


if __name__ == "__main__":
    main(sys.argv)
