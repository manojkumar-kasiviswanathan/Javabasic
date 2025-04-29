
public enum ConfigurationManager {
    CORE,
    ENCRYPTION,
    KAFKA,
    MONGO,
    REST,
    AWS,
    SQL,
    WIREMOCK_INTERCEPTOR;

    private final ObjectMapper mapper =
            new ObjectMapper(new YAMLFactory()).registerModule(new Jdk8Module());
    private ConfigurationProcessor processor;
    private String initCapString = "";
    private String camelString = "";

    ConfigurationManager() {

        String[] words = this.name().split("_");

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (i == 0) {
                camelString = camelString.concat(word.toLowerCase());
            } else {
                camelString =
                        camelString.concat(word.substring(0, 1).concat(word.toLowerCase().substring(1)));
            }
            initCapString =
                    initCapString.concat(word.substring(0, 1).concat(word.toLowerCase().substring(1)));
        }
    }

    // Force lazy loading of the processor / model classes
    private void loadProcessor() {

        String baseClassPath = "uk.gov.dwp.tas." + camelString.toLowerCase();
        String processorClassPath = baseClassPath + ".config.ConfigurationProcessor" + this.toString();
        String modelClassPath = baseClassPath + ".model.ConfigurationModel" + this.toString();
        String filePath =
                System.getProperty(camelString + "ConfigPath", "src/config/" + camelString + ".yaml");

        try {
            Class<?> processorClass = Class.forName(processorClassPath);
            this.processor =
                    (ConfigurationProcessor<?>) processorClass.getDeclaredConstructor().newInstance();
            Class<?> modelClass = Class.forName(modelClassPath);
            processor.processServiceConfiguration(
                    (ConfigurationModel) mapper.readValue(new File(filePath), modelClass));
        } catch (ClassNotFoundException e) {
            Utils.failWithPrint(
                    "Unable to find classes for processor and/or model at paths ["
                            + processorClassPath
                            + ","
                            + modelClassPath
                            + "] for type "
                            + this.name());
        } catch (InstantiationException
                 | IllegalAccessException
                 | IllegalArgumentException
                 | InvocationTargetException
                 | NoSuchMethodException
                 | SecurityException e) {
            Utils.failWithPrint(
                    "Unable to create instance of processor class at path "
                            + processorClassPath
                            + " for type "
                            + this.name());
        } catch (IOException e) {
            Utils.failWithPrint(
                    "Failed to process service configuration " + camelString + " yaml at path " + filePath);
        }
    }

    /**
     * to get instance of configuration manager.
     *
     * @return type of configuration manager
     */
    public <T extends ConfigurationModel> T get() {
        if (null == this.processor) {
            this.loadProcessor();
        }
        return (T) this.processor.getConfig();
    }

    @Override
    public String toString() {
        return initCapString;
    }
}
