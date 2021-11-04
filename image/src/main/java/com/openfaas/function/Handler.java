package com.openfaas.function;

import com.openfaas.model.IHandler;
import com.openfaas.model.IResponse;
import com.openfaas.model.IRequest;
import com.openfaas.model.Response;

import static com.jayway.jsonpath.JsonPath.read;

public class Handler extends com.openfaas.model.AbstractHandler {

    public IResponse Handle(IRequest req) {
        String bucket = new String();
        String key = new String();

        Response res = new Response();

        try {
            bucket = read(req.getBody(), "$.Records[0].s3.bucket.name");
            key = read(req.getBody(), "$.Records[0].s3.object.key");
        } catch (Exception e) {
            res.setHeader("Content-Type", "application/json");
            res.setStatusCode(400);
            res.setBody("{\"message\": \"Body should be a file\"}");
            return res;
        }

        System.out.println(bucket);
        System.out.println(key);

        res.setBody("Hello, world!");

        return res;
    }
}
