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
 * JDoc annotations are Copyright 2007-2015 Johannes Kinder
 */

/* 
 * Original code for this class taken from the Java HotSpot VM. 
 * Modified for use with the Jakstab project. All modifications 
 * Copyright 2007-2015 Johannes Kinder <jk@jakstab.org>
 */

package org.jakstab.disasm.z;

import org.jakstab.asm.Immediate;
import org.jakstab.asm.Instruction;
import org.jakstab.asm.Operand;
import org.jakstab.asm.Operation;
import org.jakstab.asm.z.*;
import org.jakstab.disasm.Disassembler;
import org.jakstab.ssl.Architecture;
import org.jakstab.util.BinaryInputBuffer;
import org.jakstab.util.Logger;

public class ZDisassembler implements Disassembler {
	private final static Logger logger = Logger.getLogger(ZDisassembler.class);

	protected final ZInstructionFactory factory;
	protected final BinaryInputBuffer code;
	private int byteIndex;

	//special for debugging
	int disassembled_instruction_count = 0;

	private ZDisassembler(BinaryInputBuffer code, ZInstructionFactory factory) {
		this.code = code;
		this.factory = factory;
	}

	/**
	 * Creates a new disassembler working on the given bytearray.
	 *
	 * @param code Byte array of code to be disassembled.
	 */
	public ZDisassembler(BinaryInputBuffer code) {
		this(code, new ZInstructionFactoryImpl());
	}
	
