package org.jakstab.translator;

import org.jakstab.asm.Immediate;
import org.jakstab.asm.Register;
import org.jakstab.asm.z.*;
import org.jakstab.rtl.expressions.*;
import org.jakstab.rtl.statements.*;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Basil on 17.04.2017.
 */
public class Translator {

    private static final RTLNumber[] rtl_number = new RTLNumber[64];

    static {
        for (int i = 0; i < 64; i++)
            rtl_number[i] = ExpressionFactory.createNumber(i);
    }

    private static final RTLNumber[] rtl_number_8bit = new RTLNumber[64];

    static {
        for (int i = 0; i < 64; i++) {
            rtl_number_8bit[i] = ExpressionFactory.createNumber(i);
            rtl_number_8bit[i] =
                    rtl_number_8bit[i].bitExtract(rtl_number_8bit[i].getBitWidth() - 8, rtl_number_8bit[i].getBitWidth() - 1);
        }
    }

    private static final RTLNumber minus_one = ExpressionFactory.createNumber(-1);
    private static int getmain_storage_count = 0;

//    private static final RTLVariable cf = ExpressionFactory.createVariable("CF", 1);
//    private static final RTLVariable of = ExpressionFactory.createVariable("OF", 1);
//    private static final RTLVariable sf = ExpressionFactory.createVariable("SF", 1);
//    private static final RTLVariable zf = ExpressionFactory.createVariable("ZF", 1);

    //zero flag
    private static final RTLVariable zf = ExpressionFactory.createVariable("ZF", 1);
    //negative flag
    private static final RTLVariable nf = ExpressionFactory.createVariable("NF", 1);
    //positive flag
    private static final RTLVariable pf = ExpressionFactory.createVariable("PF", 1);
    //overflow flag
    private static final RTLVariable of = ExpressionFactory.createVariable("OF", 1);

    //Program Status Word
    private static final RTLVariable psw = ExpressionFactory.createVariable("PSW", 128);
    //Condition Code
    private static final RTLBitRange cc = ExpressionFactory.createBitRange(psw, rtl_number[18], rtl_number[19]);
    private static final RTLNumber[] cc_value = new RTLNumber[]
            {
                    ExpressionFactory.createNumber(0, 2),
                    ExpressionFactory.createNumber(1, 2),
                    ExpressionFactory.createNumber(-2, 2),
                    ExpressionFactory.createNumber(-1, 2)
            };

    public static final RTLExpression TRUE = ExpressionFactory.TRUE;
    public static final RTLExpression FALSE = ExpressionFactory.FALSE;

    private static final int pc_bit_width = ExpressionFactory.pc.getBitWidth();

    public static StatementSequence translate(ZInstruction instruction) {
        StatementSequence statementSequence;

//        ZInstructionType type = instruction.getType();
//        switch (type) {
//            case Arithmetic:
//                statementSequence = translateArithmetic((ZArithmeticInstruction) instruction);
//                break;
//            case Branch:
//                statementSequence = translateBranch((ZBranchInstruction) instruction);
//                break;
//            case Load:
//                statementSequence = translateLoad((ZLoadInstruction) instruction);
//                break;
//            case Move:
//                statementSequence = translateMove((ZMoveInstruction) instruction);
//                break;
//            case Store:
//                statementSequence = translateStore((ZStoreInstruction) instruction);
//                break;
//            default:
//                statementSequence = translateGeneral(instruction);
//        }

        switch (instruction.getFormat()) {
            case RI:
                statementSequence = translateRI(instruction);
                break;
            case RR:
                statementSequence = translateRR(instruction);
                break;
            case RRm:
                statementSequence = translateRRm(instruction);
                break;
            case RXb:
                statementSequence = translateRXb(instruction);
                break;
            case RXa:
                statementSequence = translateRXa(instruction);
                break;
            case RSa:
                statementSequence = translateRSa(instruction);
                break;
            case SSa:
                statementSequence = translateSSa(instruction);
                break;
            case SSb:
                statementSequence = translateSSb(instruction);
                break;
            case RSb:
                statementSequence = translateRSb(instruction);
                break;
            case SI:
                statementSequence = translateSI(instruction);
                break;
            case RSa2:
                statementSequence = translateRSa2(instruction);
                break;
            case S:
                statementSequence = translateS(instruction);
                break;
            case I:
                statementSequence = translateI(instruction);
                break;
            default:
                statementSequence = null;
        }

        return statementSequence;
    }

    //set condition code
    private static void setCC(StatementSequence instrBody) {
        instrBody.addLast(
                new AssignmentTemplate(2, cc,
                        ExpressionFactory.createConditionalExpression(ExpressionFactory.createOr(zf, of),
                                ExpressionFactory.createConditionalExpression(of, cc_value[3], cc_value[0]),
                                ExpressionFactory.createConditionalExpression(nf, cc_value[1], cc_value[2]))));
    }

    private static void setFlags(StatementSequence instrBody) {
        instrBody.addLast(new RTLVariableAssignment(zf,
                ExpressionFactory.createConditionalExpression(
                        ExpressionFactory.createEqual(cc, cc_value[0]), TRUE, FALSE)));
        instrBody.addLast(new RTLVariableAssignment(nf,
                ExpressionFactory.createConditionalExpression(
                        ExpressionFactory.createEqual(cc, cc_value[1]), TRUE, FALSE)));
        instrBody.addLast(new RTLVariableAssignment(pf,
                ExpressionFactory.createConditionalExpression(
                        ExpressionFactory.createEqual(cc, cc_value[2]), TRUE, FALSE)));
        instrBody.addLast(new RTLVariableAssignment(of,
                ExpressionFactory.createConditionalExpression(
                        ExpressionFactory.createEqual(cc, cc_value[3]), TRUE, FALSE)));
    }

    private static void addFlagsAssignment(StatementSequence instrBody, RTLExpression result,
                                           RTLExpression left_operand, RTLExpression right_operand) {
        instrBody.addLast(new RTLVariableAssignment(zf, ExpressionFactory.createEqual(result, rtl_number[0])));
        instrBody.addLast(new RTLVariableAssignment(nf, ExpressionFactory.createLessThan(result, rtl_number[0])));
        instrBody.addLast(new RTLVariableAssignment(pf, ExpressionFactory.createGreaterThan(result, rtl_number[0])));
        instrBody.addLast(new RTLVariableAssignment(of,
                ExpressionFactory.createOr
                        (
                                ExpressionFactory.createOperation(
                                        Operator.AND,
                                        ExpressionFactory.createLessThan(left_operand, rtl_number[0]),
                                        ExpressionFactory.createLessThan(right_operand, rtl_number[0]),
                                        ExpressionFactory.createGreaterOrEqual(result, rtl_number[0])),
                                ExpressionFactory.createOperation(
                                        Operator.AND,
                                        ExpressionFactory.createGreaterOrEqual(right_operand, rtl_number[0]),
                                        ExpressionFactory.createGreaterOrEqual(left_operand, rtl_number[0]),
                                        ExpressionFactory.createLessThan(result, rtl_number[0]))
                        )));
        setCC(instrBody);
    }

    private static void subtractFlagsAssignment(StatementSequence instrBody, RTLExpression result,
                                                RTLExpression left_operand, RTLExpression right_operand) {
        instrBody.addLast(new RTLVariableAssignment(zf, ExpressionFactory.createEqual(left_operand, right_operand)));
        instrBody.addLast(new RTLVariableAssignment(nf, ExpressionFactory.createLessThan(result, rtl_number[0])));
        instrBody.addLast(new RTLVariableAssignment(pf, ExpressionFactory.createGreaterThan(result, rtl_number[0])));
        instrBody.addLast(new RTLVariableAssignment(of,
                ExpressionFactory.createOr
                        (
                                ExpressionFactory.createOperation(
                                        Operator.AND,
                                        ExpressionFactory.createLessThan(left_operand, rtl_number[0]),
                                        ExpressionFactory.createGreaterOrEqual(right_operand, rtl_number[0]),
                                        ExpressionFactory.createGreaterThan(result, rtl_number[0])),
                                ExpressionFactory.createOperation(
                                        Operator.AND,
                                        ExpressionFactory.createLessThan(right_operand, rtl_number[0]),
                                        ExpressionFactory.createGreaterOrEqual(left_operand, rtl_number[0]),
                                        ExpressionFactory.createLessThan(result, rtl_number[0]))
                        )));
        setCC(instrBody);
    }

