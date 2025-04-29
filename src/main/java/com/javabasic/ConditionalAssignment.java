


import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


/** Model for Configuration Manager. */
public class ConfigurationModelCore implements ConfigurationModel {
    /** An Optional String for the Service Constants KeyValuePairs. */
    @JsonProperty("serviceConstants")
    private final Map<String, Object> serviceConstants = new HashMap<>();

    /** An Optional Boolean for the debug indicator. */
    @JsonProperty("debug")
    private Boolean debug = false;

    /**
     * Getter for the debug indicator.
     *
     * @return A debug indicator
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * Setter for the debug indicator.
     *
     * @param debug A boolean for the debug indicator which is set as an Optional
     */
    public void setDebug(final Boolean debug) {
        this.debug = debug;
    }

    /**
     * Getter for the serviceConstants.
     *
     * @return A List of KeyValuePairs
     */
    public Map<String, Object> getServiceConstants() {
        return new HashMap<>(serviceConstants);
    }

    /**
     * Setter for serviceConstant via Map.
     *
     * @param constants Map of constants
     */
    public void setServiceConstants(final Map<String, Object> constants) {
        serviceConstants.putAll(
                constants.entrySet().stream()
                        .collect(Collectors.toMap(entry -> entry.getKey().toLowerCase(), Map.Entry::getValue)));
    }

    /**
     * Setter for serviceConstant via key and value.
     *
     * @param key key for constant
     * @param value value for constant
     */
    public void setServiceConstant(final String key, final Object value) {
        Utils.print(
                "DATA - Set Service Constant"
                        + "\n\tKey: "
                        + key
                        + "\n\tValue: "
                        + value
                        + "\n\tType: "
                        + value.getClass().getName());

        serviceConstants.put(key.toLowerCase(), value);
    }

    /**
     * Getter for the serviceConstants by Name.
     *
     * @param name The Key name for the service configurationManager value
     * @return An Optional KeyValuePair Instance
     */
    public Object getServiceConstantByName(String name) {
        if (!serviceConstants.containsKey(name.toLowerCase())) {
            Utils.failWithPrint("Constant: " + name + " is not present");
        }
        return serviceConstants.get(name.toLowerCase());
    }

    /** clear all service constants. */
    public void clearAllServiceConstant() {
        serviceConstants.clear();
    }

    /**
     * Remove service constants by Name.
     *
     * @param name The Key name for the service configurationManager value
     */
    public void removeServiceConstantByName(String name) {
        if (!serviceConstants.containsKey(name.toLowerCase())) {
            Utils.failWithPrint("Constant: " + name + " is not present");
        }
        serviceConstants.remove(name.toLowerCase());
    }
}
