package org.jakstab.asm.z;

/**
 * Created by Basil on 09.04.2017.
 */
public class ZOpcode {

    private char value;

    public ZOpcode(char value) {
        this.value = value;
    }

    public char getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("%04x", (int) value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof ZOpcode))
            return false;

        ZOpcode opcode = (ZOpcode) obj;
        return this.value == opcode.value;
    }

    @Override
    public int hashCode() {
        return 17 * 31 * value;
    }
}
