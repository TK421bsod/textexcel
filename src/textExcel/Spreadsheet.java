package textExcel;

import textExcel.Grid;
import textExcel.Cell;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

public class Spreadsheet implements Grid
{

	public ArrayList<ArrayList<Cell>> layout = new ArrayList<ArrayList<Cell>>();

	private Boolean save(String filename){
		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter(filename));
			for (int i = 0; i < layout.size(); i++){
				ArrayList<Cell> row = layout.get(i);
				for (int j = 0; j < row.size(); j++){
					Cell c = row.get(j);
					if (c instanceof EmptyCell){
						continue;
					}
					String cellIdentifier = Character.toString((char) (j+65)) + (i+1);
					System.out.println("Saving cell " + cellIdentifier + " ");
					String cellType = c.getClass().getSimpleName();
					String fullCellText = c.fullCellText();
					out.write(cellIdentifier+","+cellType+","+fullCellText);
					out.newLine();
				}
			}
			out.flush();
			out.close();
		} catch (IOException e){
			System.out.println("Sorry, something went wrong when saving data.");
			return false;
		}
		System.out.println("Done.");
		return true;
	}

	private Boolean open(String filename){
		Scanner inputFile;
		try{
			inputFile = new Scanner(new File(filename));
		} catch (FileNotFoundException e){
			System.out.println("That file wasn't found.");
			return false;
		}
		while (inputFile.hasNextLine()){
			String line = inputFile.nextLine();
			String[] values = line.split(",");
			String cellIdentifier = values[0];
			String cellType = values[1];
			String fullCellText = values[2];
			setCell(cellIdentifier, fullCellText);
		}
		inputFile.close();
		return true;
	}

	public void clear(Cell targetCell){
		setCell(targetCell.getLocation().getRaw());
	}

	public void clear(){
		//Just re-initialize the spreadsheet.
		initializeSpreadsheet();
		System.out.println("Done clearing the spreadsheet.");
	}

	public Boolean hasRequiredArguments (String[] tokens, int required){
		if (tokens.length < required+1){
			System.out.println("\nSorry, this command takes at least one more parameter.");
			return false;
		}
		return true;
	}

	@Override
	public String processCommand(String command)
	{
		//Split the input into chunks, up to 3
		// e.g "A4 = ( B1 + B2 )" -> ["A4", "=", "( B1 + B2 )"]
		String[] tokens = command.split(" ", 3);
		System.out.println();
		//Check for specific commands first.
		//quit, save, open, clear
		switch (tokens[0].toLowerCase()){
			case "quit":
				return "quit";
			
			case "save":
				save(tokens[1]);
				System.out.println(getGridText());
				return "save";
			
			case "open":
				if (! hasRequiredArguments(tokens, 1)){
					return "";
				}
				Boolean ret = open(tokens[1]);
				if (!ret){
					System.out.println("Couldn't open that file.");
					return "";
				}
				System.out.println(getGridText());
				return "open";

			case "clear":
				//were we provided a cell name?
				if (tokens.length > 1){
					//is it valid?
					Cell target;
					try{
						target = getCell(tokens[1]);
					} catch (Exception e){
						System.out.println("Sorry, the cell name '" + tokens[1] + "' is invalid.");
						return "";
					}
					clear(target);
				} else {
					clear();
				}
				System.out.println(getGridText());
				return "clear";
		}
		//Not a command? The user may be referring to a specific Cell.
		//is the provided Cell name valid?
		Cell target;
		try {
			target = getCell(tokens[0].toUpperCase());
		} catch (Exception e){
			System.out.println("Sorry, the cell name '" + tokens[0] + "' is invalid.");
			return "";
		}
		//are we assigning a value to a Cell?
		try{
			if (tokens[1].equals("=")){
				if (tokens.length < 3){
					//nothing provided? reset to EmptyCell
					setCell(tokens[0]);
					System.out.println(getGridText());
					return "assign";
				}
				if (tokens[2].contains("\"")){
					if (tokens[2].length() - tokens[2].replace("\"", "").length() < 2){
						System.out.println("Sorry, there are unclosed quotes in that cell value.");
						return "";
					}
				}
				setCell(tokens[0], tokens[2]);
				System.out.println(getGridText());
				return "assign";
			} 
		} catch (IndexOutOfBoundsException e){
			;
		}
		System.out.println(target.fullCellText());
		return "print";
	}

	@Override
	public Cell getCell(Location loc) throws IndexOutOfBoundsException
	{
		//System.out.println("Getting Cell at row " + loc.getRow() + ", column " + loc.getCol());
		return layout.get(loc.getRow()-1).get(loc.getCol()-1);
	}

	public Cell getCell(String name) throws IndexOutOfBoundsException{
		//System.out.println("Creating new SpreadsheetLocation for cell " + name);
		SpreadsheetLocation loc = new SpreadsheetLocation(name);
		return getCell(loc);
	}

	public Cell setCell(String name, String value){
		System.out.println("Setting cell " + name + " to " + value);
		SpreadsheetLocation loc = new SpreadsheetLocation(name);
		Cell newCell;
		try{
			newCell = initializeCell(name, Double.parseDouble(value));
		} catch (Exception e) {
			newCell = initializeCell(name, value);
		}
		//Replace the existing row with a new row containing the new Cell
		layout.get(loc.getRow()-1).set(loc.getCol()-1, newCell);
		return newCell;
	}

	public Cell setCell(String name){
		SpreadsheetLocation loc = new SpreadsheetLocation(name);
		Cell newCell = initializeCell(name);
		layout.get(loc.getRow()-1).set(loc.getCol()-1, newCell);
		return newCell;
	}

	@Override
	public String getGridText()
	{
		String gridText = "   ";
		//header
		for (int i = 1; i <= getCols(); i++){
			gridText += "|" + (char) (i+64) + "         |";
		}
		gridText += "\n";
		//rest of grid
		String rowString;
		for (ArrayList<Cell> row : layout){
			//get our row number as a String
			rowString = Integer.toString(layout.indexOf(row)+1);
			//pad
			for (int spaces = 1; spaces <= 3 - rowString.toCharArray().length;){
				rowString += " ";
			}
			//add columns to rows
			for (Cell cell : row){
				//add padded / truncated cell text
				rowString +=  "|" + cell.abbreviatedCellText() + "|";
			}
			rowString += "\n";
			gridText += rowString;
		}
		return gridText;
	}

	@Override
	public int getRows()
	{
		return 20;
	}

	@Override
	public int getCols()
	{
		return 12;
	}

	public Cell initializeCell(String cellName, String value){
		if (value.contains("\"")){
			//System.out.println("Creating TextCell");
			return new TextCell(cellName, value);
		} else if (value.contains("%")){
			//System.out.println("Creating PercentCell");
		    return new PercentCell(cellName, value);
		} else if (value.contains("(")){
            //System.out.println("Creating FormulaCell");
            return new FormulaCell(cellName, value, this);
		}
		System.out.println("Sorry, that cell value is invalid.");
        return new EmptyCell(cellName);
	}

	public Cell initializeCell(String cellName, double value){
		//System.out.println("Creating ValueCell");
		return new ValueCell(cellName, value);
	}

	public Cell initializeCell(String cellName){
		//System.out.println("Creating EmptyCell");
		return new EmptyCell(cellName);
	}

	public void initializeSpreadsheet(){
		//initialize grid to an empty ArrayList
		layout = new ArrayList<ArrayList<Cell>>();
		System.out.println("Initializing spreadsheet.");
		for (int row = 1; row <= getRows(); row++){
			//initialize a new row
			ArrayList newRow = new ArrayList<Cell>(); 
			for (int col = 1; col <= getCols(); col++){
				//this might work?
				String cellName = Character.toString((char) (col+64)) + row;
				//System.out.println("Initializing new Cell with name '" + cellName + "'.");
				//System.out.println("Row " + row + ", Column " + col + "\n");
				newRow.add(initializeCell(cellName));
			}
			layout.add(newRow);
		}
		System.out.println("Done.");
	}

	public Spreadsheet(){
		initializeSpreadsheet();
	}

}
