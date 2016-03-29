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

      clustal[0] = clustal[0].replace(" ", "")
      if not clustal[0].startsWith('CLUSTALW')
        return "No CLUSTALW Header"

}


$("#alignment").bind 'input propertychange ', (event) ->

  alignment = $("#alignment").val()
  format = $("#format").val()
  val = validator.validate(new Validation())

  switch format
    when "fas"
      if val.hasError('alignment')
        error = val.errors['alignment']
        $('#alert').empty()
        $('#alert').prop('hidden',false).append(error)
        $('#submitbutton').prop('disabled', true);
      else
        $('#submitbutton').prop('disabled', false);
        $('#alert').prop('hidden',true)
    when "clu"
      if val.hasError('clustal')
        error = val.errors['clustal']
        $('#alert').empty()
        $('#alert').prop('hidden',false).append(error)
        $('#submitbutton').prop('disabled', true);
      else
        $('#submitbutton').prop('disabled', false);
        $('#alert').prop('hidden',true)