    private static void subtractLogicalFlagsAssignment(StatementSequence instrBody, RTLExpression result,
                                                       RTLExpression left_operand, RTLExpression right_operand) {
        instrBody.addLast(new RTLVariableAssignment(zf, FALSE));
        instrBody.addLast(new RTLVariableAssignment(nf, ExpressionFactory.createUnsignedLessThan(left_operand, right_operand)));
        instrBody.addLast(new RTLVariableAssignment(pf, ExpressionFactory.createEqual(left_operand, right_operand)));
        instrBody.addLast(new RTLVariableAssignment(of, ExpressionFactory.createUnsignedGreaterThan(left_operand, right_operand)));
        setCC(instrBody);
    }

    private static void compareFlagsAssignment(StatementSequence instrBody,
                                               RTLExpression left_operand, RTLExpression right_operand) {
        instrBody.addLast(new RTLVariableAssignment(nf, ExpressionFactory.createLessThan(left_operand, right_operand)));
        instrBody.addLast(new RTLVariableAssignment(pf, ExpressionFactory.createGreaterThan(left_operand, right_operand)));
        instrBody.addLast(new RTLVariableAssignment(of, FALSE));
        instrBody.addLast(new RTLVariableAssignment(zf, ExpressionFactory.createEqual(left_operand, right_operand)));
        setCC(instrBody);
    }

    private static void compareLogicalFlagsAssignment(StatementSequence instrBody,
                                                      RTLExpression left_operand, RTLExpression right_operand) {
        instrBody.addLast(new RTLVariableAssignment(zf, ExpressionFactory.createEqual(left_operand, right_operand)));
        instrBody.addLast(new RTLVariableAssignment(nf, ExpressionFactory.createUnsignedLessThan(left_operand, right_operand)));
        instrBody.addLast(new RTLVariableAssignment(pf, ExpressionFactory.createUnsignedGreaterThan(left_operand, right_operand)));
        instrBody.addLast(new RTLVariableAssignment(of, FALSE));
        setCC(instrBody);
    }

    private static void testFlagsAssignment(StatementSequence instrBody, RTLExpression result) {
        instrBody.addLast(new RTLVariableAssignment(zf, ExpressionFactory.createEqual(result, rtl_number[0])));
        instrBody.addLast(new RTLVariableAssignment(nf, ExpressionFactory.createLessThan(result, rtl_number[0])));
        instrBody.addLast(new RTLVariableAssignment(pf, ExpressionFactory.createGreaterThan(result, rtl_number[0])));
        instrBody.addLast(new RTLVariableAssignment(of, FALSE));
        setCC(instrBody);
    }

    private static void logicalFlagsAssignment(StatementSequence instrBody, RTLExpression result) {
        RTLNumber zero;
        if (result.getBitWidth() == 8)
            zero = rtl_number_8bit[0];
        else if (result.getBitWidth() == 16)
            zero = rtl_number[0].bitExtract(rtl_number[0].getBitWidth() - 16, rtl_number[0].getBitWidth() - 1);
        else
            zero = rtl_number[0];
        instrBody.addLast(new RTLVariableAssignment(zf, ExpressionFactory.createEqual(result, zero)));
        instrBody.addLast(new RTLVariableAssignment(nf, ExpressionFactory.createNotEqual(result, zero)));
        instrBody.addLast(new RTLVariableAssignment(pf, FALSE));
        instrBody.addLast(new RTLVariableAssignment(of, FALSE));
        setCC(instrBody);
    }

    //TODO: Check if this is correct
    private static void shiftRightFlagsAssignment(StatementSequence instrBody, RTLExpression result) {
        testFlagsAssignment(instrBody, result);
    }

    //TODO: Check if this is correct
    private static void shiftLeftArithmeticFlagsAssignment(StatementSequence instrBody, RTLExpression result, RTLNumber count) {
        instrBody.addLast(new RTLVariableAssignment(zf, ExpressionFactory.createEqual(result, rtl_number[0])));
        instrBody.addLast(new RTLVariableAssignment(nf, ExpressionFactory.createLessThan(result, rtl_number[0])));
        instrBody.addLast(new RTLVariableAssignment(pf, ExpressionFactory.createGreaterThan(result, rtl_number[0])));
        if (count.intValue() > 30)
            instrBody.addLast(new RTLVariableAssignment(of, TRUE));
        else
            instrBody.addLast(new RTLVariableAssignment(of,
                    ExpressionFactory.createBitRange(result,
                            ExpressionFactory.createPlus(count, 1),
                            ExpressionFactory.createPlus(count, 1))));
        setCC(instrBody);
    }

