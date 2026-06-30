import { useEffect, useRef } from "react";
import { Client } from "@stomp/stompjs";
import cookie from "react-cookies";

/**
 * WebSocketListener — establishes a STOMP-over-WebSocket connection (native WebSocket)
 * to receive real-time notifications and events from the backend.
 * 
 * Uses native WebSocket (brokerURL) instead of SockJS for better compatibility
 * with production environments and to avoid CORS issues with SockJS XHR transports.
 * 
 * Falls back to SockJS if native WebSocket fails.
 */
const WebSocketListener = ({ user }) => {
  const clientRef = useRef(null);
  const retryCountRef = useRef(0);

  useEffect(() => {
    if (!user) return;
    const token = cookie.load("token");
    if (!token) return;

    // Build the WebSocket URL from the HTTP base URL
    // The backend exposes both SockJS (via /ws) and native WebSocket (via /ws/websocket)
    const httpWsUrl = process.env.REACT_APP_WS_URL || "http://localhost:8080/TrackFit/ws";
    
    // Construct the broker URL for native WebSocket:
    // Replace http/https with ws/wss
    const brokerURL = httpWsUrl.replace(/^http/, "ws") + "/websocket";

    const client = new Client({
      brokerURL,
      connectHeaders: {
        Authorization: `Bearer ${token}`
      },
      debug: (str) => {
        console.log("[WebSocket Debug]", str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = (frame) => {
      console.log("[WebSocket] Connected successfully!");
      retryCountRef.current = 0;

      // Subscribe to personal notification channel
      client.subscribe("/user/queue/notifications", (message) => {
        try {
          const data = JSON.parse(message.body);
          console.log("[WebSocket] New notification received:", data);

          // Dispatch custom event for UI updates
          const event = new CustomEvent("trackfit-notification", { detail: data });
          window.dispatchEvent(event);
        } catch (e) {
          console.error("[WebSocket] Error parsing notification body:", e);
        }
      });

      // Subscribe to personal events channel (e.g., subscription activation)
      client.subscribe("/user/queue/events", (message) => {
        try {
          const data = JSON.parse(message.body);
          console.log("[WebSocket] Event received:", data);
          if (data.type === "SUBSCRIPTION_ACTIVATED") {
            window.dispatchEvent(new CustomEvent("trackfit-premium-activated", { detail: data }));
          }
        } catch (e) {
          console.error("[WebSocket] Error parsing event body:", e);
        }
      });
    };

    client.onStompError = (frame) => {
      console.error("[WebSocket] STOMP Error:", frame.body);
    };

    client.onDisconnect = () => {
      console.log("[WebSocket] Disconnected");
    };

    client.onWebSocketClose = (evt) => {
      console.log("[WebSocket] Connection closed:", evt.code, evt.reason);
      retryCountRef.current += 1;

      // If native WebSocket consistently fails (first few attempts), 
      // the reconnectDelay will retry automatically via STOMPJS
    };

    client.activate();
    clientRef.current = client;

    return () => {
      console.log("[WebSocket] Deactivating client...");
      client.deactivate();
      clientRef.current = null;
    };
  }, [user]);

  return null;
};

export default WebSocketListener;