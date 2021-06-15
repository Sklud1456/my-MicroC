/* File MicroC/Machine.java
   A unified-stack abstract machine for imperative programs.
   sestoft@itu.dk * 2001-03-21, 2009-09-24

   To execute a program file using this abstract machine, do:

      java Machine <programfile> <arg1> <arg2> ...

   or, to get a trace of the program execution:

      java Machinetrace <programfile> <arg1> <arg2> ...

*/

import Type.*;
import java.io.*;
import java.util.ArrayList;
import java.util.regex.Pattern;

class Machine {
  public static void main(String[] args)        
    throws FileNotFoundException, IOException {
    if (args.length == 0) 
      System.out.println("Usage: java Machine <programfile> <arg1> ...\n");
    else
      execute(args, false);
  }

  // These numeric instruction codes must agree with Machine.fs:

  final static int 
    CSTI = 0, ADD = 1, SUB = 2, MUL = 3, DIV = 4, MOD = 5, 
    EQ = 6, LT = 7, NOT = 8, 
    DUP = 9, SWAP = 10, 
    LDI = 11, STI = 12, 
    GETBP = 13, GETSP = 14, INCSP = 15, 
    GOTO = 16, IFZERO = 17, IFNZRO = 18, CALL = 19, TCALL = 20, RET = 21, 
    PRINTI = 22, PRINTC = 23, 
    LDARGS = 24,
    STOP = 25,
    CSTF =26, 
    CSTC =27;


  final static int STACKSIZE = 1000;
  
  // Read code from file and execute it

  static void execute(String[] args, boolean trace) throws FileNotFoundException, IOException {
    ArrayList<Integer> program = readfile(args[0]);// Read the program from file
    BaseType[] stack = new BaseType[STACKSIZE];// The evaluation stack
    BaseType[] inputargs = new BaseType[args.length - 1];
//    int[] p = readfile(args[0]);
//    int[] s = new int[STACKSIZE];
//    int[] iargs = new int[args.length-1];
//    for (int i=1; i<args.length; i++)           // Push commandline arguments
//      iargs[i-1] = Integer.parseInt(args[i]);
    for (int i = 1; i < args.length; i++) {
      if(Pattern.compile("(?i)[a-z]").matcher(args[i]).find()){
        char[] input = args[i].toCharArray();
        CharType[] array = new CharType[input.length];
        for(int j = 0; j < input.length; ++j) {
          array[j] = new CharType(input[j]);
        }
//        inputArgs[i-1] = new CubyArrayType(array);
      }
      else if(args[i].contains(".")){
        inputargs[i-1] = new FloatType(new Float(args[i]).floatValue());
      }
      else {
        inputargs[i-1] = new IntType(new Integer(args[i]).intValue());
      }
    }
    long starttime = System.currentTimeMillis();
    execcode(program, stack, inputargs, trace);            // Execute program proper
    long runtime = System.currentTimeMillis() - starttime;
    System.err.println("\nRan " + runtime/1000.0 + " seconds");
  }

  // The machine: execute the code starting at p[pc] 

