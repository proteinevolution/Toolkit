Validation = (data) ->

  this.alignment = $("#alignment").val()


validator = new m.validator {

  alignment: (alignment) ->

    if alignment.indexOf('>') == -1
      return "Not alignment"
}


$("#alignment").bind 'input propertychange', (event) ->

  #console.log(new Validation().alignment)
  if(validator.validate(new Validation()).hasError('alignment'))
    console.log("Not alignment")
  else
    console.log("Is alignment")

###






  // Our mithril model
var Todo = function (data) {
  this.name = m.prop(data.name || '')
  this.done = m.prop(data.done)
}

// Initialize a new validator
var validator = new m.validator({

  // Check model name property
  name: function (name) {
    if (!name) {
      return "Name is required."
    }
  }

})

// Results in "Name is required."
validator.validate(new Todo()).hasError('name')
###