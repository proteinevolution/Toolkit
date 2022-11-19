import { computed, Ref } from 'vue';
import { useWebSocket, WebSocketStatus } from '@vueuse/core';
import Logger from 'js-logger';

const logger = Logger.get('Websocket');

// We want to have one centralized Websocket connection. As such, we cannot call useWebSocket() multiple times.
const isSecure: boolean = location.protocol === 'https:';
const websocketUrl: string = isSecure ? 'wss://' + location.host + '/ws/' : 'ws://' + location.host + '/ws/';
const {
    data: textData,
    status,
    send,
} = useWebSocket(websocketUrl, {
    autoReconnect: {
        retries: 5,
        delay: 2000,
        onFailed: () => {
            logger.error('Failed to reconnect websocket. Will not try again.');
        },
    },
    onError: (ws, event) => {
        logger.error('Websocket error', event);
    },
    onMessage: (ws, event) => {
        logger.debug('Receiving', event.data);
    },
});

/**
 * A wrapper around the useWebSocket composable that initializes the websocket with toolkit parameters.
 */
export default function useToolkitWebsocket<T = any>(): {
    data: Ref<T>;
    status: Ref<WebSocketStatus>;
    send: (data: object) => boolean;
} {
    const data = computed(() => JSON.parse(textData.value));
    const sendWrapper = (data: object) => {
        logger.debug('Sending', data);
        return send(JSON.stringify(data));
    };
    return { data, status, send: sendWrapper };
}
