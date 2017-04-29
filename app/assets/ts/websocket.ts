/*
 *  Handles the Connection to the WebSocket and its connection to ModelView elements like provided by
 *  Mithril.
 */

let ws : WebSocket = null,
    connect      : Function,
    connected    : boolean = false,
    connecting   : boolean = false,
    reconnecting : boolean = false,
    connectionCountdown : Function,
    connectionTimeRandomizer : Function,
    onClose   : Function,
    onError   : Function,
    onMessage : Function,
    onOpen    : Function,
    reconnect : Function,
    retryCount     : number  = 1,
    retryCountdown : number  = 0,
    addJob : Function,
    retryCountdownInterval : number = null;


connect = function() : any {
    if (!connecting) {          // check if we are already connecting
        reconnecting = false;   // we are not reconnecting
        connecting   = true;    // we are connecting
        clearInterval(retryCountdownInterval);  // Remove the timer
        ws = new WebSocket($("body").data("ws-url"));   // create the new websocket
        ws.onopen    = function(evt : Event)        : any { return onOpen(evt); };
        ws.onclose   = function(evt : CloseEvent)   : any { return onClose(evt); };
        ws.onmessage = function(evt : MessageEvent) : any { return onMessage(evt); };
        ws.onerror   = function(evt : ErrorEvent)   : any { return onError(evt); };
        return
    }
};

connectionCountdown = function() : any {
    retryCountdown = retryCountdown - 1;            // count one down
    $("#offline-alert-timer").text(retryCountdown); // render count down text
    if (retryCountdown < 1) {   // we are at 0, retry now.
        retryCountdown = 1;     // reset countdown.
        clearInterval(retryCountdownInterval);  // remove the interval.
        return connect();       // reconnect.
    }
};

connectionTimeRandomizer = function() : any {   // Randomizes the countdown time
    if (retryCount < 38) { retryCount++; }      // maximum of 38 reconnects before constant change
    return parseInt("" + Math.random() * 3 * retryCount + 6);   // 38 * 3s + 6s = 120s = 2min
};

reconnect = function(force : any) : any{    // reconnect the websocket to the server.
    if (force == null) { force = false; }   // force is there to ensure a reconnect
    if ((!reconnecting && !connected && !connecting) || force) {
        console.log("Trying to Reconnect Websocket.");
        ws.close();             //close any remaining connection
        connected = false;      // set the connected status to false
        connecting = false;     // set the connecting status to false
        reconnecting = true;    // set the reconnecting status to true
        retryCountdown = parseInt(connectionTimeRandomizer());  // set the retry countdown
        $("#offline-alert").fadeIn();   // show the "Reconnecting ..." message
        connectionCountdown();  // start the countdown
        retryCountdownInterval = setInterval(connectionCountdown, 1000);
    }
};

onOpen = function(event : Event) : any {
    console.log("Websocket is Connected.");
    clearInterval(retryCountdownInterval);  // Close all reconnection timers
    connected = true;               // We are connected now.
    connecting = false;             // No Longer connecting
    retryCount = 1;                 // Reset the retry counter
    $("#offline-alert").fadeOut();  // Hide the Offline alert
};

onError = function(event : ErrorEvent) : any {
    setTimeout(reconnect(true), 3000);
};

onClose = function(event : CloseEvent) : any {
    connected = false;
    setTimeout(reconnect, 5000);
};

onMessage = function(event : MessageEvent) : any {
    let message : any = JSON.parse(event.data);
    switch (message.type) {
        case "ClearJob":
            m.startComputation();
            JobListComponent.removeJob(message.jobID);
            m.endComputation();
            break;
        case "PushJob":
            m.startComputation();
            JobListComponent.pushJob(JobListComponent.Job(message.job));
            LiveTable.pushJob(message.job);
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
        default:
            break;
    }
};


let sendMessage = function(object : any) : any {
    return ws.send(JSON.stringify(object));
};
addJob = function(jobID : string) : any { sendMessage({ "type": "AddJob", "jobID": jobID }); };

connect();

$("#offline-alert").on('click', function(event) { connect(); });