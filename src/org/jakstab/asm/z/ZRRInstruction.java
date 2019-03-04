package org.jakstab.asm.z;

/**
 * Created by Basil on 04.05.2017.
 */
public class ZRRInstruction extends ZInstruction {

    public ZRRInstruction(ZOpcode opcode, ZRegister operand1, ZRegister operand2) { super(opcode, operand1, operand2); }

    @Override
    public ZInstructionFormat getFormat() {
        return ZInstructionFormat.RR;
    }

    @Override
    public ZRegister getOperand1() { return (ZRegister) super.getOperand1(); }

    @Override
    public ZRegister getOperand2() { return (ZRegister) super.getOperand2(); }
}
