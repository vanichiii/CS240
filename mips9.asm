#TiC-TAC-TOE GAME
.data
#-------Lables-------
.eqv BASE_ADDRESS, 0x10040000
.eqv PLAYER_ONE, 1
.eqv PLAYER_TWO, 2
.eqv WIDTH, 32                 # 256 / 8 = 32 units wide
.eqv HEIGHT, 32
.eqv COLOR_BG, 0x00FFFFFF      # White background
.eqv COLOR_GRID, 0x001A1A1A    # Dark grid lines
.eqv COLOR_X, 0x00FF3333       # Red for Player X
.eqv COLOR_O, 0x00F38BA8       # Pink for Player O

# ---------- Ascii/Text---------
msg_title: .asciiz "\nWelcome Player!"
msg_player: .asciiz "\nPlayer "
msg_turn:   .asciiz "'s turn! Press any number from 1-9: "
msg_error: .asciiz "\nError, this move is invalid! Try again."
msg_tie: .asciiz "\nIt's a tie!"
msg_winner: .asciiz "\nCongratz to the winner!!"

# Game Board Array (9 bytes, initialized to 0)
# 0 = Empty, 1 = Player 1 (X), 2 = Player 2 (O)
board:      .byte 0, 0, 0, 0, 0, 0, 0, 0, 0

.text
.globl main
main:
li $s0, BASE_ADDRESS    #stores base address
li $s1, PLAYER_ONE
li $s2, PLAYER_TWO

#sets background color
move $a0, $s0
li $a1, COLOR_BG
jal print_board

#print title screen
li $v0, 4               #command for priting string
    la $a0, msg_title   #print title screen
    syscall
    
jal draw_grid

game_loop:
    jal check_draw
    bnez $v0, game_draw
    
    jal get_current_player
    move $s3, $v0        #stores current player
   
    #Ask player for next number 
    li $v0, 4
    la $a0, msg_player
    syscall
    
     # Print the player number
    li $v0, 1
    move $a0, $s3
    syscall
    
    # Print the rest of the prompt
    li $v0, 4
    la $a0, msg_turn
    syscall
    
    #reading input
    li $v0, 5
    syscall
    move $s4, $v0        #stores input
    
    #check input validity
    blt $s4, 1, error
    bgt $s4, 9, error
    
    #convert range from 1-9 to 0-8
    subi $s4, $s4, 1
    la $t0, board
    add $t0, $t0, $s4
    lb $t1, 0($t0)       #load status at index
    bnez $t1, error      #show error if not 0
    
    sb $s3, 0($t0)       #save token to board data
    
    move $a0, $s4
    move $a1, $s3        # Active player token
    jal draw_player_marker
    
    #check for winner
    jal check_win
    bnez $v0, game_won

    j game_loop          #jumps immediately to the top of loop
    
#-------Scenario handler or something-------
error:
	li $v0, 4
	la $a0, msg_error
	syscall
	j game_loop      #returns back to game_loop

game_won:
	li $v0, 4
	la $a0, msg_winner
	syscall
	j exit		#returns to exit

game_draw:
    li $v0, 4
    la $a0, msg_tie
    syscall
    j exit

exit:
	li $v0, 10
	syscall

#-----Calculations and such-----

#Finding address
calc_address:
sll $t0, $a0, 2 #column shift - column # * offset (4)
sll $t1, $a1, 7 #row shift - row # * offset (128) -- (row # * 4 * 8)
add $v0, $t0, $s0 #add column shift to base 
add $v0, $v0, $t1 #add row shift to base
jr $ra

draw_pixel:
subi $sp, $sp, 8
sw   $ra, 4($sp)
sw   $a2, 0($sp)       # preserve color input across child call
jal  calc_address      # returns target location memory address in $v0
lw   $a2, 0($sp)
sw   $a2, 0($v0)       # store color hex value directly onto display pixel
lw   $ra, 4($sp)
addi $sp, $sp, 8
jr   $ra
    

# Clear Screen: $a0 = base address, $a1 = color
print_board:
    li $t2, 1024           # 32 * 32 total matrix units
board_loop:
    sw $a1, 0($a0)
    addi $a0, $a0, 4
    subi $t2, $t2, 1
    bnez $t2, board_loop
    jr $ra

#Drawing grid
draw_grid:
    subi $sp, $sp, 4
    sw $ra, 0($sp)

    # Line 1: First Vertical Line at X=10
    li $t2, 0              # Loop tracker (y = 0)
v_line1:
    li $a0, 10
    move $a1, $t2
    li $a2, COLOR_GRID
    jal draw_pixel
    addi $t2, $t2, 1
    blt $t2, 32, v_line1

    # Line 2: Second Vertical Line at X=21
    li $t2, 0
v_line2:
    li $a0, 21
    move $a1, $t2
    li $a2, COLOR_GRID
    jal draw_pixel
    addi $t2, $t2, 1
    blt $t2, 32, v_line2

    # Line 3: First Horizontal Line at Y=10
    li $t2, 0              # Loop tracker (x = 0)
