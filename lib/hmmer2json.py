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


def hit2dict(hit, num):
    """
    Turns Biopython Hit object into dictionary
    that can be easily serialized by json.

    :param hit: Bio.SearchIO._model.Hit object
    :param num: number in the ordered list of hits
    :return: dictionary representation of an object
    """

    hit_json = {
        'num': num,
        'description': hit.description,
        'id': hit.id,
        'bias': hit.bias,
        'bitscore': hit.bitscore,
        'evalue': hit.evalue,
        'dom_exp_num': hit.domain_exp_num,
        'domain_obs_num': hit.domain_obs_num
    }

    return hit_json


def hsp2dict(hsp, num):
    """
    Turns Biopython HSP object into dictionary
    that can be easily serialized by json.

    :param hsp: Bio.SearchIO._model.HSP
    :param num: number in the ordered list of HSPs, same
                as for the hit representing given HSP in
                the hit list.
    :return:
    """

    hsp_json = {
        'num': num,
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
        'hit_len': hsp.hit_end - hsp.hit_start + 1,
        'aln_ann': hsp.aln_annotation
    }

    return hsp_json


def hmmer2json(in_file, evalue):
    """
    Reads hmmer results with biopython and turns
    it into data that can be serialized with json.

    :param in_file: Input file with hmmer results
    :param evalue: e-value of inclusion threshold,
                   based on that hits and HSPs will
                   be in the above or below threshold
                   lists.
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

            i = 1
            # Read hits table and add to results dict
            for hit in qres.hits:
                if hit.evalue <= evalue:
                    results['hits']['above_threshold'].append(hit2dict(hit=hit,
                                                                       num=i))
                    i += 1
                else:
                    results['hits']['below_threshold'].append(hit2dict(hit=hit,
                                                                       num=i))
                    i += 1

            # Read HSPs to a temporary dict and filter to
            # add only best scoring domain
            # for multi domain hits
            for hsp in qres.hsps:
                if hsp.hit_id not in hsps:
                    hsps[hsp.hit_id] = hsp
                else:
                    favorite = min(hsp, hsps[hsp.hit_id],
                                   key=lambda x: x.evalue)
                    hsps[favorite.hit_id] = favorite

    # Add unique domains in the same order as hits
    for hit in results['hits']['above_threshold']:
        curr_hsp = hsps[hit['id']]
        results['hsps']['above_threshold'].append(hsp2dict(hsp=curr_hsp,
                                                           num=hit['num']))

    for hit in results['hits']['below_threshold']:
        curr_hsp = hsps[hit['id']]
        results['hsps']['below_threshold'].append(hsp2dict(hsp=curr_hsp,
                                                           num=hit['num']))

    return results


def main(arg):
    """Runs parser on input data"""

    # If output file is not specified - specify default
    if not arg.output_file:
        arg.output_file = '%s.json' % \
                           arg.input_file[:args.input_file.rfind('.')]

    # Read HMMER output into JSON serializable format
    json_data = hmmer2json(arg.input_file, arg.e_value)

    # Dump output in JSON format
    with open(arg.output_file, 'w') as handle:
        json.dump(json_data, handle)


if __name__ == '__main__':
    args = parse_user_arguments(sys.argv[1:])
    main(args)
