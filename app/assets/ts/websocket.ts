let wsRoute = jsRoutes.controllers.Application.ws;

class WebsocketWrapper {
    attempts  : number        = 0;
    webSocket : WebSocket     = null;
    messages  : Array<string> = [];

    constructor() {
        this.connect();
    }

    /**
     * Connects the web socket to the server
     * @returns {boolean}
     */
    connect : Function = function() : boolean {
        console.log("[Websocket] Connecting...");
        // Block as long as there is a working websocket
        if (this.webSocket == null || this.webSocket.readyState == WebSocket.CLOSED) {
            // Add one to the attempts
            this.attempts++;
            // check if the protocol is https / wss
            let isSecure: boolean = location.protocol === "https:";
            // create the new websocket
            this.webSocket = new WebSocket(wsRoute().webSocketURL(isSecure));
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
        let self = this;

        // generate a random time to reconnect at (between 2 and 40 seconds)
        let time = WebsocketWrapper.backoffTime(this.attempts, 2000, 40000);
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
            console.log("[Websocket] Sending message:", object);
            this.webSocket.send(JSON.stringify(object));
        } else {
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
        let self = this;
        console.log("[Websocket] Connected successfully.");

        // reset the number of attempts taken
        this.attempts = 0;

        // Hide the Offline alert
        $("#offline-alert").fadeOut();

        // send all messages which have been created while the web socket was not connected
        this.messages.forEach(function(message : string) {
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
        let message : any = JSON.parse(event.data);
        console.log("[Websocket] Received a message:", message);
        switch (message.type) {
            case "ClearJob":
                m.startComputation();
                JobListComponent.removeJob(message.jobID);
                if (message.delete) {JobManager.removeFromTable(message.jobID);}
                else {if(JobManager.table){JobManager.reload();}}
                m.endComputation();
                break;
            case "PushJob":
                //console.log("WSS " + JSON.stringify(message.job));
                JobListComponent.pushJob(JobListComponent.Job(message.job));
                LiveTable.pushJob(message.job);
                JobManager.pushToTable(message.job);
                if(message.job.status == 4 || message.job.status == 5) {
                    notifications += 1;
                    titlenotifier.set(notifications);
                    JobRunningComponent.terminate(message.jobID);
                    console.log("[Websocket] " + message.job.jobID + " has finished with status: " + message.job.status);
                }
                break;
            case "UpdateLoad":
                // Tried to limit this by saving the "currentRoute", but we might need something proper in the future.
                if (currentRoute === "index" && !noRedraw) {
                    LoadBar.updateLoad(message.load);
                }
                break;
            case "Ping":
                ws.send({
                    "type": "Ping"
                });
                break;
            case "WatchLogFile":
                JobRunningComponent.updateLog(message.jobID, message.lines);
                break;
            case "MaintenanceAlert":
                $('.maintenance_alert').show();
                break;
            default:
                break;
        }
    };

    // error event just returns the error for now
    eventError : Function = function(event : ErrorEvent) : any {
        console.error("[Websocket] Error:", event.error);
    };

    /**
     * Shows the reason for a close event and reacts
     * @param {CloseEvent} event
     * @returns {any}
     */
    eventClose : Function = function(event : CloseEvent) : any {
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
    static backoffTime : Function = function(k : number, minBackoff : number, maxBackoff : number) {
        let maxInterval = (Math.pow(1.5, k)) * 1000;
        // If the generated interval is more than 30 seconds, truncate it down to 30 seconds.
        if (maxInterval > maxBackoff) { maxInterval = maxBackoff; }

        // generate the interval to a random number between 0 and the maxInterval determined from above
        return Math.floor(minBackoff + Math.random() * maxInterval);
    };

}

/**
 * Websocket wrapper object
 * @type {WebsocketWrapper}
 */
let ws : WebsocketWrapper = new WebsocketWrapper();

let notifications = 0;
declare var titlenotifier: any;