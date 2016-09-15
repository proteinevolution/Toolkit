###*
# Provides Visualizations and functions for handling alignment results ins various formats.
# They can be declared global to access them from anywhere in the views.
###

###*
# Computes a simple HTML table and some statistics about an alignment file.
# This is perhaps the most simple visualization one can imagine for an alignment file
# TODO Add possibility to highlight all segments that belong to one particular sequence (like chekckboxes)
# @param {string} tabid The ID of the HTML table the alignment rows should be mounted on
# @param {string} statid The ID of the HMTL table the statistic elements should be added to
# @param {string} pathToAlignmentFile The path to the alignment file made available by the server.
###
@fastaSimple = (tabid, statid, pathToFastaFile) ->

  # Makes AJAX call to the file on server
  $.get(pathToFastaFile, (content) ->

    $('#' + tabid ).append content

  )

@simple = (tabid, statid, pathToAlignmentFile) ->

  # Makes AJAX call to the file on server
  $.get(pathToAlignmentFile, (content) ->

    # Counts the number of sequences
    counter = 0
    noSeq = null
    headerseen = false

    for line in content.split "\n"

        # No of sequences in the alignment has been determined
        if not line.trim() and counter != 0 and noSeq == null
          noSeq = counter

        spt = line.split(/[ \t]+/)

        if headerseen == false
          header = spt.toString()
          header = header.replace(/,/g, " ");
          $('#' + tabid).prepend("<tr><td>#{header}</td></tr>")
          headerseen = true

        if spt.length == 2
          $('#' + tabid).prepend("<tr><td>#{spt[0]}</td><td>#{spt[1]}</td> </tr>")
          counter++

        else if spt.length == 1 and beforeLength != spt.length
          $('#' + tabid).prepend("<tr class=\"spaceUnder\"><td></td><td></td></tr>")
          beforeLength = spt.length
    $('#' + statid).prepend("<tr><td>Number of sequences</td><td>#{noSeq}</td></tr>")
  , 'text')


@blastOutput = (tabid, statid, pathToBlastOutput) ->

    # Makes AJAX call to the file on server
  $.get(pathToBlastOutput, (content) ->

    $('#' + tabid ).append content

   )





  