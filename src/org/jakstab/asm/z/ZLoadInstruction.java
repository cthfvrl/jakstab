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

import org.jakstab.asm.MoveInstruction;
import org.jakstab.asm.DataType;
import org.jakstab.asm.Operand;
import org.jakstab.asm.Register;
import org.jakstab.ssl.Architecture;

import java.util.ArrayList;

public class ZLoadInstruction extends ZInstruction
implements MoveInstruction
{
	public ZLoadInstruction(ZOpcode opcode, ZRegister op1, ZStorageOperand op2, ZRegister op3, DataType dataType) {
		super(opcode, op1, op2, op3, null, dataType);
		ZInstructionFormat format = Architecture.getFormat(opcode);
		if (!(format == ZInstructionFormat.RSa))
			throw new Error("Unexpected format of instruction!");
	}

	public ZLoadInstruction(ZOpcode opcode, ZRegister op1, ZStorageOperand op2, ZRegister op3) {
		this(opcode, op1, op2, op3, DataType.UNKNOWN);
	}

	public ZLoadInstruction(ZOpcode opcode, ZRegister op1, ZStorageOperand op2, ZMask op3, DataType dataType) {
		super(opcode, op1, op2, op3, null, dataType);
		ZInstructionFormat format = Architecture.getFormat(opcode);
		if (!(format == ZInstructionFormat.RSb))
			throw new Error("Unexpected format of instruction!");
	}

	public ZLoadInstruction(ZOpcode opcode, ZRegister op1, ZStorageOperand op2, ZMask op3) {
		this(opcode, op1, op2, op3, DataType.UNKNOWN);
	}

	public ZLoadInstruction(ZOpcode opcode, ZRegister op1, ZRegister op2) {
		super(opcode, op1, op2, null, null, DataType.UNKNOWN);
		ZInstructionFormat format = Architecture.getFormat(opcode);
		if (!(format == ZInstructionFormat.RR))
			throw new Error("Unexpected format of instruction!");
	}

	public ZLoadInstruction(ZOpcode opcode, ZRegister op1, ZStorageOperand op2) {
		super(opcode, op1, op2, null, null, DataType.UNKNOWN);
		ZInstructionFormat format = Architecture.getFormat(opcode);
		if (!(format == ZInstructionFormat.RXa))
			throw new Error("Unexpected format of instruction!");
	}

	public ZMask getMask() {
		if (getFormat() != ZInstructionFormat.RSb)
			return null;
		return (ZMask) getOperand3();
	}

	public int getDestinationsCount() {
		if (getFormat() != ZInstructionFormat.RSa)
			return 1;
		return ((ZStorageOperand) getOperand2()).getLength() / 4;
	}

	public ZRegister getLoadDestination()
	{
		if (getFormat() == ZInstructionFormat.RSa){
			if (((Register) getOperand1()).getNumber() == ((Register) getOperand3()).getNumber())
				return (ZRegister) getOperand1();
			else {
				throw new Error("There are several destinations! Please, try to use getLoadDestinations() for this instruction.");
			}
		}
		return (ZRegister) getOperand1();
	}

	public ArrayList<ZRegister> getLoadDestinations()
	{
		ArrayList<ZRegister> destinations = new ArrayList<ZRegister>();
		Register r1 = (Register) getOperand1();
		int r1_num = r1.getNumber();
		if (getFormat() != ZInstructionFormat.RSa) {
			destinations.add(new ZRegister(r1_num));
			return destinations;
		}
		Register r3 = (Register) getOperand3();
		int r3_num = r3.getNumber();
		if (r1_num == r3_num) {
			destinations.add(new ZRegister(r1_num));
			return destinations;
		}
		if (r1.getNumber() < r3.getNumber()) {
			for (int i = r1_num; i <= r3_num; i++)
				destinations.add(new ZRegister(i));
			return destinations;
		}
		for (int i = r1_num; i <= 15; i++)
			destinations.add(new ZRegister(i));
		for (int i = 0; i <= r3_num; i++)
			destinations.add(new ZRegister(i));
		return destinations;
	}

	public Operand getLoadSource()
	{
		return getOperand2();
	}

	@Override
	public Operand getMoveSource()
	{
		return getLoadSource();
	}

	@Override
	public ZRegister getMoveDestination()
	{
		return getLoadDestination();
	}
}
