#! /usr/bin/env python
"""
Script for parsing HMMER3 output text format into JSON format.
"""

import json
import argparse
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

    arguments = parser.parse_args(argv)

    return arguments


def hit2dict(hit):
    """
    Turns Biopython Hit object into dictionary
    that can be easily serialized by json.

    :param hit: Bio.SearchIO._model.Hit object
    :return: dictionary representation of an object
    """

    hit_json = {
        'description': hit.description,
        'id': hit.id,
        'bias': hit.bias,
        'bitscore': hit.bitscore,
        'evalue': hit.evalue,
        'dom_exp_num': hit.domain_exp_num,
        'domain_obs_num': hit.domain_obs_num
    }

    return hit_json


def hsp2dict(hsp):
    """
    Turns Biopython HSP object into dictionary
    that can be easily serialized by json.

    :param hsp: Bio.SearchIO._model.HSP
    :return:
    """

    hsp_json = {
        'bias': hsp.bias,
        'bitscore': hsp.bitscore,
        'evalue': hsp.evalue,
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
        'aln_ann': hsp.aln_annotation
    }

    return hsp_json


def hmmer2json(in_file, evalue):
    """
    Reads hmmer results with biopython and turns
    it into data that can be serialized with json.

    :param in_file: Input file with hmmer results
    :return: json dict
    """

    # This is how json output will be structured
    results = {'id': '',
               'description': '',
               'hits': {
                   'above_threshold': [],
                   'below_threshold': []
               },
               'hsps': {
                   'above_threshold': [],
                   'below_threshold': []
               }
               }

    # Not the best way to do this, but for now it will work
    # Maybe we can improve Biopython parser so it's easier
    # to extract best domain hit
    hsps = {}

    with open(in_file, 'r') as handle:
        for qres in SearchIO.parse(handle=handle,
                                   format='hmmer3-text'):

            # Result descriptions
            results['id'] = qres.id
            results['description'] = qres.description
            # print('Nr of hits: %d' % len(qres.hits))

            # Read hits table and add to results dict
            for hit in qres.hits:
                if hit.evalue <= evalue:
                    results['hits']['above_threshold'].append(hit2dict(hit))
                else:
                    results['hits']['below_threshold'].append(hit2dict(hit))

            # Read HSPs to a temporary dict and filter to
            # add only best scoring domain
            # for multi domain hits
            for hsp in qres.hsps:
                if hsp.hit_id not in hsps:
                    hsps[hsp.hit_id] = hsp
                else:
                    favorite = min(hsp, hsps[hsp.hit_id], key=lambda x: x.evalue)
                    hsps[favorite.hit_id] = favorite

    # Add unique domains in the same order as hits
    for hit in results['hits']['above_threshold']:
        results['hsps']['above_threshold'].append(hsp2dict(hsps[hit['id']]))

    for hit in results['hits']['below_threshold']:
        results['hsps']['below_threshold'].append(hsp2dict(hsps[hit['id']]))

    return results


def main(args):
    """Runs parser on input data"""

    # If output file is not specified - specify default
    if not args.output_file:
        args.output_file = '%s.json' % \
                           args.input_file[:args.input_file.rfind('.')]

    # Read HMMER output into JSON serializable format
    json_data = hmmer2json(args.input_file, args.e_value)

    # Dump output in JSON format
    with open(args.output_file, 'w') as handle:
        json.dump(json_data, handle)


if __name__ == '__main__':
    args = parse_user_arguments(sys.argv[1:])
    main(args)
