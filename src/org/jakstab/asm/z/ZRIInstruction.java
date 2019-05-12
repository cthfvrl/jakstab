package org.jakstab.asm.z;

import org.jakstab.asm.Immediate;

public class ZRIInstruction extends ZInstruction {

    public ZRIInstruction(ZOpcode opcode, ZRegister register, Immediate immediate) {
        super(opcode, register, immediate);
    }

    @Override
    public ZInstructionFormat getFormat() {
        return ZInstructionFormat.RI;
    }

    @Override
    public ZRegister getOperand1() {
        return (ZRegister) super.getOperand1();
    }

    @Override
    public Immediate getOperand2() {
        return (Immediate) super.getOperand2();
    }
}
