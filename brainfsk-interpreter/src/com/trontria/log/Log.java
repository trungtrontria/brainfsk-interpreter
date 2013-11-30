package com.trontria.log;

public class Log {
	private static boolean on;
	public static void debug(boolean on) {
		Log.on = on;
	}
	public static void debug(String tag, String msg) {
		if (!on) return;
		
		StringBuilder builder = new StringBuilder();
		builder.append("[").append(tag).append("] ").append(msg);
		System.out.println(builder.toString());
	}
	public static void log(String tag, String msg) {
		StringBuilder builder = new StringBuilder();
		builder.append("[").append(tag).append("] ").append(msg);
		System.out.println(builder.toString());
	}
}
