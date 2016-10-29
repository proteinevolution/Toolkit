###
#  Handles the Connection to the WebSocket and its connection to ModelView elements like provided by
#  Mithril.
###
# The Websocket will be globally available, needs to be defined here
@ws = null
####connection stati, reconnection counter and timer####
connected  = false
connecting = false
reconnecting = false
retryCount = 1
retryCountdown = 0
retryCountdownInterval = null

####connection methods####
connect = () ->
  if !connecting
    reconnecting = false
    connecting   = true
    clearInterval(retryCountdownInterval)
    this.ws = new WebSocket $("body").data("ws-url")
    this.ws.onopen    = (evt) -> onOpen(evt)
    this.ws.onclose   = (evt) -> onClose(evt)
    this.ws.onmessage = (evt) -> onMessage(evt)
    this.ws.onerror   = (evt) -> onError(evt)

connectionCountdown = () ->
  retryCountdown = retryCountdown - 1
  $("#offline-alert-timer").text(retryCountdown)
  if(retryCountdown < 1)
    retryCountdown = 1
    clearInterval(retryCountdownInterval)
    connect()

connectionTimeRandomizer = () ->
  if(retryCount < 38)
    retryCount++
  return parseInt(Math.random() * 3 * retryCount + 6)

reconnect = (force = false) ->
  if (!reconnecting && !connected && !connecting) || force
    ws.close()
    connected      = false
    connecting     = false
    reconnecting   = true
    retryCountdown = parseInt(connectionTimeRandomizer())
    $("#offline-alert").fadeIn()
    connectionCountdown()
    retryCountdownInterval = setInterval(connectionCountdown, 1000)

####events####
onOpen = (event) ->
  clearInterval(retryCountdownInterval)
  connected  = true
  connecting = false
  retryCount = 1
  $("#offline-alert").fadeOut()
  m.startComputation()
  #jobs.vm.loadList()
  m.endComputation()

onError = (event) ->
  setTimeout(reconnect(true), 3000)

onClose = (event) ->
  connected      = false
  setTimeout(reconnect, 5000)
  #alert "WS Closed " + event.code + " - " + event.reason + " - " + event.wasClean

# Handles the behavior that occurs if the WebSocket receives data from the Server
onMessage = (event) ->

  message = JSON.parse event.data

  switch message.type
  # Jobstate has changed
    when "UpdateJob"
      state = message.job.state.toString()
      mainID = message.job.mainID.toString()
      m.startComputation()
      Job.updateState(mainID, state)
      m.endComputation()
      if jobs.vm.getJobState(message.job) == 'running'
        $('#trafficbar').css
          'background': '#ffff00'
          'box-shadow': '0 0 10px #ffce27'
      else if jobs.vm.getJobState(message.job) == 'error'
        if window.Notification and Notification.permission != 'denied'
          Notification.requestPermission (status) ->
          n = new Notification('Bioinformatics Toolkit',
            body: 'Job ' + message.job.mainID + " has failed!"
            icon: '')
          titlenotifier.add();
        $('#trafficbar').css
          'background': '#ff0000'
          'box-shadow': '0 0 10px #d2071d'
      else if jobs.vm.getJobState(message.job) == 'done'
        if window.Notification and Notification.permission != 'denied'
          Notification.requestPermission (status) ->
          n = new Notification('Bioinformatics Toolkit',
            body: 'Job ' + message.job.mainID + " has finished!"
            icon: '')
          titlenotifier.add();
        $('#trafficbar').css
          'background': 'green'
          'box-shadow': '0 0 10px darkgreen'
      else if jobs.vm.getJobState(message.job) == 'other'
        $('#trafficbar').css
          'background': 'transparent'
          'box-shadow': '0 0 10px transparent'

      # Show user a popup with the submission
      if state == '0'
        $('.jobformclear').click()

    # User was looking for a job id which was not valid
    when "JobIDUnknown"
      text = "Sorry, but there is no such Job ID."
      $(".jobsearchform").notify(text)

    when "AutoCompleteReply"
      $("#jobsearch").autocomplete(source:message.list)

    when "SearchReply"
      jobHTMLString = "<p>found jobs:</p>"
      for job in message.list
        jobHTMLString += "<p>MainID: " + job.mainID + " JobID: " + job.job_id + "</p>"
      $("#modal").html(jobHTMLString).foundation('open')

    when "Ping"
      sendMessage("type":"Ping")

@sendMessage = (object) ->
  ws.send(JSON.stringify(object))

@addJob = (mainID) ->
  sendMessage("type":"AddJob","mainID":mainID)

# everything is in the DOM, start the connection.
connect()
# let user reconnect manually
$("#offline-alert").on 'click', (event) ->
  connect()