  static int execcode(ArrayList<Integer> program, BaseType[] stack, BaseType[] inputargs, boolean trace) {
    int bp = -999;	// Base pointer, for local variable access 
    int sp = -1;	// Stack top pointer
    int pc = 0;		// Program counter: next instruction
    for (;;) {
      if (trace) 
        printsppc(stack, bp, sp, program, pc);
      switch (program.get(pc++)) {
        case CSTI:
          stack[sp + 1] = new IntType(program.get(pc++)); sp++; break;
        case CSTF:
          stack[sp + 1] = new FloatType(Float.intBitsToFloat(program.get(pc++))); sp++; break;
        case CSTC:
          stack[sp + 1] = new CharType((char)(program.get(pc++).intValue())); sp++; break;
        case ADD:{
          stack[sp - 1] = binaryOperator(stack[sp-1], stack[sp], "+");
          sp--;
          break;
        }
        case SUB:{
          stack[sp - 1] = binaryOperator(stack[sp-1], stack[sp], "-");
          sp--;
          break;
        }
        case MUL: {
          stack[sp - 1] = binaryOperator(stack[sp-1], stack[sp], "*");
          sp--;
          break;
        }
        case DIV:
          stack[sp - 1] = binaryOperator(stack[sp-1], stack[sp], "/");
          sp--;
          break;
        case MOD:
          stack[sp - 1] = binaryOperator(stack[sp-1], stack[sp], "%");
          sp--;
          break;
        case EQ:
          stack[sp - 1] = binaryOperator(stack[sp-1], stack[sp], "==");
          sp--;
          break;
        case LT:
          stack[sp - 1] = binaryOperator(stack[sp-1], stack[sp], "<");
          sp--;
          break;
        case NOT: {
          Object result = null;
          if(stack[sp] instanceof FloatType){
            result = ((FloatType)stack[sp]).getValue();
          }else if (stack[sp] instanceof IntType){
            result = ((IntType)stack[sp]).getValue();
          }
          stack[sp] = (Float.compare(new Float(result.toString()), 0.0f) == 0 ? new IntType(1) : new IntType(0));
          break;
        }
        case DUP:
          stack[sp+1] = stack[sp];
          sp++;
          break;
        case SWAP: {
          BaseType tmp = stack[sp];  stack[sp] = stack[sp-1];  stack[sp-1] = tmp;
          break;
        }
        case LDI:
          stack[sp] = stack[((IntType)stack[sp]).getValue()]; break;
        case STI:
          stack[((IntType)stack[sp-1]).getValue()] = stack[sp]; stack[sp-1] = stack[sp]; sp--; break;
        case GETBP:
          stack[sp+1] = new IntType(bp); sp++; break;
        case GETSP:
          stack[sp+1] = new IntType(sp); sp++; break;
        case INCSP:
          sp = sp + program.get(pc++); break;
        case GOTO:
          pc = program.get(pc); break;
        case IFZERO: {
          Object result = null;
          int index = sp--;
          if(stack[index] instanceof IntType){
            result = ((IntType)stack[index]).getValue();
          }else if(stack[index] instanceof FloatType){
            result = ((FloatType)stack[index]).getValue();
          }
          pc = (Float.compare(new Float(result.toString()), 0.0f) == 0 ? program.get(pc) : pc + 1);
          break;
        }
        case IFNZRO: {
          Object result = null;
          int index = sp--;
          if (stack[index] instanceof IntType) {
            result = ((IntType) stack[index]).getValue();
          } else if (stack[index] instanceof FloatType) {
            result = ((FloatType) stack[index]).getValue();
          }
          pc = (Float.compare(new Float(result.toString()), 0.0f) != 0 ? program.get(pc) : pc + 1);
          break;
        }
        case CALL: {
          int argc = program.get(pc++);
          for (int i=0; i<argc; i++)
            stack[sp-i+2] = stack[sp-i];
          stack[sp-argc+1] = new IntType(pc+1); sp++;
          stack[sp-argc+1] = new IntType(bp);   sp++;
          bp = sp+1-argc;
          pc = program.get(pc);
          break;
        }
        case TCALL: {
          int argc = program.get(pc++);
          int pop  = program.get(pc++);
          for (int i=argc-1; i>=0; i--)
            stack[sp-i-pop] = stack[sp-i];
          sp = sp - pop; pc = program.get(pc);
        } break;
        case RET: {
          BaseType res = stack[sp];
          sp = sp - program.get(pc); bp = ((IntType)stack[--sp]).getValue(); pc = ((IntType)stack[--sp]).getValue();
          stack[sp] = res;
        } break;
        case PRINTI: {
          Object result;
          if(stack[sp] instanceof IntType){
            result = ((IntType)stack[sp]).getValue();
          }else if(stack[sp] instanceof FloatType){
            result = ((FloatType)stack[sp]).getValue();
          }else {
            result = ((CharType)stack[sp]).getValue();
          }

          System.out.print(String.valueOf(result) + " ");
          break;
        }
        case PRINTC:
          System.out.print((new CharType(stack[sp])).getValue()); break;
        case LDARGS:
          for (int i=0; i < inputargs.length; i++) // Push commandline arguments
            stack[++sp] = inputargs[i];
          break;
        case STOP:
          return sp;
      default:                  
        throw new RuntimeException("Illegal instruction " + program.get(pc-1)
                                   + " at address " + (pc-1));
      }
    }
  }

