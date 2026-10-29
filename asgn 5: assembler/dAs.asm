.data
# Data bytes: 160
00400000: addiu $t0, $zero, 1
00400004: addiu $t1, $zero, 15
00400008: div $t0, $t1
0040000C: mfhi $t2
00400010: beq $t2, $zero, TARGET
00400014: addiu $t1, $zero, 3
00400018: div $t0, $t1
0040001C: mfhi $t2
00400020: beq $t2, $zero, TARGET
00400024: addiu $t1, $zero, 5
00400028: div $t0, $t1
0040002C: mfhi $t2
00400030: beq $t2, $zero, TARGET
00400034: addiu $v0, $zero, 1
00400038: addu $a0, $t0, $zero
0040003C: syscall
00400040: j 0x40007c
00400044: addiu $v0, $zero, 4
00400048: lui $a0, 4097
0040004C: ori $a0, $a0, 10
00400050: syscall
00400054: j 0x40007c
00400058: addiu $v0, $zero, 4
0040005C: lui $a0, 4097
00400060: ori $a0, $a0, 0
00400064: syscall
00400068: j 0x40007c
0040006C: addiu $v0, $zero, 4
00400070: lui $a0, 4097
00400074: ori $a0, $a0, 5
00400078: syscall
0040007C: addiu $v0, $zero, 4
00400080: lui $a0, 4097
00400084: ori $a0, $a0, 19
00400088: syscall
0040008C: addi $t0, $t0, 1
00400090: slti $at, $t0, 101
00400094: bne $at, $zero, TARGET
00400098: addiu $v0, $zero, 10
0040009C: syscall
