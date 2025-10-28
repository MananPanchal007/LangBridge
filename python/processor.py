# python/processor.py
# Flask app which accepts a doc, performs a simple transformation,
# and calls the Go AnalyzeService to enrich the document.

from flask import Flask, request, jsonify
import requests

app = Flask(__name__)

GO_ANALYZE = "http://localhost:8003/analyze"

@app.route("/process", methods=["POST"])
def process():
    data = request.get_json() or {}
    # Minimal transformation: add processed_at
    data['processed_at'] = "processor:" + __import__('time').ctime()
    # Call Go service to enrich
    try:
        r = requests.post(GO_ANALYZE, json={"text": data.get("analysis_text",""), "meta": data.get("meta",{})}, timeout=3)
        data['go_enrichment'] = r.json()
    except Exception as e:
        data['go_enrichment'] = {"error": str(e)}
    return jsonify(data)

if __name__ == "__main__":
    app.run(port=8006)
