package org.jakstab.asm.z;

import org.jakstab.asm.Operand;

/**
 * Created by Basil on 04.05.2017.
 */
public class ZRSbInstruction extends ZInstruction {

    public ZRSbInstruction(ZOpcode opcode, ZRegister operand1, ZStorageOperand operand2, ZMask mask) {
        super(opcode, operand1, operand2, mask);
    }

    @Override
    public ZInstructionFormat getFormat() {
        return ZInstructionFormat.RSb;
    }

    @Override
    public ZRegister getOperand1() { return (ZRegister) super.getOperand1(); }

    @Override
    public ZStorageOperand getOperand2() { return (ZStorageOperand) super.getOperand2(); }

    public ZMask getMask() { return (ZMask) super.getOperand3(); }
}
