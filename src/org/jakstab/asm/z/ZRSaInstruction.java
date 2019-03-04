package org.jakstab.asm.z;

/**
 * Created by Basil on 04.05.2017.
 */
public class ZRSaInstruction extends ZInstruction {

    public ZRSaInstruction(ZOpcode opcode, ZRegister operand1, ZStorageOperand operand2, ZRegister operand3) {
        super(opcode, operand1, operand2, operand3);
    }

    @Override
    public ZInstructionFormat getFormat() {
        return ZInstructionFormat.RSa;
    }

    @Override
    public ZRegister getOperand1() { return (ZRegister) super.getOperand1(); }

    @Override
    public ZStorageOperand getOperand2() { return (ZStorageOperand) super.getOperand2(); }

    @Override
    public ZRegister getOperand3() { return (ZRegister) super.getOperand3(); }
}