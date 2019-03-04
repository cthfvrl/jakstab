package org.jakstab.asm.z;

import org.jakstab.asm.Operand;

/**
 * Created by Basil on 04.05.2017.
 */
public class ZSSaInstruction extends ZInstruction {

    public ZSSaInstruction(ZOpcode opcode, ZStorageOperand operand1, ZStorageOperand operand2) {
        super(opcode, operand1, operand2);
    }

    @Override
    public ZInstructionFormat getFormat() {
        return ZInstructionFormat.SSa;
    }

    @Override
    public ZStorageOperand getOperand1() {
        return (ZStorageOperand) super.getOperand1();
    }

    @Override
    public ZStorageOperand getOperand2() {
        return (ZStorageOperand) super.getOperand2();
    }

    public int getLength() {
        return getOperand1().getLength();
    }
}
