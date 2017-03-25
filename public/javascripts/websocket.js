/*
 *  Handles the Connection to the WebSocket and its connection to ModelView elements like provided by
 *  Mithril.
 */
var connect, connected, connecting, connectionCountdown, connectionTimeRandomizer, onClose, onError, onMessage, onOpen, reconnect, reconnecting, retryCount, retryCountdown, retryCountdownInterval;

this.ws = null;

connected = false;

connecting = false;

reconnecting = false;

retryCount = 1;

retryCountdown = 0;

retryCountdownInterval = null;

connect = function() {
    if (!connecting) {
        reconnecting = false;
        connecting = true;
        clearInterval(retryCountdownInterval);
        this.ws = new WebSocket($("body").data("ws-url"));
        this.ws.onopen = function(evt) {
            return onOpen(evt);
        };
        this.ws.onclose = function(evt) {
            return onClose(evt);
        };
        this.ws.onmessage = function(evt) {
            return onMessage(evt);
        };
        return this.ws.onerror = function(evt) {
            return onError(evt);
        };
    }
};

connectionCountdown = function() {
    retryCountdown = retryCountdown - 1;
    $("#offline-alert-timer").text(retryCountdown);
    if (retryCountdown < 1) {
        retryCountdown = 1;
        clearInterval(retryCountdownInterval);
        return connect();
    }
};

connectionTimeRandomizer = function() {
    if (retryCount < 38) {
        retryCount++;
    }
    return parseInt(Math.random() * 3 * retryCount + 6);
};

reconnect = function(force) {
    if (force == null) {
        force = false;
    }
    if ((!reconnecting && !connected && !connecting) || force) {
        ws.close();
        connected = false;
        connecting = false;
        reconnecting = true;
        retryCountdown = parseInt(connectionTimeRandomizer());
        $("#offline-alert").fadeIn();
        connectionCountdown();
        return retryCountdownInterval = setInterval(connectionCountdown, 1000);
    }
};

onOpen = function(event) {
    clearInterval(retryCountdownInterval);
    connected = true;
    connecting = false;
    retryCount = 1;
    $("#offline-alert").fadeOut();
};

onError = function(event) {
    return setTimeout(reconnect(true), 3000);
};

onClose = function(event) {
    connected = false;
    return setTimeout(reconnect, 5000);
};

onMessage = function(event) {
    var i, job, jobHTMLString, len, message, ref, text;
    message = JSON.parse(event.data);
    switch (message.type) {
        case "ClearJob":
            m.startComputation();
            JobListComponent.removeJob(message.jobID);
            return m.endComputation();
        case "PushJob":
            m.startComputation();
            JobListComponent.pushJob(JobListComponent.Job(message.job));
            m.endComputation();
            if (message.state === 0) {
                return $('.jobformclear').click();
            }
            break;
        case "JobIDUnknown":
            text = "Sorry, but there is no such Job ID.";
            return $(".jobsearchform").notify(text);
        case "AutoCompleteReply":
            return $("#jobsearch").autocomplete({
                source: message.list
            });
        case "SearchReply":
            jobHTMLString = "<p>found jobs:</p>";
            ref = message.list;
            for (i = 0, len = ref.length; i < len; i++) {
                job = ref[i];
                jobHTMLString += "<p>Job ID: " + job.jobID + " JobID: " + job.jobID + "</p>";
            }
            return $("#modal").html(jobHTMLString).foundation('open');
        case "Ping":
            return sendMessage({
                "type": "Ping"
            });
    }
};

window.sendMessage = function(object) {
    return ws.send(JSON.stringify(object));
};

this.addJob = function(jobID) {
    return sendMessage({
        "type": "AddJob",
        "jobID": jobID
    });
};

connect();

$("#offline-alert").on('click', function(event) {
    return connect();
});