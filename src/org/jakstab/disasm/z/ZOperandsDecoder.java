/*
 * Copyright 2002-2003 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *  
 */

/* 
 * Original code for this class taken from the Java HotSpot VM. 
 * Modified for use with the Jakstab project. All modifications 
 * Copyright 2007-2015 Johannes Kinder <jk@jakstab.org>
 */

package org.jakstab.disasm.z;

import org.jakstab.asm.DataType;
import org.jakstab.asm.Immediate;
import org.jakstab.asm.Instruction;
import org.jakstab.asm.Operand;
import org.jakstab.asm.z.ZInstructionFactory;
import org.jakstab.util.BinaryInputBuffer;
import org.jakstab.util.Logger;

public abstract class ZOperandsDecoder {
	private final static Logger logger = Logger.getLogger(ZOperandsDecoder.class);

	public ZOperandsDecoder() {};

	static int readByte(BinaryInputBuffer bytesArray, int index) {
		int ret = 0;
		if (index < bytesArray.getSize()) {
			ret = bytesArray.getByteAt(index);
			ret = ret & 0xff;
		} else {
			throw new ArrayIndexOutOfBoundsException("Disassembler requested byte outside of file area: 0x" + Long.toHexString(index));
		}
		return ret;
	}

	// Read two bytes
	static int readHalfword(BinaryInputBuffer bytesArray, int index) {
		int ret = 0;
		ret = readByte(bytesArray, index) << 8;
		ret |= readByte(bytesArray, index + 1);
		return ret;
	}

	static int readFirstAndLastByteOpcode(BinaryInputBuffer bytesArray, int index) {
		int ret = 0;
		ret = readByte(bytesArray, index) << 8;
		ret |= readByte(bytesArray, index + 5);
		return ret;
	}

	static int read12BitOpcode(BinaryInputBuffer bytesArray, int index) {
		int ret = 0;
		ret = readByte(bytesArray, index) << 8;
		ret |= (readByte(bytesArray, index + 1) & 0x0f) << 4;
		ret >>>= 4;
		return ret;
	}



	protected int getLeftNibble(int b) {
		return (b & 0xf0) >> 4;
	}

	protected int getRightNibble(int b)
	{
		return b & 0xf;
	}

	protected int getRightmostInt12(int halfword) {
		return halfword & 0xfff;
	}

	public Operand[] decode(BinaryInputBuffer bytesArray, int index) {
		return null;
	}

}
