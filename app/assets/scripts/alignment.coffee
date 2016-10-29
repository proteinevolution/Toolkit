###*
# Provides Visualizations and functions for handling alignment results ins various formats.
# They can be declared global to access them from anywhere in the views.
###


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
          $('#' + tabid).first().append("<tr><td>#{header}</td></tr>")
          headerseen = true

        if spt.length == 2
          $('#' + tabid).first().append("<tr><td>#{spt[0]}</td><td>#{spt[1]}</td> </tr>")
          counter++

        else if spt.length == 1 and beforeLength != spt.length
          $('#' + tabid).first().append("<tr class=\"spaceUnder\"><td></td><td></td></tr>")
          beforeLength = spt.length
    $('#' + statid).first().append("<tr><td>Number of sequences</td><td>#{noSeq}</td></tr>")
  , 'text')



@tcoffeeColored = (tabid, Colored) ->

  # Makes AJAX call to the file on server
  $.get(Colored, (content) ->

    $('#' + tabid ).prepend content

  )








  