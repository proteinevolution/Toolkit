###
#  Handles the Connection to the WebSocket and its connection to ModelView elements like provided by
#  Mithril.
###

$ ->
  ws = new WebSocket $("body").data("ws-url")

  # Handles the behavior that occurs if the WebSocket receives data from the Server
  ws.onmessage = (event) ->

    # Need to make recalc of Mithril explicit here, since this code is not part of the Mithril view
    m.startComputation()

    message = JSON.parse event.data

    switch message.type
      # Jobstate has changed
      when "jobstate"
        state = message.newState.toString()
        console.log(state)
        todo.vm.update(message.jobid, state)

        # Show user a popup with the submission
        if state == '0'
          text = "Job submitted successfully."
          $(".jobformsubmit").notify(text, "success")
          $('.jobformclear').click()

      # User has entered a JobID that is already in use
      # // TODO This case can be handled on client-side
      when "jobidinvalid"
        text = "Sorry, but this jobID is already used by you."
        $(".jobformsubmit").notify(text)
    m.endComputation()



###
  #  File Uplaod
  $("#file").submit (event) ->
    event.preventDefault

    # Load file from the input  field
    files = document.getElementById('files').files
    if !files.length
      alert 'Please select a file!'

    file = files[0]

    # Load a worker to deal with the file
    worker = new Worker('fileupload.js')

###

###

$.notify.addStyle("buttonsimple", {
  html:
  "<div>" +
    "<div class='clearfix'>" +
    "<div class='title' data-notify-html='title'/>" +
    "<div class='buttons'>" +
    "<button class='no'>Cancel</button>" +
    "<button class='yes' data-notify-text='button'></button>" +
    "</div>" +
    "</div>" +
    "</div>"
})


$(".notifyjs-foo-base .no").onclick() -> this.trigger("notify-hide")


###



###
  $ ->
  ws = new WebSocket $("body").data("ws-url")
  ws.onmessage = (event) ->
    message = JSON.parse event.data
    switch message.type
      when "message"
        $("#board tbody").append("<tr><td>" + message.uid + "</td><td>" + message.msg + "</td></tr>")
      else
        console.log(message)

  $("#msgform").submit (event) ->
    event.preventDefault()
    console.log($("#msgtext").val())
    # send the message to watch the stock
    ws.send(JSON.stringify({msg: $("#msgtext").val()}))
    # reset the form
    $("#msgtext").val("")




//listen for click events from this style
$(document).on('click', '.notifyjs-foo-base .no', function() {
  //programmatically trigger propogating hide event
  $(this).trigger('notify-hide');
});
$(document).on('click', '.notifyjs-foo-base .yes', function() {
  //show button text
  alert($(this).text() + " clicked!");
  //hide notification
  $(this).trigger('notify-hide');
});


###