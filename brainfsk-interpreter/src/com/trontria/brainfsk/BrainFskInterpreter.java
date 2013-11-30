package com.trontria.brainfsk;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.trontria.log.Log;

public class BrainFskInterpreter {
	public static final String TAG = BrainFskInterpreter.class.getSimpleName();
	
	public static class Register {
		public static final int REG_SRC_PTS = 0x00;
		public static final int REG_DAT_PTS = 0x01;
	}
	
	private String memSource;
	private List<Integer> memData; 
	private List<Integer> register;
	private List<Integer> conditionStack;
	
	public BrainFskInterpreter() {
		reset();
		Log.debug(false);
	}
	
	public void reset() {
		Log.debug(TAG, "Initialize");
		
		// Data of the memory, mostly pointers
		memData = new ArrayList<>();
		
		// List of the registers, contains data
		if (register == null) {
			register = new ArrayList<Integer>();
			register.add(0);
			register.add(0);
		}
		setRegisterValue(Register.REG_DAT_PTS, 0);
		
		// Contains the loop list
		conditionStack = new LinkedList<>();
	}
	private void interprete() {
		BrainFskToken token = null;
		while ((token = nextToken()) != null) {
			if (token.getTokenType() == BrainFskToken.TokenType.TOKEN_MOVE_RIGHT) {
				datNext();
				srcNext();
			} else if (token.getTokenType() == BrainFskToken.TokenType.TOKEN_MOVE_LEFT) {
				datPrev();
				srcNext();
			} else if (token.getTokenType() == BrainFskToken.TokenType.TOKEN_INC) {
				inc();
				srcNext();
			} else if (token.getTokenType() == BrainFskToken.TokenType.TOKEN_DEC) {
				dec();
				srcNext();
			} else if (token.getTokenType() == BrainFskToken.TokenType.TOKEN_OUTPUT) {
				out();
				srcNext();
			} else if (token.getTokenType() == BrainFskToken.TokenType.TOKEN_DUMP) {
				dump();
				srcNext();
			} else if (token.getTokenType() == BrainFskToken.TokenType.TOKEN_INPUT) {
				set(in());
				srcNext();
			} else if (token.getTokenType() == BrainFskToken.TokenType.TOKEN_RESET) {
				reset();
				srcNext();
			} else if (token.getTokenType() == BrainFskToken.TokenType.TOKEN_CONDITION_OPEN) {
				Log.debug(TAG, "conditionOpen@" + currentSrcPts());
				if (equalZero()) {
					Log.debug(TAG, "zero");
					int index = 0;
					while (!isEmpty() && ((index = peek()) >= currentSrcPts() )) {
						pop();
						Log.debug(TAG, "Popped " + index);
						Log.debug(TAG, "stack@" + conditionStack.size());
					}
					skipTillOut();
				} else {
					Log.debug(TAG, "noZero");
					if (isEmpty() || currentSrcPts() != peek()) {
						push(currentSrcPts());
						Log.debug(TAG, "Pushed " + currentSrcPts());
					}
					Log.debug(TAG, "peek@" + peek());
					srcNext();
				}
			} else if (token.getTokenType() == BrainFskToken.TokenType.TOKEN_CONDITION_CLOSE) {
				Log.debug(TAG, "conditionClose@" + currentSrcPts());
				srcGoto(peek());
			} else {
				// Do nothing, just skip
				srcNext();
			}
		}
	}
	public void interprete(String sourceString, boolean continueInterprete) {
		Log.debug(TAG, "About to interprete: " + sourceString);
		memSource = sourceString;
		srcGoto(0);
		if (!continueInterprete)
			reset();
		interprete();
	}
	
	private BrainFskToken nextToken() {
		// No more to read, return null to signal eof
		if (currentSrcPts() < 0 || currentSrcPts() >= memSource.length()) 
			return null;
		
		// Read next token
		char c = memSource.charAt(currentSrcPts());
		BrainFskToken token = BrainFskToken.fromChar(c);
		return token;
	}
	// ========================================================================
	// Conditonal operations
	// ========================================================================
	private void push(int offset) {
		conditionStack.add(offset);
	}
	private int pop() {
		int offset = peek();
		conditionStack.remove(conditionStack.size() - 1);
		return offset;
	}
	private int peek() {
		int offset = conditionStack.get(conditionStack.size() - 1);
		return offset;
	}
	