    //TODO: Check if this is correct
    private static void shiftLeftLogicalFlagsAssignment(StatementSequence instrBody, RTLExpression result, RTLNumber count) {
        instrBody.addLast(new RTLVariableAssignment(zf, ExpressionFactory.createEqual(result, rtl_number[0])));
        instrBody.addLast(new RTLVariableAssignment(nf, ExpressionFactory.createLessThan(result, rtl_number[0])));
        instrBody.addLast(new RTLVariableAssignment(pf, ExpressionFactory.createGreaterThan(result, rtl_number[0])));
        if (count.intValue() > 30)
            instrBody.addLast(new RTLVariableAssignment(of, TRUE));
        else
            instrBody.addLast(new RTLVariableAssignment(of, ExpressionFactory.createBitRange(result, count, count)));
        setCC(instrBody);
    }

//    private static void logicalFlagsAssignment(StatementSequence instrBody, RTLExpression result,
//                                               RTLExpression left_operand, RTLExpression right_operand)
//    {
//        instrBody.addLast(new RTLVariableAssignment(zf, ExpressionFactory.createEqual(result, rtl_number[0])));
//        instrBody.addLast(new RTLVariableAssignment(nf, ExpressionFactory.createNotEqual(result, rtl_number[0])));
//        instrBody.addLast(new RTLVariableAssignment(pf, FALSE));
//        instrBody.addLast(new RTLVariableAssignment(of, FALSE));
//
//        RTLVariableAssignment cf_assignment = new RTLVariableAssignment(cf, FALSE);
//        RTLVariableAssignment of_assignment = new RTLVariableAssignment(of, FALSE);
//        RTLVariableAssignment sf_assignment = new RTLVariableAssignment(sf, ExpressionFactory.createLessThan(result, rtl_number[0]));
//        RTLVariableAssignment zf_assignment = new RTLVariableAssignment(zf, ExpressionFactory.createEqual(result, rtl_number[0]));
//
//        setCC(instrBody);
//        instrBody.addLast(cf_assignment);
//        instrBody.addLast(of_assignment);
//        instrBody.addLast(sf_assignment);
//        instrBody.addLast(zf_assignment);
//    }

//    private static void subtractFlagsAssignment(StatementSequence instrBody, RTLExpression result,
//                                               RTLExpression left_operand, RTLExpression right_operand)
//    {
//        RTLVariableAssignment cf_assignment = new RTLVariableAssignment(cf,
//                ExpressionFactory.createUnsignedLessThan(left_operand, right_operand));
//        RTLVariableAssignment of_assignment = new RTLVariableAssignment(of,
//                ExpressionFactory.createOr
//                        (
//                                ExpressionFactory.createOperation(
//                                        Operator.AND,
//                                        ExpressionFactory.createLessThan(left_operand, rtl_number[0]),
//                                        ExpressionFactory.createGreaterOrEqual(right_operand, rtl_number[0]),
//                                        ExpressionFactory.createGreaterThan(result, rtl_number[0])),
//                                ExpressionFactory.createOperation(
//                                        Operator.AND,
//                                        ExpressionFactory.createLessThan(right_operand, rtl_number[0]),
//                                        ExpressionFactory.createGreaterOrEqual(left_operand, rtl_number[0]),
//                                        ExpressionFactory.createLessThan(result, rtl_number[0]))
//                        ));
//        RTLVariableAssignment sf_assignment = new RTLVariableAssignment(sf, ExpressionFactory.createLessThan(result, rtl_number[0]));
//        RTLVariableAssignment zf_assignment = new RTLVariableAssignment(zf, ExpressionFactory.createEqual(left_operand, right_operand));
//
//        instrBody.addLast(cf_assignment);
//        instrBody.addLast(of_assignment);
//        instrBody.addLast(sf_assignment);
//        instrBody.addLast(zf_assignment);
//    }

//    private static RTLVariableAssignment addCFAssignment(RTLExpression result, RTLExpression left_operand, RTLExpression right_operand)
//    {
//        return new RTLVariableAssignment(cf,
//                ExpressionFactory.createOr(
//                       ExpressionFactory.createAnd(
//                               ExpressionFactory.createLessThan(left_operand, zero),
//                               ExpressionFactory.createLessThan(right_operand, zero)
//                       ),
//                       ExpressionFactory.createAnd(
//                               ExpressionFactory.createGreaterOrEqual(result, zero),
//                               ExpressionFactory.createOr(
//                                       ExpressionFactory.createLessThan(left_operand, zero),
//                                       ExpressionFactory.createLessThan(right_operand, zero)
//                               )
//                       )
//                )
//        );
//    }
//
//    private static RTLVariableAssignment addOFAssignment(RTLExpression result, RTLExpression left_operand, RTLExpression right_operand)
//    {
//        return new RTLVariableAssignment(of,
//                ExpressionFactory.createOr
//                        (
//                                ExpressionFactory.createOperation(
//                                        Operator.AND,
//                                        ExpressionFactory.createLessThan(left_operand, zero),
//                                        ExpressionFactory.createLessThan(right_operand, zero),
//                                        ExpressionFactory.createGreaterOrEqual(result, zero)),
//                                ExpressionFactory.createOperation(
//                                        Operator.AND,
//                                        ExpressionFactory.createGreaterOrEqual(right_operand, zero),
//                                        ExpressionFactory.createGreaterOrEqual(left_operand, zero),
//                                        ExpressionFactory.createLessThan(result, zero))
//                        ));
//    }
//
//    private static RTLVariableAssignment addSFAssignment(RTLExpression result, RTLExpression left_operand, RTLExpression right_operand)
//    {
//        return new RTLVariableAssignment(sf, ExpressionFactory.createLessThan(result, zero));
//}
//
//    private static RTLVariableAssignment addZFAssignment(RTLExpression result, RTLExpression left_operand, RTLExpression right_operand)
//    {
//        return new RTLVariableAssignment(zf, ExpressionFactory.createEqual(result, zero));
//    }
//    private static RTLVariableAssignment logicalCFAssignment(RTLExpression result, RTLExpression left_operand, RTLExpression right_operand)
//    {
//        return new RTLVariableAssignment(cf, FALSE);
//    }
//
//    private static RTLVariableAssignment logicalOFAssignment(RTLExpression result, RTLExpression left_operand, RTLExpression right_operand)
//    {
//        return new RTLVariableAssignment(of, FALSE);
//    }
//
//    private static RTLVariableAssignment logicalSFAssignment(RTLExpression result, RTLExpression left_operand, RTLExpression right_operand)
//    {
//        return new RTLVariableAssignment(sf, ExpressionFactory.createLessThan(result, zero));
//    }
//
//    private static RTLVariableAssignment logicalZFAssignment(RTLExpression result, RTLExpression left_operand, RTLExpression right_operand)
//    {
//        return new RTLVariableAssignment(zf, ExpressionFactory.createEqual(result, zero));
//    }

//    private static RTLVariableAssignment subtractCFAssignment(RTLExpression result, RTLExpression left_operand, RTLExpression right_operand)
//    {
//        return new RTLVariableAssignment(cf,
//                ExpressionFactory.createUnsignedLessThan(left_operand, right_operand));
//    }
//
//    private static RTLVariableAssignment subtractOFAssignment(RTLExpression result, RTLExpression left_operand, RTLExpression right_operand)
//    {
//        return new RTLVariableAssignment(of,
//                ExpressionFactory.createOr
//                        (
//                                ExpressionFactory.createOperation(
//                                        Operator.AND,
//                                        ExpressionFactory.createLessThan(left_operand, zero),
//                                        ExpressionFactory.createGreaterOrEqual(right_operand, zero),
//                                        ExpressionFactory.createGreaterThan(result, zero)),
//                                ExpressionFactory.createOperation(
//                                        Operator.AND,
//                                        ExpressionFactory.createLessThan(right_operand, zero),
//                                        ExpressionFactory.createGreaterOrEqual(left_operand, zero),
//                                        ExpressionFactory.createLessThan(result, zero))
//                        ));
//    }
//
//    private static RTLVariableAssignment subtractSFAssignment(RTLExpression result, RTLExpression left_operand, RTLExpression right_operand)
//    {
//        return new RTLVariableAssignment(sf, ExpressionFactory.createLessThan(result, zero));
//    }
//
//    private static RTLVariableAssignment subtractZFAssignment(RTLExpression result, RTLExpression left_operand, RTLExpression right_operand)
//    {
//        return new RTLVariableAssignment(zf, ExpressionFactory.createEqual(left_operand, right_operand));
//    }
    //    private static void addFlagsAssignment(StatementSequence instrBody, RTLExpression result,
//                                            RTLExpression left_operand, RTLExpression right_operand)
//    {
//        RTLVariableAssignment cf_assignment = new RTLVariableAssignment(cf,
//                ExpressionFactory.createOr(
//                        ExpressionFactory.createAnd(
//                                ExpressionFactory.createLessThan(left_operand, rtl_number[0]),
//                                ExpressionFactory.createLessThan(right_operand, rtl_number[0])
//                        ),
//                        ExpressionFactory.createAnd(
//                                ExpressionFactory.createGreaterOrEqual(result, rtl_number[0]),
//                                ExpressionFactory.createOr(
//                                        ExpressionFactory.createLessThan(left_operand, rtl_number[0]),
//                                        ExpressionFactory.createLessThan(right_operand, rtl_number[0])
//                                )
//                        )
//                )
//        );
//        RTLVariableAssignment of_assignment = new RTLVariableAssignment(of,
//                ExpressionFactory.createOr
//                        (
//                                ExpressionFactory.createOperation(
//                                        Operator.AND,
//                                        ExpressionFactory.createLessThan(left_operand, rtl_number[0]),
//                                        ExpressionFactory.createLessThan(right_operand, rtl_number[0]),
//                                        ExpressionFactory.createGreaterOrEqual(result, rtl_number[0])),
//                                ExpressionFactory.createOperation(
//                                        Operator.AND,
//                                        ExpressionFactory.createGreaterOrEqual(right_operand, rtl_number[0]),
//                                        ExpressionFactory.createGreaterOrEqual(left_operand, rtl_number[0]),
//                                        ExpressionFactory.createLessThan(result, rtl_number[0]))
//                        ));
//        RTLVariableAssignment sf_assignment = new RTLVariableAssignment(sf, ExpressionFactory.createLessThan(result, rtl_number[0]));
//        RTLVariableAssignment zf_assignment = new RTLVariableAssignment(zf, ExpressionFactory.createEqual(result, rtl_number[0]));
//
//        instrBody.addLast(cf_assignment);
//        instrBody.addLast(of_assignment);
//        instrBody.addLast(sf_assignment);
//        instrBody.addLast(zf_assignment);
//    }

    private static StatementSequence translateRI(ZInstruction instruction) {
        StatementSequence instructionBody = new StatementSequence();

        ZRegister register = (ZRegister) instruction.getOperand1();
        RTLVariable register_operand = ExpressionFactory.createVariable(register.toString(), pc_bit_width);
        Immediate immediate = (Immediate) instruction.getOperand2();

        switch (instruction.getType()) {
            case Branch: {
                switch (instruction.getMnemonic()) {
                    case "JAS":
                    case "BRAS": {
                        RTLVariableAssignment register_assignment_statement = new RTLVariableAssignment(register_operand, ExpressionFactory.pc);
                        instructionBody.addLast(register_assignment_statement);

                        // Subtract 4 from I*2, as pc point after BRAS and BRAS is of lenght 4
                        RTLNumber immediate_value = ExpressionFactory.createNumber(immediate.getNumber().intValue() * 2 - 4);
                        RTLExpression branch_target = ExpressionFactory.createPlus(ExpressionFactory.pc, immediate_value);
                        RTLGoto branch_statement = new RTLGoto(branch_target, RTLGoto.Type.JUMP);
                        instructionBody.addLast(branch_statement);
                        break;
                    }
                }
            }
        }
        return instructionBody;
    }

