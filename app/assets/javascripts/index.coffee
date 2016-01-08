$ ->
  ws = new WebSocket $("body").data("ws-url")

  # Handles the behavior that occurs if the WebSocket receives data from the Server
  # TODO For instance, we can update the Job Monitor Widget here
  ws.onmessage = (event) ->
    alert "There has been an Event"

  # Handles the behavior when the submit button is pressed in a job form
  $(".jobform").submit (event) ->
    event.preventDefault()
    # send form data over the websocket
    ws.send(JSON.stringify({jobinit: $(".jobform").serialize()}))



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
###