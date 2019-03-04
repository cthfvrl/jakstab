package org.jakstab.disasm.z;

import org.jakstab.asm.Operand;
import org.jakstab.asm.z.ZRegister;
import org.jakstab.asm.z.ZStorageOperand;
import org.jakstab.util.BinaryInputBuffer;

/**
 * Created by Basil on 12.04.2017.
 */
public class ZSSaDecoder extends ZOperandsDecoder {
    @Override
    public Operand[] decode(BinaryInputBuffer bytesArray, int index) {

        index++;
        Operand[] operands = new Operand[2];

        int length = readByte(bytesArray, index) + 1;

        index++;
        ZRegister base1 = new ZRegister(getLeftNibble(readByte(bytesArray, index)));
        long displacement1 = getRightmostInt12(readHalfword(bytesArray, index));
        operands[0] = new ZStorageOperand(length, base1, null, displacement1);

        index++;
        index++;
        ZRegister base2 = new ZRegister(getLeftNibble(readByte(bytesArray, index)));
        long displacement2 = getRightmostInt12(readHalfword(bytesArray, index));
        operands[1] = new ZStorageOperand(length, base2, null, displacement2);

        return operands;
    }
}
