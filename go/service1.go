// go/service1.go
// A simple net/http server that accepts POST /analyze and returns JSON analysis.
// Run: go run service1.go

package main

import (
    "encoding/json"
    "log"
    "net/http"
)

type AnalyzeReq struct {
    Token string `json:"token,omitempty"`
    User  string `json:"user,omitempty"`
    Text  string `json:"text,omitempty"`
    Meta  map[string]interface{} `json:"meta,omitempty"`
}

func analyzeHandler(w http.ResponseWriter, r *http.Request) {
    var req AnalyzeReq
    if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
        http.Error(w, "bad request", http.StatusBadRequest)
        return
    }
    resp := map[string]interface{}{
        "summary":      "analysis-for-" + req.User,
        "confidence":   0.93,
        "token_preview": func() string { if req.Token=="" {return "none"}; return req.Token[:6] }(),
    }
    if req.Text != "" {
        resp["text_len"] = len(req.Text)
    }
    w.Header().Set("Content-Type", "application/json")
    json.NewEncoder(w).Encode(resp)
}

func main() {
    http.HandleFunc("/analyze", analyzeHandler)
    log.Println("Go AnalyzeService listening on :8003")
    log.Fatal(http.ListenAndServe(":8003", nil))
}
