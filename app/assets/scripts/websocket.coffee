###
#  Handles the Connection to the WebSocket and its connection to ModelView elements like provided by
#  Mithril.
###
# The Websocket will be globally available
@ws = new WebSocket $("body").data("ws-url")

  # Handles the behavior that occurs if the WebSocket receives data from the Server
ws.onmessage = (event) ->

  # Need to make recalc of Mithril explicit here, since this code is not part of the Mithril view
  m.startComputation()

  message = JSON.parse event.data

  switch message.type
      # Jobstate has changed
    when "updatejob"
      state = message.state.toString()
      console.log(state)
      jobs.vm.update(message.job_id, state, message.toolname)

      # Show user a popup with the submission
      if state == '0'
        $('.jobformclear').click()

    # User was looking for a job_id which was not valid
    when "jobidinvalid"
      text = "Sorry, but there is no such Job ID."
      $(".jobsearchform").notify(text)

    # Updates the Joblist by handing over a Json Array of Jobs
    when "joblist"
      jobs.vm.updateList(message.list)

    when "autocomplete"
      autocomplete.data.response(message.list)

    when "ping"
      requestJson = ("type":"ping")
      alert "type: " + requestJson.type
      ws.send(requestJson)

  m.endComputation()