h_line1:
    move $a0, $t2
    li $a1, 10
    li $a2, COLOR_GRID
    jal draw_pixel
    addi $t2, $t2, 1
    blt $t2, 32, h_line1

    # Line 4: Second Horizontal Line at Y=21
    li $t2, 0
h_line2:
    move $a0, $t2
    li $a1, 21
    li $a2, COLOR_GRID
    jal draw_pixel
    addi $t2, $t2, 1
    blt $t2, 32, h_line2

    lw $ra, 0($sp)
    addi $sp, $sp, 4
    jr $ra

# Render Player Symbol Artifact: $a0 = board slot position (0-8), $a1 = player (1 or 2)
draw_player_marker:
    subi $sp, $sp, 16
    sw $ra, 0($sp)
    sw $s5, 4($sp)
    sw $s6, 8($sp)
    sw $s7, 12($sp)

    move $s7, $a1          # Store active token copy safely
    
    # Convert index 0-8 to grid coordinates
    li $t0, 3
    div $a0, $t0
    mflo $s5               # Row position (0-2)
    mfhi $s6               # Column position (0-2)
    
    # Compute bounding starting offsets
    mul $s6, $s6, 11       # X starting step
    addi $s6, $s6, 2       # Grid padding
    mul $s5, $s5, 11       # Y starting step
    addi $s5, $s5, 2       # Grid padding

    beq $s7, 2, draw_circle
    
    li $t2, 0              # Loop Counter
draw_x_loop:
    add $a0, $s6, $t2
    add $a1, $s5, $t2
    li $a2, COLOR_X
    jal draw_pixel
    
    add $a0, $s6, $t2
    li $t3, 6
    sub $t3, $t3, $t2
    add $a1, $s5, $t3
    li $a2, COLOR_X
    jal draw_pixel

    addi $t2, $t2, 1
    blt $t2, 7, draw_x_loop
    j finished_draw
    
draw_circle:
    #outline
    li $t2, 0
    
draw_o_loop:
    add $a0, $s6, $t2
    move $a1, $s5
    li $a2, COLOR_O
    jal draw_pixel
    
    add $a0, $s6, $t2
    addi $a1, $s5, 6
    jal draw_pixel
    
    move $a0, $s6
    add $a1, $s5, $t2
    jal draw_pixel
    
    addi $a0, $s6, 6
    add $a1, $s5, $t2
    jal draw_pixel

    addi $t2, $t2, 1
    blt $t2, 7, draw_o_loop

finished_draw:
    lw $ra, 0($sp)
    lw $s5, 4($sp)
    lw $s6, 8($sp)
    lw $s7, 12($sp)
    addi $sp, $sp, 16
    jr $ra
    
#--------Getters, Checkers, etc-------
get_current_player:
    la $t0, board
    li $t1, 0              # count moves taken
    li $t2, 0              # array loop index
count_loop:
    add $t3, $t0, $t2
    lb $t4, 0($t3)
    beq $t4, 0, empty_slot
    addi $t1, $t1, 1
empty_slot:
    addi $t2, $t2, 1
    blt $t2, 9, count_loop
    li $t5, 2
    div $t1, $t5
    mfhi $t6
    bnez $t6, p2_turn
    li $v0, 1              # Player 1
    jr $ra

p2_turn:
    li $v0, 2              # Player 2
    jr $ra

check_win:
    la $t0, board
    li $t1, 0
row_check:
    add $t2, $t0, $t1
    lb $t3, 0($t2)
    lb $t4, 1($t2)
    lb $t5, 2($t2)
    and $t6, $t3, $t4
    and $t6, $t6, $t5
    bnez $t6, win_found
    addi $t1, $t1, 3
    blt $t1, 9, row_check
    li $t1, 0

col_check:
    add $t2, $t0, $t1
    lb $t3, 0($t2)
    lb $t4, 3($t2)
    lb $t5, 6($t2)
    and $t6, $t3, $t4
    and $t6, $t6, $t5
    bnez $t6, win_found
    addi $t1, $t1, 1
    blt $t1, 3, col_check
    lb $t3, 0($t0)
    lb $t4, 4($t0)
    lb $t5, 8($t0)
    and $t6, $t3, $t4
    and $t6, $t6, $t5
    bnez $t6, win_found
    lb $t3, 2($t0)
    lb $t4, 4($t0)
    lb $t5, 6($t0)
    and $t6, $t3, $t4
    and $t6, $t6, $t5
    bnez $t6, win_found
    li $v0, 0              
    jr $ra   

win_found:
    move $v0, $t3          
    jr $ra

check_draw:
    la $t0, board
    li $t1, 0
draw_loop:
    add $t2, $t0, $t1
    lb $t3, 0($t2)
    beq $t3, 0, clear_not_draw
    addi $t1, $t1, 1
    blt $t1, 9, draw_loop
    li $v0, 1              
    jr $ra
clear_not_draw:
    li $v0, 0              
    jr $ra