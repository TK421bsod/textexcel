package textExcel;

public class PercentCell implements Cell {
    private String value = "";
    private String abbreviatedValue;
    public Location loc;

    @Override
    public String abbreviatedCellText(){
        return abbreviatedValue;
    }

    public String fullCellText(){
        return Double.toString(Double.parseDouble(value.substring(0, value.toCharArray().length-2))/100.0);
    }

    @Override
    public Location getLocation(){
        return loc;
    }

    public PercentCell(String name, String value){
        loc = new SpreadsheetLocation(name);
        this.value = value;
        abbreviatedValue = value.split(".")[0] + "%";
    }
}