.text
main:
    PACK $6, $0, 11       # ea0 = R6 = 11
    PACK $7, $0, 0        # ea1 = R7 = 0

LOOP_SUM:
    RAISE $7, $7, $6      # ea1 += ea0

    PACK $9, $0, 1        # R9 = 1
    BREAK $8, $6, $9      # ea2 = ea0 - 1

    RAISE $7, $7, $8      # ea1 += ea2

    FORMLOOP $6, LOOP_SUM # decrement-and-branch

    FREEZE $7, 0($24)     # store result into MEM[sp]
    MEDITATE              # halt
