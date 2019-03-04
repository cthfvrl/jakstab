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

package org.jakstab.asm.z;

import org.jakstab.asm.*;
import org.jakstab.rtl.Context;
import org.jakstab.rtl.expressions.Operator;
import org.jakstab.ssl.Architecture;

public class ZInstruction extends AbstractInstruction
implements Instruction, MemoryInstruction, Cloneable {

	final private ZOpcode opCode;
	final private DataType dataType; //RTL dataType
	private Operand[] operands;
	private String description;

	public String toString(long currentPc, SymbolFinder symFinder) {
		if (description == null)
			description = initDescription(currentPc, symFinder);
		return description;
	}

	public ZInstruction(ZOpcode opcode, Operand op1, Operand op2, Operand op3, Operand op4, DataType dataType) {
		super(Architecture.getMnemonic(opcode));
		ZInstructionFormat format = Architecture.getFormat(opcode);
		this.opCode = opcode;
		this.operands = new Operand[] {op1, op2, op3, op4};
		for (int i = 0; i < 4; i++){
			if (operands[i] == null) {
				if (format.getOperand_type(i) != null)
					throw new Error("Invalid type of operand" + i);
			}
			else
				if (operands[i].getClass() != format.getOperand_type(i))
					throw new Error("Invalid type of operand" + i);
		}
		this.dataType = dataType;
		// initialized when needed for the first time
		description = null;
	}

	public ZInstruction(ZOpcode opcode, Operand op1, Operand op2, Operand op3, Operand op4) {
		this(opcode, op1, op2, op3, op4, DataType.UNKNOWN);
	}

	public ZInstruction(ZOpcode opcode, Operand op1, Operand op2, Operand op3) {
		this(opcode, op1, op2, op3, null, DataType.UNKNOWN);
	}

	public ZInstruction(ZOpcode opcode, Operand op1, Operand op2) {
		this(opcode, op1, op2, null, null, DataType.UNKNOWN);
	}

	public ZInstruction(ZOpcode opcode, Operand op1) {
		this(opcode, op1, null, null, null, DataType.UNKNOWN);
	}
	
	public ZInstructionFormat getFormat() {
		return Architecture.getFormat(this.opCode);
	}
	
	public int getSize() {
		return getFormat().getSize();
	}

	@Override
	public int getOperandCount() {
		return getFormat().getOperandCount();
	}

	public Class<? extends Operand> getOperandType(int i) {
		return getFormat().getOperand_type(i);
	}

	@Override
	public Operand getOperand(int i) {
		return operands[i];
	}

	public Operand getOperand1() {
		return operands[0];
	}

	public Operand getOperand2() {
		return operands[1];
	}

	public Operand getOperand3() {
		return operands[2];
	}

	public Operand getOperand4() {
		return operands[3];
	}

	public ZOpcode getOpCode() { return opCode; }

	public String getMnemonic() { return Architecture.getMnemonic(opCode); }

	public ZInstructionType getType() { return Architecture.getType(opCode); }

	public DataType getDataType() {
		return dataType;
	}
	
	protected String initDescription(long currentPc, SymbolFinder symFinder) {
		StringBuffer buf = new StringBuffer();
		buf.append(getName());

		for (int i = 5; i > getMnemonic().length(); i--)
			buf.append(" ");
		if (operands[0] != null) {
			//buf.append("(");
			//buf.append(operands[2].getClass().getSimpleName());
			//buf.append(")");
			buf.append(operands[0].toString(currentPc, symFinder));
		}
		if (operands[2] != null) {
			//buf.append("(");
			//buf.append(operands[2].getClass().getSimpleName());
			//buf.append(")");
			buf.append(comma);
			buf.append(operands[2].toString(currentPc, symFinder));
		}
		if (operands[1] != null) {
			//buf.append("(");
			//buf.append(operands[1].getClass().getSimpleName());
			//buf.append(")");
			buf.append(comma);
			buf.append(operands[1].toString(currentPc, symFinder));
		}
		if(operands[3] != null) {
			//buf.append("(");
			//buf.append(operands[0].getClass().getSimpleName());
			//buf.append(")");
			buf.append(comma);
			buf.append(operands[3].toString(currentPc, symFinder));
		}
		return buf.toString();
	}

	protected static String comma = ",";
	protected static String spaces = "\t";

	@Override
	public Instruction evaluate(Context ctx) {
		
		boolean changed = false;
		Operand[] evaledOperands = new Operand[operands.length];
		for (int i = 0; i < getOperandCount(); i++) {
			evaledOperands[i] = operands[i].evaluate(ctx);
			changed |= evaledOperands[i] != operands[i];				
		}
		if (!changed) 
			return this;
		else {
			ZInstruction inst = null;
			try {
				inst = (ZInstruction) super.clone();
				inst.operands = new Operand[inst.operands.length];
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException(e);
			}
			System.arraycopy(evaledOperands, 0, inst.operands, 0, inst.operands.length);
			return inst;
		}
	}

}
