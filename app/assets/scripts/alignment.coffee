###*
# Provides Visualizations and functions for handling alignment results ins various formats.
# They can be declared global to access them from anywhere in the views.
###


### TODO this script should be replaced with server side processing ###

@simple = (metaid, tabid, statid, pathToAlignmentFile) ->


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
        if(line.indexOf('*') > -1)
          continue
        if headerseen == false
          header = spt.toString()
          header = header.replace(/,/g, " ");
          $('#' + metaid).first().append("#{header}")
          $('#' + tabid).first().append("<div style='height: 30px;'></div>")
          $('#' + tabid).first().append("<tr><td></td><td></td><td>Header</td><td>Sequences</td></tr>")
          headerseen = true
          $('#' + tabid).first().append("<div style='height: 30px;'></div>")

        if spt.length == 2
          $('#' + tabid).first().append("<tr><td>#{counter}</td><td><input style='margin-top: 16px; padding-right: 5px;'  type='checkbox' name='hits' class='hits' value='#{counter}'> &nbsp;&nbsp;</td><td>#{spt[0]}</td><td>#{spt[1]}</td></tr>")
          counter++

        else if spt.length == 1 and beforeLength != spt.length
          $('#' + tabid).first().append("<tr class=\"spaceUnder\"><td></td><td></td></tr>")
          beforeLength = spt.length
    $('#' + statid).first().append("<tr><td>Number of sequences</td><td>#{noSeq}</td></tr>")
  , 'text')







  