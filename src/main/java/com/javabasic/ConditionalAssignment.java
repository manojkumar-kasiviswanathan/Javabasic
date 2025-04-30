        RestAssuredConfig config =
                RestAssured.config()
                        .httpClient(
                                HttpClientConfig.httpClientConfig()
                                        .httpClientFactory(
                                                () ->
                                                        HttpClientBuilder.create()
                                                                .setDefaultRequestConfig(
                                                                        RequestConfig.custom()
                                                                                .setConnectTimeout(20000)
                                                                                .setConnectionRequestTimeout(20000)
                                                                                .setSocketTimeout(20000)
                                                                                .build())
                                                                .build()));
