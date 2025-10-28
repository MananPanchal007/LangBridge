; asm/call_curl1.asm
; NASM x86_64 assembly that execs curl to call AuthService:
; Builds a process that calls: curl -s "http://localhost:8001/auth?user=asm1"
; Assemble & link:
; nasm -f elf64 call_curl1.asm && ld call_curl1.o -o call_curl1
; ./call_curl1

section .data
    curldb db "curl",0
    dash db "-s",0
    url db "http://localhost:8001/auth?user=asm1",0
    args dq curldb, dash, url, 0

section .text
    global _start

_start:
    ; prepare pointers for execlp via stack
    mov rdi, curldb      ; filename
    lea rsi, [rel args]  ; argv (pointer to array)
    ; Use execvp syscall via libc (call execvp using syscall? easier to call execve)
    ; We'll call execve (syscall 59): execve(const char *filename, char *const argv[], char *const envp[]);
    xor rdx, rdx         ; envp = NULL
    mov rax, 59
    syscall

    ; if execve fails, exit with code 1
    mov rdi, 1
    mov rax, 60
    syscall
