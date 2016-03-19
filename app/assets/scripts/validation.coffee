Validation = (data) ->

  this.alignment = $("#alignment").val()


validator = new m.validator {

  alignment: (alignment) ->

    if not process(alignment)
        return "nope alignment"

}

process = (alignment) ->

  seq = alignment.split('\n')

  seqlength = -1

  for sequence in seq
    if sequence.startsWith('>')
      if seqlength == 0
        return false
      seqlength = 0
    else
      seqlength += sequence.length
      console.log(seqlength)

  return seqlength != 0





$("#alignment").bind 'input propertychange', (event) ->

  alignment = $("#alignment").val()
  process(alignment)
  #console.log(new Validation().alignment)
  if(validator.validate(new Validation()).hasError('alignment'))
    console.log("Not alignment")
    $('#foo').prop('disabled', true);
  else
    console.log("Is alignment")
    $('#foo').prop('disabled', false);
