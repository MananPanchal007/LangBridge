# LangBridge System Flowchart

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      MICROSERVICES                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  📋 AUTH SERVICE              📊 ANALYZE SERVICE           │
│  ┌──────────────┐            ┌──────────────┐               │
│  │ Python Flask │            │ Go HTTP      │               │
│  │ Port: 8001   │            │ Port: 8003   │               │
│  │              │            │              │               │
│  │ GET /auth    │            │ POST /analyze│               │
│  └──────────────┘            └──────────────┘               │
│                                                             │
│  📝 PROCESSOR SERVICE        🗂️  JAVA SERVER               │
│  ┌──────────────┐            ┌──────────────┐               │
│  │ Python Flask │            │ Java Server  │               │
│  │ Port: 8006   │            │ Port: 8005   │               │
│  │              │            │              │               │
│  │ POST /process│            │ POST /javaprocess            │
│  └──────────────┘            └──────────────┘               │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## 🎯 Client Interaction Patterns

### Pattern 1: Simple GET (Assembly Scripts)
```
call_curl1.asm or call_curl2.asm
         │
         ▼
    Executes curl command
         │
         ▼
    Calls Service
         │
         ▼
    Prints Response
```

### Pattern 2: Auth → Process (C++ Client 1, Go Client)
```
Client Application
    │
    ├─► [Auth Service] Get token
    │   Port: 8001
    │
    ▼
Client receives token
    │
    ├─► [Target Service] Use token
    │   Java: 8005 or Go: 8003
    │
    ▼
Client prints result
```

### Pattern 3: Complex 3-Step Workflow (Java Client)
```
┌─────────────────────────────────────────────────────────────┐
│                     UserClient.java                         │
└─────────────────────────────────────────────────────────────┘

                    START
                      │
        ┌─────────────┼─────────────┐
        │             │             │
        ▼             ▼             ▼
   ┌────────┐   ┌─────────┐   ┌─────────┐
   │ STEP 1 │──►│ STEP 2  │──►│ STEP 3  │
   └────────┘   └─────────┘   └─────────┘
        │             │             │
        │             │             │
        ▼             ▼             ▼
   ┌─────────┐   ┌─────────┐   ┌─────────┐
   │  AUTH   │   │ANALYZE  │   │PROCESS  │
   │ :8001   │   │ :8003   │   │ :8005   │
   └─────────┘   └─────────┘   └─────────┘
        │             │             │
        │             │             │
        ▼             ▼             ▼
   ┌─────────┐   ┌─────────┐   ┌─────────┐
   │ Returns │   │ Returns │   │ Returns │
   │  Token  │   │ Analysis│   │  Doc ID │
   └─────────┘   └─────────┘   └─────────┘
        │             │             │
        └─────────────┼─────────────┘
                      │
                      ▼
                   PRINT ALL
                   RESPONSES
                      │
                      ▼
                    END
```

## 🔄 Detailed Flow Examples

### Example 1: UserClient.java Complete Flow

```
USER INPUT: "manan"
     │
     ▼
┌────────────────────────────┐
│  STEP 1: Authentication    │
└────────────────────────────┘
     │
     ├─► HTTP GET
     │   URL: http://localhost:8001/auth?user=manan
     │
     ▼
┌────────────────────────────┐
│ authService.py             │
│ - Receives: user="manan"   │
│ - Generates: token using   │
│   SHA256 hash              │
└────────────────────────────┘
     │
     ▼
     Returns: "a3f9d8e1c2b4..."
     │
     ▼
┌────────────────────────────┐
│  STEP 2: Analysis          │
└────────────────────────────┘
     │
     ├─► HTTP POST
     │   URL: http://localhost:8003/analyze
     │   Body: {
     │     "token": "a3f9d8...",
     │     "user": "manan"
     │   }
     │
     ▼
┌────────────────────────────┐
│ service1.go                │
│ - Receives token & user    │
│ - Creates analysis response│
└────────────────────────────┘
     │
     ▼
     Returns: {
       "summary": "analysis-for-manan",
       "confidence": 0.93,
       "token_preview": "a3f9d8"
     }
     │
     ▼
┌────────────────────────────┐
│  STEP 3: Document Storage  │
└────────────────────────────┘
     │
     ├─► HTTP POST
     │   URL: http://localhost:8005/javaprocess
     │   Body: {
     │     "user": "manan",
     │     "analysis": {...}
     │   }
     │
     ▼
┌────────────────────────────┐
│ JavaServer.java            │
│ - Receives user & analysis │
│ - Generates docId          │
└────────────────────────────┘
     │
     ▼
     Returns: {
       "status": "ok",
       "docId": "DOC-1234567890"
     }
     │
     ▼
   Print all results
     │
     ▼
    END
```

