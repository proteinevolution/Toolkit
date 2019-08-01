export default {
    header: 'Jobs',
    details: {
        jobID: 'Job ID: {jobID},',
        parentID: 'Parent Job ID: {0},',
        dateCreated: 'Created: {dateCreated}',
    },
    states: {
        1: 'Prepared',
        2: 'Queued',
        3: 'Running',
        4: 'Error',
        5: 'Done',
        6: 'Submitted',
        7: 'Pending',
        8: 'Limit Reached',
        9: 'Deleted',
    },
    stateMessages: {
        queued: 'Your submission is queued!',
        running: 'Your submission is being processed!',
        error: 'Your job has reached error state!',
        prepared: 'Your job is prepared!',
        submitted: 'Your job was successfully submitted!',
    },
    results: {
        titles: {
            hitlist: 'Hitlist',
            fastaAlignment: 'FASTA Alignment',
            clustalAlignment: 'CLUSTAL Alignment',
            alignmentViewer: 'AlignmentViewer',
            treeView: 'Tree',
            summary: 'Summary',
            dataView: 'Data',
            results: 'Results',
            resultsConfidence: 'Results With Confidence',
            textOutput: 'Text Output',
            reducedSet: 'Reduced Set',
            clusters: 'Clusters',
            rawOutput: 'Raw Output',
            queryTemplateMSA: 'Query Template MSA',
            queryMSA: 'Query MSA',
            probabilityPlot: 'Probability Plot',
            evaluePlot: 'E-Value Plot',
            probList: 'ProbList',
            probState: 'ProbState',
            predictedDomains: 'Predicted Domains',
            numericalData: 'Numerical Data',
            structure3dWithAxes: '3D Structure With Axes',
            structure3d: '3D Structure',
            plots: 'Plots',
            ccProb: 'CC-Prob',
        },
        actions: {
            selectAll: 'Select All',
            deselectAll: 'Deselect All',
            forward: 'Forward',
            forwardAll: 'Forward All',
            forwardSelected: 'Forward Selected',
            forwardQueryA3M: 'Forward Query A3M',
            download: 'Download',
            downloadHits: 'Download Hits',
            downloadMSA: 'Download MSA',
            downloadReducedA3M: 'Download Reduced A3M',
            downloadFullA3M: 'Download Full A3M',
            exportMSA: 'Export MSA',
            colorMSA: 'Color MSA',
            colorSeqs: 'Color Seqs',
            wrapSeqs: 'Wrap Seqs',
            unwrapSeqs: 'Unwrap Seqs',
            downloadTree: 'Download Tree',
            downloadPDBFile: 'Download PDB File',
            model: 'Model using selection',
        },
        alignment: {
            numSeqs: 'Number of Sequences: <b>{num}</b>',
            numSeqsReduced: 'Number of Sequences (up to {reduced} best matches): <b>{num}</b>',
            loadingHits: 'Loading hits...',
        },
        seq2ID: {
            numRetrieved: 'Retrieved {num} IDs',
        },
        tprpred: {
            noResults: 'No repeats found! Please consider picking a less stringent E-value inclusion cut-off.',
        },
        patsearch: {
            noResults: 'No hits found! Please consider picking a different target database.',
        },
        hhomp: {
            noResults: 'No hits found! Please re-run HHomp with an increased number of MSA generation steps or with ' +
                'a custom-built multiple sequence alignment as input (set "Maximal no. of MSA generation steps" to ' +
                '0). Please also consider picking a different target database and/or using different parameters.',
            numHits: 'Number of Hits: <b>{num}</b>',
            probOMP: 'Overall probability of the query to be an OMP: <b>{num}%</b>',
            table: {
                num: 'Nr',
                hit: 'Hit',
                name: 'Name',
                probHits: 'Prob (hits)',
                probOMP: 'Prob (OMP)',
                eVal: 'E-value',
                ssScore: 'SS',
                cols: ' Aligned cols',
                targetLength: 'Target Length',
            },
            templateAlignment: 'Template alignment',
            alignmentInfo: 'Probability (hit): {probabHit}%&emsp;&emsp;Probability (OMP): {probabOMP}%&emsp;&emsp;' +
                'E-value: {eval}&emsp;&emsp;Score: {score}&emsp;&emsp;Aligned Cols: {alignedCols}&emsp;&emsp;' +
                'Identities: {identities}%',
        },
        hhblits: {
            noResults: 'No hits found! Please re-run HHblits with a custom-built multiple sequence alignment as ' +
                'input. Please also consider using different parameters.',
            numHits: 'Number of Hits: <b>{num}</b>',
            table: {
                num: 'Nr',
                hit: 'Hit',
                name: 'Name',
                probHits: 'Probability',
                eVal: 'E-value',
                cols: 'Aligned cols',
                targetLength: 'Target Length',
            },
            templateAlignment: 'Template alignment',
            alignmentInfo: 'Probability: {probab}%&emsp;&emsp;E-value: {eval}&emsp;&emsp;' +
                'Score: {score}&emsp;&emsp;Aligned Cols: {alignedCols}&emsp;&emsp;' +
                'Identities: {identities}%&emsp;&emsp;Similarity: {similarity}',
        },
        hhpred: {
            noResults: 'No hits found! Please re-run HHpred with an increased number of MSA generation steps or with ' +
                'a custom-built multiple sequence alignment as input (set "Maximal no. of MSA generation steps" to ' +
                '0). You could also consider picking a different target database and/or using different parameters.',
            qa3mWarning: 'Note: your query alignment consists of only <b>{num}</b> sequence(s).',
            uniclustWarning: ' You could improve the sensitivity of your search vastly by building a larger query ' +
                'alignment with PSI-BLAST over nre70. You could also consider increasing the number of MSA ' +
                'generation iterations and/or relaxing the E-value threshold.',
            psiblastWarning: ' You could improve the sensitivity of your search vastly by building a larger query ' +
                'alignment; for instance, by increasing the number of MSA generation iterations and/or relaxing the ' +
                'E-value threshold. Alternatively, you could input your own alignment.',
            customWarning: ' You could improve the sensitivity of your search vastly by providing a larger ' +
                'query alignment.',
            numHits: '<b>Number of Hits: {num}</b>',
            table: {
                num: 'Nr',
                hit: 'Hit',
                name: 'Name',
                probHits: 'Probability',
                eVal: 'E-value',
                ssScore: 'SS',
                cols: 'Cols',
                targetLength: 'Target Length',
            },
            templateAlignment: 'Template alignment',
            templateStructure: 'Template 3D Structure',
            alignmentInfo: 'Probability: {probab}&emsp;&emsp;E-value: {eval}&emsp;&emsp;Score: {score}&emsp;&emsp;' +
                'Aligned cols: {alignedCols}&emsp;&emsp;Identities: {ident}%&emsp;&emsp;Similarity: ' +
                '{similarity}&emsp;&emsp;',
        },
        psiblast: {
            noResults: 'No hits found! Please re-run ProtBLAST/PSI-BLAST with a custom-built multiple sequence ' +
                'alignment. You could also consider picking a different target database and/or using ' +
                'different parameters.',
            numHits: 'Number of Hits: <b>{num}</b>',
            table: {
                num: 'Nr',
                accession: 'Accession ID',
                description: 'Title',
                eValue: 'E-value',
                bitscore: 'Bitscore',
                ref_len: 'Length',
                hit_len: 'Aligned Positions',
            },
            alignmentInfo: 'E-value: {eval},&emsp;Length: {refLen},&emsp; Score: {bitScore} bits ({score})' +
                ',&emsp;Identities: {ident}/{hitLen} ({perIdent}%),&emsp;Positives: {pos}/{hitLen}  ' +
                '({perPos}%),&emsp;Gaps: {gap}/{hitLen}  ({perGap}%)',
        },
        hmmer: {
            noResults: 'No hits found! Please re-run HMMER with a custom-built multiple sequence alignment as input ' +
                '(set "MSA enrichment iterations using HHblits" to 0). You could also consider picking a different ' +
                'target database and/or using different parameters.',
            numHits: 'Number of Hits: <b>{num}</b>',
            table: {
                num: 'Nr',
                accession: 'ID',
                description: 'Title',
                full_evalue: 'E-value',
                eValue: 'Ind. E-value',
                bitscore: 'Bitscore',
                hit_len: 'Aligned Positions',
            },
            alignmentInfo: 'E-value: {fullEval}&emsp;&emsp;Ind. E-value: {eval}&emsp;&emsp;' +
                'Score: {bitScore}&emsp;&emsp;Aligned positions: {hitLen}&emsp;&emsp;' +
                'Observed domains: {observedDomains}',
        },
        imagesView: {
            noPeriodicity: 'No periodicity spectrum found!',
            noRepeats: 'No significant repeats found for given values!',
        },
        plot: {
            numHits: 'Number of Hits: <b>{num}</b>',
            noResults: 'No hits found! <br> Please consider picking a less stringent E-value/probability cut-off.' +
                'and/or pick a different target database.',
            prob: {
                title: 'Cumulative histogram of probability values',
                xLabel: 'Probability',
                yLabel: 'Count',
                legend: 'No. HSPs',
            },
            eval: {
                title: 'Cumulative histogram of E-values',
                xLabel: '-log10(E-Value)',
                yLabel: 'Count',
                legend: 'No. HSPs',
            },
        },
        hitlist: {
            visLink: 'Vis',
            hitsLink: 'Hits',
            alnLink: 'Aln',
            vis: 'Visualization',
            hits: 'Hitlist',
            aln: 'Alignments',
            resubmitSection: 'Resubmit Section',
            table: {
                paginationInfo: 'Displaying {start} to {end} of {total} hits',
                paginationInfoFiltered: 'Displaying {start} to {end} of ' +
                    '{totalRows} hits (filtered from {totalNoFilter} total hits)',
                noData: 'No hits found!',
                noDataFiltered: 'No matching hits found.',
                perPage: {
                    show: 'Show',
                    entries: 'Entries',
                },
                filter: 'Search:',
            },
        },
        hhrepid: {
            noResults: 'No repeats found!<br> Please try increasing the number of MSA generation steps or input your ' +
                'own custom MSA.',
            resultsForType: 'Results for repeats type {type}:',
            numResults: 'No. of repeats: {num}',
            pValue: 'P-value: {pval}',
            length: 'Length: {pval}',
        },
        templateAlignment: {
            title: 'Template Alignment (~100 most distinct sequences)',
            forwardTo: 'Forward to',
        },
        templateStructure: {
            title: 'Template 3D Structure',
        },
        sequenceFeatures: {
            coil: '&#9726;Coiled coil segment(s)&emsp;',
            tm: '&#9726;Transmembrane segment(s)&emsp;',
            signal: '&#9726;Signal peptide&emsp;',
        },
    },
    forwarding: {
        selectPlaceholder: 'Please select a tool',
    },
    submitJob: 'Submit Job',
    resubmitJob: 'Resubmit Job',
    delete: 'Delete Job',
    foundIdenticalCopy: 'We found an identical copy of your job in our database!<br>' +
        'Job ID: {jobID}, which was created {createdAt}.',
    startJob: 'Start job anyways',
    loadExistingJob: 'Load existing job',
    loadExistingJobAndDelete: 'Load existing job and delete this one',
    notifications: {
        titles: {
            update: 'Job Update',
        },
        jobFinished: 'Your {tool} job has finished!',
    },
    citationInfo: `If you use {tool} within the Toolkit for your research, please cite:<br>
<a href="http://www.sciencedirect.com/science/article/pii/S0022283617305879" target="_blank" rel="noopener">
A Completely Reimplemented MPI Bioinformatics Toolkit with a New HHpred Server at its Core.
</a>`,
    jobIDDetails: 'Job ID: {jobID}',
};