    private static StatementSequence translateRR(ZInstruction instruction) {
        StatementSequence instructionBody = new StatementSequence();

        ZRegister left_register = (ZRegister) instruction.getOperand1();
        RTLVariable left_operand = ExpressionFactory.createVariable(left_register.toString(), pc_bit_width);
        ZRegister right_register = (ZRegister) instruction.getOperand2();
        RTLVariable right_operand = ExpressionFactory.createVariable(right_register.toString(), pc_bit_width);

        RTLVariableAssignment register_assignment_statement = null;

        ZInstructionType type = instruction.getType();
        String mnemonic = instruction.getMnemonic();
        switch (type) {
            case Arithmetic: {
                RTLExpression result = null;
                switch (mnemonic) {
                    case "AR": {
                        result = ExpressionFactory.createPlus(left_operand, right_operand);
                        register_assignment_statement = new RTLVariableAssignment(left_operand, result);
                        instructionBody.addLast(register_assignment_statement);
                        addFlagsAssignment(instructionBody, result, left_operand, right_operand);
                        break;
                    }
                    case "CR": {
                        compareFlagsAssignment(instructionBody, left_operand, right_operand);
                        break;
                    }
                    case "LTR": {
                        register_assignment_statement = new RTLVariableAssignment(left_operand, right_operand);
                        instructionBody.addLast(register_assignment_statement);
                        testFlagsAssignment(instructionBody, right_operand);
                        break;
                    }
                    case "OR": {
                        result = ExpressionFactory.createOr(left_operand, right_operand);
                        register_assignment_statement = new RTLVariableAssignment(left_operand, result);
                        instructionBody.addLast(register_assignment_statement);
                        logicalFlagsAssignment(instructionBody, result);
                        break;
                    }
                    case "SLR": {
                        result = ExpressionFactory.createMinus(left_operand, right_operand);
                        register_assignment_statement = new RTLVariableAssignment(left_operand, result);
                        instructionBody.addLast(register_assignment_statement);
                        subtractLogicalFlagsAssignment(instructionBody, result, left_operand, right_operand);
                        break;
                    }
                    case "SR": {
                        result = ExpressionFactory.createMinus(left_operand, right_operand);
                        register_assignment_statement = new RTLVariableAssignment(left_operand, result);
                        instructionBody.addLast(register_assignment_statement);
                        subtractFlagsAssignment(instructionBody, result, left_operand, right_operand);
                        break;
                    }
                    case "XR": {
                        result = ExpressionFactory.createXor(left_operand, right_operand);
                        register_assignment_statement = new RTLVariableAssignment(left_operand, result);
                        instructionBody.addLast(register_assignment_statement);
                        logicalFlagsAssignment(instructionBody, result);
                        break;
                    }
                }
                break;
            }
            case Branch: {
                RTLGoto branch_statement = null;
                RTLExpression branch_condition = null;
                RTLVariable branch_target = right_operand;

                switch (mnemonic) {
                    case "BASR":
                        //TODO: when 31-bit AMODE it is wrong semantic for BAL; need to be fixed!
                    case "BALR": {
                        register_assignment_statement = new RTLVariableAssignment(left_operand, ExpressionFactory.pc);
                        instructionBody.addLast(register_assignment_statement);

                        if (right_register.getNumber() != 0) {
                            branch_statement = new RTLGoto(branch_target, RTLGoto.Type.JUMP);
//                            //TODO: need to be removed!!!
//                            instructionBody.addLast(new RTLGoto(ExpressionFactory.pc, RTLGoto.Type.JUMP));
                        }
                        break;
                    }
                    case "BCTR": {
                        register_assignment_statement = new RTLVariableAssignment(left_operand,
                                ExpressionFactory.createMinus(left_operand, rtl_number[1]));
                        instructionBody.addLast(register_assignment_statement);

                        if (right_register.getNumber() != 0) {
                            branch_condition = ExpressionFactory.createNotEqual(left_operand, rtl_number[0]);
                            branch_statement = new RTLGoto(branch_target, branch_condition, RTLGoto.Type.JUMP);
                        }
                        break;
                    }
                    case "BSM": {
                        RTLBitRange left_operand_bit63 = ExpressionFactory.createBitRange(left_operand, rtl_number[63], rtl_number[63]);
                        RTLBitRange left_operand_bit32 = ExpressionFactory.createBitRange(left_operand, rtl_number[32], rtl_number[32]);
                        RTLBitRange right_operand_bit63 = ExpressionFactory.createBitRange(right_operand, rtl_number[63], rtl_number[63]);
                        RTLBitRange right_operand_bit32 = ExpressionFactory.createBitRange(right_operand, rtl_number[32], rtl_number[32]);

                        RTLBitRange psw_bit31 = ExpressionFactory.createBitRange(psw, rtl_number[31], rtl_number[31]);
                        RTLBitRange psw_bit32 = ExpressionFactory.createBitRange(psw, rtl_number[32], rtl_number[32]);

                        RTLBitRange amode = ExpressionFactory.createBitRange(psw, rtl_number[31], rtl_number[32]);

                        if (left_register.getNumber() != 0) {
                            instructionBody.addLast(new AssignmentTemplate(1, left_operand_bit63,
                                    ExpressionFactory.createConditionalExpression(ExpressionFactory.createEqual(psw_bit31, TRUE),
                                            TRUE, left_operand_bit63)));
                            instructionBody.addLast(new AssignmentTemplate(1, left_operand_bit32,
                                    ExpressionFactory.createConditionalExpression(ExpressionFactory.createEqual(psw_bit31, FALSE),
                                            psw_bit32, left_operand_bit32)));
                        }
                        if (right_register.getNumber() != 0) {
                            instructionBody.addLast(new AssignmentTemplate(2, amode,
                                    ExpressionFactory.createConditionalExpression(
                                            ExpressionFactory.createEqual(right_operand_bit63, TRUE),
                                            minus_one.bitExtract(minus_one.getBitWidth() - 2, minus_one.getBitWidth() - 1),
                                            ExpressionFactory.createConditionalExpression(
                                                    ExpressionFactory.createEqual(right_operand_bit32, TRUE),
                                                    rtl_number[1].bitExtract(rtl_number[1].getBitWidth() - 2, rtl_number[1].getBitWidth() - 1),
                                                    rtl_number[0].bitExtract(rtl_number[0].getBitWidth() - 2, rtl_number[0].getBitWidth() - 1)
                                            ))));
                            RTLVariable temp = ExpressionFactory.createVariable("temp", 32);
                            instructionBody.addLast(new RTLVariableAssignment(branch_target,
                                    ExpressionFactory.createNumber(0x7FFFFFFE, 32)));
                            branch_statement = new RTLGoto(temp, RTLGoto.Type.JUMP);
                        }
                        break;
                    }
                }
                if (branch_statement != null)
                    instructionBody.addLast(branch_statement);
                break;
            }
            case Move:
            case Store:
            case Load: {
                switch (mnemonic) {
                    case "LPDR": {
                        left_operand = ExpressionFactory.createVariable("FR" + left_register.getNumber(), pc_bit_width);
                        right_operand = ExpressionFactory.createVariable("FR" + right_register.getNumber(), pc_bit_width);
                        //TODO: Check if this is correct
                        register_assignment_statement =
                                new RTLVariableAssignment(left_operand, ExpressionFactory.createConditionalExpression(
                                        ExpressionFactory.createGreaterThan(right_operand, rtl_number[0]),
                                        right_operand,
                                        ExpressionFactory.createNeg(right_operand)
                                ));
                        instructionBody.addLast(register_assignment_statement);
                        instructionBody.addLast(
                                new RTLVariableAssignment(zf, ExpressionFactory.createEqual(left_operand, rtl_number[0])));
                        instructionBody.addLast(
                                new RTLVariableAssignment(nf, FALSE));
                        instructionBody.addLast(
                                new RTLVariableAssignment(pf, ExpressionFactory.createGreaterThan(left_operand, rtl_number[0])));
                        instructionBody.addLast(
                                new RTLVariableAssignment(of, FALSE));
                        setCC(instructionBody);
                        break;
                    }
                    case "LPR": {
                        //TODO: Check if this is correct
                        register_assignment_statement =
                                new RTLVariableAssignment(left_operand, ExpressionFactory.createConditionalExpression(
                                        ExpressionFactory.createGreaterThan(right_operand, rtl_number[0]),
                                        right_operand,
                                        ExpressionFactory.createNeg(right_operand)
                                ));
                        instructionBody.addLast(register_assignment_statement);
                        instructionBody.addLast(
                                new RTLVariableAssignment(zf, ExpressionFactory.createEqual(left_operand, rtl_number[0])));
                        instructionBody.addLast(
                                new RTLVariableAssignment(nf, FALSE));
                        instructionBody.addLast(
                                new RTLVariableAssignment(pf, ExpressionFactory.createGreaterThan(left_operand, rtl_number[0])));
                        instructionBody.addLast(
                                new RTLVariableAssignment(of, ExpressionFactory.createEqual(right_operand,
                                        ExpressionFactory.createNumber(0x80000000, 32))));
                        setCC(instructionBody);
                        break;
                    }
                    case "LR": {
                        register_assignment_statement = new RTLVariableAssignment(left_operand, right_operand);
                        instructionBody.addLast(register_assignment_statement);
                        break;
                    }
                }
                break;
            }
        }
        return instructionBody;
    }

//    private static RTLExpression getCondition(ZMask mask)
//    {
//        RTLExpression condition;
//        switch (mask.getValue())
//        {
//            //Branch (unconditional)
//            case 15:
//                condition = TRUE;
//                break;
//
//            //Branch on Not Overflow
//            //OF == 0
//            case 14:
//                condition = ExpressionFactory.createEqual(of, FALSE);
//                break;
//
//            //Branch on Not High (less or equal)
//            //ZF == 1 or SF != OF
//            case 13:
//                condition = ExpressionFactory.createOr(ExpressionFactory.createEqual(zf, TRUE),
//                        ExpressionFactory.createNotEqual(sf, of));
//                break;
//
//            //Branch on Not Less (greater or equal)
//            //SF == OF
//            case 11:
//                condition = ExpressionFactory.createEqual(sf, of);
//                break;
//
//            //Branch on Equal
//            //ZF == 1
//            case 8:
//                condition = ExpressionFactory.createEqual(zf, TRUE);
//                break;
//
//            //Branch on Not Equal
//            //ZF == 0
//            case 7:
//                condition = ExpressionFactory.createEqual(zf, FALSE);
//                break;
//
//            //Branch on Low
//            //SF != OF
//            case 4:
//                condition = ExpressionFactory.createNotEqual(sf, zf);
//                break;
//
//            //Branch on High
//            //ZF == 0 and SF == OF
//            case 2:
//                condition = ExpressionFactory.createAnd(ExpressionFactory.createEqual(zf, FALSE),
//                        ExpressionFactory.createEqual(sf, of));
//                break;
//
//            //Branch on Overflow
//            //OF == 1
//            case 1:
//                condition = ExpressionFactory.createEqual(of, TRUE);
//                break;
//
//            default:
//                condition = null;
//        }
//        return condition;
//    }