### Example 2: processor.py Flow

```
External Client (e.g., cpp_client2.cpp)
         │
         ▼
    POST /process
    {
      "doc": "hello from cpp2",
      "analysis_text": "check grammar"
    }
         │
         ▼
┌────────────────────────────┐
│  processor.py receives     │
└────────────────────────────┘
         │
         ▼
    Add timestamp field:
    data['processed_at'] = "processor: Wed..."
         │
         ▼
    Now calls Go service:
         │
         ├─► POST http://localhost:8003/analyze
         │   {
         │     "text": "check grammar",
         │     "meta": {...}
         │   }
         │
         ▼
┌────────────────────────────┐
│  service1.go               │
│  Returns enrichment data   │
└────────────────────────────┘
         │
         ▼
┌────────────────────────────┐
│  processor.py combines     │
│  original data + enrichment│
└────────────────────────────┘
         │
         ▼
    Returns full enriched document:
    {
      "doc": "hello from cpp2",
      "analysis_text": "check grammar",
      "processed_at": "...",
      "go_enrichment": {
        "summary": "...",
        "confidence": 0.93
      }
    }
         │
         ▼
    Return to client
```

## 🌐 Network Topology

```
┌──────────────────────────────────────────────────────┐
│                    localhost                         │
│                                                      │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌────────── ┐│
│  │ Python  │  │   Go    │  │  Java   │  │  Python   ││
│  │ Auth    │  │Analyze  │  │ Server  │  │ Processor ││
│  │ :8001   │  │ :8003   │  │ :8005   │  │  :8006    ││
│  └────┬────┘  └────┬────┘  └────┬────┘  └────┬──────┘│
│       │            │             │            │      │
│       └────────────┴─────────────┴────────────┘      │
│                      ▲                               │
│                      │                               │
│              HTTP Requests                           │
│                      │                               │
│       ┌──────────────┼──────────────┐                │
│       │              │              │                │
│  ┌────▼───┐    ┌─────▼────┐   ┌─────▼────┐           │
│  │ Java   │    │  C++ 1   │   │  C++ 2   │           │
│  │ Client │    │  Client  │   │  Client  │           │
│  └────────┘    └──────────┘   └──────────┘           │
│                                                      │
│  ┌─────────┐  ┌─────────┐                            │
│  │   Go    │  │ Assembly│                            │
│  │ Client  │  │ Scripts │                            │
│  └─────────┘  └─────────┘                            │
│                                                      │
└──────────────────────────────────────────────────────┘
```

## 📝 Quick Reference Guide

### Starting the System

```bash
# Terminal 1: Start Python Auth Service
cd python && python authService.py

# Terminal 2: Start Go Analyze Service  
cd go && go run service1.go

# Terminal 3: Start Java Server
cd java && javac JavaServer.java && java JavaServer

# Terminal 4: Start Python Processor (optional)
cd python && python processor.py
```

### Running Clients

```bash
# Run Java client (most comprehensive)
cd java && javac UserClient.java && java UserClient

# Run C++ client 1
cd cpp && g++ cpp_client1.cpp -lcurl -o cpp_client1 && ./cpp_client1

# Run C++ client 2
cd cpp && g++ cpp_client2.cpp -lcurl -o cpp_client2 && ./cpp_client2

# Run Go client
cd go && go run service2.go

# Run Assembly scripts
cd asm && nasm -f elf64 call_curl1.asm && ld call_curl1.o -o call_curl1 && ./call_curl1
```

### Data Flow Summary

1. **Auth Flow**: Client → Python(8001) → Token
2. **Analysis Flow**: Client → Go(8003) → Analysis Data  
3. **Document Flow**: Client → Python(8006) → Go(8003) → Enriched Data
4. **Storage Flow**: Client → Java(8005) → Document ID

