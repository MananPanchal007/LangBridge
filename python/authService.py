# python/auth_service.py
# Flask app that returns a simple token for a user.
# GET /auth?user=<name>

from flask import Flask, request, jsonify
import time, hashlib

app = Flask(__name__)

@app.route("/auth")
def auth():
    user = request.args.get("user","anonymous")
    # simple deterministic token:
    token = hashlib.sha256(f"{user}:{int(time.time()/60)}".encode()).hexdigest()[:20]
    return token

if __name__ == "__main__":
    app.run(port=8001)
