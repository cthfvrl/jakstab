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

import org.jakstab.asm.DataType;
import org.jakstab.asm.Immediate;
import org.jakstab.asm.MoveInstruction;
import org.jakstab.asm.Operand;
import org.jakstab.ssl.Architecture;

public class ZMoveInstruction extends ZInstruction
implements MoveInstruction {

	public ZMoveInstruction(ZOpcode opcode, ZStorageOperand op1, Operand op2, DataType dataType) {
		super(opcode, op1, op2, null, null, dataType);
		ZInstructionFormat format = Architecture.getFormat(opcode);
		if (!(format == ZInstructionFormat.SSa || format == ZInstructionFormat.SSb || format == ZInstructionFormat.SI))
			throw new Error("Unexpected format of instruction!");
	}

	public ZMoveInstruction(ZOpcode opcode, ZStorageOperand op1, Operand op2) {
		this(opcode, op1, op2, DataType.UNKNOWN);
	}

	@Override
	public ZStorageOperand getMoveDestination() {
		return (ZStorageOperand) getOperand1();
	}

	@Override
	public Operand getMoveSource() {
		return getOperand2();
	}
}
