package com.trontria.brainfsk;

import java.io.IOException;
import java.util.ArrayList;

import com.trontria.log.Log;

public class BrainFskInterpreter {
	public static final String TAG = BrainFskInterpreter.class.getSimpleName();
	
	public static class Register {
		public static final int REG_SRC_PTS = 0x00;
		public static final int REG_DAT_PTS = 0x01;
	}
	
	private String memSource;
	private ArrayList<Integer> memData; 
	private ArrayList<Integer> register;
	
	public BrainFskInterpreter() {
		init("");
	}
	
	private void init(String source) {
		Log.debug(TAG, "Initialize");
		memSource = source;
		memData = new ArrayList<>();
		
		register = new ArrayList<Integer>();
		register.add(0);
		register.add(0);
	}
	
	public void interprete(String sourceString) {
		Log.debug(TAG, "About to interprete: " + sourceString);
		init(sourceString);
		
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
			} else if (token.getTokenType() == BrainFskToken.TokenType.TOKEN_INPUT) {
				set(in());
				srcNext();
			} else {
				// Do nothing, just skip
			}
		}
		
		// Dump final values
		dump();
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
	// Dump registers
	// ========================================================================
	public void dump() {
		Log.debug(TAG, "Registers: ");
		for (int i = 0; i < register.size(); i++) {
			Log.debug(TAG, "PTS @" + i + ": " + getRegisterValue(i));
		}
		Log.debug(TAG, "Data: ");
		for (int i = 0; i < memData.size(); i++) {
			Log.debug(TAG, "DAT @" + i + ": " + memData.get(i));
		}
	}
	// ========================================================================
	// Data
	// ========================================================================
	private void out() {
		System.out.print((char) get());
	}
	private int in() {
		try {
			return System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
			return '\0';
		}
	}
	private void inc() {
		// Log.debug(TAG, "inc");
		set(get() + 1);
	}
	private void dec() {
		// Log.debug(TAG, "dec");
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
		// Log.debug(TAG, "datNext");
		moveNext(Register.REG_DAT_PTS);
		
		// Add the data so there'll be no index out of bound
		while (currentDatPts() >= memData.size()) {
			memData.add(0);
		}
	}
	private void datPrev() {
		// sLog.debug(TAG, "datPrev");
		movePrevious(Register.REG_DAT_PTS);
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