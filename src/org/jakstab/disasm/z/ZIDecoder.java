package org.jakstab.disasm.z;

import org.jakstab.asm.DataType;
import org.jakstab.asm.Immediate;
import org.jakstab.asm.Operand;
import org.jakstab.asm.z.ZRegister;
import org.jakstab.util.BinaryInputBuffer;

/**
 * Created by Basil on 14.05.2017.
 */
public class ZIDecoder extends ZOperandsDecoder {
    @Override
    public Operand[] decode(BinaryInputBuffer bytesArray, int index) {
        index++;
        Operand[] operands = new Operand[] { new Immediate(readByte(bytesArray, index), DataType.INT8) };
        return operands;
    }
}