    private static RTLExpression getCondition(ZMask mask) {
        RTLExpression condition;
        switch (mask.getValue()) {
            //Branch (unconditional)
            case 15:
                condition = TRUE;
                break;

            //Branch on Not Overflow
            case 14:
                condition = ExpressionFactory.createEqual(of, FALSE);
                break;

            //Branch on Not High (less or equal)
            case 13:
                condition = ExpressionFactory.createEqual(pf, FALSE);
                break;

            //Branch on Not Less (greater or equal)
            case 11:
                condition = ExpressionFactory.createEqual(nf, FALSE);
                break;

            //Branch on Equal
            case 8:
                condition = ExpressionFactory.createEqual(zf, TRUE);
                break;

            //Branch on Not Equal
            case 7:
                condition = ExpressionFactory.createEqual(zf, FALSE);
                break;

            //Branch on Low
            case 4:
                condition = ExpressionFactory.createEqual(nf, TRUE);
                break;

            //Branch on High
            case 2:
                condition = ExpressionFactory.createEqual(pf, TRUE);
                break;

            //Branch on Overflow
            case 1:
                condition = ExpressionFactory.createEqual(of, TRUE);
                break;

            default:
                condition = null;
        }
        return condition;
    }

    //special for BCR instruction
    private static StatementSequence translateRRm(ZInstruction instruction) {
        StatementSequence instructionBody = new StatementSequence();

        ZRegister branch_target_register = (ZRegister) instruction.getOperand2();
        RTLVariable branch_target = ExpressionFactory.createVariable(branch_target_register.toString(), pc_bit_width);

        RTLGoto branch_statement;

        RTLExpression condition = getCondition((ZMask) instruction.getOperand1());

        if (condition != null) {
            branch_statement = new RTLGoto(branch_target, condition, RTLGoto.Type.JUMP);
            instructionBody.addLast(branch_statement);
        }
        return instructionBody;
    }

    private static StatementSequence translateRXb(ZInstruction instruction) {
        StatementSequence instructionBody = new StatementSequence();

        ZStorageOperand branch_target_operand = (ZStorageOperand) instruction.getOperand2();
        int displacement = (int) (branch_target_operand).getDisplacement();

        Register base_register = (branch_target_operand).getBase();
        RTLVariable base = ExpressionFactory.createVariable(base_register.toString(), pc_bit_width);

        Register index_register = (branch_target_operand).getIndex();
        RTLVariable index = null;
        if (index_register != null)
            index = ExpressionFactory.createVariable(index_register.toString(), pc_bit_width);

        RTLExpression branch_target_address = ExpressionFactory.createNumber(displacement);
        if (base_register.getNumber() != 0)
            branch_target_address = ExpressionFactory.createPlus(branch_target_address, base);
        if (index != null && index_register.getNumber() != 0)
            branch_target_address = ExpressionFactory.createPlus(branch_target_address, index);

        RTLGoto branch_statement;

        RTLExpression condition = getCondition((ZMask) instruction.getOperand1());

        if (condition != null) {
            branch_statement = new RTLGoto(branch_target_address, condition, RTLGoto.Type.JUMP);
            instructionBody.addLast(branch_statement);
        }
        return instructionBody;
    }

