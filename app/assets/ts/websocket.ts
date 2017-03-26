/*
 *  Handles the Connection to the WebSocket and its connection to ModelView elements like provided by
 *  Mithril.
 */
let connect : any,
    connected : boolean,
    connecting : any,
    connectionCountdown : any,
    connectionTimeRandomizer : any,
    onClose : any,
    onError : any,
    onMessage : any,
    onOpen : any,
    reconnect : any,
    reconnecting : any,
    retryCount : number,
    retryCountdown : number,
    addJob : any,
    retryCountdownInterval : number;


let ws : any = null;

connected = false;

connecting = false;

reconnecting = false;

retryCount = 1;

retryCountdown = 0;

retryCountdownInterval = null;

connect = function() : any {
    if (!connecting) {
        reconnecting = false;
        connecting = true;
        clearInterval(retryCountdownInterval);
        ws = new WebSocket($("body").data("ws-url"));
        ws.onopen = function(evt : any) : any {
            return onOpen(evt);
        };
        ws.onclose = function(evt : any) : any{
            return onClose(evt);
        };
        ws.onmessage = function(evt : any) : any{
            return onMessage(evt);
        };
        return ws.onerror = function(evt : any) : any{
            return onError(evt);
        };
    }
};

connectionCountdown = function() : any {
    retryCountdown = retryCountdown - 1;
    $("#offline-alert-timer").text(retryCountdown);
    if (retryCountdown < 1) {
        retryCountdown = 1;
        clearInterval(retryCountdownInterval);
        return connect();
    }
};

connectionTimeRandomizer = function() : any {
    if (retryCount < 38) {
        retryCount++;
    }
    return parseInt("" + Math.random() * 3 * retryCount + 6);
};

reconnect = function(force : any) : any{
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

onOpen = function(event : any) : any {
    clearInterval(retryCountdownInterval);
    connected = true;
    connecting = false;
    retryCount = 1;
    $("#offline-alert").fadeOut();
};

onError = function(event : any) : any {
    return setTimeout(reconnect(true), 3000);
};

onClose = function(event : any) : any {
    connected = false;
    return setTimeout(reconnect, 5000);
};

onMessage = function(event : any) : any {
    let i : number,
        job : any,
        jobHTMLString : string,
        len : number,
        message : any,
        ref : any,
        text : string;
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
        case "UpdateLoad":
            console.log(message.load);
            //$(".clusterLoad").text(message.load);
            $("td#currentLoad").text((parseFloat(message.load) * 100).toPrecision(4) + " %");
            if(message.load > 0.9)
                $("td#currentLoad").css("color", "red");
            else if (message.load < 0.7)
                $("td#currentLoad").css("color", "green");
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
            return (<any>window).sendMessage({
                "type": "Ping"
            });
    }
};

(<any>window).sendMessage = function(object : any) : any {
    return ws.send(JSON.stringify(object));
};

addJob = function(jobID : string) : any {
    return (<any>window).sendMessage({
        "type": "AddJob",
        "jobID": jobID
    });
};

connect();

$("#offline-alert").on('click', function(event) {
    return connect();
});