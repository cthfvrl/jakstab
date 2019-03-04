package org.jakstab.asm.z;

import org.jakstab.asm.Immediate;
import org.jakstab.asm.Operand;

/**
 * Created by Basil on 04.05.2017.
 */
public class ZSIInstruction extends ZInstruction {

    public ZSIInstruction(ZOpcode opcode, ZStorageOperand storageOperand, Immediate immediate) {
        super(opcode, storageOperand, immediate);
    }

    @Override
    public ZInstructionFormat getFormat() {
        return ZInstructionFormat.SI;
    }

    @Override
    public ZStorageOperand getOperand1() {
        return (ZStorageOperand) super.getOperand1();
    }

    @Override
    public Immediate getOperand2() {
        return (Immediate) super.getOperand2();
    }
}
