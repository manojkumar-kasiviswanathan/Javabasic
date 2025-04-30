
 private String endpointName;
    private Endpoint endpoint;
    private String tokenPath;
    private JSONObject consumerTokens;

    /**
     * All Args constructor.
     *
     * @param endpointName - name of endpoint.
     * @param endpoint - url variable
     * @param tokenPath - path of token file
     */
    public RestEndpoint(String endpointName, Endpoint endpoint, String tokenPath) {
        this.endpointName = endpointName;
        this.endpoint = endpoint;
        this.tokenPath = tokenPath;
        if (tokenPath != null && !tokenPath.isEmpty()) {
            try {
                this.consumerTokens = new JSONObject(Files.readString(Path.of(tokenPath)));
            } catch (IOException exception) {
                Utils.failWithPrint("Token File " + tokenPath + "does not exist");
            }
        }
    }

    public RestEndpoint() {}

    public String getEndpointName() {
        return endpointName;
    }

    public void setEndpointName(String endpointName) {
        this.endpointName = endpointName;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public String getTokenPath() {
        return tokenPath;
    }

    /**
     * Extract token data and store it in JSON object.
     *
     * @param tokenPath - path of token file
     */
    public void setTokenPath(String tokenPath) {
        this.tokenPath = tokenPath;
        if (tokenPath != null && !tokenPath.isEmpty()) {
            try {
                this.consumerTokens = new JSONObject(Files.readString(Path.of(tokenPath)));
            } catch (IOException exception) {
                Utils.failWithPrint("Token File " + tokenPath + "does not exist");
            }
        }
    }

    public JSONObject getConsumerTokens() {
        return consumerTokens;
    }




private String url;
    private boolean ssl;
    private String keyStorePath;

    /**
     * All Args constructor.
     *
     * @param url - endpoint url
     * @param keyStorePath - keystore file path
     * @param ssl - if ssl needed
     */
    public Endpoint(String url, String keyStorePath, boolean ssl) {
        this.url = url;
        this.keyStorePath = keyStorePath;
        this.ssl = ssl;
        if (ssl && !Files.exists(Path.of(keyStorePath))) {
            Utils.failWithPrint("Key Store Path File " + keyStorePath + "does not exist");
        }
    }

    public Endpoint() {}

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public String getKeyStorePath() {
        return keyStorePath;
    }

    /**
     * check if keystore path doesn't exist if SSL enabled.
     *
     * @param keyStorePath - keystore path
     */
    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
        if (ssl && !Files.exists(Path.of(keyStorePath))) {
            Utils.failWithPrint("Key Store Path File " + keyStorePath + "does not exist");
        }
    }


/** An Optional List of HeaderDefinition Object Instances. */
    private final Optional<List<HeaderDefinition>> headerDefinitions = Optional.empty();

    /** List of Endpoint URLs. */
    private final Optional<List<RestEndpoint>> restEndpoints = Optional.empty();

    /**
     * Getter for the List of Header Definitions.
     *
     * @return An Optional List of HeaderDefinition Object instances
     */
    public List<HeaderDefinition> getHeaderDefinitions() {
        return headerDefinitions.orElse(new ArrayList<>());
    }

    /**
     * Getter for the Header Definitions by referenceName.
     *
     * @param reference A string reference name of the Headers
     * @return An Optional HeaderDefinition Object instances
     */
    public HeaderDefinition getHeaderDefinition(final String reference) {
        HeaderDefinition headerDef =
                getHeaderDefinitions().stream()
                        .filter(hd -> hd.getReferenceName().equals(reference))
                        .findFirst()
                        .orElse(null);

        if (headerDef == null) {
            throw new FailedStepError(
                    "Header with name " + reference + " Cannot be found in Service Configuration");
        }

        return headerDef;
    }

    /**
     * Getter for the List of Endpoint Urls.
     *
     * @return An Optional List of endpoint urls instances
     */
    public List<RestEndpoint> getRestEndpoints() {
        return restEndpoints.orElse(new ArrayList<>());
    }

    /**
     * Getter for the List of Endpoint Urls.
     *
     * @return An Optional List of endpoint urls instances
     */
    public RestEndpoint getRestEndpointsByName(String endpointName) {
        return getRestEndpoints().stream()
                .filter(r -> r.getEndpointName().equals(endpointName))
                .findFirst()
                .orElse(null);
    }

    /**
     * Getter for the Header Definitions by referenceName.
     *
     * @param endpointName A string reference name of the Headers
     * @return An Optional Endpoint Object instances
     */
    public Endpoint getEndpointByName(final String endpointName) {
        RestEndpoint endpoint = getRestEndpointsByName(endpointName);

        if (endpoint == null) {
            throw new FailedStepError(
                    "Endpoint with name " + endpointName + " Cannot be found in Service Configuration");
        }

        return endpoint.getEndpoint();
    }