  public static BaseType binaryOperator(BaseType lhs, BaseType rhs, String operator) {
    Object left = null;
    Object right =null;
    int flag = 0;
    if (lhs instanceof FloatType) {
      left = ((FloatType) lhs).getValue();
      flag = 1;
    } else if (lhs instanceof IntType) {
      left = ((IntType) lhs).getValue();
    }

    if (rhs instanceof FloatType) {
      right = ((FloatType) rhs).getValue();
      flag = 1;
    } else if (rhs instanceof IntType) {
      right = ((IntType) rhs).getValue();
    }
    BaseType result = null;

    switch(operator){
      case "+":{
        if (flag == 1) {
          result =  new FloatType(Float.parseFloat(String.valueOf(left)) + Float.parseFloat(String.valueOf(right)));
        } else {
          result = new IntType(Integer.parseInt(String.valueOf(left)) + Integer.parseInt(String.valueOf(right)));
        }
        break;
      }
      case "-":{
        if (flag == 1) {
          result = new FloatType(Float.parseFloat(String.valueOf(left)) - Float.parseFloat(String.valueOf(right)));
        } else {
          result = new IntType(Integer.parseInt(String.valueOf(left)) - Integer.parseInt(String.valueOf(right)));
        }
        break;
      }
      case "*":{
        if (flag == 1) {
          result = new FloatType(Float.parseFloat(String.valueOf(left)) * Float.parseFloat(String.valueOf(right)));
        } else {
          result = new IntType(Integer.parseInt(String.valueOf(left)) * Integer.parseInt(String.valueOf(right)));
        }
        break;
      }
      case "/":{
//        if(Float.compare(Float.parseFloat(String.valueOf(right)), 0.0f) == 0){
//          throw new OperatorError("OpeatorError: Divisor can't not be zero");
//        }
        if (flag == 1) {
          result = new FloatType(Float.parseFloat(String.valueOf(left)) / Float.parseFloat(String.valueOf(right)));
        } else {
          result = new IntType(Integer.parseInt(String.valueOf(left)) / Integer.parseInt(String.valueOf(right)));
        }
        break;
      }
      case "%":{

          result = new IntType(Integer.parseInt(String.valueOf(left)) % Integer.parseInt(String.valueOf(right)));

        break;
      }
      case "==":{
        if (flag == 1) {
          if((float) left == (float) right){
            result = new IntType(1);
          }
          else{
            result = new IntType(0);
          }
        } else {
          if((int) left == (int) right){
            result = new IntType(1);
          }
          else{
            result = new IntType(0);
          }
        }
        break;
      }
      case "<":{
        if (flag == 1) {
          if((float) left < (float) right){
            result = new IntType(1);
          }
          else{
            result = new IntType(0);
          }
        } else {
          if((int) left < (int) right){
            result = new IntType(1);
          }
          else{
            result = new IntType(0);
          }
        }
        break;
      }
    }
    return result;
  }
  // Print the stack machine instruction at p[pc]

  static String insname(ArrayList<Integer>program, int pc) {
    switch (program.get(pc)) {
      case CSTI:   return "CSTI " + program.get(pc+1);
      case CSTF:   return "CSTF"  + program.get(pc+1);
      case CSTC:   return "CSTC " + (char)(program.get(pc+1).intValue());
      case ADD:    return "ADD";
      case SUB:    return "SUB";
      case MUL:    return "MUL";
      case DIV:    return "DIV";
      case MOD:    return "MOD";
      case EQ:     return "EQ";
      case LT:     return "LT";
      case NOT:    return "NOT";
      case DUP:    return "DUP";
      case SWAP:   return "SWAP";
      case LDI:    return "LDI";
      case STI:    return "STI";
      case GETBP:  return "GETBP";
      case GETSP:  return "GETSP";
      case INCSP:  return "INCSP " + program.get(pc+1);
      case GOTO:   return "GOTO " + program.get(pc+1);
      case IFZERO: return "IFZERO " + program.get(pc+1);
      case IFNZRO: return "IFNZRO " + program.get(pc+1);
      case CALL:   return "CALL " + program.get(pc+1) + " " + program.get(pc+2);
      case TCALL:  return "TCALL " + program.get(pc+1) + " " + program.get(pc+2) + " " +program.get(pc+3);
      case RET:    return "RET " + program.get(pc+1);
      case PRINTI: return "PRINTI";
      case PRINTC: return "PRINTC";
      case LDARGS: return "LDARGS";
      case STOP:   return "STOP";
      default:     return "<unknown>";
    }
  }

  // Print current stack and current instruction

  static void printsppc(BaseType[] stack, int bp, int sp, ArrayList<Integer>program, int pc) {
    System.out.print("[ ");
    for (int i = 0; i <= sp; i++) {
      Object result = null;
      if(stack[i] instanceof IntType){
        result = ((IntType)stack[i]).getValue();
      }else if(stack[i] instanceof FloatType){
        result = ((FloatType)stack[i]).getValue();
      }else if(stack[i] instanceof CharType){
        result = ((CharType)stack[i]).getValue();
      }
      System.out.print(String.valueOf(result) + " ");
    }
    System.out.print("]");
    System.out.println("{" + pc + ": " + insname(program, pc) + "}");
  }

  // Read instructions from a file

  public static ArrayList<Integer> readfile(String filename) throws FileNotFoundException, IOException {
    ArrayList<Integer> program = new ArrayList<Integer>();
    Reader inp = new FileReader(filename);

    StreamTokenizer tStream = new StreamTokenizer(inp);
    tStream.parseNumbers();
    tStream.nextToken();
    while (tStream.ttype == StreamTokenizer.TT_NUMBER) {
      program.add(new Integer((int)tStream.nval));
      tStream.nextToken();
    }

    inp.close();

    return program;
  }
}

// Run the machine with tracing: print each instruction as it is executed

class Machinetrace {
  public static void main(String[] args)        
    throws FileNotFoundException, IOException {
    if (args.length == 0) 
      System.out.println("Usage: java Machinetrace <programfile> <arg1> ...\n");
    else
      Machine.execute(args, true);
  }
}
