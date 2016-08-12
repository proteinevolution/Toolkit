###
#  Handles the Connection to the WebSocket and its connection to ModelView elements like provided by
#  Mithril.
###
# The Websocket will be globally available
@ws = new WebSocket $("body").data("ws-url")

# requests the joblist
ws.onopen = (event) ->
  ws.send(JSON.stringify("type":"GetJobList"))

#TODO add a check for this event instead of giving an alert
ws.onclose = (event) ->
  #alert "WS Closed " + event.code + " - " + event.reason + " - " + event.wasClean

# Handles the behavior that occurs if the WebSocket receives data from the Server
ws.onmessage = (event) ->

  # Need to make recalc of Mithril explicit here, since this code is not part of the Mithril view
  m.startComputation()

  message = JSON.parse event.data

  switch message.type
    # Jobstate has changed
    when "UpdateJob"
      state = message.state.toString()
      console.log(state)
      jobs.vm.update(message.job_id, state, message.toolname)

      # Show user a popup with the submission
      if state == '0'
        $('.jobformclear').click()

    # get all jobs from the server
    when "UpdateAllJobs"
      ws.send(JSON.stringify("type":"GetJobList"))

    # User was looking for a job id which was not valid
    when "JobIDUnknown"
      text = "Sorry, but there is no such Job ID."
      $(".jobsearchform").notify(text)

    # Updates the Joblist by handing over a Json Array of Jobs
    when "JobList"
      jobs.vm.updateList(message.list)

    when "AutoComplete"
      autocomplete.data.response(message.list)

    when "Ping"
      requestJson = ("type":"Ping")
      ws.send(requestJson)

  m.endComputation()
