package uk.gov.dwp.tas.rest.rest;

import static uk.gov.dwp.tas.core.utils.Utils.failWithPrint;
import static uk.gov.dwp.tas.core.utils.Utils.print;

import com.jayway.jsonpath.InvalidModificationException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.Header;
import io.restassured.path.json.config.JsonPathConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.dwp.tas.core.config.ConfigurationManager;
import uk.gov.dwp.tas.core.marker.Marker;
import uk.gov.dwp.tas.core.utils.JsonParser;
import uk.gov.dwp.tas.core.utils.Utils;
import uk.gov.dwp.tas.rest.model.ConfigurationModelRest;
import uk.gov.dwp.tas.rest.model.RestEndpoint;

/** Rest Manager. */
public class RestManager {

    /** A Map to store Header Objects. */
    private final Map<String, Header> headers = new HashMap<>();

    /** List Object Instance to store the Actual Response from Requests. */
    private final List<Response> actualResponses = new ArrayList<>();

    /** The QueryParams Map to use for this request. */
    private final Map<String, String> queryParams = new HashMap<>();

    /** A ConfigurationManager Instance to store the YAML Service Config. */
    private final ConfigurationModelRest restConfig;

    /** Rest Endpoint. * */
    private RestEndpoint restEndpoint;

    /** The Environment URL to use for this request. */
    private String endpointURI = "";

    /** Payload Path Object for use in POST/PATCH Requests. */
    private JSONObject jsonObject = null;

    private String stringPayload = null;

    /** Constructor. */
    public RestManager() {
        restConfig = ConfigurationManager.REST.get();
    }

    /**
     * Setter for the endpoint URL from name.
     *
     * @param endpointName endpoint URL for the REST Assured Call
     */
    public void setEnvironmentURLForConnection(String endpointName) {
        RestEndpoint endpoint =
                restConfig
                        .getRestEndpointsByName(endpointName);
        if (endpoint != null) {
            this.restEndpoint = endpoint;
            ClientAuth.configure(endpoint.getEndpoint());
        } else {
            failWithPrint("REST Connection " + endpointName + "is not defined in service configuration");
        }

        // Some Scenario Logging but plan to add a better logging model
        print("INFO - Environment URL Set to " + restEndpoint.getEndpoint().getUrl());
    }

    /**
     * Getter for the Endpoint URI.
     *
     * @return endpointURI String for Environment URI for the REST Assured Call
     */
    public String getEndpointURI() {
        if (this.endpointURI.isEmpty()) {
            failWithPrint("To Send a REST Operation you must set an environmentURI in Steps");
        }

        return this.endpointURI;
    }

    /**
     * Setter for the Endpoint URI.
     *
     * @param endpointURI Endpoint URI for the REST Assured Call
     */
    public void setEndpointURI(String endpointURI) {
        this.endpointURI = endpointURI;

        // Some Scenario Logging but plan to add a better logging model
        print("INFO - Environment URI Set to " + endpointURI);
    }

    /**
     * A Support function to remove a header to the Class Map of Headers. Currently, this framework
     * only supports Keys with Header values but this can be extended in future
     *
     * @param key The Key of the Header
     */
    public void removeHeader(final String key) {

        if (key.isBlank()) {
            failWithPrint("Custom Header cannot be blank Key Value when removing");
        }

        String headerKey = Marker.processMarker(key);

        if (!headers.containsKey(headerKey)) {
            failWithPrint("Custom Header was never added Header Key: " + key);
        } else {
            headers.remove(headerKey);
        }

        // Some Scenario Logging but plan to add a better logging model
        print("INFO - Header Removed with Key " + key);
    }

    /**
     * A Support function to add a header to the Class Map of Headers. Currently, this framework only
     * supports Keys with Header values but this can be extended in future
     *
     * @param key The Key of the Header
     * @param value The String value of the Header
     */
    public void addHeader(final String key, final String value) {

        if (key.isBlank() || value.isBlank()) {
            failWithPrint("Custom Header cannot be blank Key Value use Marker @@BLANK@@");
        }

        String headerKey = Marker.processMarker(key);
        headers.put(headerKey, new Header(headerKey, Marker.processMarker(value)));

        print("INFO - Header Set for Key " + key + " Value " + value);
    }

    /**
     * Method add headers via Ref.
     *
     * @param headersRef - header ref
     */
    public void addHeaders(String headersRef) {
        restConfig.getHeaderDefinition(headersRef).getHeaders().forEach(this::addHeader);

        // Some Scenario Logging but plan to add a better logging model
        print("INFO - Default Headers Added from " + headersRef);
    }

