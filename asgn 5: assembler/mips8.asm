.data
fizz:     .asciiz "Fizz"
buzz:     .asciiz "Buzz"
fizzbuzz: .asciiz "FizzBuzz"
newline:  .asciiz "\n"

.text
.globl main

main:
    li $t0, 1                  

loop:
    li $t1, 15
    div $t0, $t1
    mfhi $t2                  

    beq $t2, $zero, printFizzBuzz

 
    li $t1, 3
    div $t0, $t1
    mfhi $t2

    beq $t2, $zero, printFizz

    li $t1, 5
    div $t0, $t1
    mfhi $t2

    beq $t2, $zero, printBuzz

    li $v0, 1
    move $a0, $t0
    syscall
    j printNewline


printFizzBuzz:
    li $v0, 4
    la $a0, fizzbuzz
    syscall
    j printNewline


printFizz:
    li $v0, 4
    la $a0, fizz
    syscall
    j printNewline


printBuzz:
    li $v0, 4
    la $a0, buzz
    syscall


printNewline:
    li $v0, 4
    la $a0, newline
    syscall

    addi $t0, $t0, 1           

    ble $t0, 100, loop         

    li $v0, 10                 
    syscall