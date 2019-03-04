package org.jakstab.disasm.z;

import org.jakstab.asm.DataType;
import org.jakstab.asm.Immediate;
import org.jakstab.asm.Operand;
import org.jakstab.asm.z.ZRegister;
import org.jakstab.asm.z.ZStorageOperand;
import org.jakstab.util.BinaryInputBuffer;

/**
 * Created by Basil on 14.05.2017.
 */
public class ZSDecoder extends ZOperandsDecoder {
    @Override
    public Operand[] decode(BinaryInputBuffer bytesArray, int index) {
        index++;
        index++;
        ZRegister base = new ZRegister(getLeftNibble(readByte(bytesArray, index)));
        long displacement = getRightmostInt12(readHalfword(bytesArray, index));
        Operand[] operands = new Operand[] { new ZStorageOperand(0, base, null, displacement) };
        return operands;
    }
}
