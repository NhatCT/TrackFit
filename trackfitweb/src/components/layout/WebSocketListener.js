import { useEffect } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import cookie from "react-cookies";

const WebSocketListener = ({ user }) => {
  useEffect(() => {
    if (!user) return;
    const token = cookie.load("token");
    if (!token) return;

    const wsUrl = process.env.REACT_APP_WS_URL || "http://localhost:8080/TrackFit/ws";
    const socket = new SockJS(wsUrl);
    const client = new Client({
      webSocketFactory: () => socket,
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

    client.activate();

    return () => {
      console.log("[WebSocket] Deactivating client...");
      client.deactivate();
    };
  }, [user]);

  return null;
};

export default WebSocketListener;