	private boolean isEmpty() {
		return conditionStack.isEmpty();
	}
	
	private void skipTillOut() {
		srcNext();
		int count = 1;
		BrainFskToken token = null;
		while (count > 0 ) {
			token = nextToken();
			if (token.getTokenType() == BrainFskToken.TokenType.TOKEN_CONDITION_OPEN) {
				count++;
				Log.debug(TAG, "Count @" + count);
			} else if (token.getTokenType() == BrainFskToken.TokenType.TOKEN_CONDITION_CLOSE) {
				count--;
				Log.debug(TAG, "Count @" + count);
			}
			srcNext();
			Log.debug(TAG, "Now @" + currentSrcPts());
		}
	}
	// ========================================================================
	// Dump registers
	// ========================================================================
	public void dump() {
		System.out.println();
		Log.log(TAG, "= DUMP DATA =====================================");
		Log.log(TAG, "Registers: ");
		for (int i = 0; i < register.size(); i++) {
			String holder = "PTS @0x%02x: %d";
			Log.log(TAG, String.format(holder, i, getRegisterValue(i)));
		}
		Log.log(TAG, "Data: ");
		for (int i = 0; i < memData.size(); i++) {
			String holder = "DAT @0x%04x: %d";
			Log.log(TAG, String.format(holder, i, memData.get(i)));
		}
		Log.log(TAG, "= END DUMP DATA =================================");
	}
	// ========================================================================
	// Data
	// ========================================================================
	private void out() {
		try {
			getOutStream().write(get());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private int in() {
		try {
			return getInStream().read();
		} catch (IOException e) {
			e.printStackTrace();
			return '\0';
		}
	}
	private void inc() {
//		Log.debug(TAG, "inc");
		set(get() + 1);
	}
	private void dec() {
//		Log.debug(TAG, "dec");
		set(get() - 1);
	}
	private int get() {
		if (currentDatPts() >= memData.size() || currentDatPts() < 0) {
			return 0;
		}
		return memData.get(currentDatPts());
	}
	private int set(int value) {
		while (currentDatPts() >= memData.size()) {
			memData.add(0);
		}
		return memData.set(currentDatPts(), value);
	}
	private boolean equalZero() {
		return get() == 0;
	}
	// ========================================================================
	// IO Stream
	// ========================================================================
	private InputStream in;
	private OutputStream out;

	public InputStream getInStream() {
		if (in == null) in = System.in;
		return in;
	}

	public void setInStream(InputStream in) {
		this.in = in;
	}
	
	public OutputStream getOutStream() {
		if (out == null) out = System.out;
		return out;
	}

	public void setOutStream(OutputStream out) {
		this.out = out;
	}
	// ========================================================================
	// Pointer
	// ========================================================================
	private int currentSrcPts() {
		return getRegisterValue(Register.REG_SRC_PTS);
	}
	private int currentDatPts() {
		return getRegisterValue(Register.REG_DAT_PTS);
	}
	private void srcGoto(int offset) {
		setRegisterValue(Register.REG_SRC_PTS, offset);
	}
	private void srcNext() {
		moveNext(Register.REG_SRC_PTS);
	}
	private void srcPrev() {
		movePrevious(Register.REG_SRC_PTS);
	}
	
	private void datNext() {
		moveNext(Register.REG_DAT_PTS);
		
//		Log.debug(TAG, "datNext" + currentDatPts());
		// Add the data so there'll be no index out of bound
		while (currentDatPts() >= memData.size()) {
			memData.add(0);
		}
	}
	private void datPrev() {
		movePrevious(Register.REG_DAT_PTS);
//		Log.debug(TAG, "datPrev" + currentDatPts());
	}
	
	private void moveNext(int regId) {
		setRegisterValue(regId, getRegisterValue(regId) + 1);
	}
	private void movePrevious(int regId) {
		setRegisterValue(regId, getRegisterValue(regId) - 1);
	}
	
	public int getRegisterValue(int regId) {
		return register.get(regId);
	}
	public void setRegisterValue(int regId, int value) {
		register.set(regId, value);
	}
}