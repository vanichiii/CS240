#BLACKJACK

.data
#-----------Labels-----------
.eqv BASE_ADDRESS, 0x10040000
.eqv WIDTH, 32                 # 256 / 8 = 32 units wide
.eqv HEIGHT, 32
.eqv COLOR_BG,    0x00FFFFFF # White background
.eqv COLOR_GREEN  0x00008000 # Table background   
.eqv COLOR_WHITE  0x00FFFFFF # Card face
.eqv COLOR_RED    0x00FF0000 # Text / Dealer color
.eqv COLOR_BLUE   0x000000FF # Player color
.eqv COLOR_BLACK  0x00000000 # Outlines

#---------- Ascii/Text---------
msg_title: .asciiz "Let's play BlackJack!!"
msg_rules: .asciiz "\nPress[h] to Hit, [s] to Stand: "
msg_player_bust: .asciiz "\nPlayer BUSTS! Dealer wins.\n"
msg_dealer_bust: .asciiz "\nDealer BUSTS! Player wins.\n"
msg_player_wins: .asciiz "\nPlayer wins!!! :D\n"
msg_dealer_wins: .asciiz "\nDealer wins!\n"
msg_tie:         .asciiz "\nIt's a push (tie)!\n"

player_score: .word 0
dealer_score: .word 0

.text
.globl main

main:
li $s0, BASE_ADDRESS    #stores base address

#sets background color
move $a0, $s0
li $a1, COLOR_BG
jal print_board

#print title screen
li $v0, 4               #command for priting string
    la $a0, msg_title   #print title screen
    syscall
    
jal print_board

# Draw Dealer initial card template
li $a0, 4        # X coordinate
li $a1, 3        # Y coordinate
jal draw_card_sprite

# Draw Player initial card template
li $a0, 4        # X coordinate
li $a1, 18       # Y coordinate
jal draw_card_sprite

# Player Starting Total (between 12 and 20)
li $v0, 42
li $a1, 9
syscall
addi $a0, $a0, 12
sw $a0, player_score

# Dealer Starting Total (between 10 and 17)
li $v0, 42
li $a1, 8
syscall
addi $a0, $a0, 10
sw $a0, dealer_score
    
game_loop:

lw $a0, player_score
jal draw_player_score_indicator

li $v0, 4
la $a0, msg_rules
syscall

# Read Character
li $v0, 12
syscall
move $t0, $v0    # Choice in $t0

# Line break on console
li $v0, 11
li $a0, 10
syscall

beq $t0, 'h', hit_action
beq $t0, 's', stand_action
j game_loop

hit_action:
#Generate random card value 2-11
li $v0, 42
li $a1, 10
syscall
addi $t1, $a0, 2

lw $t2, player_score
add $t2, $t2, $t1
sw $t2, player_score

#Check if Player busted
li $t3, 21
bgt $t2, $t3, player_bust
    
#Draw an additional card sprite offset to the right
li $a0, 14
li $a1, 18
jal draw_card_sprite
j game_loop

stand_action:
dealer_turn:
lw $t2, dealer_score
li $t3, 17
bge $t2, $t3, evaluate_winner

#Dealer Hits (Draw 2-11)
li $v0, 42
li $a1, 10
syscall
addi $t1, $a0, 2
    
lw $t2, dealer_score
add $t2, $t2, $t1
sw $t2, dealer_score

# Draw additional card for dealer
    li $a0, 14
    li $a1, 3
    jal draw_card_sprite
    j dealer_turn

evaluate_winner:
lw $t0, player_score
lw $t1, dealer_score

li $t3, 21
bgt $t1, $t3, dealer_bust
beq $t0, $t1, game_tie
bgt $t0, $t1, player_wins
j dealer_wins

#---Ending Game-----
player_bust:
li $v0, 4
la $a0, msg_player_bust
syscall
li $a0, COLOR_RED
jal fill_screen_color
j exit

