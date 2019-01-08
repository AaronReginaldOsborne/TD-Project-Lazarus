package ca.agoldfish.td_project_lazarus;

import java.util.Comparator;

public class CustomSizeComparator implements Comparator<PropertyPackage> {
    @Override
    public int compare(PropertyPackage o1, PropertyPackage o2) {
        Double val1 =  new Double( getDoubleFromMoney(o1.getSize()));
        Double val2 = new Double( getDoubleFromMoney(o2.getSize()));
        return val1.compareTo(val2);
    }

    private double getDoubleFromMoney(String value) {
        //check between min and max values
        boolean isFound = value.toLowerCase().indexOf("acres") !=-1? true: false;
        String parseMoney = value.replaceAll("[^\\d.]+", "");
        double output = 0.0;
        try{
            output = Double.parseDouble(parseMoney);
            if(isFound)
                output*=43560;
        }
        catch (Exception ex){

        }
        return output;
    }
}
