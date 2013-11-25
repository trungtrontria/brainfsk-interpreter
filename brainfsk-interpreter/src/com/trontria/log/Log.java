package com.trontria.log;

public class Log {
	public static void debug(String tag, String msg) {
		StringBuilder builder = new StringBuilder();
		builder.append("[").append(tag).append("] ").append(msg);
		System.out.println(builder.toString());
	}
}
