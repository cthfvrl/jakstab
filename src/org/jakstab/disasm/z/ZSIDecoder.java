package org.jakstab.disasm.z;

import org.jakstab.asm.DataType;
import org.jakstab.asm.Immediate;
import org.jakstab.asm.Operand;
import org.jakstab.asm.z.ZRegister;
import org.jakstab.asm.z.ZStorageOperand;
import org.jakstab.util.BinaryInputBuffer;

/**
 * Created by Basil on 12.04.2017.
 */
public class ZSIDecoder extends ZOperandsDecoder {
    @Override
    public Operand[] decode(BinaryInputBuffer bytesArray, int index) {

        index++;
        Operand[] operands = new Operand[2];

        operands[1] = new Immediate(readByte(bytesArray, index), DataType.INT8);

        index++;
        ZRegister base = new ZRegister(getLeftNibble(readByte(bytesArray, index)));
        long displacement = getRightmostInt12(readHalfword(bytesArray, index));
        operands[0] = new ZStorageOperand(0, base, null, displacement);

        return operands;
    }
}
