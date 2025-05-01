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
