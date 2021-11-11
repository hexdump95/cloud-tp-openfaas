package com.openfaas.function;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static com.jayway.jsonpath.JsonPath.read;

public class Handler extends AbstractHandler {
    public static Logger log = LoggerFactory.getLogger(Handler.class);

    public IResponse Handle(IRequest req) {
        String uri = "";

        Response res = new Response();
        try {
            uri = Files.asCharSource(new File("/var/openfaas/secrets/secret-mongodb-uri"), Charsets.UTF_8).readFirstLine();
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("imageDB");
            MongoCollection<Document> collection = database.getCollection("images");
            try {
                String bucket = read(req.getBody(), "$.Records[0].s3.bucket.name");
                String key = read(req.getBody(), "$.Records[0].s3.object.key");
                String createdAt = Instant.parse(read(req.getBody(), "$.Records[0].eventTime"))
                        .atZone(ZoneId.of("America/Argentina/Buenos_Aires"))
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"));

                InsertOneResult insertOne = collection.insertOne(new Document()
                        .append("bucket", bucket)
                        .append("key", key)
                        .append("created_at", createdAt));
                log.info(insertOne.toString());
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return res;
    }
}
