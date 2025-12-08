import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

const stompClient = new Client({
  webSocketFactory: () => new SockJS("http://localhost:8080/ws"),
  debug: (str) => console.log(str),
  reconnectDelay: 5000,
});

stompClient.activate();

export default stompClient;
