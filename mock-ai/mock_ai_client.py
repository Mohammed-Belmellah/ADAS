import websocket
import datetime, json, random, time
import requests
import threading
import uuid

BACKEND_URL = "http://localhost:8080"
DRIVER_ID = "149ba15d-904a-4b74-9020-26b85622ce0e"

def create_session():
    now = datetime.datetime.now().replace(microsecond=0).isoformat()
    payload = {"driverId": DRIVER_ID, "startTime": now}
    r = requests.post(f"{BACKEND_URL}/api/sessions", json=payload)
    print("HTTP", r.status_code, r.text)
    if r.status_code in (200, 201):
        return r.json()["id"]
    return None

def generate_emotion_batch(session_id):
    emotions = ["happy", "neutral", "sad", "angry", "surprised" , "tired"]
    batch = {
        "sessionId": session_id,
        "records": [
            {
                "sessionId": session_id,
                "emotions": random.sample(emotions, 3),
                "confidences": [round(random.random(), 2) for _ in range(3)],
                "detectedAt": datetime.datetime.now().replace(microsecond=0).isoformat()
            }
        ]
    }
    return json.dumps(batch)

def end_session(session_id):
        payload = {"endTime": datetime.datetime.now().replace(microsecond=0).isoformat()}
        r = requests.put(f"{BACKEND_URL}/api/sessions/{session_id}/end-with-report",json=payload)
        print("HTTP", r.status_code, r.text)
        if r.status_code in (200, 201):
            print("Session clôturée !")
        else:
            print("Erreur lors de la clôture de session") 

class STOMPWebSocketClient:
    def __init__(self):
        self.ws = None
        self.connected = False
        self.subscriptions = {}
        
    def on_message(self, ws, message):
        print(f"Raw WebSocket message: {message}")
        
        # Parse STOMP frame
        if message.startswith("CONNECTED"):
            print("STOMP connection established!")
            self.connected = True
        elif message.startswith("MESSAGE"):
            print(f"Received STOMP message: {message}")
        elif message.startswith("ERROR"):
            print(f"STOMP error: {message}")
        
    def on_error(self, ws, error):
        print(f"WebSocket error: {error}")
        
    def on_close(self, ws, close_status_code, close_msg):
        print("WebSocket connection closed")
        self.connected = False
        
    def on_open(self, ws):
        print("WebSocket connection opened, sending STOMP CONNECT...")
        # Send STOMP CONNECT frame
        connect_frame = "CONNECT\naccept-version:1.0,1.1,2.0\nhost:/\n\n\x00"
        ws.send(connect_frame)
        
    def connect(self):
        # Connect to WebSocket endpoint with SockJS transport
        self.ws = websocket.WebSocketApp(
            "ws://localhost:8080/ws/websocket",  # Direct WebSocket, not SockJS polling
            on_message=self.on_message,
            on_error=self.on_error,
            on_close=self.on_close,
            on_open=self.on_open
        )
        
        # Start WebSocket in a separate thread
        self.ws_thread = threading.Thread(target=self.ws.run_forever)
        self.ws_thread.daemon = True
        self.ws_thread.start()
        
        # Wait for connection to be established
        for i in range(50):  # Wait up to 5 seconds
            if self.connected:
                break
            time.sleep(0.1)
        
        return self.connected
        
    def subscribe(self, destination, subscription_id=None):
        if not subscription_id:
            subscription_id = str(uuid.uuid4())
            
        subscribe_frame = f"SUBSCRIBE\ndestination:{destination}\nid:{subscription_id}\n\n\x00"
        if self.ws and self.ws.sock:
            self.ws.send(subscribe_frame)
            self.subscriptions[subscription_id] = destination
            print(f"Subscribed to {destination} with id {subscription_id}")
        
    def send_message(self, destination, body):
        if not self.connected:
            print("Not connected to STOMP server!")
            return False
            
        if self.ws and self.ws.sock:
            # Create STOMP SEND frame
            receipt_id = str(uuid.uuid4())
            frame = f"SEND\ndestination:{destination}\ncontent-type:application/json\ncontent-length:{len(body)}\nreceipt:{receipt_id}\n\n{body}\x00"
            self.ws.send(frame)
            print(f"Sent STOMP message to {destination}")
            return True
        return False
        
    def disconnect(self):
        if self.ws and self.connected:
            disconnect_frame = "DISCONNECT\n\n\x00"
            self.ws.send(disconnect_frame)
            time.sleep(0.1)
        if self.ws:
            self.ws.close()

          

def main():
    session_id = create_session()
    if not session_id:
        print("Impossible de créer la session")
        return

    print(f"Session créée avec ID: {session_id}")
    
    # Create WebSocket STOMP client
    client = STOMPWebSocketClient()
    
    if not client.connect():
        print("Failed to connect to WebSocket server")
        return
    
    print("Connected to WebSocket server successfully!")
    
    # Subscribe to the response topic
    client.subscribe("/topic/emotions")
    
    
    try:
        for i in range(20):
            payload = generate_emotion_batch(session_id)
            success = client.send_message("/app/emotions/batch", payload)
            
            if success:
                print(f"Batch {i+1} envoyé: {payload}")
            else:
                print(f"Échec de l'envoi du batch {i+1}")
                
            time.sleep(3)
            
    except KeyboardInterrupt:
        print("Interruption par l'utilisateur")
    finally:
        client.disconnect()
        end_session(session_id)
        print("Disconnected from server")

if __name__ == "__main__":
    main()