	@Override
	public final Instruction decodeInstruction(long index) {
		Instruction instruction = null;
		ZOperandsDecoder operandsDecoder = null;
		byteIndex = (int)index; // For 64bit systems, this needs to be fixed

		// Only for debugging!!!
		//byteIndex = 0;

		try {
			//Read opcode
			ZOpcode opcode = new ZOpcode((char) ZOperandsDecoder.readByte(code, byteIndex));
			if (!Architecture.one_byte_table_contains(opcode))
			{
				opcode = new ZOpcode((char) ZOperandsDecoder.readHalfword(code, byteIndex));
				if (!Architecture.two_byte_table_contains(opcode))
				{
					opcode = new ZOpcode((char) ZOperandsDecoder.readFirstAndLastByteOpcode(code, byteIndex));
					if (!Architecture.two_byte_table_contains(opcode)) {
						opcode = new ZOpcode((char) ZOperandsDecoder.read12BitOpcode(code, byteIndex));
						if (!Architecture.two_byte_table_contains(opcode))
							throw new Error("Invalid operation code: " + opcode + ", index: " + index);
					}
				}
			}

			ZInstructionFormat format = Architecture.getFormat(opcode);
			operandsDecoder = format.getOperandsDecoder();

			Operand[] operands = operandsDecoder.decode(code, byteIndex);

			String mnemonic = Architecture.getMnemonic(opcode);
			ZInstructionType type = Architecture.getType(opcode);

			// if there is no explicit length of storage operand
			if (format == ZInstructionFormat.RSb) {
				switch (((ZMask)operands[2]).getValue()) {
					case 0:
						((ZStorageOperand)operands[1]).setLength(0);
						break;
					case 1:
					case 2:
					case 4:
					case 8:
						((ZStorageOperand)operands[1]).setLength(1);
						break;
					case 3:
					case 5:
					case 6:
					case 9:
					case 10:
					case 12:
						((ZStorageOperand)operands[1]).setLength(2);
						break;
					case 7:
					case 11:
					case 13:
					case 14:
						((ZStorageOperand)operands[1]).setLength(3);
						break;
					case 15:
						((ZStorageOperand)operands[1]).setLength(4);
						break;
					default:
						throw new Error("Invalid value of mask!");
				}
			}
			if (format == ZInstructionFormat.RXa && type != ZInstructionType.Branch && !mnemonic.equals("EX") && !mnemonic.equals("LA")) {
				switch (mnemonic) {
					case "IC":
					case "STC":
						((ZStorageOperand)operands[1]).setLength(1);
						break;
					case "AH":
					case "CH":
					case "LH":
					case "MH":
					case "SH":
					case "STH":
						((ZStorageOperand)operands[1]).setLength(2);
						break;
					case "A":
					case "AL":
					case "C":
					case "CL":
					case "D":
					case "L":
					case "M":
					case "N":
					case "O":
					case "S":
					case "SL":
					case "ST":
					case "X":
						((ZStorageOperand)operands[1]).setLength(4);
						break;
					case "CVB":
					case "CVD":
						((ZStorageOperand)operands[1]).setLength(8);
						break;
					default:
						throw new Error("Unknown RX-a instruction! mnemonic = " + mnemonic);
				}
			}
			if (format == ZInstructionFormat.RSa && type != ZInstructionType.Branch) {
				switch (mnemonic) {
					case "CS":
						((ZStorageOperand)operands[1]).setLength(4);
						break;
					case "CDS":
						((ZStorageOperand)operands[1]).setLength(8);
						break;
					case "LM":
					case "STM":
						int r1_num = ((ZRegister) operands[0]).getNumber();
						int r3_num = ((ZRegister) operands[2]).getNumber();
						int length = 4;
						if (r1_num < r3_num)
							length *= r3_num - r1_num + 1;
						if (r1_num > r3_num)
							length *= r3_num + 1 + 15 - r1_num + 1;
						((ZStorageOperand)operands[1]).setLength(length);
						break;
					default:
						throw new Error("Unknown RS-a instruction!");
				}
			}

			Operation operation = null;
			//choose proper operation for arithmetic instruction
			if (type == ZInstructionType.Arithmetic) {
				switch (mnemonic) {
					case "A":
					case "AH":
					case "AR":
						operation = Operation.ADD;
						break;
					case "AL":
					case "ALR":
						operation = Operation.ADDC;
						break;
					case "C":
					case "CH":
					case "CR":
					case "CS":
						operation = Operation.C;
						break;
					case "CL":
					case "CLC":
					case "CLCL":
					case "CLI":
					case "CLM":
					case "CLR":
						operation = Operation.CL;
						break;
					case "M":
					case "MH":
					case "MR":
						operation = Operation.SMUL;
						break;
					case "N":
					case "NC":
					case "NI":
					case "NR":
						operation = Operation.AND;
						break;
					case "O":
					case "OC":
					case "OI":
					case "OR":
						operation = Operation.OR;
						break;
					case "S":
					case "SH":
					case "SR":
						operation = Operation.SUB;
						break;
					case "SL":
					case "SLR":
						operation = Operation.SUBC;
						break;
					case "SLA":
					case "SLDA":
						operation = Operation.SLA;
						break;
					case "SLDL":
					case "SLL":
						operation = Operation.SLL;
						break;
					case "SRA":
					case "SRDA":
						operation = Operation.SRA;
						break;
					case "SRDL":
					case "SRL":
						operation = Operation.SRL;
						break;
					case "X":
					case "XC":
					case "XI":
					case "XR":
						operation = Operation.XOR;
						break;
					default:
						operation = null;
				}
			}

			//choose proper constructor for instruction
			switch (type) {
				case Arithmetic:
					switch (format) {
						case RSa:
						case RSb:
							instruction = factory.newArithmeticInstruction(opcode, operation, operands[0], operands[1], operands[2]);
							break;
						case RSa2:
						case RXa:
						case RXb:
						case RR:
						case RRm:
						case SI:
						case SSa:
						case SSb:
							instruction = factory.newArithmeticInstruction(opcode, operation, operands[0], operands[1]);
							break;
						default:
							throw new Error("Invalid format for arithmetic instruction!");
					}
					break;
				case Branch:
					switch (format) {
						case RSa:
							instruction = factory.newBranchInstruction(opcode, (ZRegister) operands[0],
									(ZStorageOperand) operands[1], (ZRegister) operands[2]);
							break;
						case RXa:
							instruction = factory.newBranchInstruction(opcode, (ZRegister) operands[0], (ZStorageOperand) operands[1]);
							break;
						case RXb:
							instruction = factory.newBranchInstruction(opcode, (ZMask) operands[0], (ZStorageOperand) operands[1]);
							break;
						case RR:
							instruction = factory.newBranchInstruction(opcode, (ZRegister) operands[0], (ZRegister) operands[1]);
							break;
						case RRm:
							instruction = factory.newBranchInstruction(opcode, (ZMask) operands[0], (ZRegister) operands[1]);
							break;
						case RI:
							instruction = factory.newBranchInstruction(opcode, (ZRegister) operands[0], (Immediate) operands[1]);
							break;
						default:
							throw new Error("Invalid format for Branch instruction!");
					}
					break;
				case Load:
					switch (format) {
						case RSa:
							instruction = factory.newLoadInstruction(opcode, (ZRegister) operands[0],
									(ZStorageOperand) operands[1], (ZRegister) operands[2]);
							break;
						case RSb:
							instruction = factory.newLoadInstruction(opcode, (ZRegister) operands[0],
									(ZStorageOperand) operands[1], (ZMask) operands[2]);
							break;
						case RR:
							instruction = factory.newLoadInstruction(opcode, (ZRegister) operands[0], (ZRegister) operands[1]);
							break;
						case RXa:
							instruction = factory.newLoadInstruction(opcode, (ZRegister) operands[0], (ZStorageOperand) operands[1]);
							break;
						default:
							throw new Error("Invalid format for Load instruction!");
					}
					break;
				case Move:
					instruction = factory.newMoveInstruction(opcode, (ZStorageOperand) operands[0], operands[1]);
					break;
				case Store:
					switch (format) {
						case RSa:
							instruction = factory.newStoreInstruction(opcode, (ZRegister) operands[0],
									(ZStorageOperand) operands[1], (ZRegister) operands[2]);
							break;
						case RSb:
							instruction = factory.newStoreInstruction(opcode, (ZRegister) operands[0],
									(ZStorageOperand) operands[1], (ZMask) operands[2]);
							break;
						case RXa:
							instruction = factory.newStoreInstruction(opcode, (ZRegister) operands[0], (ZStorageOperand) operands[1]);
							break;
						default:
							throw new Error("Invalid format for Store instruction!");
					}
					break;
				case General: {
					switch (format) {
						case RSa:
						case RSb:
							instruction = factory.newGeneralInstruction(opcode, operands[0], operands[1], operands[2]);
							break;
						case RSa2:
						case RXa:
						case RXb:
						case RR:
						case RRm:
						case SI:
						case SSa:
						case SSb:
							instruction = factory.newGeneralInstruction(opcode, operands[0], operands[1]);
							break;
						case I:
						case S:
							instruction = factory.newGeneralInstruction(opcode, operands[0]);
							break;
						default:
							throw new Error("Invalid format for general instruction!");
					}
					break;
				}
				default:
					throw new Error("Invalid type of instruction!");
			}

//			switch (format)
//			{
//				case RR:
//					instruction = new ZRRInstruction(opcode, (ZRegister) operands[0], (ZRegister) operands[1]);
//					break;
//				case RRm:
//					instruction = new ZRRmInstruction(opcode, (ZMask) operands[0], (ZRegister) operands[1]);
//					break;
//				case RXa:
//					instruction = new ZRXaInstruction(opcode, (ZRegister) operands[0], (ZStorageOperand) operands[1]);
//					break;
//				case RXb:
//					instruction = new ZRXbInstruction(opcode, (ZMask) operands[0], (ZStorageOperand) operands[1]);
//					break;
//				case RSa:
//					instruction =
//							new ZRSaInstruction(opcode, (ZRegister) operands[0], (ZStorageOperand) operands[1], (ZRegister) operands[2]);
//					break;
//				case RSa2:
//					instruction = new ZRSa2Instruction(opcode, (ZRegister) operands[0], (ZStorageOperand) operands[1]);
//					break;
//				case RSb:
//					instruction =
//							new ZRSbInstruction(opcode, (ZRegister) operands[0], (ZStorageOperand) operands[1], (ZMask) operands[2]);
//					break;
//				case SSa:
//					instruction = new ZSSaInstruction(opcode, (ZStorageOperand) operands[0], (ZStorageOperand) operands[1]);
//					break;
//				case SSb:
//					instruction = new ZSSbInstruction(opcode, (ZStorageOperand) operands[0], (ZStorageOperand) operands[1]);
//					break;
//				case SI:
//					instruction = new ZSIInstruction(opcode, (ZStorageOperand) operands[0], (Immediate) operands[1]);
//					break;
//			}

			//len = instrDecoder.getCurrentIndex();
			//byteIndex = len;
			//byteIndex = instrDecoder.getCurrentIndex();
		} catch (Exception exp) {
			logger.error("Error during disassembly:", exp);
			if (logger.isInfoEnabled())
				exp.printStackTrace();
			return null;
		}
		disassembled_instruction_count++;
		return instruction;
	}

}
