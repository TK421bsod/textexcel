package textExcel;

import textExcel.Cell;
import textExcel.SpreadsheetLocation;
import java.lang.StringIndexOutOfBoundsException;

public class TextCell implements Cell {
    private String value = "";
    private String abbreviatedValue;
    public Location loc;

    @Override
    public Location getLocation(){
        return loc;
    }

    @Override
    public String abbreviatedCellText(){
        return abbreviatedValue;
    }

    @Override
    public String fullCellText(){
        return "\"" + value + "\"";
    }

    public TextCell(String name, String value){
        this.loc = new SpreadsheetLocation(name);
        value = value.replace("\"", "");
        this.value = value;
        try{
            this.abbreviatedValue = value.substring(0, 10);
        } catch (StringIndexOutOfBoundsException e){
            this.abbreviatedValue = value;
            for (int i = 10-value.toCharArray().length; i > 0; i--){
                this.abbreviatedValue += " ";
            }
        }
    }
}
