Validation = (data) ->

  a = $("#alignment").val()
  this.alignment = a ? a : ""


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






}

#process = (alignment) ->



 # return seqlength != 0 and tmp == seqlength and headercount > 1


$("#alignment").bind 'input propertychange', (event) ->

  alignment = $("#alignment").val()
  #console.log(new Validation().alignment)
  if(validator.validate(new Validation()).hasError('alignment'))
    console.log(validator.validate(new Validation()).errors)
    if validator.validate(new Validation()).errors["alignment"] == "At least two sequences"
      $('#alert').empty()
      $('#alert').prop('hidden',false).append("At least two sequences")
      $('#submitbutton').prop('disabled', true);

    else if validator.validate(new Validation()).errors["alignment"] == "No empty sequences"
      $('#alert').empty()
      $('#alert').prop('hidden',false).append("No empty sequences")
      $('#submitbutton').prop('disabled', true);

    else if validator.validate(new Validation()).errors["alignment"] == "Sequences have to be the same length"
      $('#alert').empty()
      $('#alert').prop('hidden',false).append("Sequences have to be the same length")
      $('#submitbutton').prop('disabled', true);

    else if validator.validate(new Validation()).errors["alignment"] == "Alignment needs more then one sequence"
      $('#alert').empty()
      $('#alert').prop('hidden',false).append("Alignment needs more then one sequence")
      $('#submitbutton').prop('disabled', true);

  else
    $('#submitbutton').prop('disabled', false);
    $('#alert').prop('hidden',true)
