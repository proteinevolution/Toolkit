Validation = (data) ->

  a = $("#alignment").val()
  this.alignment = a ? a : ""
  this.clustal = a ? a : ""


validator = new m.validator {

  alignment: (alignment) ->
    seq = alignment.split('\n')

    seqlength = -1
    tmp = -1
    headercount = 0

    if seq.length < 2
      return "At least two sequences"

    for sequence in seq
      sequence = sequence.trim()
      if sequence.startsWith('>')
        headercount++
        if seqlength == 0
          return "No empty sequences"
        if tmp == -1
          tmp = seqlength
        else if seqlength != tmp
          return "Sequences have to be the same length"
        seqlength = 0
      else
        seqlength += sequence.length

    if headercount <= 1
      return "Alignment needs more then one sequence"

    if seqlength == 0
      return "No empty sequences"

    if seqlength != tmp
      return "Sequences have to be the same length"

  clustal: (clustal) ->
    clustal = clustal.split('\n')

    headerSeen = false

    for sequence in clustal

      # Skip empty rows
      if sequence.match(/^\s*$/)
        continue

      if headerSeen == true
        sequence = sequence.trim()
        lines = sequence.split(/\s+/g);
        if lines.length != 2 and lines.length != 3
          return "Each line has to include name/sequence and optional length"
        ## if lines[1].match(/[AaRrNnDdCcQqEeGgHhIiLlKkMmFfPpSsTtWwYyVv-]+/)
        ## return 'nope'
        if lines[1].length > 60
          return 'More then 60 sequence symbols in one line'

      else
        header = sequence.trim().replace(' ', '')
        if not header.startsWith('CLUSTAL')
          return 'No CLUSTAL Header'
        # Check header
        headerSeen = true


}

@revalidate = () ->
  alignment = $("#alignment").val()
  format = $("#format").val()
  val = validator.validate(new Validation())

  switch format
    when "fas"
      if val.hasError('alignment')
        error = val.errors['alignment']
        $('#alert').empty()
        $('#alert').prop('hidden',false).append(error)
        $('#submitJob').prop('disabled', true);
        $('#prepareJob').prop('disabled', true);
      else
        $('#submitJob').prop('disabled', false);
        $('#prepareJob').prop('disabled', false);
        $('#alert').prop('hidden',true)
    when "clu"
      if val.hasError('clustal')
        error = val.errors['clustal']
        $('#alert').empty()
        $('#alert').prop('hidden',false).append(error)
        $('#submitJob').prop('disabled', true);
        $('#prepareJob').prop('disabled', true);
      else
        $('#submitJob').prop('disabled', false);
        $('#prepareJob').prop('disabled', false);
        $('#alert').prop('hidden',true)
