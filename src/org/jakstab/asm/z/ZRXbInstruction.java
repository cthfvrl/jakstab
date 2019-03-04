package org.jakstab.asm.z;

import org.jakstab.asm.Operand;

/**
 * Created by Basil on 04.05.2017.
 */
public class ZRXbInstruction extends ZInstruction {

    public ZRXbInstruction(ZOpcode opcode, ZMask mask, ZStorageOperand branch_target) {
        super(opcode, mask, branch_target);
    }

    @Override
    public ZInstructionFormat getFormat() {
        return ZInstructionFormat.RXb;
    }

    public ZMask getMask() {
        return (ZMask) getOperand1();
    }

    @Override
    public ZStorageOperand getOperand2() {
        return (ZStorageOperand) super.getOperand2();
    }
}