    /**
     * Function to set the Query parameters directly within the http request. This is an agnostic
     * function whereby the framework has no knowledge of valid query parameters for the service under
     * test. The query string past is split on the & symbol and then split into key/value pairs. Each
     * key/value pair is set as a query parameter in the request.
     *
     * @param query Query parameter seperated by & and in the form key=value&key=value
     */
    public void setQuery(String query) {
        if (query.isEmpty()) {
            return;
        }

        Arrays.asList(query.split("&"))
                .forEach(
                        params -> {
                            if (!params.contains("=")) {
                                failWithPrint("Query Param has no = symbol to identify key/value pair " + params);
                            }

                            List<String> values =
                                    Arrays.stream(params.split("=", 2)).map(Marker::processMarker).toList();

                            queryParams.put(values.get(0), values.get(1));
                        });

        // Some Scenario Logging but plan to add a better logging model
        print("INFO - Query Set " + query);
    }

    /**
     * Function to set the Authorization parameters directly within the http request. Although the
     * overall framework is agnostic this is somewhat common behaviour across CI. CI REST Service have
     * Bearer Token from cognito and this framework supports lookup of these. On init service
     * configuration is used to load the tokens from a json file. Within CI (concourse) these are
     * real-time and last 60 min. For Local testing you need to obtain one externally via POSTMAN/Curl
     * and store in src/main/resources/token.json.
     *
     * @param consumer The consumer token to use
     */
    public void setAuth(final String consumer) {
        try {
            // User can overwrite the consumer for this Step using Environment Variable ConsumerOverwrite
            // or a Service Definition Value
            addHeader("Authorization", "Bearer " + restEndpoint.getConsumerTokens().getString(consumer));

            print("INFO - Auth Header set for consumer " + consumer);
        } catch (JSONException exp) {
            failWithPrint("Consumer token not found " + consumer.toLowerCase());
        }
    }

    /**
     * Function to set the Payload within the http request. This is an agnostic function whereby the
     * framework has no knowledge of valid payloads or schemas for the service under test
     *
     * @param payloadFilename A filename for the Payload usually aligned with the convention
     *     "FeatureIndex"-"TestID"-"ResponseCode".json e.g. 09-01-200.json
     * @param payloadPath The path for the Payload file within Context of the Feature File
     */
    public void addPayload(final String payloadFilename, final String payloadPath) {
        if (payloadFilename.isEmpty()) {
            failWithPrint("Add Payload Called with Empty File");
        }

        if (payloadPath.isEmpty()) {
            failWithPrint("Add Payload Called with Empty File Path");
        }

        if (payloadFilename.endsWith(".json")) {
            this.jsonObject = Utils.loadJsonFromFile(payloadPath, payloadFilename);
        } else {
            this.stringPayload =
                    Utils.readFile(System.getProperty("user.dir"), payloadPath, payloadFilename);
        }

        print("INFO - Payload Set from File " + payloadFilename + " at path " + payloadPath);
    }

    /**
     * Function to update a field within the JSON Payload of a http request. The payload needs to have
     * been set before this function can be used.
     *
     * @param key A field within the payload, this uses dot notation
     * @param value The value to set for the payload field
     */
    @SuppressWarnings("java:S3776")
    public void setJsonValue(final String key, final Object value) {
        if (Optional.ofNullable(this.jsonObject).isEmpty()) {
            failWithPrint(
                    "JSON Payload has not been set, use the REST sets payload from file prior to this step");
        } else {
            // allows you to create a new attribute as an object and array
            Object newValue = value;
            if (value.equals("{}")) {
                newValue = new JSONObject();
            } else if (value.equals("[]")) {
                newValue = new JSONArray();
            }

            try {
                this.jsonObject =
                        new JSONObject(JsonPath.parse(this.jsonObject.toString()).set(key, value).jsonString());
            } catch (PathNotFoundException attributeNotFound) {
                // this catch block is hit if the attribute does not already exist
                var splitKey = key.split("\\.");
                String path =
                        Arrays.stream(splitKey).limit(splitKey.length - 1L).collect(Collectors.joining("."));
                String lastNode = splitKey[splitKey.length - 1];
                try {
                    // checking if trying to add to an array
                    if (lastNode.matches(".+\\[.+]")) {
                        this.jsonObject =
                                new JSONObject(
                                        JsonPath.parse(this.jsonObject.toString())
                                                .add(path + "." + lastNode.substring(0, lastNode.length() - 3), newValue)
                                                .jsonString());
                    } else {
                        // otherwise, a new attribute (key value pair) is created
                        this.jsonObject =
                                new JSONObject(
                                        JsonPath.parse(this.jsonObject.toString())
                                                .put(path, lastNode, newValue)
                                                .jsonString());
                    }
                } catch (PathNotFoundException pathNotFound) {
                    // Attribute can only be overwritten or created in an existing object/array, this is hit
                    // if
                    // new paths have to be created in order to succeed
                    failWithPrint(
                            "JSON Payload does not have the path specified. "
                                    + "Create the path beforehand if needed");
                } catch (InvalidModificationException invalidModificationException) {
                    // Trying to modify attribute with wrong data type, e.g. trying to create a new attribute
                    // directly in a JSONArray
                    failWithPrint(
                            "Invalid JSON Payload Modification. Make sure you are assigning"
                                    + " the correct data type to this attribute");
                }
            }
        }
    }

