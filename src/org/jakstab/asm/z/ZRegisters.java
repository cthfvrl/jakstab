/*
 * Copyright 2001 Sun Microsystems, Inc.  All Rights Reserved.
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

import org.jakstab.util.Logger;

public class ZRegisters {

	private final static Logger logger = Logger.getLogger(ZRegisters.class);

	public static final int NUM_REGISTERS = 16;

	public static final ZRegister R0;
	public static final ZRegister R1;
	public static final ZRegister R2;
	public static final ZRegister R3;
	public static final ZRegister R4;
	public static final ZRegister R5;
	public static final ZRegister R6;
	public static final ZRegister R7;
	public static final ZRegister R8;
	public static final ZRegister R9;
	public static final ZRegister R10;
	public static final ZRegister R11;
	public static final ZRegister R12;
	public static final ZRegister R13;
	public static final ZRegister R14;
	public static final ZRegister R15;

	private static ZRegister registers32[];

	static {
		R0 = new ZRegister(0);
		R1 = new ZRegister(1);
		R2 = new ZRegister(2);
		R3 = new ZRegister(3);
		R4 = new ZRegister(4);
		R5 = new ZRegister(5);
		R6 = new ZRegister(6);
		R7 = new ZRegister(7);
		R8 = new ZRegister(8);
		R9 = new ZRegister(9);
		R10 = new ZRegister(10);
		R11 = new ZRegister(11);
		R12 = new ZRegister(12);
		R13 = new ZRegister(13);
		R14 = new ZRegister(14);
		R15 = new ZRegister(15);


		registers32 = (new ZRegister[] {
				R0, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15
		});
	}

	public static int getNumberOfRegisters() {
		return NUM_REGISTERS;
	}

	public static ZRegister getRegister32(int regNum) {
		if (regNum < 0 || regNum >= NUM_REGISTERS) {
			logger.error("Invalid integer register number!");
			return null;
		}
		return registers32[regNum];
	}

	/**
	 * Returns the name of the 32bit register with the given code number.
	 */
	public static String getRegisterName(int regNum) {
		if (regNum < 0 || regNum >= NUM_REGISTERS) {
			logger.error("Invalid integer register number!");
			return null;
		}
		return registers32[regNum].toString();
	}
}
