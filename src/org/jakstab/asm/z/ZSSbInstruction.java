package org.jakstab.asm.z;

/**
 * Created by Basil on 04.05.2017.
 */
public class ZSSbInstruction extends ZInstruction {

    public ZSSbInstruction(ZOpcode opcode, ZStorageOperand operand1, ZStorageOperand operand2) {
        super(opcode, operand1, operand2);
    }

    @Override
    public ZInstructionFormat getFormat() {
        return ZInstructionFormat.SSb;
    }

    @Override
    public ZStorageOperand getOperand1() {
        return (ZStorageOperand) super.getOperand1();
    }

    @Override
    public ZStorageOperand getOperand2() {
        return (ZStorageOperand) super.getOperand2();
    }

    public int getLength1() {
        return getOperand1().getLength();
    }

    public int getLength2() {
        return getOperand2().getLength();
    }
}
