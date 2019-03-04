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

import org.jakstab.asm.ArithmeticInstruction;
import org.jakstab.asm.Operand;
import org.jakstab.asm.Operation;
import org.jakstab.ssl.Architecture;

public class ZArithmeticInstruction extends ZInstruction
implements ArithmeticInstruction {
	final private Operation operation; //RTL operation

	public ZArithmeticInstruction(ZOpcode opcode, Operation operation, Operand op1, Operand op2, Operand op3) {
		super(opcode, op1, op2, op3);
		this.operation = operation;
		ZInstructionFormat format = Architecture.getFormat(opcode);
		if (!(format == ZInstructionFormat.RSa || format == ZInstructionFormat.RXa || format == ZInstructionFormat.RR  ||
				format == ZInstructionFormat.RSb || format == ZInstructionFormat.SI || format == ZInstructionFormat.SSa ||
				format == ZInstructionFormat.SSb || format == ZInstructionFormat.RSa2))
			throw new Error("Unexpected format of instruction!");
	}

	public ZArithmeticInstruction(ZOpcode opcode, Operation operation, Operand op1, Operand op2) {
		this(opcode, operation, op1, op2, null);
	}

	public ZMask getMask() {
		if (getFormat() != ZInstructionFormat.RSb)
			return null;
		return (ZMask) getOperand3();
	}

	public Operand getDestination() {
		return getOperand1();
	}
	
	public Operation getOperation() {
		return operation;
	}

}
