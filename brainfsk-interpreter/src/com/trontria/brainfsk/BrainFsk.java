/**
 * 
 */
package com.trontria.brainfsk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author TrungNQ
 *
 */
public class BrainFsk {
	private static void startShell(BrainFskInterpreter interpreter) {
		while (true) {
			System.out.print("\n> ");
			try{ // fibonacci.bf helloworld.bf
			    BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
			    String s = bufferRead.readLine();
			    if (s.toLowerCase().equals("exit")) {
			    	System.exit(0);
			    } else if (s.toLowerCase().equals("reset")) {
			    	interpreter.reset();
			    	continue;
			    }
			    interpreter.interprete(s, true);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BrainFskInterpreter interpreter = new BrainFskInterpreter();
		if (args.length > 0) {
			for (String arg : args) {
				if (arg.toLowerCase().equals("shell")) {
					startShell(interpreter);
					break;
				}
				String source = FileIO.readFile(arg);
				interpreter.interprete(source, true);
			}
		} else {
			startShell(interpreter);
		}
		
//		// Hello world
//		interpreter.interprete("++++++++++[>+++++++>++++++++++>+++>+<<<<-]>++.>+.+++++++..+++.>++.<<+++++++++++++++.>.+++.------.--------.>+.>.");
//		// 27
//		interpreter.interprete("+++[>+++[>+++<-]<-]#");
//		// Moving cell 0 to cell 4
//		interpreter.interprete("++++++++++>>>>[-]<<<<[->>>>+<<<<]#");
//		// Fibonacci
//		String source = FileIO.readFile("fibonacci.bf");
//		interpreter.interprete(source);
	}
}