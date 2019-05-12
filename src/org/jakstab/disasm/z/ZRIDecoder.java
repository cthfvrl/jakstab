package org.jakstab.disasm.z;

import org.jakstab.asm.DataType;
import org.jakstab.asm.Immediate;
import org.jakstab.asm.Operand;
import org.jakstab.asm.z.ZRegister;
import org.jakstab.util.BinaryInputBuffer;

public class ZRIDecoder extends ZOperandsDecoder {
    @Override
    public Operand[] decode(BinaryInputBuffer bytesArray, int index) {
        index++;
        Operand[] operands = new Operand[2];

        int current_byte = readByte(bytesArray, index);
        operands[0] = new ZRegister(getLeftNibble(current_byte));

        index++;
        operands[1] = new Immediate(readHalfword(bytesArray, index), DataType.INT16);

        return operands;
    }
}
