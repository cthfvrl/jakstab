package org.jakstab.asm.z;

/**
 * Created by Basil on 04.05.2017.
 */

//special for BCR instruction
public class ZRRmInstruction extends ZInstruction{

    public ZRRmInstruction(ZOpcode opcode, ZMask mask, ZRegister branch_target) { super(opcode, mask, branch_target); }

    @Override
    public ZInstructionFormat getFormat() {
        return ZInstructionFormat.RRm;
    }

    public ZMask getMask() { return (ZMask) getOperand1(); }

    @Override
    public ZRegister getOperand2() { return (ZRegister) super.getOperand2(); }
}
