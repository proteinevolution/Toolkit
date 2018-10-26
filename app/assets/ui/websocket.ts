class WebsocketWrapper {
    attempts  : number        = 0;      // current number of attempts for the reconnect
    webSocket : WebSocket     = null;   // current websocket
    messages  : Array<string> = [];     // messages which have not been sent due to a disconnect
    timedOut  : boolean       = false;  // flag which is reset whenever a message is received
    interval  : number        = null;   // interval id for the timeout check function

    // Config
    static showConsoleMessages    : boolean = true;       // will show web socket console messages
    static reconnectTimeMin       : number  =  2000; //ms // minimum time in between reconnects
    static reconnectTimeMax       : number  = 40000; //ms // maximum time in between reconnects
    static connectionCheckTimeout : number  = 20000; //ms // time between each timeout check

    constructor() {
        const self = this;
        // Start the connection process
        this.connect();
        // set the timer for connection checks
        this.interval =
            setInterval(function() { self.checkTimeout() }, WebsocketWrapper.connectionCheckTimeout);
    }

    /**
     * Connects the web socket to the server
     * @returns {boolean}
     */
    connect : Function = function() : boolean {
        if (WebsocketWrapper.showConsoleMessages)
            console.log("[Websocket] Connecting...");
        // Block as long as there is a working websocket
        if (this.webSocket == null || this.webSocket.readyState == WebSocket.CLOSED) {
            // Add one to the attempts
            this.attempts++;
            // check if the protocol is https / wss
            const isSecure: boolean = location.protocol === "https:";
            const route = isSecure ? "wss://"+ location.host + "/ws/" : "ws://" + location.host + "/ws/";
            // create the new websocket
            this.webSocket = new WebSocket(route);
            // remember self for later
            let self = this;
            this.webSocket.onopen    = function (evt: Event)       : any { return self.eventOpen(evt);    };
            this.webSocket.onclose   = function (evt: CloseEvent)  : any { return self.eventClose(evt);   };
            this.webSocket.onmessage = function (evt: MessageEvent): any { return self.eventMessage(evt); };
            this.webSocket.onerror   = function (evt: ErrorEvent)  : any { return self.eventError(evt);   };
            return true;
        } else {
            return false;
        }
    };

    /**
     * Reconnects the web socket
     * @returns {any}
     */
    reconnect : Function = function() : any {
        // remember self for later
        const self = this;

        // generate a random time to reconnect at (between 2 and 40 seconds)
        let time = WebsocketWrapper.backoffTime(
                this.attempts,
                WebsocketWrapper.reconnectTimeMin,
                WebsocketWrapper.reconnectTimeMax
            );
        if (WebsocketWrapper.showConsoleMessages)
            console.log("[Websocket] Trying to reconnect in "+time+"ms. Attempt: " + self.attempts);

        // close any existing web sockets
        if (this.webSocket != null) this.webSocket.close();

        // set the timer to reconnect
        setTimeout(function() {
            if (!self.connect()) {
                // web socket was not closed, try again
                self.reconnect();
            }
        }, time);
    };

    /**
     * Send method will send a message to the server as long as the web socket is connected
     * otherwise it will keep it in the backlog to send after the connection stands again
     * @param {Object} object
     * @returns {any}
     */
    send : Function = function(object : Object) : any {
        if (this.webSocket.readyState == WebSocket.OPEN) {
            if (WebsocketWrapper.showConsoleMessages)
                console.log("[Websocket] Sending message:", object);
            this.webSocket.send(JSON.stringify(object));
        } else {
            if (WebsocketWrapper.showConsoleMessages)
                console.log("[Websocket] Storing message for sending after the connection has been rebuilt:", object);
            this.messages.push(JSON.stringify(object));
        }
    };


    /**
     * Event functions
     */
    /**
     * takes the opening event from the web socket and does the initialisation works
     * @param {Event} event
     * @returns {any}
     */
    eventOpen : Function = function(event : Event) : any {
        const self = this;
        if (WebsocketWrapper.showConsoleMessages)
            console.log("[Websocket] Connected successfully.");

        // reset the number of attempts taken
        this.attempts = 0;

        // Hide the Offline alert
        $("#offline-alert").fadeOut();

        // send all messages which have been created while the web socket was not connected
        this.messages.forEach(function(message : string) {
            if (WebsocketWrapper.showConsoleMessages)
                console.log("[Websocket] Sending delayed message:", message);
            self.webSocket.send(message);
        });

        this.messages = [];
    };

    /**
     * takes the message from the websocket and relays it to the correct part of the page
     * @param {MessageEvent} event
     * @returns {any}
     */
    eventMessage : Function = function(event : MessageEvent) : any {
        // Parse the message
        const message : any = JSON.parse(event.data);

        // log it to the console
        if (WebsocketWrapper.showConsoleMessages)
            console.log("[Websocket] Received a message:", message);

        // Got a message, so we are not timed out
        this.timedOut = false;

        // Give the message to the corresponding part of the script
        switch (message.type) {

            // The message is a request to clear a job from the users view
            case "ClearJob":
                m.startComputation();
                JobListComponent.removeJob(message.jobID);
                if (message.delete) {JobManager.removeFromTable(message.jobID);}
                else {if(JobManager.table){JobManager.reload();}}
                m.endComputation();
                break;

            // The message is a request to add or modify a job in the users view
            case "PushJob":
                //console.log("WSS " + JSON.stringify(message.job));
                JobListComponent.pushJob(JobListComponent.Job(message.job));
                LiveTable.pushJob(message.job);
                JobManager.pushToTable(message.job);
                if(message.job.status == 4 || message.job.status == 5) {
                    JobRunningComponent.terminate(message.jobID);
                    if (WebsocketWrapper.showConsoleMessages)
                        console.log("[Websocket] " + message.job.jobID + " has finished with status: " + message.job.status);
                }
                break;

            // Show browser notification
            case "ShowNotification":
                switch (message.notificationType) {
                    case "job_update": // means that tag = jobid -> show job update if page open
                        if (Toolkit.trackedJobIDs.indexOf(message.tag) !== -1 || Toolkit.currentJobID === message.tag) { // s this tab tracking the job or is the job visible
                            if (WebsocketWrapper.showConsoleMessages) {
                                console.log("[Websocket] Notification about tracked job", message.tag);
                            }
                            TitleManager.setAlert();
                            NotificationManager.showJobNotification(message.tag, message.title, message.body);
                        }
                        break;
                    default:
                        TitleManager.setAlert();
                        NotificationManager.showNotification(message.tag, message.title, message.body);
                        break;
                }
                break;

            // The message is a update to the current load display
            case "UpdateLoad":
                // Tried to limit redraw reqyests by this by saving the "currentRoute",
                // but we might need something proper in the future.
                if (currentRoute === "index" && !noRedraw) {
                    LoadBar.updateLoad(message.load);
                }
                break;

            // The server is pinging this client - send an answer
            case "Ping":
                this.send(WebsocketWrapper.ping(message.date));
                break;

            // The server replied to a ping request
            case "Pong":
                if (WebsocketWrapper.showConsoleMessages)
                    console.log("Ping: ", (Date.now() - message.date), "ms");
                break;

            // Update the log file of a running job which the user has
            case "WatchLogFile":
                JobRunningComponent.updateLog(message.jobID, message.lines);
                break;

            // Maintenance is going on
            case "MaintenanceAlert":
                $('.maintenance-alert').show();
                TitleManager.setAlert();
                NotificationManager.showNotification("maintenance", "Maintenance", "The Toolkit is going down for maintenance. Thank you for understanding!");
                break;

            // The message was not what we expected
            default:
                break;
        }
    };

    // error event just returns the error for now
    eventError : Function = function(event : ErrorEvent) : any {
        if (event.message) console.error("[Websocket] Error:", event.message);
    };

    /**
     * Shows the reason for a close event and reacts
     * @param {CloseEvent} event
     * @returns {any}
     */
    eventClose : Function = function(event : CloseEvent) : any {
        if (WebsocketWrapper.showConsoleMessages)
            console.log("[Websocket] Web socket closed: ", event.reason, event.code);
        $("#offline-alert").fadeIn(); // show the "Reconnecting ..." message
        this.reconnect();
    };

    /**
     * misc helper functions
     */

    /**
     * exponential backoff algorithm generates a random number between min and max backoff.
     * @param {number} k           iteration
     * @param {number} minBackoff  minimum time to the next connect
     * @param {number} maxBackoff  maximum time to the next connect
     * @returns {number} backoff time
     */
    static backoffTime : Function = function(k : number, minBackoff : number, maxBackoff : number) : number {
        let maxInterval = (Math.pow(1.5, k)) * 1000;
        // If the generated interval is more than max backoff seconds, truncate it down to 30 seconds.
        if (maxInterval > maxBackoff) { maxInterval = maxBackoff; }

        // generate the interval to a random number between min and the current max backoff determined above
        return Math.floor(minBackoff + Math.random() * (maxInterval - minBackoff));
    };

    /**
     * if there was no change in the timedOut flag since the last timeout check,
     * then send a ping message to see how long the delay is.
     */
    checkTimeout : Function = function () : void {
        if (this.timedOut) {
            if (WebsocketWrapper.showConsoleMessages)
                console.log("[Websocket] Sending ping request due to timeout");
            this.send(WebsocketWrapper.ping());
        } else {
            this.timedOut = true;
        }
    };

    /**
     * Generates a ping request / answer object
     * @param {number} msTime currentTime as sent from the server
     * @returns {Object}
     */
    static ping : Function = function (msTime? : number) : object {
        return {
            "type": msTime ? "Pong" : "Ping",
            "date": msTime ? msTime : Date.now()
        };
    };
}

/**
 * Websocket wrapper object
 * @type {WebsocketWrapper}
 */
const ws : WebsocketWrapper = new WebsocketWrapper();
