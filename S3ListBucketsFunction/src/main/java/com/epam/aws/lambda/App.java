package com.epam.aws.lambda;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        List<Bucket> buckets = getBuckets();

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);
        try {
            return response
                    .withStatusCode(200)
                    .withBody(objectMapper.readTree(getBucketJsonString(buckets)).toString());
        } catch (IOException e) {
            return response
                    .withBody("{}")
                    .withStatusCode(500);
        }
    }

    private AmazonS3 getAmazonS3() {
        return AmazonS3ClientBuilder.standard()
                                    .withRegion(Regions.EU_CENTRAL_1)
                                    .build();
    }

    private List<Bucket> getBuckets() {
        return getAmazonS3().listBuckets();
    }

    private String getBucketJsonString(List<Bucket> buckets) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            objectMapper.writeValue(out, buckets);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return String.format("{ \"buckets\": %s }", out.toString());
    }
}
