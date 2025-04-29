package uk.gov.dwp.tas.core.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.dwp.tas.core.config.BaseBDD;
import uk.gov.dwp.tas.core.config.ConfigurationManager;
import uk.gov.dwp.tas.core.error.FailedStepError;
import uk.gov.dwp.tas.core.marker.Marker;
import uk.gov.dwp.tas.core.model.ConfigurationModelCore;

/** Util class. */
public class Utils {

    private static final Pattern NUMBER = Pattern.compile("^\\d+$");
    private static final Pattern DECIMAL = Pattern.compile("^\\d+[.]\\d+$");
    private static final Pattern JSON_ARRAY = Pattern.compile("^\\[.+]$");
    private static final Pattern JSON_OBJECT = Pattern.compile("^\\{.+}$");
    private static final Pattern BOOL = Pattern.compile("^[Tt]rue$|^[Ff]alse$");

    private Utils() {}

    /**
     * string to respective object.
     *
     * @param string value
     * @return Object
     */
    public static Object stringToObject(String string) {

        try {
            if (NUMBER.matcher(string).find()) {
                return Integer.valueOf(string);
            }
            if (DECIMAL.matcher(string).find()) {
                return Double.valueOf(string);
            }
            if (BOOL.matcher(string).find()) {
                return Boolean.valueOf(string);
            }
            if (JSON_ARRAY.matcher(string).find()) {
                return new JSONArray(string);
            }
            if (JSON_OBJECT.matcher(string).find()) {
                return new JSONObject(string);
            }
        } catch (JSONException e) {
            failWithPrint("String " + string + " is not valid json, detail: " + e.getMessage());
        }
        return string;
    }

    /**
     * print message with exception.
     *
     * @param <V> Type to return (keeps compiler happy can be ignored)
     * @param message message to print
     * @return N/A - Will throw FailedStepError (keeps compiled happy can be ignored)
     */
    public static <V> V failWithPrint(String message) {
        printMessage(message, "ERROR");
        throw new FailedStepError(message);
    }

    /**
     * print message on console.
     *
     * @param <V> Type to return (keeps compiler happy can be ignored)
     * @param message message to print
     * @return N/A - Will return null (keeps compiled happy can be ignored)
     */
    public static <V> V print(String message) {
        if (ConfigurationManager.CORE.<ConfigurationModelCore>get().isDebug()) {
            printMessage(message);
        }
        return null; // this makes switch happy
    }

    /**
     * print message on console.
     *
     * @param message message to print
     */
    public static void printMessage(String message) {
        printMessage(message, "INFO");
    }

    @SuppressWarnings("java:S106") // suppressing the System.out.println warning
    private static void printMessage(String message, String level) {
        try {
            BaseBDD.getScenario()
                    .ifPresent(
                            s -> {
                                String messageString =
                                        "\n========== "
                                                + level
                                                + " Start ==========\n"
                                                + message
                                                + "\n========== "
                                                + level
                                                + " End ============\n";

                                s.log(messageString);
                            });
        } catch (IllegalStateException e) {
            System.out.println("========== " + level + " Start ==========");
            System.out.println(message);
            System.out.println("========== " + level + " End ============");
        }
    }

    /**
     * Utility to load json from string.
     *
     * @param json data
     * @return JSONObject
     */
    public static JSONObject loadJsonFromString(String json) {
        // Load a File and resolve all Markers
        return loadJsonFromString(json, true);
    }

    /**
     * Utility to load json from string.
     *
     * @param json data
     * @param resolveMarkers resolve markers needed or not
     * @return JSONObject
     */
    public static JSONObject loadJsonFromString(String json, boolean resolveMarkers) {
        // Load a File and resolve all Markers
        try {
            return resolveMarkers ? new JSONObject(Marker.processMarker(json)) : new JSONObject(json);
        } catch (JSONException e) {
            return failWithPrint("String could not be converted to JSON, " + e.getMessage());
        }
    }

