package org.jakstab.disasm.z;

import org.jakstab.asm.Operand;
import org.jakstab.asm.z.ZRegister;
import org.jakstab.asm.z.ZStorageOperand;
import org.jakstab.util.BinaryInputBuffer;

/**
 * Created by Basil on 12.04.2017.
 */
public class ZRSaDecoder extends ZOperandsDecoder {
    @Override
    public Operand[] decode(BinaryInputBuffer bytesArray, int index) {

        index++;
        Operand[] operands = new Operand[3];

        int current_byte = readByte(bytesArray, index);
        operands[0] = new ZRegister(getLeftNibble(current_byte));
        operands[2] = new ZRegister(getRightNibble(current_byte));

        index++;
        ZRegister base = new ZRegister(getLeftNibble(readByte(bytesArray, index)));
        long displacement = getRightmostInt12(readHalfword(bytesArray, index));
        operands[1] = new ZStorageOperand(0, base, null, displacement);

        return operands;
    }
}
