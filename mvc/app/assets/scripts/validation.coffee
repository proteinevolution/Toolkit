Validation = (data) ->

  a = $("#alignment").val()
  this.alignment = a ? a : ""


validator = new m.validator {

  alignment: (alignment) ->

    if not process(alignment)
        return "No Alignment"

}

process = (alignment) ->

  seq = alignment.split('\n')

  if seq.length < 2
    return false

  seqlength = -1
  tmp = -1
  headercount = 0

  for sequence in seq
    sequence = sequence.trim()
    if sequence.startsWith('>')
      headercount++
      if seqlength == 0
        return false
      if tmp == -1
        tmp = seqlength
      else if seqlength != tmp
        return false
      seqlength = 0

    else
      seqlength += sequence.length
      console.log(seqlength)



  return seqlength != 0 and tmp == seqlength and headercount > 1 


$("#alignment").bind 'input propertychange', (event) ->

  alignment = $("#alignment").val()
  #console.log(new Validation().alignment)
  if(validator.validate(new Validation()).hasError('alignment'))
    console.log("Not alignment")
    $('#foo').prop('disabled', true);
  else
    console.log("Is alignment")
    $('#foo').prop('disabled', false);
