import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

let stompClient = null

const wsPath = import.meta.env.VITE_WS_PATH || '/ws'
const reconnectDelayMs = Number(import.meta.env.VITE_WS_RECONNECT_DELAY_MS)

export const connectWebSocket = (onMessageCallback, userId) =>
  new Promise((resolve, reject) => {
    const socketFactory = () => new SockJS(wsPath)

    const client = new Client({
      webSocketFactory: socketFactory,
      reconnectDelay: Number.isFinite(reconnectDelayMs) && reconnectDelayMs > 0 ? reconnectDelayMs : 5000,
      onConnect: () => {
        const handler = (message) => {
          if (onMessageCallback) {
            const body = JSON.parse(message.body)
            onMessageCallback(body)
          }
        }

        // 全局广播
        client.subscribe('/topic/alerts', handler)

        // 指定用户单播：/topic/alerts.user.{userId}
        if (userId) {
          client.subscribe(`/topic/alerts.user.${userId}`, handler)
        }

        resolve()
      },
      onStompError: (frame) => {
        console.error('STOMP error', frame)
      },
      onWebSocketError: (event) => {
        console.error('WebSocket error', event)
        reject(event)
      },
    })

    client.activate()
    stompClient = client
  })

export const disconnectWebSocket = () => {
  if (stompClient) {
    stompClient.deactivate()
    stompClient = null
  }
}

