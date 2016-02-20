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
      jobs.vm.update(message.job_id, state)

      # Show user a popup with the submission
      if state == '0'
        $('.jobformclear').click()

    # User has entered a JobID that is already in use
    # // TODO This case can be handled on client-side
    when "jobidinvalid"
      text = "Sorry, but this jobID is already used by you."
      $(".jobformsubmit").notify(text)

    when "joblist"
      for job in message.jobs
        jobs.vm.update(job.i, job.s)

  m.endComputation()
