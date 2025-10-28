// go/service2.go
// A small client that POSTs a request to JavaServer and prints response.
// Run: go run service2.go

package main

import (
    "bytes"
    "fmt"
    "io"
    "net/http"
)

func main() {
    jsonBody := []byte(`{"from":"go_client","note":"please save doc"}`)
    resp, err := http.Post("http://localhost:8005/javaprocess", "application/json", bytes.NewReader(jsonBody))
    if err != nil {
        panic(err)
    }
    defer resp.Body.Close()
    b, _ := io.ReadAll(resp.Body)
    fmt.Println("JavaServer replied:", string(b))
}
