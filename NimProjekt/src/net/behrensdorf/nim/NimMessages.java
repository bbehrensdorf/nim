package net.behrensdorf.nim;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

public class NimMessages {
	
//	static {
//		String goodNames = getServletContext().getRealPath("/resources/good_messages.txt");
//		GOOD_MESSAGES = fileToArray(goodNames);
//	}

	static String[] fileToArray(String fileName) {
		Path filePath = Paths.get(fileName);
		List<String> stringList = null;
		try {
			stringList = Files.readAllLines(filePath, Charset.forName("UTF-8"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return stringList.toArray(new String[] {});

	}
	static void readGoodMessages(String fileName) {
		
		GOOD_MESSAGES=fileToArray(fileName);
	}
	static void readBadMessages(String fileName) {
		
		BAD_MESSAGES=fileToArray(fileName);
	}
	
	static void readStartMessages(String fileName) {
		
		START_MESSAGES=fileToArray(fileName);
	}

	public static String[] BAD_MESSAGES = {};
	public static String[] GOOD_MESSAGES={};
	public static String[] START_MESSAGES = {};
	static final Random rnd = new Random();

	public static String getBadMessage() {
		return BAD_MESSAGES[rnd.nextInt(BAD_MESSAGES.length)];
	}

	public static String getGoodMessage() {
		return GOOD_MESSAGES[rnd.nextInt(GOOD_MESSAGES.length)];
	}

	public static String getStartMessage() {
		return START_MESSAGES[rnd.nextInt(START_MESSAGES.length)];
	}
}
