package textExcel;

public class ValueCell implements Cell {
    private double value;
    private String abbreviatedValue;
    public Location loc;

    @Override
    public String abbreviatedCellText(){
        return abbreviatedValue;
    }

    public String fullCellText(){
        return Double.toString(value);
    }

    @Override
    public Location getLocation(){
        return loc;
    }

    public ValueCell(String name, double value){
        loc = new SpreadsheetLocation(name);
        this.value = value;
        String temp = Double.toString(value);
        try{
            abbreviatedValue = temp.substring(0, 10);
        } catch (StringIndexOutOfBoundsException e){
            abbreviatedValue = temp;
            for (int i = 10-temp.toCharArray().length; i > 0; i--){
                abbreviatedValue += " ";
            }
        }
    }
}