    /**
     * Function to remove a field within the JSON Payload of a http request. The payload needs to have
     * been set before this function can be used.
     *
     * @param key A field within the payload, this uses dot notation
     */
    public void removeJsonField(final String key) {
        if (Optional.ofNullable(this.jsonObject).isEmpty()) {
            failWithPrint(
                    "JSON Payload has not been set, use the REST sets payload from file prior to this step");
        } else {
            try {
                this.jsonObject =
                        new JSONObject(JsonPath.parse(this.jsonObject.toString()).delete(key).jsonString());
            } catch (PathNotFoundException pathNotFoundException) {
                failWithPrint("Key in path not found in JSON Payload");
            }
        }
    }

    /**
     * initialise rest.
     *
     * @return RequestSpecification
     */
    public RequestSpecification initialiseRest() {
        return RestAssured.given()
                .config(
                        RestAssured.config()
                                .encoderConfig(
                                        EncoderConfig.encoderConfig()
                                                .appendDefaultContentCharsetToContentTypeIfUndefined(false)));
    }

    /**
     * Function to send the REST Operation. This is an agnostic function whereby the framework has no
     * knowledge of valid operations for the service under test.
     *
     * @param action A REST Action VERB POST,PUT,GET,PATCH,DELETE
     * @param count The count of how many times to send the request
     */
    public void sendRequest(final String action, final int count) {
        if (restEndpoint == null || restEndpoint.getEndpoint() == null) {
            failWithPrint(
                    "Rest Manager has not been initialised, please set a Environment "
                            + "Details in rest.yml config file and use it via BDD Step - "
                            + "'REST uses endpoint {string}.'");
        }
        synchronized (this) {
            // BaseURI is the environment URL inside RestAssured
            RestAssured.baseURI = this.restEndpoint.getEndpoint().getUrl();
            // Base Path is the endpoint we are calling
            RestAssured.basePath = this.getEndpointURI();
        }

        /* RequestSpecification Object Instance used to store the API Request. */
        RequestSpecification httpRequest = initialiseRest();

        // Add Headers to Rest Request to avoid having specific Set Headers call
        headers.values().forEach(httpRequest::header);

        // Add Query Parameters to Rest Request
        for (Map.Entry<String, String> queryParam : queryParams.entrySet()) {
            httpRequest.queryParam(queryParam.getKey(), queryParam.getValue());
        }

        // Add Payload if Available
        if (!action.equalsIgnoreCase("get")) {
            if (jsonObject != null) {
                httpRequest.body(jsonObject.toString());
            } else if (stringPayload != null) {
                httpRequest.body(stringPayload);
            }
        }

        for (int i = 0; i < count; i++) {
            // Log the Response to help debugging
            print("\nRequest (" + (i + 1) + ")\n-----------\n");
            print(httpRequest.request().log().everything().toString());

            actualResponses.add(
                    switch (action.toLowerCase()) {
                        case "patch" -> httpRequest.relaxedHTTPSValidation().when().patch();
                        case "post" -> httpRequest.relaxedHTTPSValidation().when().post();
                        case "delete" -> httpRequest.relaxedHTTPSValidation().when().delete();
                        case "put" -> httpRequest.relaxedHTTPSValidation().when().put();
                        default -> httpRequest.relaxedHTTPSValidation().when().get();
                    });

            // Log the Response to help debugging
            print(
                    "\nResponse ("
                            + (i + 1)
                            + ")\n-----------\n "
                            + "Status Code: "
                            + actualResponses.get(i).getStatusCode()
                            + "\n"
                            + actualResponses.get(i).prettyPrint());
        }
    }

    /**
     * Function to send the REST Operation with a Results Reference. This is an agnostic function
     * whereby the framework has no knowledge of valid operations for the service under test.
     *
     * @param action A REST Action VERB POST,PUT,GET,PATCH,DELETE
     * @param count The count of how many times to send the request
     * @param reference The Result reference to store this data
     */
    public void sendRequest(final String action, final int count, final String reference) {

        actualResponses.clear();

        sendRequest(action, count);

        if (count == 1) {
            new RestResult(reference, actualResponses.getFirst());
        } else {
            IntStream.range(0, actualResponses.size())
                    .forEach(i -> new RestResult(reference + (i + 1), actualResponses.get(i)));
        }
    }

