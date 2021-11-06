package com.openfaas.function;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import com.openfaas.model.AbstractHandler;
import com.openfaas.model.IResponse;
import com.openfaas.model.IRequest;
import com.openfaas.model.Response;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.File;
import java.io.IOException;

import static com.jayway.jsonpath.JsonPath.read;

public class Handler extends AbstractHandler {

    public IResponse Handle(IRequest req) {
        String bucket;
        String key;
        String uri = "";

        Response res = new Response();
        try {
            uri = Files.asCharSource(new File("/var/openfaas/secrets/secret-mongodb-uri"), Charsets.UTF_8).readFirstLine();
        } catch (IOException e) {
            res.setStatusCode(400);
        }

        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("imageDB");
            MongoCollection<Document> collection = database.getCollection("images");
            try {
                bucket = read(req.getBody(), "$.Records[0].s3.bucket.name");
                key = read(req.getBody(), "$.Records[0].s3.object.key");

                collection.insertOne(new Document()
                        .append("bucket", bucket)
                        .append("key", key));

                res.setStatusCode(201);
            } catch (Exception e) {
                res.setStatusCode(400);
            }
        }
        return res;
    }
}
