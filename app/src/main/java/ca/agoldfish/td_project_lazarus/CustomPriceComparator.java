package ca.agoldfish.td_project_lazarus;

import java.util.Comparator;

public class CustomPriceComparator implements Comparator<PropertyPackage> {
    @Override
    public int compare(PropertyPackage o1, PropertyPackage o2) {
        Double val1 =  new Double( getDoubleFromMoney(o1.getPrice()));
        Double val2 = new Double( getDoubleFromMoney(o2.getPrice()));
        return val1.compareTo(val2);
    }

    private double getDoubleFromMoney(String value) {
        //check between min and max values
        String parseMoney = value.replaceAll("[^\\d.]+", "");
        return Double.parseDouble(parseMoney);
    }
}
