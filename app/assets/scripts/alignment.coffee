@simple = (tabid, statid, pathToAlignmentFile) ->

  $.get(pathToAlignmentFile, (content) ->

    counter = 0
    noSeq = null
    for line in content.split "\n"

        # Number of sequences were counted
        if not line.trim() and counter != 0 and noSeq == null
          noSeq = counter

        spt = line.split(/[ \t]+/)
        if spt.length == 2
          $('#' + tabid).append("<tr><td>#{spt[0]}</td><td>#{spt[1]}</td> </tr>")
          counter++

        else
          $('#' + tabid).append("<tr class=\"spaceUnder\"><td></td><td></td></tr>")
    $('#' + statid).append("<tr><td>Number of sequences</td><td>#{noSeq}</td></tr>")
  , 'text')

