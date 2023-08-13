package textExcel;

import textExcel.Location;
import java.util.ArrayList;

public class SpreadsheetLocation implements Location
{

    private int row;
    private int col;
    public String raw;

    @Override
    public int getRow()
    {
        return row;
    }

    @Override
    public int getCol()
    {
        return col;
    }
    
    @Override
    public String getRaw(){
        return raw;
    }

    public SpreadsheetLocation(String cellName)
    {
        raw = cellName;
        //Split into list of characters, max of 2 elements
        String[] parts = cellName.split("(?!^)", 2);
        //System.out.println(parts[0]);
        //System.out.println(Character.getNumericValue(parts[0].charAt(0)));
        //Get the column as a letter
        col = Character.getNumericValue(parts[0].charAt(0))-9;
        //Get the row as a number
        row = Integer.parseInt(parts[1]);
    }

}
