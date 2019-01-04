import Logger from 'js-logger';

Logger.useDefaults({
    defaultLevel: process.env.NODE_ENV === 'development' ? Logger.DEBUG : Logger.WARN,
    formatter(messages) {
        // prefix each log message with a timestamp.
        messages.unshift(new Date().toUTCString());
    },
});

const consoleHandler = Logger.createDefaultHandler();
Logger.setHandler((messages, context) => {
    consoleHandler(messages, context);
    // myHandler(messages, context); TODO implement custom handler
});
