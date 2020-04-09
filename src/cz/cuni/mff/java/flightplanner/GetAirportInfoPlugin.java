package cz.cuni.mff.java.flightplanner;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

public class GetAirportInfoPlugin implements Plugin {

    OutputStream outStream = null;

    @Override
    public String name() { return this.getClass().getName(); }

    @Override
    public String description() { return "Write information about chosen airports."; }

    @Override
    public String keyword() { return "airport info"; }

    @Override
    public Integer pluginID() { return 2; }

    /**
     * Handles the action for airport information listing. This method lets the
     * user enter all the required information about the output, then searches
     * for all the relevant information and finally outputs the desired information
     * in the way precised by the user.
     */
    @Override
    public void action() {
        boolean auto = DialogCenter.getResponse(null,
                                                "Do you want the " + this.keyword() + " output to be managed automatically? (Y/n): ",
                                                "Y",
                                                true
                                               );
        List<Airport> foundAirports = Airport.searchAirports(null, false);
        if (auto)
            outStream = DialogCenter.chooseOutputForm("", false, null);
        for (Airport apt : foundAirports) {
            if (!auto)
                outStream = DialogCenter.chooseOutputForm(" for " + apt.icaoCode + " airport", true, apt.icaoCode);
            else {
                if (outStream instanceof FileOutputStream)
                    outStream = DialogCenter.setFileOutputStream(false, apt.icaoCode);
            }
            PrintStream pr = new PrintStream(outStream);
            pr.printf("------------ Information about %s airport. ------------%n" +
                      "The ICAO (International civil aviation organization) code of this airport is: %s%n" +
                      "The %s airport is situated in: %s, %s.%n" +
                      "It is a %s.%n",  apt.icaoCode,       apt.icaoCode,
                                        apt.Name,           apt.municipality,
                                        apt.countryCode,    apt.cat.name().replace("_", " size ")
                     );
            pr.printf("The coordinates of %s are: %.4f, %.4f and its elevation is: %.0f feet (%.1f meters above sea level).%n",
                        apt.Name,       apt.geoLat,
                        apt.geoLong,    apt.elevation,
                        Airport.ftTomConverter(apt.elevation)
                     );
            String appendChar = apt.runways.length > 1 ? "s" : "";
            pr.printf("This airport has %d runway%s:%n", apt.runways.length, appendChar);
            for (String rwy : apt.runways) {
                String[] fields = rwy.split(",", -1);
                pr.printf("Runway identification is: %s/%s.%n", fields[5], fields[11]);
                pr.printf("    It's length is: %s feet (%.1f meters) and width: %s feet (%.1f meters).%n",
                            fields[0], Airport.ftTomConverter(Double.parseDouble(fields[0])),
                            fields[1], Airport.ftTomConverter(Double.parseDouble(fields[1]))
                         );
            }
            pr.printf("------------ End of information about %s airport.------------%n%n", apt.icaoCode);
            if (outStream instanceof FileOutputStream) {
                pr = System.out;
                pr.println("Information about " + apt.icaoCode + " airport has been successfully written to the file.");
            } else { //the outStream goes to the screen
                boolean notLastEntry = foundAirports.iterator().hasNext();
                boolean autoPrint = notLastEntry &&
                                    DialogCenter.getResponse(null, "To print automatically write 'a': ", "a", true);
                if (notLastEntry && !autoPrint)
                    DialogCenter.getResponse(null, "Press Enter to continue.\n", "", true);
            }
        }
    }
}
