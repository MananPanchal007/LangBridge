; asm/call_curl2.asm
; Calls JavaServer endpoint: curl -s -X POST -H "Content-Type: application/json" -d '{"asm":"ping"}' http://localhost:8005/javaprocess

section .data
    curldb db "curl",0
    dashX db "-X",0
    post db "POST",0
    h db "-H",0
    ct db "Content-Type: application/json",0
    d db "-d",0
    payload db "{\"asm\":\"ping\"}",0
    url db "http://localhost:8005/javaprocess",0
    ; argv array: curl -s -X POST -H "Content-Type: application/json" -d '{"asm":"ping"}' URL
    args dq curldb, dashX, post, h, ct, d, payload, url, 0

section .text
    global _start
_start:
    xor rdx, rdx
    mov rdi, curldb
    lea rsi, [rel args]
    mov rax, 59
    syscall
    mov rdi, 1
    mov rax, 60
    syscall
