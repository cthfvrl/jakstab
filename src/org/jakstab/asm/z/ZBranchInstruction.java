/*
 * Copyright 2003 Sun Microsystems, Inc.  All Rights Reserved.
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

package org.jakstab.asm.z;

import org.jakstab.asm.*;
import org.jakstab.ssl.Architecture;

public class ZBranchInstruction extends ZInstruction
implements BranchInstruction {

	public ZBranchInstruction(ZOpcode opcode, ZRegister op1, ZStorageOperand op2, ZRegister op3) {
		super(opcode, op1, op2, op3);
		ZInstructionFormat format = Architecture.getFormat(opcode);
		if (!(format == ZInstructionFormat.RSa))
			throw new Error("Unexpected format of instruction!");
	}

	public ZBranchInstruction(ZOpcode opcode, ZRegister op1, ZStorageOperand op2) {
		super(opcode, op1, op2);
		ZInstructionFormat format = Architecture.getFormat(opcode);
		if (!(format == ZInstructionFormat.RXa))
			throw new Error("Unexpected format of instruction!");
	}

	public ZBranchInstruction(ZOpcode opcode, ZMask op1, ZStorageOperand op2) {
		super(opcode, op1, op2);
		ZInstructionFormat format = Architecture.getFormat(opcode);
		if (!(format == ZInstructionFormat.RXb))
			throw new Error("Unexpected format of instruction!");
	}

	public ZBranchInstruction(ZOpcode opcode, ZRegister op1, ZRegister op2)
	{
		super(opcode, op1, op2);
		ZInstructionFormat format = Architecture.getFormat(opcode);
		if (!(format == ZInstructionFormat.RR))
			throw new Error("Unexpected format of instruction!");
	}

	public ZBranchInstruction(ZOpcode opcode, ZMask op1, ZRegister op2)
	{
		super(opcode, op1, op2);
		ZInstructionFormat format = Architecture.getFormat(opcode);
		if (!(format == ZInstructionFormat.RRm))
			throw new Error("Unexpected format of instruction!");
	}

	public ZMask getMask() {
		if (getFormat() == ZInstructionFormat.RXb || getFormat() == ZInstructionFormat.RRm)
			return (ZMask) getOperand1();
		return null;
	}

	@Override
	public Operand getBranchDestination() {
		return getOperand2();
	}

	//TODO
	@Override
	public boolean isConditional() {
		return true;
	}

	@Override
	public boolean isIndirect() { return true; }

	@Override
	public boolean isLeaf() {
		return false;
	}

}
