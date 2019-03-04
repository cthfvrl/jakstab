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
import org.jakstab.asm.MemoryOperand;
import org.jakstab.asm.SymbolFinder;

/**
 *  Represents a memory operand pointed to by Base Index Displacement on zArchitecture.
 *  May actually be an absolute address if base and index are null.
 */
public class ZStorageOperand extends MemoryOperand {

	private int length;

	public ZStorageOperand(DataType dataType, int length, ZRegister base, ZRegister index, long disp) {
		super(dataType, base, index, disp, 1);
		this.length = length;
	}

	public ZStorageOperand(int length, ZRegister base, ZRegister index, long disp) {
		this(DataType.UNKNOWN, length, base, index, disp);
	}

	public ZStorageOperand(int length, ZRegister base, long disp) {
		this(DataType.UNKNOWN, length, base, null, disp);
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	/* (non-Javadoc)
         * @see org.jakstab.asm.Operand#toString(long, org.jakstab.asm.SymbolFinder)
         */
	@Override
	public String toString(long currentPc, SymbolFinder symFinder) {
		if (getBase() == null && getIndex() == null)
			return symFinder.getSymbolFor(getDisplacement());
		else return toString();
	}

	@Override
	public String toString() {

		StringBuffer buf = new StringBuffer();
		org.jakstab.asm.Register base = getBase();
		org.jakstab.asm.Register index = getIndex();
		int scaleVal = getScale();

		long disp = getDisplacement();
		if (disp != 0 || ((base == null || base.getNumber() == 0) && (index == null || index.getNumber() == 0))) {
			buf.append("0x" + Long.toHexString(disp).toUpperCase());
			//buf.append("" + disp);
		} 

		if( (base != null && base.getNumber() != 0) || (index != null && index.getNumber() != 0) || (scaleVal > 1) )
			buf.append('(');

		if (index != null && index.getNumber() != 0)
			buf.append(index.toString());

		if ((index != null && base != null && base.getNumber() != 0) || scaleVal > 1) buf.append(',');
		if (base != null && base.getNumber() != 0) buf.append(base.toString());
		if (scaleVal > 1) buf.append(',' + Integer.toString(scaleVal));

		if( (base != null && base.getNumber() != 0) || (index != null && index.getNumber() != 0) || (scaleVal > 1) )
			buf.append(')');

		return buf.toString();
	}
}