    /**
     * Method to validate response code with expected status code.
     *
     * @param expectedResponseStatusCode - status code
     */
    public void validateResponseCode(final int expectedResponseStatusCode) {
        if (actualResponses.isEmpty()) {
            failWithPrint("No Requests have been sent to compare actual responseCode versus expected");
        }

        actualResponses.forEach(
                response -> {
                    if (response.getStatusCode() != expectedResponseStatusCode) {
                        failWithPrint(
                                "Comparator Operations equals FAILED:"
                                        + " Expected: "
                                        + expectedResponseStatusCode
                                        + " Actual: "
                                        + response.getStatusCode());
                    }
                });

        print("INFO - Test Validation Completed Response Codes All Match");
    }

    /**
     * Method to validate the response content type with expected string data.
     *
     * @param expectedContentType - data as String
     */
    public void validateResponseContentType(final String expectedContentType) {
        if (actualResponses.isEmpty()) {
            failWithPrint(
                    "No Requests have been sent to compare actual response ContentType versus expected");
        }

        actualResponses.forEach(
                response -> {
                    if (!response.getBody().asString().isEmpty()
                            && !response.getContentType().equals(expectedContentType)) {
                        failWithPrint(
                                "Comparator Operations equals FAILED:"
                                        + " Expected: "
                                        + expectedContentType
                                        + " Actual: "
                                        + response.getContentType());
                    }
                });

        print("INFO - Test Validation Completed Response ContentType All Match");
    }

    /**
     * Method to validate Response from template file with path.
     *
     * @param templateFileName - file name
     * @param templateFilePath - file path
     */
    public void validateResponse(final String templateFileName, final String templateFilePath) {
        if (actualResponses.isEmpty()) {
            failWithPrint("No Requests have been sent to compare actual response versus expected");
        }

        if (templateFilePath.isEmpty() || templateFileName.isEmpty()) {
            failWithPrint("Response Template must be specified");
        }

        final JSONObject rawJsonTemplate =
                Utils.loadJsonFromFile(
                        Marker.processMarker(templateFilePath), Marker.processMarker(templateFileName), false);

        List<Marker> markers = JsonParser.parseMarkers(rawJsonTemplate);
        populateMarkers(markers);

        // Check Dynamic Markers
        markers.forEach(Marker::checkDynamicData);

        IntStream.range(0, actualResponses.size())
                .forEach(
                        count -> {
                            JSONObject expectedJsonContent =
                                    (JSONObject) JsonParser.replaceJsonResponse(rawJsonTemplate, markers, count);
                            JSONObject actualJsonResponse =
                                    Utils.loadJsonFromString(actualResponses.get(count).getBody().asString(), false);
                            JsonParser.compareJson(expectedJsonContent, actualJsonResponse);
                        });

        print("INFO - Test Validation Completed Content Matches Template File");
    }

    /**
     * A function to extract JSON Data value for a specific key. The function used JSON Path to
     * extract data via the JSON key The data is extracted for each response and is stored in the
     * Class Map responseData
     *
     * @param markers An Arraylist of Marker Details Containing JSON Data references
     */
    public void populateMarkers(final List<Marker> markers) {

        markers.forEach(
                marker ->
                        marker.setActualValues(
                                actualResponses.stream()
                                        .map(
                                                response ->
                                                        response
                                                                .getBody()
                                                                .jsonPath(
                                                                        new JsonPathConfig(JsonPathConfig.NumberReturnType.DOUBLE))
                                                                .get(marker.getKey()))
                                        .collect(Collectors.toCollection(ArrayList::new))));
    }

    /** A Support function to unset a query to the Class Map of queryParams. */
    public void resetQueryParameter() {
        queryParams.clear();
    }

    /** A Support function to unset a Headers to the Class Map of headers. */
    public void resetHeaders() {
        headers.clear();
    }

    /**
     * A Support function to remove a query to the Class Map of query. Currently, this framework only
     * supports Keys with query values but this can be extended in future
     *
     * @param key The Key of the query
     */
    public void removeQueryParameter(final String key) {

        if (key.isBlank()) {
            failWithPrint("Custom Query Parameter cannot be blank Key Value when removing");
        }

        String queryKey = Marker.processMarker(key);

        if (!queryParams.containsKey(queryKey)) {
            failWithPrint("Custom Query Parameter was never added Query Parameter Key: " + key);
        } else {
            queryParams.remove(queryKey);
        }

        // Some Scenario Logging but plan to add a better logging model
        print("INFO - Query Parameter Removed with Key " + key);
    }
}
