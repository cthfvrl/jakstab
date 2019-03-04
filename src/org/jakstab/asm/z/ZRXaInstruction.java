package org.jakstab.asm.z;

import org.jakstab.asm.Operand;

/**
 * Created by Basil on 04.05.2017.
 */
public class ZRXaInstruction extends ZInstruction {

    public ZRXaInstruction(ZOpcode opcode, ZRegister operand1, ZStorageOperand operand2) {
        super(opcode, operand1, operand2);
    }

    @Override
    public ZInstructionFormat getFormat() {
        return ZInstructionFormat.RXa;
    }

    @Override
    public ZRegister getOperand1() {
        return (ZRegister) super.getOperand1();
    }

    @Override
    public ZStorageOperand getOperand2() {
        return (ZStorageOperand) super.getOperand2();
    }
}
