package textExcel;

import java.util.ArrayList;
import java.util.HashMap;

public class FormulaCell implements Cell {
    private String value = "";
    private String abbreviatedValue;
    public Location loc;
    private Spreadsheet mainSpreadsheet;

    @Override
    public String abbreviatedCellText(){
        String result = parseFormula(value);
        String abbreviatedValue;
        try{
            abbreviatedValue = result.substring(0, 10);
        } catch (StringIndexOutOfBoundsException e){
            abbreviatedValue = result;
            for (int i = 10-result.toCharArray().length; i > 0; i--){
                abbreviatedValue += " ";
            }
        }
        return abbreviatedValue;
    }

    @Override
    public Location getLocation(){
        return loc;
    }

    @Override
    public String fullCellText(){
        return value;
    }

    public ArrayList<Cell> getRange(String raw){
        //Split our range into two cells ("A1-A4" -> {"A1", "A4"})
        String[] parts = raw.split("-");
        //Split the two cell names into parts ("A20" -> {"A", "20"})
        String[] lowerBound = parts[0].split("(?!^)", 2);
        String[] upperBound = parts[1].split("(?!^)", 2);
        ArrayList<Cell> range = new ArrayList<Cell>();
        //Iterate over each column selected in the range.
        //We get the numeric value of the column identifiers (e.g lowerBound[0]) after converting them to Characters (lowerBound[0].charAt(0))
        //I fucking hate this but am leaving it in anyways
        for (int i = Character.getNumericValue(lowerBound[0].charAt(0)); i <= Character.getNumericValue(upperBound[0].charAt(0)); i++){
            //Go through each row selected within this column.
            for (int j = Integer.parseInt(lowerBound[1]); j <= Integer.parseInt(upperBound[1]); j++){
                //Get the cell to add to the range.
                Cell current = mainSpreadsheet.getCell(Character.toString((char) (i+55)) + j);
                if (! (current instanceof ValueCell)){
                    //System.out.println("Warning - Cell " + current.getLocation().getRaw() + " is not a ValueCell, skipping");
                    continue;
                }
                range.add(current);
            }
        }
        return range;
    }

    public Double performOperation(Double operand1, Double operand2, String operator){
        switch (operator){
            case "+":
                return operand1 + operand2;
            case "-":
                return operand1 - operand2;
            case "*":
                return operand1 * operand2;
            case "/":
                return operand1 / operand2;
            case "^":
                return Math.pow(operand1, operand2);
        }
        return 0.0;
    }

    public String processEquation(ArrayList<String> operators, ArrayList<Double> operands){
        //TODO: finish impl?
        HashMap<String, Integer> importance = new HashMap<String, Integer>();
        importance.put("+", 1); 
        importance.put("-", 1);
        importance.put("*", 2);
        importance.put("/", 2);
        importance.put("^", 3);
        int currentIndex = -1;
        int index = -1;
        //Double currentOperand;
        //Double nextOperand;
        //For each operator in this equation (e.g *, +, *)
        for (String currentOperator : operators){
            currentIndex++;
            //Create a list of operators that are coming up
            ArrayList<String> nextOperators = new ArrayList<String>();
            for (int i = currentIndex+1; i <= operators.size(); i++){
                nextOperators.add(operators.get(i));
            }
            //Look ahead for other operators that supersede this one.
            for (String nextOperator : nextOperators){
                index++;
                if (importance.get(currentOperator) < importance.get(nextOperator)){
                    ;
                } else if (importance.get(currentOperator) >= importance.get(nextOperator)){
                    ;
                }
            }
            currentIndex++;
            Double currentOperand = operands.get(currentIndex);
            try {
                Double nextOperand = operands.get(currentIndex+1);
            } catch (IndexOutOfBoundsException e) {
                continue;
            }
        }
        return "";
    }

    public String parseFormula(String raw){
        //TODO: processEquation impl
        String[] tokens = raw.split(" ");
        String type = "";
        String result = "";
        int index = -1;
        if (tokens.length < 2){
            System.out.println("Sorry, that formula is too short.");
            return "ERROR1";
        }
        System.out.println("Parsing formula " + raw);
        //first, check for operations like AVG and SUM
        for ( String token : tokens ){
            //System.out.println("Current token: " + token);
            index++;
            switch (token.toLowerCase()){
                case "avg":
                    type = "AVG";
                    continue;
                case "sum":
                    type = "SUM";
                    continue;
                case "(":
                    continue;
            }
            double total = 0.0;
            ArrayList<Cell> selected = new ArrayList<Cell>();
            //now that we've figured out what type of token this is, check for special commands
            if (type == "AVG" || type == "SUM"){
                //System.out.println("This formula is AVG / SUM");
                if (tokens.length > 4){
                    System.out.println("Sorry, that avg/sum formula is invalid.");
                    return "          ";
                }
                if (token.indexOf("-") > -1){
                    //System.out.println("Getting data range...");
                    selected = getRange(token);
                    total = 0.0;
                    for (Cell current : selected){
                        total += Double.parseDouble(current.fullCellText());
                    }
                } else {
                    System.out.println("Sorry, you didn't provide a range of data.");
                    return "          ";
                }
            }
            if (type == "AVG"){
                Double average = total / (double) selected.size();
                return Double.toString(average);
            } else if (type == "SUM"){
                return Double.toString(total);
            }
        }
        //we haven't returned yet, so parse equations
        ArrayList<String> operators = new ArrayList<String>();
        ArrayList<Double> operands = new ArrayList<Double>();
        //first pass, identify operators and operands
        for ( String t : tokens ){
            //first, try to identify operators
            switch (t.toLowerCase()){
                case "*":
                    operators.add("*");
                    continue;
                case "+":
                    operators.add("+");
                    continue;
                case "-":
                    operators.add("-");
                    continue;
                case "/":
                    operators.add("/");
                    continue;
                case "^":
                    operators.add("^");
                    continue;
                case "(":
                    continue;
                case ")":
                    continue;
            }
            //nothing found? this must be an operand.
            //try looking for cell references first.
            try {
                Cell cellRef = mainSpreadsheet.getCell(t);
                if (cellRef instanceof EmptyCell){
                    System.out.println("Cell " + t + " is empty! Substituting 1.0 in place of this cell.");
                    operands.add(1.0);
                } else if ( ! (cellRef instanceof ValueCell || cellRef instanceof PercentCell || cellRef instanceof FormulaCell)){
                    System.out.println("Sorry, the cell " + t + " can't be used in an equation.");
                    return "        ";
                } else if (cellRef instanceof PercentCell){
                    operands.add(Double.parseDouble(cellRef.fullCellText()));
                } else {
                    operands.add(Double.parseDouble(cellRef.abbreviatedCellText()));
                }
            } catch (IndexOutOfBoundsException e) {
                //Not a valid cell name? This could be a number
                try {
                    operands.add(Double.parseDouble(t));
                } catch (NumberFormatException f){
                    //Not convertible into a Double? It's probably an out of range cell name
                    System.out.println("Sorry, the cell name '" + t + "' is invalid.");
                    return "          ";
                }
            } catch (NumberFormatException e){
                System.out.println("Looks like we got an invalid value for that cell. Defaulting to 1.0.");
                operands.add(1.0);
            }
        }
        //second pass: apply operators to operands
        return processEquation(operators, operands);
    }

    public FormulaCell(String name, String value, Spreadsheet spreadsheet){
        loc = new SpreadsheetLocation(name);
        this.value = value;
        this.mainSpreadsheet = spreadsheet;
    }
}