    private static StatementSequence translateRXa(ZInstruction instruction) {
        StatementSequence instructionBody = new StatementSequence();

        ZRegister operand1 = (ZRegister) instruction.getOperand1();

        ZStorageOperand operand2 = (ZStorageOperand) instruction.getOperand2();
        int displacement = (int) operand2.getDisplacement();

        Register base_register = operand2.getBase();
        RTLVariable base = ExpressionFactory.createVariable(base_register.toString(), pc_bit_width);

        Register index_register = operand2.getIndex();
        RTLVariable index = null;
        if (index_register != null)
            index = ExpressionFactory.createVariable(index_register.toString(), pc_bit_width);

        RTLExpression storage_operand_address = ExpressionFactory.createNumber(displacement);
        if (base_register.getNumber() != 0)
            storage_operand_address = ExpressionFactory.createPlus(storage_operand_address, base);
        if (index != null && index_register.getNumber() != 0)
            storage_operand_address = ExpressionFactory.createPlus(storage_operand_address, index);

        RTLMemoryLocation storage_operand = ExpressionFactory.createMemoryLocation(storage_operand_address, 32);
        RTLVariable register_operand = ExpressionFactory.createVariable(operand1.toString(), pc_bit_width);
        RTLVariableAssignment register_assignment_statement;
        RTLMemoryAssignment storage_assignment_statement;
        AssignmentTemplate general_assignment_statement;

        String mnemonic = instruction.getMnemonic();
        switch (instruction.getType()) {
            case Arithmetic: {
                RTLExpression result = null;
                switch (mnemonic) {
                    case "A": {
                        result = ExpressionFactory.createPlus(register_operand, storage_operand);
                        register_assignment_statement = new RTLVariableAssignment(register_operand, result);
                        instructionBody.addLast(register_assignment_statement);
                        addFlagsAssignment(instructionBody, result, register_operand, storage_operand);
                        break;
                    }
                    case "C": {
                        compareFlagsAssignment(instructionBody, register_operand, storage_operand);
                        break;
                    }
                    case "CH": {
                        storage_operand = ExpressionFactory.createMemoryLocation(storage_operand_address, 16);
                        compareFlagsAssignment(instructionBody,
                                ExpressionFactory.createBitRange(register_operand, rtl_number[16], rtl_number[31]), storage_operand);
                        break;
                    }
                    case "N": {
                        result = ExpressionFactory.createAnd(register_operand, storage_operand);
                        register_assignment_statement = new RTLVariableAssignment(register_operand, result);
                        instructionBody.addLast(register_assignment_statement);
                        logicalFlagsAssignment(instructionBody, result);
                        break;
                    }
                    case "O": {
                        result = ExpressionFactory.createOr(register_operand, storage_operand);
                        register_assignment_statement = new RTLVariableAssignment(register_operand, result);
                        instructionBody.addLast(register_assignment_statement);
                        logicalFlagsAssignment(instructionBody, result);
                        break;
                    }
                    case "S": {
                        result = ExpressionFactory.createMinus(register_operand, storage_operand);
                        register_assignment_statement = new RTLVariableAssignment(register_operand, result);
                        instructionBody.addLast(register_assignment_statement);
                        subtractFlagsAssignment(instructionBody, result, register_operand, storage_operand);
                        break;
                    }
                    case "X": {
                        result = ExpressionFactory.createXor(register_operand, storage_operand);
                        register_assignment_statement = new RTLVariableAssignment(register_operand, result);
                        instructionBody.addLast(register_assignment_statement);
                        logicalFlagsAssignment(instructionBody, result);
                        break;
                    }
                }
                break;
            }
            case Branch: {
                RTLGoto branch_statement = null;
                RTLExpression branch_condition = null;
                RTLExpression branch_target = storage_operand_address;

                switch (mnemonic) {
                    case "BAS":
                        //TODO: when 31-bit AMODE it is wrong semantic for BAL; need to be fixed!
                    case "BAL": {
                        register_assignment_statement = new RTLVariableAssignment(register_operand, ExpressionFactory.pc);
                        instructionBody.addLast(register_assignment_statement);

                        if (operand1.getNumber() != 0)
                            branch_statement = new RTLGoto(branch_target, RTLGoto.Type.JUMP);
                        break;
                    }
                    case "BCT": {
                        register_assignment_statement = new RTLVariableAssignment(register_operand,
                                ExpressionFactory.createMinus(register_operand, rtl_number[1]));
                        instructionBody.addLast(register_assignment_statement);

                        if (operand1.getNumber() != 0) {
                            branch_condition = ExpressionFactory.createNotEqual(register_operand, rtl_number[0]);
                            branch_statement = new RTLGoto(branch_target, branch_condition, RTLGoto.Type.JUMP);
                        }
                        break;
                    }
                }
                if (branch_statement != null)
                    instructionBody.addLast(branch_statement);
                break;
            }
            case Move:
            case Store:
            case Load: {
                switch (mnemonic) {
                    case "IC": {
                        general_assignment_statement = new AssignmentTemplate(8,
                                ExpressionFactory.createBitRange(register_operand, rtl_number[24], rtl_number[31]),
                                ExpressionFactory.createBitRange(storage_operand, rtl_number[24], rtl_number[31])
                        );
                        instructionBody.addLast(general_assignment_statement);
                        break;
                    }
                    case "LA": {
                        register_assignment_statement = new RTLVariableAssignment(register_operand, storage_operand_address);
                        instructionBody.addLast(register_assignment_statement);
                        break;
                    }
                    case "L": {
                        register_assignment_statement = new RTLVariableAssignment(register_operand, storage_operand);
                        instructionBody.addLast(register_assignment_statement);
                        break;
                    }
                    case "LH": {
                        RTLMemoryLocation storage_halfword =
                                ExpressionFactory.createMemoryLocation(storage_operand_address, 16);
                        general_assignment_statement =
                                new AssignmentTemplate(16,
                                        ExpressionFactory.createBitRange(register_operand, rtl_number[16], rtl_number[31]),
                                        storage_halfword);
                        instructionBody.addLast(general_assignment_statement);
                        break;
                    }
                    case "ST": {
                        storage_assignment_statement = new RTLMemoryAssignment(storage_operand, register_operand);
                        instructionBody.addLast(storage_assignment_statement);
                        break;
                    }
                    case "STC": {
                        RTLMemoryLocation storage_byte = ExpressionFactory.createMemoryLocation(storage_operand_address, 8);
                        storage_assignment_statement = new RTLMemoryAssignment(storage_byte,
                                ExpressionFactory.createBitRange(register_operand, rtl_number[24], rtl_number[31]));
                        instructionBody.addLast(storage_assignment_statement);
                        break;
                    }
                    case "STH": {
                        RTLMemoryLocation storage_halfword = ExpressionFactory.createMemoryLocation(storage_operand_address, 16);
                        storage_assignment_statement = new RTLMemoryAssignment(storage_halfword,
                                ExpressionFactory.createBitRange(register_operand, rtl_number[16], rtl_number[31]));
                        instructionBody.addLast(storage_assignment_statement);
                        break;
                    }
                }
                break;
            }
        }
        return instructionBody;
    }

    //get registers to save/restore (STM, LM)
    private static ArrayList<ZRegister> getRegistersCyclicRange(ZRegister r_first, ZRegister r_last) {
        ArrayList<ZRegister> destinations = new ArrayList<ZRegister>();
        int r_first_num = r_first.getNumber();
        int r_last_num = r_last.getNumber();
        if (r_first_num == r_last_num) {
            destinations.add(new ZRegister(r_first_num));
            return destinations;
        }
        if (r_first.getNumber() < r_last.getNumber()) {
            for (int i = r_first_num; i <= r_last_num; i++)
                destinations.add(new ZRegister(i));
            return destinations;
        }
        for (int i = r_first_num; i <= 15; i++)
            destinations.add(new ZRegister(i));
        for (int i = 0; i <= r_last_num; i++)
            destinations.add(new ZRegister(i));
        return destinations;
    }

    private static StatementSequence translateRSa(ZInstruction instruction) {
        StatementSequence instructionBody = new StatementSequence();

        ZRegister operand1 = (ZRegister) instruction.getOperand1();
        RTLVariable register_operand1 = ExpressionFactory.createVariable(operand1.toString(), pc_bit_width);

        ZStorageOperand operand2 = (ZStorageOperand) instruction.getOperand2();

        int displacement = (int) operand2.getDisplacement();
        Register base_register = operand2.getBase();
        RTLVariable base = ExpressionFactory.createVariable(base_register.toString(), pc_bit_width);

        Register index_register = operand2.getIndex();
        RTLVariable index = null;
        if (index_register != null)
            index = ExpressionFactory.createVariable(index_register.toString(), pc_bit_width);

        RTLExpression storage_operand_address = ExpressionFactory.createNumber(displacement);
        if (base_register.getNumber() != 0)
            storage_operand_address = ExpressionFactory.createPlus(storage_operand_address, base);
        if (index != null && index_register.getNumber() != 0)
            storage_operand_address = ExpressionFactory.createPlus(storage_operand_address, index);
        RTLMemoryLocation storage_operand = ExpressionFactory.createMemoryLocation(storage_operand_address, pc_bit_width);

        ZRegister operand3 = (ZRegister) instruction.getOperand3();
        RTLVariable register_operand3 = ExpressionFactory.createVariable(operand3.toString(), pc_bit_width);

        RTLVariableAssignment register_assignment_statement = null;
        RTLMemoryAssignment storage_assignment_statement = null;

        String mnemonic = instruction.getMnemonic();
        switch (instruction.getType()) {
            case Arithmetic: {
                break;
            }
            case Branch: {
                break;
            }
            case Move:
            case Store:
            case Load: {
                switch (mnemonic) {
                    case "LM":
                    case "STM": {
                        ArrayList<ZRegister> registersList = getRegistersCyclicRange(operand1, operand3);
                        RTLMemoryLocation current_storage_operand;
                        RTLVariable current_register_operand;
                        AssignmentTemplate current_assignment_statement;

                        for (ZRegister current_register : registersList) {
                            storage_operand_address = ExpressionFactory.createPlus(storage_operand_address, rtl_number[4]);
                            current_storage_operand = ExpressionFactory.createMemoryLocation(storage_operand_address, 32);
                            current_register_operand = ExpressionFactory.createVariable(current_register.toString(), 32);
                            if (mnemonic.equals("LM"))
                                current_assignment_statement =
                                        new AssignmentTemplate(32, current_register_operand, current_storage_operand);
                            else
                                current_assignment_statement =
                                        new AssignmentTemplate(32, current_storage_operand, current_register_operand);
                            instructionBody.addLast(current_assignment_statement);
                        }
                        break;
                    }
                }
                break;
            }
        }
        return instructionBody;
    }

    private static StatementSequence translateRSa2(ZInstruction instruction) {
        StatementSequence instructionBody = new StatementSequence();

        ZRegister register = (ZRegister) instruction.getOperand1();
        RTLVariable register_operand = ExpressionFactory.createVariable(register.toString(), pc_bit_width);

        ZStorageOperand shift_operand = (ZStorageOperand) instruction.getOperand2();
        byte displacement = (byte) (shift_operand.getDisplacement() & 0x3f);
        RTLNumber count = ExpressionFactory.createNumber(displacement);

        switch (instruction.getMnemonic()) {
            //TODO: check if this is correct
            case "SLA": {
                RTLVariableAssignment shift_statement =
                        new RTLVariableAssignment(register_operand,
                                ExpressionFactory.createShiftLeftArithmetic(register_operand, count));
                instructionBody.addLast(shift_statement);
                shiftLeftArithmeticFlagsAssignment(instructionBody, register_operand, count);
                break;
            }
            //TODO: check if this is correct
            case "SLL": {
                RTLVariableAssignment shift_statement =
                        new RTLVariableAssignment(register_operand,
                                ExpressionFactory.createShiftLeft(register_operand, count));
                instructionBody.addLast(shift_statement);
                shiftLeftLogicalFlagsAssignment(instructionBody, register_operand, count);
                break;
            }
            //TODO: check if this is correct
            case "SRA": {
                RTLVariableAssignment shift_statement =
                        new RTLVariableAssignment(register_operand,
                                ExpressionFactory.createShiftRightArithmetic(register_operand, count));
                instructionBody.addLast(shift_statement);
                shiftRightFlagsAssignment(instructionBody, register_operand);
                break;
            }
            //TODO: check if this is correct
            case "SRL": {
                RTLVariableAssignment shift_statement =
                        new RTLVariableAssignment(register_operand,
                                ExpressionFactory.createShiftRight(register_operand, count));
                instructionBody.addLast(shift_statement);
                shiftRightFlagsAssignment(instructionBody, register_operand);
                break;
            }
        }
        return instructionBody;
    }

