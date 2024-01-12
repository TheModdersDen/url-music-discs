package urlmusicdiscs;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Date;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

public class Utils {

    public Holidays currentHoliday = Holidays.NONE;
    public String currentHolidayName = Holidays.getHolidayName(currentHoliday);

    private enum Holidays {
        NEW_YEARS_DAY(1),
        APRIL_FOOLS_DAY(2),
        FOURTH_OF_JULY(3),
        HALLOWEEN(4),
        CHRISTMAS_DAY(5),
        CHRISTMAS_EVE(6),
        NEW_YEARS_EVE(7),
        NONE(0);

        public static String getHolidayName(Holidays holiday) {
            switch (holiday) {
                case NEW_YEARS_DAY:
                    return "New Year's Day";
                case APRIL_FOOLS_DAY:
                    return "April Fool's Day";
                case FOURTH_OF_JULY:
                    return "Fourth of July";
                case HALLOWEEN:
                    return "Halloween";
                case CHRISTMAS_DAY:
                    return "Christmas Day";
                case CHRISTMAS_EVE:
                    return "Christmas Eve";
                case NEW_YEARS_EVE:
                    return "New Year's Eve";
                default:
                    return "None";
            }
        }

        /**
         * The holiday (ID).
         */
        private final int holiday;

        /**
         * Constructor for the Holidays enum.
         * @param holiday the holiday (ID)
         */
        Holidays(int holiday) {
            this.holiday = holiday;
        }

        /**
         * Gets the holiday (ID).
         * @return int the holiday (ID)
         */
        public int getHoliday() {
            return holiday;
        }
    }

    /**
     * Gets the current date from the system using the Java Date class.
     * @return String the current date
     * @throws IOException if the date cannot be retrieved
     */
    public static String getDate() {
        return new Date().toString().split(" ")[0];
    }

    /**
     * Gets the current time from the system using the Java Date class.
     * @return String the current time
     */
    public static String getTime() {
        return getDate().toString().split(" ")[3];
    }

    /**
     * Gets the current date and time from the system using the Java Date class.
     * @return String with the current date and time
     */
    public static String getDateTime() {
        return getDate() + " " + getTime();
    }

    /**
     * Gets the current holiday from the system using the Java Date class.
     * @return the current holiday (using the Holidays enum)
     */
    public static Holidays isHoliday() {
        String date = getDate();
        if (date.contains("Jan 01")) {
            return Holidays.NEW_YEARS_DAY;
        } else if (date.contains("Apr 01")) {
            return Holidays.APRIL_FOOLS_DAY;
        } else if (date.contains("Jul 04")) {
            return Holidays.FOURTH_OF_JULY;
        } else if (date.contains("October 31")) {
            return Holidays.HALLOWEEN;
        } else if (date.contains("Dec 25")) {
            return Holidays.CHRISTMAS_DAY;
        } else if (date.contains("Dec 24")) {
            return Holidays.CHRISTMAS_EVE;
        } else if (date.contains("Dec 31")) {
            return Holidays.NEW_YEARS_EVE;
        } else {
            return Holidays.NONE;
        }
    }

    /**
     * Gets the holiday URL JSON from the internet and parses it.
     * @param URL the URL to get the JSON from
     * @return A String array with the holiday URLs (in order of the Holidays enum)
     */
    public JsonArray getHolidaysFromURL(String urlString) {
    JsonArray jsonArray = null;
    try {
        URL url = new URL(urlString);
        // Enable CORS (Cross-Origin Resource Sharing) for the URL:
        url.openConnection().addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.71 Safari/537.36");
        url.openConnection().addRequestProperty("Access-Control-Allow-Origin", "https://themoddersden.com/");
        url.openConnection().addRequestProperty("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        url.openConnection().addRequestProperty("Access-Control-Allow-Headers", "Content-Type");
        url.openConnection().addRequestProperty("Access-Control-Max-Age", "86400");
        InputStream input = url.openConnection().getInputStream();
        Reader reader = new InputStreamReader(input);
        jsonArray = JsonParser.parseReader(reader).getAsJsonArray();

        // Close the reader and input stream:
        reader.close();
        input.close();
    } catch (Exception e) {
        if (URLMusicDiscs.DEBUG_MODE)
            URLMusicDiscs.LOGGER.debug("Failed to get holiday URLs from the internet.", e);
    }
    return jsonArray;
}
}
