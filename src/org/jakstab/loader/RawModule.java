/*
 * RawModule.java - This file is part of the Jakstab project.
 * Copyright 2007-2015 Johannes Kinder <jk@jakstab.org>
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, see <http://www.gnu.org/licenses/>.
 */
package org.jakstab.loader;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.jakstab.asm.AbsoluteAddress;
import org.jakstab.asm.DummySymbolFinder;
import org.jakstab.asm.SymbolFinder;
import org.jakstab.disasm.Disassembler;
import org.jakstab.disasm.x86.X86Disassembler;
import org.jakstab.disasm.z.ZDisassembler;
import org.jakstab.rtl.expressions.ExpressionFactory;
import org.jakstab.rtl.expressions.RTLMemoryLocation;
import org.jakstab.rtl.expressions.RTLNumber;
import org.jakstab.ssl.Architecture;
import org.jakstab.util.BinaryFileInputBuffer;
import org.jakstab.util.Logger;

/**
 * @author Johannes Kinder
 */
public class RawModule implements ExecutableImage {

    private static final Logger logger = Logger.getLogger(RawModule.class);

    private final BinaryFileInputBuffer inBuf;
    private final BinaryFileInputBuffer code;
    private final AbsoluteAddress baseAddress;
    private Disassembler disassembler;

    public RawModule(File file, Architecture architecture) throws IOException {
        logger.info("Loading image as raw binary...");
        InputStream inStream = new FileInputStream(file);
        inBuf = new BinaryFileInputBuffer(inStream);
        code = new BinaryFileInputBuffer(getByteArray());
        baseAddress = new AbsoluteAddress(0x0);
    }

    @Override
    public Disassembler getDisassembler() {
        if (disassembler == null) {
            //Basil
            /*disassembler = new X86Disassembler(inBuf);*/
            disassembler = new ZDisassembler(code);
        }
        return disassembler;
    }

    @Override
    public AbsoluteAddress getEntryPoint() {
        return baseAddress;
    }

    @Override
    public Set<ExportedSymbol> getExportedSymbols() {
        return Collections.emptySet();
    }

    @Override
    public long getFilePointer(AbsoluteAddress va) {
        return va.getValue() - baseAddress.getValue();
    }

    @Override
    public AbsoluteAddress getMaxAddress() {
        return new AbsoluteAddress(baseAddress.getValue() + inBuf.getSize());
    }

    @Override
    public AbsoluteAddress getMinAddress() {
        return baseAddress;
    }

    @Override
    public SymbolFinder getSymbolFinder() {
        return DummySymbolFinder.getInstance();
    }

    @Override
    public Set<UnresolvedSymbol> getUnresolvedSymbols() {
        return Collections.emptySet();
    }

    @Override
    public AbsoluteAddress getVirtualAddress(long fp) {
        return new AbsoluteAddress(baseAddress.getValue() + fp);
    }

    @Override
    public boolean isCodeArea(AbsoluteAddress va) {
        return true;
    }

    @Override
    public boolean isReadOnly(AbsoluteAddress a) {
        return false;
    }

    @Override
    public RTLNumber readMemoryLocation(RTLMemoryLocation m) throws IOException {
        if (!(m.getAddress() instanceof RTLNumber))
            return null;
        AbsoluteAddress va = new AbsoluteAddress((RTLNumber) m.getAddress());
        long fp = getFilePointer(va);
        assert m.getBitWidth() % 8 == 0 : "Non-byte-aligned memory reference!";
        long val = 0;
        int bytes = m.getBitWidth() / 8;

        code.seek(fp);
        for (int i = 0; i < bytes; i++) {
            val = val | ((long) code.readBYTE()) << ((bytes - i - 1) * 8);
        }
        //Basil
//		for (int i = 0; i < bytes - 1; i++) {
//			val = val | ((long) inBuf.readBYTE()) << (i * 8);
//		}
//		// do not mask the MSB with 0xFF, so we get sign extension for free
//		val = val | (((long)inBuf.readINT8()) << (bytes - 1) * 8);
        //logger.debug("Read constant value " + val + " from address " + m + " (file offset: " + Long.toHexString(fp) + ") in image.");
        return ExpressionFactory.createNumber(val, m.getBitWidth());
    }

    @Override
    public Iterator<AbsoluteAddress> codeBytesIterator() {
        throw new UnsupportedOperationException("Code iteration not yet implemented for " + this.getClass().getSimpleName() + "!");
    }

