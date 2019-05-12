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

import org.jakstab.asm.Immediate;
import org.jakstab.asm.Operand;
import org.jakstab.asm.Operation;

public class ZInstructionFactoryImpl implements ZInstructionFactory {

	/* Branch Instructions */

	public ZInstruction newBranchInstruction(ZOpcode opcode, ZRegister op1, ZStorageOperand op2, ZRegister op3) {
		return new ZBranchInstruction(opcode, op1, op2, op3);
	}

	public ZInstruction newBranchInstruction(ZOpcode opcode, ZRegister op1, ZStorageOperand op2) {
		return new ZBranchInstruction(opcode, op1, op2);
	}

	public ZInstruction newBranchInstruction(ZOpcode opcode, ZMask op1, ZStorageOperand op2) {
		return new ZBranchInstruction(opcode, op1, op2);
	}

	public ZInstruction newBranchInstruction(ZOpcode opcode, ZRegister op1, ZRegister op2) {
		return new ZBranchInstruction(opcode, op1, op2);
	}

	public ZInstruction newBranchInstruction(ZOpcode opcode, ZMask op1, ZRegister op2) {
		return new ZBranchInstruction(opcode, op1, op2);
	}

	public ZInstruction newBranchInstruction(ZOpcode opcode, ZRegister op1, Immediate op2) {
		return new ZBranchInstruction(opcode, op1, op2);
	}

	/* Store Instructions */

	public ZInstruction newStoreInstruction(ZOpcode opcode, ZRegister op1, ZStorageOperand op2, ZRegister op3) {
		return new ZStoreInstruction(opcode, op1, op2, op3);
	}

	public ZInstruction newStoreInstruction(ZOpcode opcode, ZRegister op1, ZStorageOperand op2, ZMask op3) {
		return new ZStoreInstruction(opcode, op1, op2, op3);
	}

	public ZInstruction newStoreInstruction(ZOpcode opcode, ZRegister op1, ZStorageOperand op2) {
		return new ZStoreInstruction(opcode, op1, op2);
	}

	/* Load Instructions */

	public ZInstruction newLoadInstruction(ZOpcode opcode, ZRegister op1, ZStorageOperand op2, ZRegister op3) {
		return new ZLoadInstruction(opcode, op1, op2, op3);
	}

	public ZInstruction newLoadInstruction(ZOpcode opcode, ZRegister op1, ZStorageOperand op2, ZMask op3) {
		return new ZLoadInstruction(opcode, op1, op2, op3);
	}

	public ZInstruction newLoadInstruction(ZOpcode opcode, ZRegister op1, ZRegister op2) {
		return new ZLoadInstruction(opcode, op1, op2);
	}

	public ZInstruction newLoadInstruction(ZOpcode opcode, ZRegister op1, ZStorageOperand op2) {
		return new ZLoadInstruction(opcode, op1, op2);
	}

	/* Move Instructions */

	public ZInstruction newMoveInstruction(ZOpcode opcode, ZStorageOperand op1, Operand op2) {
		return new ZMoveInstruction(opcode, op1, op2);
	}

	/* Arithmetic and Bitvector Logic Instructions */

	public ZInstruction newArithmeticInstruction(ZOpcode opcode, Operation operation, Operand op1, Operand op2, Operand op3) {
		return new ZArithmeticInstruction(opcode, operation, op1, op2, op3);
	}

	public ZInstruction newArithmeticInstruction(ZOpcode opcode, Operation operation, Operand op1, Operand op2) {
		return new ZArithmeticInstruction(opcode, operation, op1, op2);
	}

	/* General Instructions */

	public ZInstruction newGeneralInstruction(ZOpcode opcode, Operand op1, Operand op2, Operand op3) {
		return new ZInstruction(opcode, op1, op2, op3);
	}

	public ZInstruction newGeneralInstruction(ZOpcode opcode, Operand op1, Operand op2) {
		return new ZInstruction(opcode, op1, op2);
	}

	public ZInstruction newGeneralInstruction(ZOpcode opcode, Operand op1) {
		return new ZInstruction(opcode, op1);
	}

}

