package org.jakstab.disasm.z;

import org.jakstab.asm.Operand;
import org.jakstab.asm.z.ZRegister;
import org.jakstab.asm.z.ZStorageOperand;
import org.jakstab.util.BinaryInputBuffer;

/**
 * Created by Basil on 12.04.2017.
 */
public class ZRSa2Decoder extends ZOperandsDecoder {
    @Override
    public Operand[] decode(BinaryInputBuffer bytesArray, int index) {

        index++;
        Operand[] operands = new Operand[2];

        operands[0] = new ZRegister(getLeftNibble(readByte(bytesArray, index)));

        index++;
        ZRegister base = new ZRegister(getLeftNibble(readByte(bytesArray, index)));
        long displacement = getRightmostInt12(readHalfword(bytesArray, index));
        operands[1] = new ZStorageOperand(-1, base, null, displacement);

        return operands;
    }
}
