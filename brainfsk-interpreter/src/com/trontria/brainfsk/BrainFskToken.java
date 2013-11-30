package com.trontria.brainfsk;

public class BrainFskToken {
	
	public static class TokenType {
		public static final int TOKEN_MOVE_RIGHT 		= 0x01;
		public static final int TOKEN_MOVE_LEFT 		= 0x02;
		public static final int TOKEN_INC 				= 0x03;
		public static final int TOKEN_DEC 				= 0x04;
		public static final int TOKEN_OUTPUT 			= 0x05;
		public static final int TOKEN_INPUT 			= 0x06;
		public static final int TOKEN_CONDITION_OPEN 	= 0x07;
		public static final int TOKEN_CONDITION_CLOSE 	= 0x08;
		public static final int TOKEN_DUMP				= 0x09;
		public static final int TOKEN_RESET				= 0x0A;
		public static final int TOKEN_UNKNOWN		 	= 0xff;
		
	}
	
	private int tokenType;
	
	/**
	 * Convert char into token, the chart is as followed: 
	 * 
	 * >	Move the pointer to the right
	 * <	Move the pointer to the left
	 * +	Increment the memory cell under the pointer
	 * -	Decrement the memory cell under the pointer
	 * .	Output the character signified by the cell at the pointer
	 * ,	Input a character and store it in the cell at the pointer
	 * [	Jump past the matching ] if the cell under the pointer is 0
	 * ]	Jump back to the matching [ if the cell under the pointer is nonzer
	 * 
	 * @param c
	 * @return
	 */
	public static BrainFskToken fromChar(char c) {
		int type = TokenType.TOKEN_UNKNOWN;
		switch (c) {
		case '>':
			type = TokenType.TOKEN_MOVE_RIGHT;
			break;
		case '<':
			type = TokenType.TOKEN_MOVE_LEFT;
			break;
		case '+':
			type = TokenType.TOKEN_INC;
			break;
		case '-':
			type = TokenType.TOKEN_DEC;
			break;
		case '[':
			type = TokenType.TOKEN_CONDITION_OPEN;
			break;
		case ']':
			type = TokenType.TOKEN_CONDITION_CLOSE;
			break;
		case '.':
			type = TokenType.TOKEN_OUTPUT;
			break;
		case ',':
			type = TokenType.TOKEN_INPUT;
			break;
		case '#':
			type = TokenType.TOKEN_DUMP;
			break;
		case '~':
			type = TokenType.TOKEN_RESET;
			break;
		default:
			type = TokenType.TOKEN_UNKNOWN;
			break;
		}
		
		return new BrainFskToken(type);
	}
	
	protected BrainFskToken(int type) {
		setTokenType(type);
	}

	public int getTokenType() {
		return tokenType;
	}

	public void setTokenType(int tokenType) {
		this.tokenType = tokenType;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BrainFskToken) {
			return ((BrainFskToken) obj).getTokenType() == getTokenType();
		}
		return false;
	}

}