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
    when "jobstate"
      state = message.newState.toString()
      console.log(state)
      jobs.vm.update(message.job_id, state, toolname)

      # Show user a popup with the submission
      if state == '0'
        $('.jobformclear').click()

    # User was looking for a job_id which was not valid
    when "jobidinvalid"
      text = "Sorry, but there is no such Job ID."
      $(".jobsearchform").notify(text)

    # Updates the Joblist by removing the old ones and requesting the list again
    when "updatejoblist"
      jobs.vm.retrieveJobs()

  m.endComputation()
