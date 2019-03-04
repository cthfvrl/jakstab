package org.jakstab.disasm.z;

import org.jakstab.asm.Operand;
import org.jakstab.asm.z.ZRegister;
import org.jakstab.util.BinaryInputBuffer;

/**
 * Created by Basil on 12.04.2017.
 */
public class ZRRDecoder extends ZOperandsDecoder {
    @Override
    public Operand[] decode(BinaryInputBuffer bytesArray, int index) {
        index++;
        Operand[] operands = new Operand[2];
        int current_byte = readByte(bytesArray, index);
        operands[0] = new ZRegister(getLeftNibble(current_byte));
        operands[1] = new ZRegister(getRightNibble(current_byte));
        return operands;
    }
}
