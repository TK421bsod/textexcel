package textExcel;

import textExcel.Spreadsheet;
import java.util.Scanner;

// Update this file with your own code.

public class TextExcel
{

	public static Spreadsheet mainSpreadsheet;
	private static Scanner input = new Scanner(System.in);

	public static void main(String[] args)
	{
		mainSpreadsheet = new Spreadsheet();
		System.out.println(mainSpreadsheet.getGridText());
	    while (true){
			System.out.print("> ");
			String command = input.nextLine();
			String ret = mainSpreadsheet.processCommand(command);
			if (ret.equals("quit")){
				break;
			}
		}
	}
}
