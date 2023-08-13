package textExcel;

public class EmptyCell implements Cell {
    private String value = "";
    public Location loc;

    @Override
    public String abbreviatedCellText(){
        return "          ";
    }

    @Override
    public Location getLocation(){
        return loc;
    }

    public String fullCellText(){
        return value;
    }

    public EmptyCell(String name){
        loc = new SpreadsheetLocation(name);
    }
}
