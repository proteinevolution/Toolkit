/*
 *  Handles the Connection to the WebSocket and its connection to ModelView elements like provided by
 *  Mithril.
 */

let ws : WebSocket = null,
    connect      : Function,
    onClose   : Function,
    onError   : Function,
    onMessage : Function,
    onOpen    : Function,
    addJob : Function;
let notifications = 0;
let attempts = 1;
declare var titlenotifier: any;
let connectCount = 0;


connect = function() : any {
        let wsRoute = jsRoutes.controllers.Application.ws;
        let isSecure : boolean = location.protocol === "https:";
        ws = new WebSocket(wsRoute().webSocketURL(isSecure));   // create the new websocket
        ws.onopen    = function(evt : Event)        : any { return onOpen(evt); };
        ws.onclose   = function(evt : CloseEvent)   : any { return onClose(evt); };
        ws.onmessage = function(evt : MessageEvent) : any { return onMessage(evt); };
        ws.onerror   = function(evt : ErrorEvent)   : any { return onError(evt); };
        return;
};

// use exponential backoff algorithm

let generateInterval = function(k : number) {
    let maxInterval = (Math.pow(2, k) - 1) * 1000;

    if (maxInterval > 30*1000) {
        maxInterval = 30*1000; // If the generated interval is more than 30 seconds, truncate it down to 30 seconds.
    }

    // generate the interval to a random number between 0 and the maxInterval determined from above
    return Math.random() * maxInterval;
};

onOpen = function(event : Event) : any {
    attempts = 1;
    console.log("Websocket is Connected.");
    $("#offline-alert").fadeOut();  // Hide the Offline alert
};

onError = function(event : ErrorEvent) : any {
    $("#offline-alert").fadeIn();   // show the "Reconnecting ..." message
    let time = generateInterval(attempts);
    console.log("trying to reconnect in ... " + time);
    // We've tried to reconnect so increment the attempts by 1
    attempts++;
    setTimeout(connect(), time);
};

onClose = function(event : CloseEvent) : any {
    $("#offline-alert").fadeIn();   // show the "Reconnecting ..." message
    let time = generateInterval(attempts);
    console.log("trying to reconnect in ... " + time);
    // We've tried to reconnect so increment the attempts by 1
    attempts++;
    //connected = false;
    setTimeout(connect(), time);
};



onMessage = function(event : MessageEvent) : any {
    let message : any = JSON.parse(event.data);
    console.log("WS received a message: ", message.type);
    switch (message.type) {
        case "ClearJob":
            m.startComputation();
            JobListComponent.removeJob(message.jobID);
            if (message.delete) {JobManager.removeFromTable(message.jobID);}
            else {if(JobManager.table){JobManager.reload();}}
            m.endComputation();
            break;
        case "PushJob":
            m.startComputation();
            //console.log("WSS " + JSON.stringify(message.job));
            JobListComponent.pushJob(JobListComponent.Job(message.job));
            LiveTable.pushJob(message.job);
            JobManager.pushToTable(message.job);
            if(message.job.status == 4 || message.job.status == 5) {
                notifications += 1;
                titlenotifier.set(notifications);
            }
            m.endComputation();
            break;
        case "UpdateLoad":
            // Tried to limit this by saving the "currentRoute", but we might need something proper in the future.
             if (currentRoute === "index" && !noRedraw) {
                LoadBar.updateLoad(message.load);
             }
            break;
        case "Ping":
            sendMessage({
                "type": "Ping"
            });
            break;
        case "WatchLogFile":
            m.startComputation();
            JobRunningComponent.updateLog(message.jobID, message.lines);
            m.endComputation();
            break;
        case "Terminate":
            m.startComputation();
            JobRunningComponent.terminate(message.jobID);
            m.endComputation();
            break;
        case "MaintenanceAlert":
            $('.maintenance_alert').show();
            break;
        default:
            break;
    }
};


let sendMessage = function(object : any) : void {
    console.log("Sending message:", object);
    ws.send(JSON.stringify(object));
};
addJob = function(jobID : string) : any { sendMessage({ "type": "AddJob", "jobID": jobID }); };

connect();

$("#offline-alert").on('click', function(event) { connect(); });