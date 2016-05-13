package nars.perception.lowlevel.spinglass;

import org.apache.commons.math3.linear.ArrayRealVector;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Dump {
    public static void dumpAsMathematicaGraphic(StringBuilder destination, ImplicitNetwork network) {
        destination.append("Graphics[{");

        for( int dotI = 0; dotI < network.spatialDots.size(); dotI++ ) {
            boolean wasIgnited = network.spatialDots.get(dotI).wasIgnited;
            if( wasIgnited ) {
                destination.append("Red,");
            }
            else {
                destination.append("Black,");
            }

            ArrayRealVector position = network.spatialDots.get(dotI).spatialPosition;
            ArrayRealVector direction = network.spatialDots.get(dotI).spinAttributes.get(0).direction;


            destination.append("Line[{");

            DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
            otherSymbols.setDecimalSeparator('.');
            //otherSymbols.setGroupingSeparator('.');

            DecimalFormat df = new DecimalFormat("#.###", otherSymbols);


            destination.append(String.format("{%s,%s},", df.format(position.getDataRef()[0]), df.format(position.getDataRef()[1])));
            ArrayRealVector positionPlusDirection = position.add(direction);
            destination.append(String.format("{%s,%s}", df.format(positionPlusDirection.getDataRef()[0]), df.format(positionPlusDirection.getDataRef()[1])));
            destination.append("}]");

            if (dotI != network.spatialDots.size() - 1) {
                destination.append(",");
            }

        }

        destination.append("}, PlotRange -> {{-1, 11}, {-1, 11}}]");

        destination.append("");
    }
}