    @Override
    public byte[] getByteArray() {
        byte[] byte_array = inBuf.getByteArray();
        ArrayList<Byte> code_list = new ArrayList<>();

        long byte_array_length = inBuf.getSize();
        //object module
        //Check if it ends with an END record
        if ((byte_array[(int) byte_array_length - 80] & 0xff) == 0x02 && (byte_array[(int) byte_array_length - 79] & 0xff) == 0xc5
                && (byte_array[(int) byte_array_length - 78] & 0xff) == 0xd5 && (byte_array[(int) byte_array_length - 77] & 0xff) == 0xc4
                && (byte_array[(int) byte_array_length - 76] & 0xff) == 0x40) {
            int current_first_code_byte_addr;
            int current_last_code_byte_addr = 0;
            int current_code_length;
            for (int i = 0; i < byte_array.length; i += 80) {
                if ((byte_array[i + 1] & 0xff) == 0xe3 && (byte_array[i + 2] & 0xff) == 0xe7 && (byte_array[i + 3] & 0xff) == 0xe3) {
                    current_first_code_byte_addr =
                            ((byte_array[i + 5] & 0xff) << 16) | ((byte_array[i + 6] & 0xff) << 8) | (byte_array[i + 7] & 0xff);

                    if (current_last_code_byte_addr + 1 < current_first_code_byte_addr)
                        for (int j = 0; j < current_first_code_byte_addr - current_last_code_byte_addr - 1; j++)
                            code_list.add((byte) 0);

                    current_code_length = ((byte_array[i + 10] & 0xff) << 8) | (byte_array[i + 11] & 0xff);
                    for (int j = 0; j < current_code_length; j++)
                        code_list.add(byte_array[i + 16 + j]);

                    current_last_code_byte_addr = current_first_code_byte_addr + current_code_length - 1;
                }
            }
        }
        //load module
        else {
            int current_displacement = 0;
            int current_record_length;
            int text_record_length = 0;
            while (current_displacement < byte_array_length) {
                switch (byte_array[current_displacement]) {
                    //SYM record
                    case 0x40: {
                        current_record_length = 4 +
                                (((byte_array[current_displacement + 2] & 0xff) << 8) | (byte_array[current_displacement + 3] & 0xff));
                        current_displacement += current_record_length;
                        break;
                    }
                    //CESD record
                    case 0x20: {
                        current_record_length = 8 +
                                (((byte_array[current_displacement + 6] & 0xff) << 8) | (byte_array[current_displacement + 7] & 0xff));
                        current_displacement += current_record_length;
                        break;
                    }
                    //Scatter/Translation record
                    case 0x10: {
                        current_record_length = 4 +
                                (((byte_array[current_displacement + 2] & 0xff) << 8) | (byte_array[current_displacement + 3] & 0xff));
                        current_displacement += current_record_length;
                        break;
                    }
                    //Control record
                    case 0x01:
                    case 0x05:
                    case 0x0d: {
                        text_record_length =
                                ((byte_array[current_displacement + 14] & 0xff) << 8) | (byte_array[current_displacement + 15] & 0xff);

                        current_record_length = 16 +
                                (((byte_array[current_displacement + 4] & 0xff) << 8) | (byte_array[current_displacement + 5] & 0xff));
                        current_displacement += current_record_length;
                        break;
                    }
                    //RLD record
                    case 0x02:
                    case 0x06:
                    case 0x0e: {
                        current_record_length = 16 +
                                (((byte_array[current_displacement + 6] & 0xff) << 8) | (byte_array[current_displacement + 7] & 0xff));
                        current_displacement += current_record_length;
                        break;
                    }
                    //Dictionary record
                    case 0x03:
                    case 0x07:
                    case 0x0f: {
                        //TODO: need to be checked
                        current_record_length = 16 +
                                (((byte_array[current_displacement + 6] & 0xff) << 8) | (byte_array[current_displacement + 7] & 0xff));
                        current_displacement += current_record_length;
                        break;
                    }
                    //CSECT Identification record (record id = 0x80)
                    case -128: {
                        current_record_length = 1 + (byte_array[current_displacement + 1] & 0xff);
                        current_displacement += current_record_length;
                        break;
                    }
                    //Invalid record
                    default: {
                        throw new Error("Invalid load module! Record identification is " + Byte.toString(byte_array[current_displacement]));
                    }
                }

                for (int i = 0; i < text_record_length; i++)
                    code_list.add(byte_array[current_displacement + i]);
                current_displacement += text_record_length;
                text_record_length = 0;
            }
        }

        byte[] code_array = new byte[code_list.size()];
        for (int i = 0; i < code_list.size(); i++)
            code_array[i] = code_list.get(i);

        return code_array;
    }

    @Override
    public boolean isImportArea(AbsoluteAddress va) {
        return false;
    }

}