    /**
     * Utility to load json from file.
     *
     * @param path file path
     * @param file file name
     * @return JSONObject
     */
    public static JSONObject loadJsonFromFile(String path, String file) {
        // Load a File and resolve all Markers
        print("Loading Json from File: " + file + " path: " + path);

        return loadJsonFromFile(path, file, true);
    }

    /**
     * Utility to load json from file.
     *
     * @param path file path
     * @param file file name
     * @param resolveMarkers resolve markers needed or not
     * @return JSONObject
     */
    public static JSONObject loadJsonFromFile(String path, String file, boolean resolveMarkers) {
        // Load a File and resolve all Markers
        try {
            return new JSONObject(
                    Objects.requireNonNull(
                            readFile(System.getProperty("user.dir"), path, file, resolveMarkers)));
        } catch (JSONException e) {
            return failWithPrint("File Content could not be converted to JSON, " + e.getMessage());
        }
    }

    /**
     * Utility to read file.
     *
     * @param dir file directory
     * @param path file path
     * @param file file name
     * @return string content
     */
    public static String readFile(String dir, String path, String file) {
        return readFile(dir, path, file, true);
    }

    /**
     * Utility to read file with flag if resolve marker needed.
     *
     * @param dir file directory
     * @param path file path
     * @param file file name
     * @param resolveMarkers resolve markers needed or not
     * @return string content
     */
    public static String readFile(String dir, String path, String file, boolean resolveMarkers) {
        if (file.isEmpty() || path.isEmpty()) {
            failWithPrint(
                    "File and Path must be specified file is (" + file + "), path is (" + path + ")");
        }

        try {
            String content =
                    resolveMarkers
                            ? Marker.processMarker(
                            Files.readString(
                                    Paths.get(dir, Marker.processMarker(path), Marker.processMarker(file))))
                            : Files.readString(Paths.get(dir, path, file));
            // Check Response Template Content is not Blank
            if (content.isEmpty()) {
                failWithPrint(file + " at " + path + " has no CONTENT");
            }
            return content;
        } catch (IOException e) {
            failWithPrint("Unable to read file at " + path);
        }
        return null; // dead code
    }

    /**
     * Utility to pause the execution flow.
     *
     * @param pauseMillis time in millisecond
     */
    public static void pause(final int pauseMillis) {
        try {
            TimeUnit.MILLISECONDS.sleep(pauseMillis);
        } catch (InterruptedException e) {
            failWithPrint("Failed to Pause for " + pauseMillis);
        }
    }

    /**
     * Utility to get optional of system property based on property name.
     *
     * @param prop property name
     * @return optional of string or empty
     */
    public static Optional<String> getSystemProperty(final String prop) {
        if (System.getProperty(prop) != null) {
            return Optional.of(System.getProperty(prop));
        }
        return Optional.empty();
    }

    /**
     * utility to count the occurrence of substring in string.
     *
     * @param str source string
     * @param sub substring
     * @return count
     */
    public static int countMatches(String str, String sub) {
        if (!isBlank(str) && !isBlank(sub)) {
            int count = 0;

            for (int idx = 0; (idx = str.indexOf(sub, idx)) != -1; idx += sub.length()) {
                ++count;
            }

            return count;
        } else {
            return 0;
        }
    }

    /**
     * check if string is empty or not.
     *
     * @param str string
     * @return boolean
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * return original value or parsed env variable value.
     *
     * @param value string
     * @return String
     */
    public static String getEnvironmentVariableValue(String value) {
        if (isValueEnvVarReference(value)) {
            return System.getenv(value.substring(2, value.length() - 1));
        } else {
            return value;
        }
    }

    /**
     * check if value is env variable reference.
     *
     * @param value string
     * @return boolean
     */
    public static boolean isValueEnvVarReference(String value) {
        return value.startsWith("${") && value.endsWith("}");
    }
}
