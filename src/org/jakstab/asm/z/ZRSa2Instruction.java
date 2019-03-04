package org.jakstab.asm.z;

import org.jakstab.asm.SymbolFinder;

/**
 * Created by Basil on 04.05.2017.
 */

//special for Shift instructions
public class ZRSa2Instruction extends ZInstruction {

    public ZRSa2Instruction(ZOpcode opcode, ZRegister operand1, ZStorageOperand operand2) {
        super(opcode, operand1, operand2);
    }

    @Override
    public ZInstructionFormat getFormat() {
        return ZInstructionFormat.RSa2;
    }

    @Override
    public ZRegister getOperand1() { return (ZRegister) super.getOperand1(); }

    @Override
    public ZStorageOperand getOperand2() { return (ZStorageOperand) super.getOperand2(); }

    @Override
    protected String initDescription(long currentPc, SymbolFinder symFinder) {
        StringBuffer buf = new StringBuffer();
        buf.append(getName());

        for (int i = 5; i > getMnemonic().length(); i--)
            buf.append(" ");

        buf.append(this.getOperand1().toString(currentPc, symFinder));
        buf.append(comma);
        buf.append(Long.toHexString(this.getOperand2().getDisplacement()));

        return buf.toString();
    }

}