    private static StatementSequence translateRSb(ZInstruction instruction) {
        StatementSequence instructionBody = new StatementSequence();

        ZRegister operand1 = (ZRegister) instruction.getOperand1();
        RTLVariable register_operand = ExpressionFactory.createVariable(operand1.toString(), pc_bit_width);

        ZStorageOperand operand2 = (ZStorageOperand) instruction.getOperand2();
        int displacement = (int) operand2.getDisplacement();
        Register base_register = operand2.getBase();
        RTLVariable base = ExpressionFactory.createVariable(base_register.toString(), pc_bit_width);

        RTLExpression storage_operand_address = ExpressionFactory.createNumber(displacement);
        if (base_register.getNumber() != 0)
            storage_operand_address = ExpressionFactory.createPlus(storage_operand_address, base);
        RTLMemoryLocation storage_operand = ExpressionFactory.createMemoryLocation(storage_operand_address, pc_bit_width);

        ZMask mask = (ZMask) instruction.getOperand3();

        RTLBitRange[] storage_bytes = new RTLBitRange[]
                {
                        ExpressionFactory.createBitRange(storage_operand, rtl_number[0], rtl_number[7]),
                        ExpressionFactory.createBitRange(storage_operand, rtl_number[8], rtl_number[15]),
                        ExpressionFactory.createBitRange(storage_operand, rtl_number[16], rtl_number[23]),
                        ExpressionFactory.createBitRange(storage_operand, rtl_number[24], rtl_number[31])
                };
        RTLBitRange[] register_bytes = new RTLBitRange[]
                {
                        ExpressionFactory.createBitRange(register_operand, rtl_number[0], rtl_number[7]),
                        ExpressionFactory.createBitRange(register_operand, rtl_number[8], rtl_number[15]),
                        ExpressionFactory.createBitRange(register_operand, rtl_number[16], rtl_number[23]),
                        ExpressionFactory.createBitRange(register_operand, rtl_number[24], rtl_number[31])
                };
        int current_byte_index = 0;
        AssignmentTemplate assignment_statement;

        String mnemonic = instruction.getMnemonic();
        switch (mnemonic) {
            case "ICM":
            case "STCM": {
                if ((mask.getValue() & 0x8) == 0x8) {
                    if (mnemonic.equals("ICM"))
                        assignment_statement = new AssignmentTemplate(8, register_bytes[0], storage_bytes[current_byte_index]);
                    else
                        assignment_statement = new AssignmentTemplate(8, storage_bytes[current_byte_index], register_bytes[0]);
                    instructionBody.addLast(assignment_statement);
                    current_byte_index++;
                }
                if ((mask.getValue() & 0x4) == 0x4) {
                    if (mnemonic.equals("ICM"))
                        assignment_statement = new AssignmentTemplate(8, register_bytes[1], storage_bytes[current_byte_index]);
                    else
                        assignment_statement = new AssignmentTemplate(8, storage_bytes[current_byte_index], register_bytes[1]);
                    instructionBody.addLast(assignment_statement);
                    current_byte_index++;
                }
                if ((mask.getValue() & 0x2) == 0x2) {
                    if (mnemonic.equals("ICM"))
                        assignment_statement = new AssignmentTemplate(8, register_bytes[2], storage_bytes[current_byte_index]);
                    else
                        assignment_statement = new AssignmentTemplate(8, storage_bytes[current_byte_index], register_bytes[2]);
                    instructionBody.addLast(assignment_statement);
                    current_byte_index++;
                }
                if ((mask.getValue() & 0x1) == 0x1) {
                    if (mnemonic.equals("ICM"))
                        assignment_statement = new AssignmentTemplate(8, register_bytes[3], storage_bytes[current_byte_index]);
                    else
                        assignment_statement = new AssignmentTemplate(8, storage_bytes[current_byte_index], register_bytes[3]);
                    instructionBody.addLast(assignment_statement);
                }
                break;
            }
        }
        return instructionBody;
    }

    private static StatementSequence translateSI(ZInstruction instruction) {
        StatementSequence instructionBody = new StatementSequence();

        Immediate immediate = (Immediate) instruction.getOperand2();
        RTLNumber immediate_value = ExpressionFactory.createNumber(immediate.getNumber().intValue());
        immediate_value = immediate_value.bitExtract(immediate_value.getBitWidth() - 8, immediate_value.getBitWidth() - 1);

        ZStorageOperand storage = (ZStorageOperand) instruction.getOperand1();
        int displacement = (int) storage.getDisplacement();
        Register base_register = storage.getBase();
        RTLVariable base = ExpressionFactory.createVariable(base_register.toString(), pc_bit_width);

        RTLExpression storage_operand_address = ExpressionFactory.createNumber(displacement);
        if (base_register.getNumber() != 0)
            storage_operand_address = ExpressionFactory.createPlus(storage_operand_address, base);
        RTLMemoryLocation storage_byte = ExpressionFactory.createMemoryLocation(storage_operand_address, 8);

        RTLMemoryAssignment storage_assignment_statement = null;

        String mnemonic = instruction.getMnemonic();
        switch (instruction.getType()) {
            case Arithmetic: {
                RTLExpression result;
                switch (mnemonic) {
                    case "NI": {
                        result = ExpressionFactory.createAnd(storage_byte, immediate_value);
                        storage_assignment_statement = new RTLMemoryAssignment(storage_byte, result);
                        instructionBody.addLast(storage_assignment_statement);
                        logicalFlagsAssignment(instructionBody, result);
                        break;
                    }
                    case "OI": {
                        result = ExpressionFactory.createOr(storage_byte, immediate_value);
                        storage_assignment_statement = new RTLMemoryAssignment(storage_byte, result);
                        instructionBody.addLast(storage_assignment_statement);
                        logicalFlagsAssignment(instructionBody, result);
                        break;
                    }
                    case "CLI": {
                        compareLogicalFlagsAssignment(instructionBody, storage_byte, immediate_value);
                        break;
                    }
                    case "TM": {
                        String mask = Integer.toBinaryString(immediate.getNumber().intValue());
                        RTLExpression[] test_results = new RTLExpression[(mask.replaceAll("0", "")).length()];
                        int j = 0;
                        for (int i = 0; i < 8; i++) {
                            if ((immediate.getNumber().intValue() & ((int) Math.pow(2, (7 - i)))) >>> (7 - i) == 1) {
                                test_results[j] = ExpressionFactory.createEqual(
                                        ExpressionFactory.createBitRange(storage_byte, rtl_number[i], rtl_number[i]), TRUE);
                                j++;
                            }
                        }
                        RTLExpression conjunction_result = ExpressionFactory.createOperation(Operator.AND, test_results);
                        RTLExpression disjunction_result = ExpressionFactory.createOperation(Operator.OR, test_results);

                        RTLVariableAssignment of_assignment = new RTLVariableAssignment(of, conjunction_result);
                        RTLVariableAssignment zf_assignment =
                                new RTLVariableAssignment(zf, ExpressionFactory.createNot(disjunction_result));
                        RTLVariableAssignment nf_assignment =
                                new RTLVariableAssignment(nf, ExpressionFactory.createNotEqual(conjunction_result, disjunction_result));
                        RTLVariableAssignment pf_assignment = new RTLVariableAssignment(pf, FALSE);

                        setCC(instructionBody);
                        instructionBody.addLast(zf_assignment);
                        instructionBody.addLast(nf_assignment);
                        instructionBody.addLast(pf_assignment);
                        instructionBody.addLast(of_assignment);
                        break;
                    }
                }
                break;
            }
            case Move: {
                switch (mnemonic) {
                    case "MVI": {
                        storage_assignment_statement = new RTLMemoryAssignment(storage_byte, immediate_value);
                        break;
                    }
                }
                instructionBody.addLast(storage_assignment_statement);
                break;
            }
        }
        return instructionBody;
    }

