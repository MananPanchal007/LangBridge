# LangBridge Architecture Diagram

## Service Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                               SERVICES (Backend)                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────┐     ┌──────────────────┐    ┌─────────────────┐        │
│  │  Python Service │     │   Go Service     │    │  Java Server    │        │
│  │  authService.py │     │  service1.go     │    │  JavaServer.java│        │
│  │  Port: 8001     │     │  Port: 8003      │    │  Port: 8005     │        │
│  └────────┬────────┘     └────────┬──────── ┘    └────────┬────────┘        │
│           │                       │                       │                 │
│           │ GET /auth             │ POST /analyze         │ POST /javaprocess│
│           │                       │                       │                 │
│           ▼                       ▼                       ▼                 │
│    Returns Token         Returns Analysis         Returns Doc ID            │
│                                                                             │
│  ┌──────────────────────────────────────────────────────────────────┐       │
│  │  Python Service - processor.py                                    │      │
│  │  Port: 8006                                                        │     │
│  │  Endpoint: POST /process                                           │     │
│  │  ┌────────────────────────────────────────────────────────────┐   │      │
│  │  │ 1. Receives doc + analysis_text                            │   │      │
│  │  │ 2. Adds processed_at timestamp                              │   │     │
│  │  │ 3. Calls Go service (8003) for enrichment                   │   │     │
│  │  │ 4. Returns enriched document                                │   │     │
│  │  └────────────────────────────────────────────────────────────┘   │      │
│  └──────────────────────────────────────────────────────────────────┘       │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                            CLIENTS (Frontend)                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌────────────────────────────────────────────────────────────┐           │
│  │ Java Client (UserClient.java)                               │           │
│  └────────────────────────────────────────────────────────────┘           │
│                                                                             │
│     Step 1: GET /auth?user=manan                ┌─────┐                     │
│        │                                       │ auth│                     │
│        │───────────────────────────────────────►│srv  │                     │
│        │                                       │8001 │                     │
│        │                                       └──┬──┘                     │
│        │◄─────────────────────────────────────────│                        │
│        │      Returns token                       │                        │
│                                                                             │
│     Step 2: POST /analyze with token             ┌─────┐                   │
│        │                                         │ go  │                   │
│        │────────────────────────────────────────►│srv  │                   │
│        │                                         │8003 │                   │
│        │                                         └──┬──┘                   │
│        │◄──────────────────────────────────────────│                      │
│        │      Returns analysis JSON                │                      │
│                                                                             │
│     Step 3: POST /javaprocess with full data       ┌────┐                 │
│        │                                           │java│                 │
│        │──────────────────────────────────────────►│srv │                 │
│        │                                           │8005│                 │
│        │                                           └──┬─┘                 │
│        │◄────────────────────────────────────────────│                   │
│        │      Returns doc ID                         │                   │
│                                                                             │
│  ┌────────────────────────────────────────────────────────────┐           │
│  │ C++ Client 1 (cpp_client1.cpp)                             │           │
│  └────────────────────────────────────────────────────────────┘           │
│                                                                             │
│     Step 1: GET /auth?user=cpp_client          ┌─────┐                    │
│        │                                       │auth │                    │
│        │──────────────────────────────────────►│srv  │                    │
│        │                                       │8001 │                    │
│        │                                       └──┬──┘                    │
│        │◄────────────────────────────────────────│                       │
│        │      Returns token                       │                       │
│                                                                             │
│     Step 2: POST /javaprocess with token         ┌────┐                  │
│        │                                         │java│                  │
│        │────────────────────────────────────────►│srv │                  │
│        │                                         │8005│                  │
│        │                                         └──┬─┘                  │
│        │◄──────────────────────────────────────────│                    │
│        │      Returns response                     │                     │
│                                                                             │
│  ┌────────────────────────────────────────────────────────────┐           │
│  │ C++ Client 2 (cpp_client2.cpp)                             │           │
│  └────────────────────────────────────────────────────────────┘           │
│                                                                             │
│        ┌─────┐                                                            │
│        │ proc│                                                            │
│        │ srv │                                                            │
│        │8006 │                                                            │
│        └──┬──┘                                                            │
│           │◄──────────────────────────────┐                               │
│           │                                │                               │
│  POST /process                             │                               │
│  {"doc":"hello",                          │                               │
│   "analysis_text":"check"}                │                               │
│                                            │                               │
│           │                                │                               │
│           │───► Calls Go service 8003 ─────┘                               │
│           │                                │                               │
│           │◄────────────────────────────────                               │
│           │      Returns enriched doc       │                               │
│                                                                             │
│  ┌────────────────────────────────────────────────────────────┐           │
│  │ Go Client (service2.go)                                    │           │
│  └────────────────────────────────────────────────────────────┘           │
│                                                                             │
│        POST /javaprocess                   ┌────┐                         │
│  {"from":"go_client",                      │java│                        │
│   "note":"please save doc"}                │srv │                        │
│        │───────────────────────────────────►│8005│                        │
│        │                                    └──┬─┘                        │
│        │◄──────────────────────────────────────│                         │
│        │      Prints response                  │                         │
│                                                                             │
│  ┌────────────────────────────────────────────────────────────┐           │
│  │ Assembly Scripts (call_curl1.asm, call_curl2.asm)         │           │
│  └────────────────────────────────────────────────────────────┘           │
│                                                                             │
│  call_curl1.asm:                           ┌─────┐                       │
│  Calls: curl "http://localhost:8001/       │auth │                       │
│         auth?user=asm1"                    │srv  │                       │
│        │──────────────────────────────────►│8001 │                       │
│        │                                   └─────┘                       │
│                                                                             │
│  call_curl2.asm:                            ┌────┐                        │
│  Calls: curl -X POST                       │java│                        │
│         http://localhost:8005/javaprocess   │srv │                        │
│        │───────────────────────────────────►│8005│                        │
│        │                                    └──┬─┘                        │
│        │◄──────────────────────────────────────│                         │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Complete Data Flow Example

