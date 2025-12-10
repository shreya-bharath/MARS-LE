package mars.mips.instructions.customlangs;
import mars.*;
import mars.mips.hardware.*;
import mars.mips.instructions.*;
import mars.util.*;
import mars.simulator.*;


public class BEND32 extends CustomAssembly {

    // We'll use register 27 ("av" in your spec) as the Avatar-state flag.
    private static final int AVATAR_REGISTER = 27;

    @Override
    public String getName() {
        return "BEND32";
    }

    @Override
    public String getDescription() {
        return "AVATAR Assembly Language (BEND32) – water/earth/fire/air + Avatar state.";
    }

    @Override
    protected void populate() {
        /*
         * Helper notes about the opcode masks:
         *  - Use BasicInstructionFormat.R_FORMAT, I_FORMAT, I_BRANCH_FORMAT, or J_FORMAT.
         *  - The mask string is 32 chars using 0,1,f,s,t.
         *    'f','s','t' correspond to the 1st, 2nd, 3rd operands in the example string.
         */

        // ---------------------------------------------------------------------
        // Basic Earth (R-type) ALU instructions
        // ---------------------------------------------------------------------

        // 1. RAISE rd, rs, rt  (add)
        // opcode = 000000, funct = 000000
        instructionList.add(
            new BasicInstruction(
                "RAISE $1,$2,$3",
                "RAISE rd,rs,rt : integer add (rd = rs + rt)",
                BasicInstructionFormat.R_FORMAT,
                "000000ssssstttttfffff00000000000",
                new SimulationCode() {
                    public void simulate(ProgramStatement statement) throws ProcessingException {
                        int[] ops = statement.getOperands();   // rd, rs, rt
                        int rd = ops[0];
                        int rs = ops[1];
                        int rt = ops[2];
                        int result = RegisterFile.getValue(rs) + RegisterFile.getValue(rt);
                        RegisterFile.updateRegister(rd, result);
                    }
                }
            )
        );

        // 2. BREAK rd, rs, rt  (sub)
        // opcode = 000000, funct = 000001
        instructionList.add(
            new BasicInstruction(
                "BREAK $1,$2,$3",
                "BREAK rd,rs,rt : integer subtract (rd = rs - rt)",
                BasicInstructionFormat.R_FORMAT,
                "000000ssssstttttfffff00000000001",
                new SimulationCode() {
                    public void simulate(ProgramStatement statement) throws ProcessingException {
                        int[] ops = statement.getOperands();
                        int rd = ops[0];
                        int rs = ops[1];
                        int rt = ops[2];
                        int result = RegisterFile.getValue(rs) - RegisterFile.getValue(rt);
                        RegisterFile.updateRegister(rd, result);
                    }
                }
            )
        );

        // 3. SEAR rd, rs, rt  (bitwise AND)
        // funct = 000010
        instructionList.add(
            new BasicInstruction(
                "SEAR $1,$2,$3",
                "SEAR rd,rs,rt : bitwise AND (rd = rs & rt)",
                BasicInstructionFormat.R_FORMAT,
                "000000ssssstttttfffff00000000010",
                new SimulationCode() {
                    public void simulate(ProgramStatement statement) throws ProcessingException {
                        int[] ops = statement.getOperands();
                        int rd = ops[0];
                        int rs = ops[1];
                        int rt = ops[2];
                        int result = RegisterFile.getValue(rs) & RegisterFile.getValue(rt);
                        RegisterFile.updateRegister(rd, result);
                    }
                }
            )
        );

        // 4. FLARE rd, rs, rt  (bitwise OR)
        // funct = 000011
        instructionList.add(
            new BasicInstruction(
                "FLARE $1,$2,$3",
                "FLARE rd,rs,rt : bitwise OR (rd = rs | rt)",
                BasicInstructionFormat.R_FORMAT,
                "000000ssssstttttfffff00000000011",
                new SimulationCode() {
                    public void simulate(ProgramStatement statement) throws ProcessingException {
                        int[] ops = statement.getOperands();
                        int rd = ops[0];
                        int rs = ops[1];
                        int rt = ops[2];
                        int result = RegisterFile.getValue(rs) | RegisterFile.getValue(rt);
                        RegisterFile.updateRegister(rd, result);
                    }
                }
            )
        );

        // 5. SPARK rd, rs, rt  (bitwise XOR)
        // funct = 000100
        instructionList.add(
            new BasicInstruction(
                "SPARK $1,$2,$3",
                "SPARK rd,rs,rt : bitwise XOR (rd = rs ^ rt)",
                BasicInstructionFormat.R_FORMAT,
                "000000ssssstttttfffff00000000100",
                new SimulationCode() {
                    public void simulate(ProgramStatement statement) throws ProcessingException {
                        int[] ops = statement.getOperands();
                        int rd = ops[0];
                        int rs = ops[1];
                        int rt = ops[2];
                        int result = RegisterFile.getValue(rs) ^ RegisterFile.getValue(rt);
                        RegisterFile.updateRegister(rd, result);
                    }
                }
            )
        );

        // ---------------------------------------------------------------------
        // Water (I-type) memory + immediate
        // ---------------------------------------------------------------------

        // 6. FLOW rt, imm(rs)   (load word, like lw)
        // opcode = 100011
        instructionList.add(
            new BasicInstruction(
                "FLOW $1,100($2)",
                "FLOW rt,imm(rs) : load word (rt = MEM[rs + signext(imm)])",
                BasicInstructionFormat.I_FORMAT,
                "100011sssssffffftttttttttttttttt",
                new SimulationCode() {
                    public void simulate(ProgramStatement statement) throws ProcessingException {
                        int[] ops = statement.getOperands();   // rt, rs, imm
                        int rt  = ops[0];
                        int rs  = ops[1];
                        int imm = ops[2];

                        int addr = RegisterFile.getValue(rs) + imm;

                        try {
                            int value = Memory.getInstance().getWord(addr);
                            RegisterFile.updateRegister(rt, value);
                        } catch (AddressErrorException e) {
                            throw new ProcessingException(statement, e);
                        }
                    }

                }
            )
        );

        // 7. FREEZE rt, imm(rs)   (store word, like sw)
        // opcode = 101011
        instructionList.add(
            new BasicInstruction(
                "FREEZE $1,100($2)",
                "FREEZE rt,imm(rs) : store word (MEM[rs + imm] = rt)",
                BasicInstructionFormat.I_FORMAT,
                "101011sssssffffftttttttttttttttt",
                new SimulationCode() {
                    public void simulate(ProgramStatement statement) throws ProcessingException {
                        int[] ops = statement.getOperands();   // rt, rs, imm
                        int rt  = ops[0];
                        int rs  = ops[1];
                        int imm = ops[2];

                        int base  = RegisterFile.getValue(rs);
                        int value = RegisterFile.getValue(rt);
                        int addr  = base + imm;

                        try {
                            Memory.getInstance().setWord(addr, value);
                        } catch (AddressErrorException e) {
                            throw new ProcessingException(statement, e);
                        }
                    }

                }
            )
        );

        // 8. PACK rt, rs, imm  (addi)
        // opcode = 001000
        instructionList.add(
            new BasicInstruction(
                "PACK $1,$2,10",
                "PACK rt,rs,imm : add immediate (rt = rs + imm)",
                BasicInstructionFormat.I_FORMAT,
                "001000sssssffffftttttttttttttttt",
                new SimulationCode() {
                    public void simulate(ProgramStatement statement) throws ProcessingException {
                        int[] ops = statement.getOperands();   // rt, rs, imm
                        int rt  = ops[0];
                        int rs  = ops[1];
                        int imm = ops[2];
                        int result = RegisterFile.getValue(rs) + imm;
                        RegisterFile.updateRegister(rt, result);
                    }
                }
            )
        );

        // ---------------------------------------------------------------------
        // Air (branches + jumps)
        // ---------------------------------------------------------------------

        // 9. SWIRL rs, rt, label  (branch if equal)
        // opcode = 000100
        instructionList.add(
            new BasicInstruction(
                "SWIRL $1,$2,label",
                "SWIRL rs,rt,label : if rs == rt branch to label",
                BasicInstructionFormat.I_BRANCH_FORMAT,
                "000100sssssffffftttttttttttttttt",
                new SimulationCode() {
                    public void simulate(ProgramStatement statement) throws ProcessingException {
                        int[] ops = statement.getOperands();   // rs, rt, offset
                        int rs  = ops[0];
                        int rt  = ops[1];
                        int off = ops[2];      // already sign-extended

                        int pc = RegisterFile.getProgramCounter();
                        if (RegisterFile.getValue(rs) == RegisterFile.getValue(rt)) {
                            int target = pc + 4 + (off << 2);
                            RegisterFile.setProgramCounter(target);
                        } else {
                            RegisterFile.setProgramCounter(pc + 4);
                        }
                    }
                }
            )
        );

        // 10. GUST rs, rt, label  (branch if not equal)
        // opcode = 000101
        instructionList.add(
            new BasicInstruction(
                "GUST $1,$2,label",
                "GUST rs,rt,label : if rs != rt branch to label",
                BasicInstructionFormat.I_BRANCH_FORMAT,
                "000101sssssffffftttttttttttttttt",
                new SimulationCode() {
                    public void simulate(ProgramStatement statement) throws ProcessingException {
                        int[] ops = statement.getOperands();   // rs, rt, offset
                        int rs  = ops[0];
                        int rt  = ops[1];
                        int off = ops[2];

                        int pc = RegisterFile.getProgramCounter();
                        if (RegisterFile.getValue(rs) != RegisterFile.getValue(rt)) {
                            int target = pc + 4 + (off << 2);
                            RegisterFile.setProgramCounter(target);
                        } else {
                            RegisterFile.setProgramCounter(pc + 4);
                        }
                    }
                }
            )
        );

        // 11. PHASE rs, rt, label  (branch if signs differ)
        // opcode = 000110
        instructionList.add(
            new BasicInstruction(
                "PHASE $1,$2,label",
                "PHASE rs,rt,label : branch if sign(rs) != sign(rt)",
                BasicInstructionFormat.I_BRANCH_FORMAT,
                "000110sssssffffftttttttttttttttt",
                new SimulationCode() {
                    public void simulate(ProgramStatement statement) throws ProcessingException {
                        int[] ops = statement.getOperands();   // rs, rt, offset
                        int rs  = ops[0];
                        int rt  = ops[1];
                        int off = ops[2];

                        int a = RegisterFile.getValue(rs);
                        int b = RegisterFile.getValue(rt);
                        boolean signsDiffer = ((a ^ b) & 0x80000000) != 0;

                        int pc = RegisterFile.getProgramCounter();
                        if (signsDiffer) {
                            int target = pc + 4 + (off << 2);
                            RegisterFile.setProgramCounter(target);
                        } else {
                            RegisterFile.setProgramCounter(pc + 4);
                        }
                    }
                }
            )
        );

        // 12. FORMLOOP rs, label  (decrement-and-branch)
        // opcode = 010000
        instructionList.add(
            new BasicInstruction(
                "FORMLOOP $1,label",
                "FORMLOOP rs,label : rs = rs - 1; if rs != 0 branch to label",
                BasicInstructionFormat.I_BRANCH_FORMAT,
                "010000sssss00000tttttttttttttttt",
                new SimulationCode() {
                    public void simulate(ProgramStatement statement) throws ProcessingException {
                        int[] ops = statement.getOperands();   // rs, offset
                        int rs  = ops[0];
                        int off = ops[1];

                        int value = RegisterFile.getValue(rs) - 1;
                        RegisterFile.updateRegister(rs, value);

                        int pc = RegisterFile.getProgramCounter();
                        if (value != 0) {
                            int target = pc + 4 + (off << 2);
                            RegisterFile.setProgramCounter(target);
                        } else {
                            RegisterFile.setProgramCounter(pc + 4);
                        }
                    }
                }
            )
        );

        // 13. GLIDE label   (unconditional jump)
        // opcode = 000010
        instructionList.add(
            new BasicInstruction(
                "GLIDE label",
                "GLIDE label : unconditional jump",
                BasicInstructionFormat.J_FORMAT,
                "000010ffffffffffffffffffffffffff",
                new SimulationCode() {
                    public void simulate(ProgramStatement statement) throws ProcessingException {
                        int[] ops = statement.getOperands();   // target address
                        int target = ops[0];
                        RegisterFile.setProgramCounter(target);
                    }
                }
            )
        );

        // 14. GLIDE.A label  (Avatar-conditional jump)
        // opcode = 000111
        instructionList.add(
            new BasicInstruction(
                "GLIDE.A label",
                "GLIDE.A label : jump only if Avatar state (AV) is 1",
                BasicInstructionFormat.J_FORMAT,
                "000111ffffffffffffffffffffffffff",
                new SimulationCode() {
                    public void simulate(ProgramStatement statement) throws ProcessingException {
                        int[] ops = statement.getOperands();   // target address
                        int target = ops[0];

                        int av = RegisterFile.getValue(AVATAR_REGISTER);
                        int pc = RegisterFile.getProgramCounter();

                        if (av != 0) {
                            RegisterFile.setProgramCounter(target);
                        } else {
                            RegisterFile.setProgramCounter(pc + 4);
                        }
                    }
                }
            )
        );

        // ---------------------------------------------------------------------
        // Unique R-type: RIPPLE (average)
        // ---------------------------------------------------------------------

        // 15. RIPPLE rd, rs, rt  (average)
        // funct = 000101
        instructionList.add(
            new BasicInstruction(
                "RIPPLE $1,$2,$3",
                "RIPPLE rd,rs,rt : rd = (rs + rt) >> 1",
                BasicInstructionFormat.R_FORMAT,
                "000000ssssstttttfffff00000000101",
                new SimulationCode() {
                    public void simulate(ProgramStatement statement) throws ProcessingException {
                        int[] ops = statement.getOperands();   // rd, rs, rt
                        int rd = ops[0];
                        int rs = ops[1];
                        int rt = ops[2];
                        int sum = RegisterFile.getValue(rs) + RegisterFile.getValue(rt);
                        int avg = sum >> 1;
                        RegisterFile.updateRegister(rd, avg);
                    }
                }
            )
        );

        // ---------------------------------------------------------------------
        // Avatar state & system interaction
        // ---------------------------------------------------------------------

        // 16. AVATAR.ON   (set AV flag = 1)
        // opcode = 000000, funct = 001000
        instructionList.add(
            new BasicInstruction(
                "AVATAR.ON",
                "AVATAR.ON : enter Avatar state (AV = 1)",
                BasicInstructionFormat.R_FORMAT,
                "00000000000000000000000000001000",
                new SimulationCode() {
                    public void simulate(ProgramStatement statement) throws ProcessingException {
                        RegisterFile.updateRegister(AVATAR_REGISTER, 1);
                    }
                }
            )
        );

        // 17. AVATAR.OFF  (set AV flag = 0)
        // opcode = 000000, funct = 001001
        instructionList.add(
            new BasicInstruction(
                "AVATAR.OFF",
                "AVATAR.OFF : exit Avatar state (AV = 0)",
                BasicInstructionFormat.R_FORMAT,
                "00000000000000000000000000001001",
                new SimulationCode() {
                    public void simulate(ProgramStatement statement) throws ProcessingException {
                        RegisterFile.updateRegister(AVATAR_REGISTER, 0);
                    }
                }
            )
        );

        // 18. MEDITATE  (halt – simple implementation)
        // opcode = 000000, funct = 001010
        instructionList.add(
            new BasicInstruction(
                "MEDITATE",
                "MEDITATE : halt/meditate – here we jump PC to 0 to end execution",
                BasicInstructionFormat.R_FORMAT,
                "00000000000000000000000000001010",
                new SimulationCode() {
                    public void simulate(ProgramStatement statement) throws ProcessingException {
                        // Simple “halt”: send PC to 0 so the simulator will
                        // quickly fall off the text segment (“cliff termination”).
                        RegisterFile.setProgramCounter(0);
                    }
                }
            )
        );

        // 19. SPIRITCALL imm  (trap into “Spirit World”)
        // opcode = 011000
        instructionList.add(
            new BasicInstruction(
                "SPIRITCALL 0",
                "SPIRITCALL imm : call into Spirit World / OS with code imm",
                BasicInstructionFormat.I_FORMAT,
                "0110000000000000ffffffffffffffff",
                new SimulationCode() {
                    public void simulate(ProgramStatement statement) throws ProcessingException {
                        int[] ops = statement.getOperands();   // imm
                        int code = ops[0];

                        // Very light-weight “OS” handling using MARS console.
                        if (code == 0) {
                            SystemIO.printString("SPIRITCALL 0: Normal mode path\n");
                        } else if (code == 1) {
                            SystemIO.printString("SPIRITCALL 1: Avatar path\n");
                        } else {
                            SystemIO.printString("SPIRITCALL " + code + ": invoked\n");
                        }
                    }
                }
            )
        );
    }
}

