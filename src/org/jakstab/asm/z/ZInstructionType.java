package org.jakstab.asm.z;

/**
 * Created by Basil on 12.04.2017.
 */
public enum ZInstructionType {
    General, Arithmetic, Branch, Load, Move, Store;

    @Override
    public String toString() { return this.name(); }

    public static ZInstructionType parseInstructionType(String name) {
        for (ZInstructionType type : ZInstructionType.values())
            if (type.toString().equals(name))
                return type;
        throw new Error("There is no such instruction type: " + name);
    }
}