```
┌──────────────────────────────────────────────────────────────────────┐
│                     Complete End-to-End Flow                         │
│              (UserClient.java Sequence)                              │
└──────────────────────────────────────────────────────────────────────┘

    UserClient
       │
       ├───► [1] GET http://localhost:8001/auth?user=manan
       │         │
       │         ▼
       │    authService.py generates token using:
       │    hash = SHA256("manan:timestamp_minute")[:20]
       │         │
       │         ▼
       │    Returns: "a3f9d8e1c2b4..."
       │
       ├───► [2] POST http://localhost:8003/analyze
       │         Body: {"token":"a3f9d8...", "user":"manan"}
       │         │
       │         ▼
       │    service1.go receives request
       │    Extracts token, user, text, meta
       │         │
       │         ▼
       │    Returns: {
       │      "summary": "analysis-for-manan",
       │      "confidence": 0.93,
       │      "token_preview": "a3f9d8"
       │    }
       │
       ├───► [3] POST http://localhost:8005/javaprocess
       │         Body: {"user":"manan", "analysis":{...}}
       │         │
       │         ▼
       │    JavaServer.java receives POST
       │    Creates docId = "DOC-timestamp"
       │         │
       │         ▼
       │    Returns: {"status":"ok", "docId":"DOC-1234567890"}
       │
       └───► Final Response Printed to Console
```

## Port Summary

| Service | Port | Language | Endpoint | Description |
|---------|------|----------|----------|-------------|
| authService.py | 8001 | Python | GET /auth | Authentication service |
| service1.go | 8003 | Go | POST /analyze | Analysis service |
| JavaServer.java | 8005 | Java | POST /javaprocess | Document processing |
| processor.py | 8006 | Python | POST /process | Document transformer |

## Client Summary

| Client | Language | Calls |
|--------|----------|-------|
| UserClient.java | Java | Complete 3-step workflow |
| cpp_client1.cpp | C++ | Gets token → posts to Java |
| cpp_client2.cpp | C++ | Posts to Python processor |
| service2.go | Go | Posts to Java server |
| call_curl1.asm | Assembly | Curls auth service |
| call_curl2.asm | Assembly | Curls Java server |

