#! /usr/bin/env python
"""
Script for parsing HMMER3 output text format into JSON format.
"""

import json
import argparse
from collections import namedtuple
from os import sys
from Bio import SearchIO


def parse_user_arguments(argv):
    """Parse user arguments to ease the use of the script"""

    parser = argparse.ArgumentParser(description=__doc__,
                                     formatter_class=
                                     argparse.RawTextHelpFormatter)

    parser.add_argument('-i',
                        '--input-file',
                        help='HMMER3 text output file',
                        type=str,
                        required=True)
    parser.add_argument('-o',
                        '--output-file',
                        help='JSON output file',
                        type=str,
                        required=False)
    parser.add_argument('-e',
                        '--e-value',
                        help='Inclusion threshold',
                        type=float,
                        required=False,
                        default=0.005)

    parser.add_argument('-m',
                        '--max-number',
                        help='Maximal number of hits to save',
                        type=int,
                        required=False,
                        default=5000)
    arguments = parser.parse_args(argv)

    return arguments


def hsp2dict(hsp, num, hit_eval, domains_nr):
    """
    Turns Biopython HSP object into dictionary
    that can be easily serialized by json.

    :param hsp: Bio.SearchIO._model.HSP
    :param num: number in the ordered list of HSPs, same
                as for the hit representing given HSP in
                the hit list.
    :param hit_eval: General evalue for full length sequence
    :param domains_nr: Number of domains from hitlist
    :return:
    """

    hsp_json = {
        'num': num,
        'bias': hsp.bias,
        'bitscore': hsp.bitscore,
        'evalue': hsp.evalue,
        'full_evalue': hit_eval,
        'domain_obs_num': domains_nr,
        'acc_avg': hsp.acc_avg,
        'env_start': hsp.env_start,
        'env_end': hsp.env_end,
        'query_id': hsp.query.id,
        'query_description': hsp.query.description,
        'query_seq': str(hsp.query.seq),
        'query_start': hsp.query_start,
        'query_end': hsp.query_end,
        'hit_id': hsp.hit.id,
        'hit_description': hsp.hit.description,
        'hit_seq': str(hsp.hit.seq),
        'hit_start': hsp.hit_start,
        'hit_end': hsp.hit_end,
        'hit_len': hsp.hit_end - hsp.hit_start + 1,
        'aln_ann': hsp.aln_annotation
    }

    return hsp_json


def hmmer2json(in_file, evalue, max_num):
    """
    Reads hmmer results with biopython and turns
    it into data that can be serialized with json.

    :param in_file: Input file with hmmer results
    :param evalue: E-value below which hits will be saved
    :param max_num: max number of hits in output file
    :return: json dict
    """

    # This is how json output will be structured
    results = {'id': '',
               'description': '',
               'hsps': [],
               }

    # Not the best way to do this, but for now it will work
    # Maybe we can improve Biopython parser so it's easier
    # to extract best domain hit
    hsps = {}
    hits_evals = {}

    with open(in_file, 'r') as handle:
        for qres in SearchIO.parse(handle=handle,
                                   format='hmmer3-text'):

            # Result descriptions
            results['id'] = qres.id
            results['description'] = qres.description
            # print('Nr of hits: %d' % len(qres.hits))

            # Read hits table and add to results dict
            HitObj = namedtuple('HitObj', ['evalue', 'domains'])

            for hit in qres.hits:
                hits_evals[hit.id] = HitObj(hit.evalue, hit.domain_obs_num)

            # Read HSPs to a temporary dict and filter to
            # add only best scoring domain
            # for multi domain hits

            for hsp in qres.hsps:
                # If HSP has independent e-value of a threshold or less
                if hsp.evalue <= evalue:
                    if hsp.hit_id not in hsps:
                        hsps[hsp.hit_id] = hsp
                    else:
                        if hsp.evalue < hsps[hsp.hit_id].evalue:
                            hsps[hsp.hit_id] = hsp

                if len(hsps) == max_num:
                    break

    hsps = sorted(list(hsps.values()), key=lambda x: x.evalue)

    results['hsps'] = [hsp2dict(hsp=hsp,
                                num=i+1,
                                hit_eval=hits_evals[hsp.hit_id].evalue,
                                domains_nr=hits_evals[hsp.hit_id].domains)
                       for i, hsp in enumerate(hsps)]

    return results


def main(arg):
    """Runs parser on input data"""

    # If output file is not specified - specify default
    if not arg.output_file:
        arg.output_file = '%s.json' % \
                           arg.input_file[:args.input_file.rfind('.')]

    # Read HMMER output into JSON serializable format
    json_data = hmmer2json(arg.input_file, arg.e_value, arg.max_number)

    # Dump output in JSON format
    with open(arg.output_file, 'w') as handle:
        json.dump(json_data, handle)


if __name__ == '__main__':
    args = parse_user_arguments(sys.argv[1:])
    main(args)