dealer_bust:
li $v0, 4
la $a0, msg_dealer_bust
syscall
li $a0, COLOR_BLUE
jal fill_screen_color
j exit

player_wins:
li $v0, 4
la $a0, msg_player_wins
syscall
li $a0, COLOR_BLUE
jal fill_screen_color
j exit

dealer_wins:
li $v0, 4
la $a0, msg_dealer_wins
syscall
li $a0, COLOR_RED
jal fill_screen_color
j exit

game_tie:
li $v0, 4
la $a0, msg_tie
syscall
li $a0, COLOR_WHITE
jal fill_screen_color

exit:
li $v0, 10
syscall

#-----Calculations and such-------
calc_address:
sll $t0, $a0, 2 #column shift - column # * offset (4)
sll $t1, $a1, 7 #row shift - row # * offset (128) -- (row # * 4 * 8)
add $v0, $t0, $s0 #add column shift to base 
add $v0, $v0, $t1 #add row shift to base
jr $ra

print_board:
move $t0, $s0       # Copy base from $s0
li $t1, 1024      # Total units (32 * 32)
li $t2, COLOR_GREEN
board_loop:
sw $t2, 0($t0)
addi $t0, $t0, 4
addi $t1, $t1, -1
bnez $t1, board_loop
jr $ra

#Fills background color    
fill_screen_color:
move $t0, $s0       # Copy base from $s0
li $t1, 1024
move $t2, $a0       # Color to paint
fill_loop:
sw $t2, 0($t0)
addi $t0, $t0, 4
addi $t1, $t1, -1
bnez $t1, fill_loop
jr $ra
    
draw_card_sprite:
addi $sp, $sp, -4
sw $ra, 0($sp)
    
move $t3, $a0       # anchor X
move $t4, $a1       # anchor Y
    
    li $t5, 0           # Row counter (Height = 10 units)
card_row_loop:
li $t6, 0           # Col counter (Width = 8 units)
card_col_loop:
add $a0, $t3, $t6
add $a1, $t4, $t5
li $a2, COLOR_WHITE
    
#stack for every dot
addi $sp, $sp, -16
sw $t3, 0($sp)
sw $t4, 4($sp)
sw $t5, 8($sp)
sw $t6, 12($sp)
jal draw_dot
    
lw $t3, 0($sp)
lw $t4, 4($sp)
lw $t5, 8($sp)
lw $t6, 12($sp)
addi $sp, $sp, 16

addi $t6, $t6, 1
li $t7, 8           # Width = 8 units
blt $t6, $t7, card_col_loop
    
addi $t5, $t5, 1
li $t7, 10          # Height = 10 units
blt $t5, $t7, card_row_loop

lw $ra, 0($sp)
addi $sp, $sp, 4
jr $ra

#status bar
draw_player_score_indicator:
addi $sp, $sp, -4
sw   $ra, 0($sp)


move $t0, $a0
li $a1, 30          # Bottom row index of 32-unit height limit
li $a0, 2           # Start X
li $t1, 0

indicator_loop:
addi $sp, $sp, -8
sw $t0, 0($sp)
sw $t1, 4($sp)
    
li $a2, COLOR_BLUE
jal draw_dot
    
lw $t0, 0($sp)
lw $t1, 4($sp)
addi $sp, $sp, 8
    
addi $a0, $a0, 1    # Dense spacing for 32x32 limit
addi $t1, $t1, 1
blt $t1, $t0, indicator_loop
lw   $ra, 0($sp)
addi $sp, $sp, 4
jr $ra

draw_dot:
addi $sp, $sp, -4
sw   $ra, 0($sp)

jal  calc_address
sw   $a2, 0($v0)

lw   $ra, 0($sp)
addi $sp, $sp, 4
jr   $ra
    
#Intercept with your custom address generator
jal calc_address    # Returns target pointer inside $v0
sw $a2, 0($v0)      # Store the requested pixel color directly into memory
    
lw $ra, 0($sp)
addi $sp, $sp, 4
jr $ra