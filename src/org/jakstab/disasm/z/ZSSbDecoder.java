package org.jakstab.disasm.z;

import org.jakstab.asm.Operand;
import org.jakstab.asm.z.ZRegister;
import org.jakstab.asm.z.ZStorageOperand;
import org.jakstab.util.BinaryInputBuffer;

/**
 * Created by Basil on 12.04.2017.
 */
public class ZSSbDecoder extends ZOperandsDecoder {
    @Override
    public Operand[] decode(BinaryInputBuffer bytesArray, int index) {

        index++;
        Operand[] operands = new Operand[2];

        int current_byte = readByte(bytesArray, index);
        int length1 = getLeftNibble(current_byte) + 1;
        int length2 = getRightNibble(current_byte) + 1;

        index++;
        ZRegister base1 = new ZRegister(getLeftNibble(readByte(bytesArray, index)));
        long displacement1 = getRightmostInt12(readHalfword(bytesArray, index));
        operands[0] = new ZStorageOperand(length1, base1, null, displacement1);

        index++;
        index++;
        ZRegister base2 = new ZRegister(getLeftNibble(readByte(bytesArray, index)));
        long displacement2 = getRightmostInt12(readHalfword(bytesArray, index));
        operands[1] = new ZStorageOperand(length2, base2, null, displacement2);

        return operands;
    }
}
