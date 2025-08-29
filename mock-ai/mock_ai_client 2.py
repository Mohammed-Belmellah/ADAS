# mock_raw_ws.py (hardened)
import os, time, json, uuid, random, datetime, threading, sys
import requests
import websocket  # pip install websocket-client

API_BASE = os.getenv("API_BASE", "http://localhost:8080")
WS_URL   = os.getenv("WS_URL",   "ws://localhost:8080/ws-raw")

KC_URL           = os.getenv("KC_URL", "http://localhost:8081")
KC_REALM         = os.getenv("KC_REALM", "adas")
KC_CLIENT_ID     = os.getenv("KC_CLIENT_ID", "adas-cli")
KC_CLIENT_SECRET = os.getenv("KC_CLIENT_SECRET", "kcIN97Vcm2E5uRNIM0DPwDDsJEC9TooD")  # mettre "" si client public
KC_USERNAME      = os.getenv("KC_USERNAME", "admin1")
KC_PASSWORD      = os.getenv("KC_PASSWORD", "admin")

DRIVER_ID = os.getenv("DRIVER_ID", "b52c7dc2-dc7e-4c55-9d6f-d9b9cd0128bd")
EMOTIONS  = ["happy","neutral","sad","angry","surprised","tired"]

BATCHES   = int(os.getenv("BATCHES", "1000"))
PAUSE_SEC = float(os.getenv("PAUSE_SEC", "2"))

TOKEN     = None

def now_iso():
    return datetime.datetime.now().replace(microsecond=0).isoformat()

def get_token(force=False):
    global TOKEN
    if TOKEN and not force:
        return TOKEN
    token_url = f"{KC_URL}/realms/{KC_REALM}/protocol/openid-connect/token"
    data = {
        "grant_type":"password",
        "client_id":KC_CLIENT_ID,
        "username":KC_USERNAME,
        "password":KC_PASSWORD
    }
    # Si client public, laisse KC_CLIENT_SECRET vide ("")
    if KC_CLIENT_SECRET:
        data["client_secret"] = KC_CLIENT_SECRET
    r = requests.post(token_url, data=data, timeout=10)
    try:
        r.raise_for_status()
    except requests.HTTPError:
        print("Token error:", r.status_code, r.text[:300])
        raise
    TOKEN = r.json()["access_token"]
    return TOKEN

def ah():
    return {"Authorization": f"Bearer {get_token()}","Content-Type":"application/json"}

def create_session():
    r = requests.post(
        f"{API_BASE}/api/sessions",
        headers=ah(),
        json={"driverId": DRIVER_ID, "startTime": now_iso()},
        timeout=10
    )
    print("create_session:", r.status_code, r.text[:200])
    r.raise_for_status()
    return r.json()["id"]

def end_session(session_id):
    hdr = ah()
    try:
        r = requests.put(
            f"{API_BASE}/api/sessions/{session_id}/end-with-report",
            headers=hdr,
            json={"endTime": now_iso()},
            timeout=20  # <-- secondes
        )
        if r.status_code == 401:
            # token expiré ? régénère et retente une fois
            hdr = {"Authorization": f"Bearer {get_token(force=True)}","Content-Type":"application/json"}
            r = requests.put(
                f"{API_BASE}/api/sessions/{session_id}/end-with-report",
                headers=hdr, json={"endTime": now_iso()}, timeout=20
            )
        print("end_session:", r.status_code, r.text[:200])
    except Exception as e:
        print("end_session error:", e)

