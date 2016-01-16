$ ->
  ws = new WebSocket $("body").data("ws-url")

  # Handles the behavior that occurs if the WebSocket receives data from the Server
  # TODO For instance, we can update the Job Monitor Widget here
  ws.onmessage = (event) ->
    message = JSON.parse event.data

    switch message.type
      when "JobInitStatus"
        text = "The Job for tool " + message.toolname + " with JobID " + message.jobid + " has been started successfully."
        $(".jobformsubmit").notify(text, message.status)

      when "JobDone"
        $(".jobformsubmit").notify(
          "Hello World", "success",
          { globalPosition:"bottom right" }
        )


  # Handles the behavior when the submit button is pressed in a job form
  $(".jobform").submit (event) ->
    event.preventDefault()

    $.ajax
      url: submitRoute.url
      type: submitRoute.type
      data: $(".jobform").serialize()
      #dataType: "json"
      error: (jqXHR, textStatus, errorThrown) ->

        alert errorThrown
      success: (data, textStatus, jqXHR) ->
        #$('body').append "Successful AJAX call: #{data}"



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