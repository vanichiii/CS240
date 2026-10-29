.data
data_bytes: .byte 70, 105, 122, 122, 0, 66, 117, 122, 122, 0, 70, 105, 122, 122, 66, 117, 122, 122, 0, 10, 0

.text
.globl main
main:
    addiu $t0, $zero, 1
L00400004:
    addiu $t1, $zero, 15
    div $t0, $t1
    mfhi $t2
    beq $t2, $zero, L00400044
    addiu $t1, $zero, 3
    div $t0, $t1
    mfhi $t2
    beq $t2, $zero, L00400058
    addiu $t1, $zero, 5
    div $t0, $t1
    mfhi $t2
    beq $t2, $zero, L0040006C
    addiu $v0, $zero, 1
    addu $a0, $t0, $zero
    syscall
    j L0040007C
L00400044:
    addiu $v0, $zero, 4
    lui $a0, 4097
    ori $a0, $a0, 10
    syscall
    j L0040007C
L00400058:
    addiu $v0, $zero, 4
    lui $a0, 4097
    ori $a0, $a0, 0
    syscall
    j L0040007C
L0040006C:
    addiu $v0, $zero, 4
    lui $a0, 4097
    ori $a0, $a0, 5
    syscall
L0040007C:
    addiu $v0, $zero, 4
    lui $a0, 4097
    ori $a0, $a0, 19
    syscall
    addi $t0, $t0, 1
    slti $at, $t0, 101
    bne $at, $zero, L00400004
    addiu $v0, $zero, 10
    syscall