    private static StatementSequence translateSSa(ZInstruction instruction) {
        StatementSequence instructionBody = new StatementSequence();

        ZStorageOperand operand1 = (ZStorageOperand) instruction.getOperand1();
        int length = operand1.getLength();
        int displacement1 = (int) operand1.getDisplacement();
        Register base_register1 = operand1.getBase();
        RTLVariable base1 = ExpressionFactory.createVariable(base_register1.toString(), pc_bit_width);
        RTLExpression storage_operand1_address = ExpressionFactory.createNumber(displacement1);
        if (base_register1.getNumber() != 0)
            storage_operand1_address = ExpressionFactory.createPlus(storage_operand1_address, base1);
        RTLMemoryLocation storage_operand1 = ExpressionFactory.createMemoryLocation(storage_operand1_address,
                operand1.getLength() * 8);

        ZStorageOperand operand2 = (ZStorageOperand) instruction.getOperand1();
        int displacement2 = (int) operand2.getDisplacement();
        Register base_register2 = operand2.getBase();
        RTLVariable base2 = ExpressionFactory.createVariable(base_register2.toString(), pc_bit_width);
        RTLExpression storage_operand2_address = ExpressionFactory.createNumber(displacement2);
        if (base_register2.getNumber() != 0)
            storage_operand2_address = ExpressionFactory.createPlus(storage_operand2_address, base2);
        RTLMemoryLocation storage_operand2 = ExpressionFactory.createMemoryLocation(storage_operand2_address,
                operand2.getLength() * 8);

        RTLMemoryAssignment storage_assignment_statement = null;
        RTLMemoryLocation current_source_byte;
        RTLMemoryLocation current_target_byte;
        RTLMemoryAssignment currentByte_assignment_statement;

        String mnemonic = instruction.getMnemonic();
        switch (instruction.getType()) {
            case Move: {
                switch (mnemonic) {
                    case "MVC": {
                        for (int i = 0; i < length; i++) {
                            current_source_byte = ExpressionFactory.createMemoryLocation(storage_operand2_address, 8);
                            current_target_byte = ExpressionFactory.createMemoryLocation(storage_operand1_address, 8);
                            currentByte_assignment_statement = new RTLMemoryAssignment(current_target_byte, current_source_byte);
                            instructionBody.addLast(currentByte_assignment_statement);
                            storage_operand1_address = ExpressionFactory.createPlus(storage_operand1_address, rtl_number[1]);
                            storage_operand2_address = ExpressionFactory.createPlus(storage_operand2_address, rtl_number[1]);
                        }
                        break;
                    }
                    case "CLC": {
                        RTLExpression[] equal_results = new RTLExpression[length];
                        RTLExpression[] lower_results = new RTLExpression[length];
                        RTLExpression[] higher_results = new RTLExpression[length];
                        for (int i = 0; i < length; i++) {
                            current_source_byte = ExpressionFactory.createMemoryLocation(storage_operand2_address, 8);
                            current_target_byte = ExpressionFactory.createMemoryLocation(storage_operand1_address, 8);

                            equal_results[i] = ExpressionFactory.createEqual(current_target_byte, current_source_byte);
                            lower_results[i] = ExpressionFactory.createUnsignedLessThan(current_target_byte, current_source_byte);
                            higher_results[i] = ExpressionFactory.createUnsignedGreaterThan(current_target_byte, current_source_byte);

                            storage_operand1_address = ExpressionFactory.createPlus(storage_operand1_address, rtl_number[1]);
                            storage_operand2_address = ExpressionFactory.createPlus(storage_operand2_address, rtl_number[1]);
                        }
                        instructionBody.addLast(
                                new RTLVariableAssignment(zf, ExpressionFactory.createOperation(Operator.AND, equal_results)));
                        instructionBody.addLast(
                                new RTLVariableAssignment(nf, ExpressionFactory.createOperation(Operator.AND, lower_results)));
                        instructionBody.addLast(
                                new RTLVariableAssignment(pf, ExpressionFactory.createOperation(Operator.AND, higher_results)));
                        instructionBody.addLast(
                                new RTLVariableAssignment(of, FALSE));
                        setCC(instructionBody);
                        break;
                    }
                    case "XC": {
                        RTLExpression current_result;
                        RTLExpression[] equal_results = new RTLExpression[length];
                        for (int i = 0; i < length; i++) {
                            current_source_byte = ExpressionFactory.createMemoryLocation(storage_operand2_address, 8);
                            current_target_byte = ExpressionFactory.createMemoryLocation(storage_operand1_address, 8);

                            current_result = ExpressionFactory.createXor(current_target_byte, current_source_byte);
                            currentByte_assignment_statement = new RTLMemoryAssignment(current_target_byte, current_result);
                            instructionBody.addLast(currentByte_assignment_statement);
                            equal_results[i] =
                                    ExpressionFactory.createEqual(current_result, ExpressionFactory.createNumber(0, 8));

                            storage_operand1_address = ExpressionFactory.createPlus(storage_operand1_address, rtl_number[1]);
                            storage_operand2_address = ExpressionFactory.createPlus(storage_operand2_address, rtl_number[1]);
                        }
                        instructionBody.addLast(
                                new RTLVariableAssignment(zf, ExpressionFactory.createOperation(Operator.AND, equal_results)));
                        instructionBody.addLast(
                                new RTLVariableAssignment(nf, ExpressionFactory.createNot(zf)));
                        instructionBody.addLast(
                                new RTLVariableAssignment(pf, FALSE));
                        instructionBody.addLast(
                                new RTLVariableAssignment(of, FALSE));
                        setCC(instructionBody);
                        break;
                    }
                }
                break;
            }
        }
        return instructionBody;
    }

    private static StatementSequence translateSSb(ZInstruction instruction) {
        return null;
    }

    private static StatementSequence translateI(ZInstruction instruction) {
        StatementSequence instructionBody = new StatementSequence();

        Immediate immediate = (Immediate) instruction.getOperand1();
        RTLVariable r0 = ExpressionFactory.createVariable("R0", pc_bit_width);
        RTLVariable r1 = ExpressionFactory.createVariable("R1", pc_bit_width);
        RTLVariable r15 = ExpressionFactory.createVariable("R15", pc_bit_width);

        switch (immediate.getNumber().intValue()) {
            case 10: {
                RTLVariable temp = ExpressionFactory.createVariable("temp", 32);
                instructionBody.addLast(new RTLAlloc(temp, "storage_area_from_GETMAIN" + getmain_storage_count));
                getmain_storage_count++;
                instructionBody.addLast(new RTLVariableAssignment(r1,
                        ExpressionFactory.createConditionalExpression(ExpressionFactory.createLessThan(r1, rtl_number[0]),
                                temp, r1)));
                break;
            }
            //TODO: need to be fixed!
            case 0:
            case 1:
            case 7:
            case 12:
            case 13:
            case 16: {
                break;
            }
        }
        return instructionBody;
    }

    private static StatementSequence translateS(ZInstruction instruction) {
        StatementSequence instructionBody = new StatementSequence();

        ZStorageOperand operand1 = (ZStorageOperand) instruction.getOperand1();
        int displacement = (int) operand1.getDisplacement();
        Register base_register = operand1.getBase();
        RTLVariable base = ExpressionFactory.createVariable(base_register.toString(), pc_bit_width);

        RTLExpression storage_operand_address = ExpressionFactory.createNumber(displacement, 64);
        if (base_register.getNumber() != 0)
            storage_operand_address = ExpressionFactory.createPlus(storage_operand_address, base);

        switch (instruction.getMnemonic()) {
            case "SPKA": {
                RTLBitRange psw_key = ExpressionFactory.createBitRange(psw, rtl_number[8], rtl_number[11]);
                RTLBitRange storage_bits56_59 = ExpressionFactory.createBitRange(storage_operand_address, rtl_number[56], rtl_number[59]);
                instructionBody.addLast(new AssignmentTemplate(4, psw_key, storage_bits56_59));
                break;
            }
            case "SSM": {
                RTLBitRange psw_system_mask = ExpressionFactory.createBitRange(psw, rtl_number[0], rtl_number[7]);
                RTLMemoryLocation storage_byte = ExpressionFactory.createMemoryLocation(storage_operand_address, 8);
                instructionBody.addLast(new AssignmentTemplate(8, psw_system_mask, storage_byte));
                break;
            }
        }
        return instructionBody;
    }

    private static StatementSequence translateGeneral(ZInstruction instruction) {
        return null;
    }
}
