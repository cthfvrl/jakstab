package org.jakstab.asm.z;

import org.jakstab.asm.DataType;
import org.jakstab.asm.Immediate;
import org.jakstab.asm.Operand;
import org.jakstab.disasm.z.*;

import java.util.ArrayList;

/**
 * Created by Basil on 06.04.2017.
 */
public enum ZInstructionFormat {
    RR(2, 2, (new ZRegister(-1)).getClass(), (new ZRegister(-1)).getClass(), null, null,
            new ZRRDecoder()),
    RSa(4, 3, (new ZRegister(-1)).getClass(), (new ZStorageOperand(-1, null, -1)).getClass(),
            (new ZRegister(-1)).getClass(), null, new ZRSaDecoder()),
    RSb(4, 3, (new ZRegister(-1)).getClass(), (new ZStorageOperand(-1, null, -1)).getClass(),
            (new ZMask(0)).getClass(), null, new ZRSbDecoder()),
    RXa(4, 2, (new ZRegister(-1)).getClass(), (new ZStorageOperand(-1, null, -1)).getClass(), null,
        null, new ZRXaDecoder()),
    RXb(4, 2, (new ZMask(0)).getClass(), (new ZStorageOperand(-1, null, -1)).getClass(), null, null,
            new ZRXbDecoder()),
    SI(4, 2, (new ZStorageOperand(-1, null, -1)).getClass(), (new Immediate(-1, null)).getClass(),
            null, null, new ZSIDecoder()),
    SSa(6, 2, (new ZStorageOperand(-1, null, -1)).getClass(),
            (new ZStorageOperand(-1, null, -1)).getClass(), null, null, new ZSSaDecoder()),
    SSb(6, 2, (new ZStorageOperand(-1, null, -1)).getClass(),
            (new ZStorageOperand(-1, null, -1)).getClass(), null, null, new ZSSbDecoder()),
    I(2, 1, (new Immediate(-1, DataType.INT8)).getClass(), null, null, null, new ZIDecoder()),
    S(4, 1, (new ZStorageOperand(-1, null, null, -1)).getClass(), null, null, null, new ZSDecoder()),
    // Special for BCR instruction
    RRm(2, 2, (new ZMask(0)).getClass(), (new ZRegister(-1)).getClass(), null, null, new ZRRmDecoder()),
    // Special for SLA, SLDA, SLDL, SLL, SRA, SRDA, SRDL, SRL instructions
    RSa2(4, 2, (new ZRegister(-1)).getClass(), (new ZStorageOperand(-1, null, -1)).getClass(),
            null, null, new ZRSa2Decoder()),
    RI(4, 2, (new ZRegister(-1)).getClass(), (new Immediate(-1, DataType.INT16)).getClass(), null, null, new ZRIDecoder());


    private int size;
    private int operandCount;
    private ArrayList<Class<? extends Operand>> operands_type = new ArrayList<Class<? extends Operand>>();
    private ZOperandsDecoder operandsDecoder;

    ZInstructionFormat(int size, int operandCount, Class<? extends Operand> op1_t, Class<? extends Operand> op2_t,
                       Class<? extends Operand> op3_t, Class<? extends Operand> op4_t, ZOperandsDecoder operandsDecoder) {
        this.size = size;
        this.operandCount = operandCount;
        this.operands_type.add(op1_t);
        this.operands_type.add(op2_t);
        this.operands_type.add(op3_t);
        this.operands_type.add(op4_t);
        this.operandsDecoder = operandsDecoder;
    }

    public int getSize() {
        return size;
    }

    public int getOperandCount() {
        return operandCount;
    }

    public Class<? extends Operand> getOperand1_type() {
        return operands_type.get(0);
    }

    public Class<? extends Operand> getOperand2_type() {
        return operands_type.get(1);
    }

    public Class<? extends Operand> getOperand3_type() {
        return operands_type.get(2);
    }

    public Class<? extends Operand> getOperand4_type() { return operands_type.get(3); }

    public Class<? extends Operand> getOperand_type(int i) {
        if (operands_type.get(i) == null)
            return null;
        return operands_type.get(i); }

    public ZOperandsDecoder getOperandsDecoder() { return operandsDecoder;  }

    @Override
    public String toString() {
        return this.name();
    }

    public static ZInstructionFormat parseInstructionFormat(String name) {
        for (ZInstructionFormat format : ZInstructionFormat.values())
            if (format.toString().equals(name))
                return format;
        throw new Error("There is no such instruction format: " + name);
    }
}