class StompRaw:
    def __init__(self, url, token):
        self.url = url
        self.token = token
        self.ws = None
        self.connected = False
        self._hb_thread = None
        self._hb_interval_ms = 0
        # client annonce 10s/10s; on négocie avec le serveur à la connexion
        self._advertised_cx_cy = (10000, 10000)  # (client-send, client-recv) en ms

    def _send(self, frame: str):
        self.ws.send(frame)

    def _start_heartbeat(self, server_hb_header: str):
        # server_hb_header = "sx,sy" en ms
        try:
            sx, sy = [int(x) for x in server_hb_header.split(",")]
        except Exception:
            sx, sy = (0, 0)
        cx, cy = self._advertised_cx_cy
        # intervalle d'envoi client = max(cx, sy) (spec STOMP 1.1+)
        interval = max(cx, sy)
        self._hb_interval_ms = interval if interval > 0 else 0

        if self._hb_interval_ms > 0 and (self._hb_thread is None or not self._hb_thread.is_alive()):
            def _hb_loop():
                while self.connected:
                    try:
                        self.ws.send("\n")  # heartbeat STOMP = LF
                    except Exception:
                        break
                    time.sleep(self._hb_interval_ms / 1000.0)
            self._hb_thread = threading.Thread(target=_hb_loop, daemon=True)
            self._hb_thread.start()

    def connect(self):
        self.ws = websocket.WebSocketApp(
            self.url,
            header=[f"Authorization: Bearer {self.token}"],
            on_open=self.on_open,
            on_message=self.on_message,
            on_error=self.on_error,
            on_close=self.on_close,
            subprotocols=["v12.stomp", "v11.stomp", "v10.stomp"]
        )
        t = threading.Thread(
            target=self.ws.run_forever,
            kwargs={"ping_interval": 20, "ping_timeout": 10},  # WS ping (différent des heartbeats STOMP)
            daemon=True
        )
        t.start()
        for _ in range(50):
            if self.connected:
                break
            time.sleep(0.1)
        return self.connected

    def on_open(self, ws):
        print("WS opened → STOMP CONNECT (raw)")
        cx, cy = self._advertised_cx_cy
        frame = (
            "CONNECT\n"
            "accept-version:1.0,1.1,2.0\n"
            "host:/\n"
            f"heart-beat:{cx},{cy}\n"
            f"Authorization: Bearer {self.token}\n"
            "\n\x00"
        )
        self._send(frame)

    def on_message(self, ws, msg):
        m = msg.replace("\r\n", "\n")
        if m.startswith("CONNECTED"):
            # parse headers
            headers = {}
            for line in m.split("\n")[1:]:
                if not line or line == "\x00":
                    break
                if ":" in line:
                    k, v = line.split(":", 1)
                    headers[k.strip()] = v.strip()
            print("STOMP CONNECTED")
            self.connected = True
            # démarre les heartbeats selon la négo
            hb = headers.get("heart-beat", "0,0")
            self._start_heartbeat(hb)

        elif m.startswith("MESSAGE"):
            head, body = m.split("\n\n", 1)
            body = body.rstrip("\x00")
            dest = ""
            for line in head.split("\n")[1:]:
                if line.startswith("destination:"):
                    dest = line.split(":", 1)[1]
                    break
            print(f"[MESSAGE] {dest} → {body[:150]}...")

        elif m.startswith("RECEIPT"):
            rid = ""
            for line in m.split("\n"):
                if line.startswith("receipt-id:"):
                    rid = line.split(":", 1)[1]
                    break
            print(f"[RECEIPT] {rid}")

        elif m.startswith("ERROR"):
            print("STOMP ERROR:", m[:400])
            # ferme proprement côté client
            try:
                self.ws.close()
            finally:
                self.connected = False

    def on_error(self, ws, error):
        print("WS error:", error)

    def on_close(self, ws, code, reason):
        print("WS closed:", code, reason)
        self.connected = False

    def subscribe(self, destination, sid=None):
        sid = sid or str(uuid.uuid4())
        frame = f"SUBSCRIBE\ndestination:{destination}\nid:{sid}\nack:auto\n\n\x00"
        self._send(frame)
        print("SUBSCRIBED", destination)

    def send_batch(self, session_id, n=3):
        if not self.connected:
            print("Not connected; drop batch.")
            return
        records = [{
            "sessionId": session_id,
            "emotions": random.sample(EMOTIONS, 3),
            "confidences": [round(random.random(), 2) for _ in range(3)],
            "detectedAt": now_iso()
        } for _ in range(n)]
        body = json.dumps({"sessionId": session_id, "records": records}, ensure_ascii=False)
        # Frame STOMP simplifiée : pas de content-length, pas de receipt (délimitation par \x00)
        frame = (
            "SEND\n"
            "destination:/app/emotions/batch\n"
            "content-type:application/json;charset=utf-8\n"
            "\n" + body + "\x00"
        )
        try:
            self._send(frame)
            print(f"SENT batch ({n}) → /app/emotions/batch")
        except Exception as e:
            print("SEND failed:", e)

    def disconnect(self):
        try:
            if self.connected:
                self._send("DISCONNECT\n\n\x00")
                time.sleep(0.1)
        finally:
            if self.ws:
                self.ws.close()


def main():
    token = get_token()
    session_id = create_session()
    print("Session:", session_id)

    c = StompRaw(WS_URL, token)
    if not c.connect():
        print("STOMP connect failed (check /ws-raw endpoint & interceptor)")
        sys.exit(2)

    c.subscribe(f"/topic/emotions.session.{session_id}")
    c.subscribe(f"/topic/alerts.session.{session_id}")

    try:
        for _ in range(BATCHES):
            if not c.connected:
                print("Disconnected; stopping producer loop.")
                break
            c.send_batch(session_id, n=random.randint(1,3))
            time.sleep(PAUSE_SEC)
    finally:
        c.disconnect()
        end_session(session_id)
        print("Done.")


if __name__ == "__main__":
    main()
