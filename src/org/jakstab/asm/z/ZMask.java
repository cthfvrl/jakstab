package org.jakstab.asm.z;

import org.jakstab.asm.Operand;

/**
 * Created by Basil on 07.04.2017.
 */
public class ZMask extends Operand {
    private final int value;

    public ZMask(int value) {
        if (value < 0 || value > 15)
            throw new Error("Invalid value of mask: value =" + value);
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString()
    {
        String result_string = Integer.toBinaryString(value);
        int value_length = result_string.length();
        for (int i = 0; i < 4 - value_length; i++)
            result_string = "0" + result_string;
        return "0b" + result_string;
    }
}
