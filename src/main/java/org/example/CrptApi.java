package org.example.testforjob.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CrptApi {
    private final int requestLimit;
    private final AtomicInteger requestCounter;
    private final long intervalMillis;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.requestLimit = requestLimit;
        this.requestCounter = new AtomicInteger(0);
        this.intervalMillis = timeUnit.toMillis(1);
    }

    public void createDocument(Document document, String signature) {
        if (requestCounter.getAndIncrement() >= requestLimit) {
            synchronized (this) {
                long startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - startTime < intervalMillis) {
                    try {
                        wait(intervalMillis - (System.currentTimeMillis() - startTime));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                requestCounter.set(0);
            }
        }
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost("https://ismp.crpt.ru/api/v3/lk/documents/create");
            httpPost.setHeader("Content-Type", "application/json");
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode documentNode = mapper.convertValue(document, ObjectNode.class);
            documentNode.put("signature", signature);
            StringEntity requestEntity = new StringEntity(documentNode.toString());
            httpPost.setEntity(requestEntity);
            CloseableHttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();

            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println("HTTP Status Code: " + statusCode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CrptApi crptApi = new CrptApi(TimeUnit.SECONDS, 5);
        Document document = new Document(
                "one",
                "2",
                "ok",
                "doc",
                true,
                "235345346346",
                "string",
                "22.12.23",
                "string",
                "string",
                "2345235",
                "34"
        );
        String signature = "example_signature";
        crptApi.createDocument(document, signature);
    }
}
@JsonIgnoreProperties(ignoreUnknown = true)
class Document {
    private String description;
    private String doc_id;
    private String doc_status;
    private String doc_type;
    private boolean importRequest;
    private String owner_inn;
    private String participant_inn;
    private String producer_inn;
    private String production_date;
    private String production_type;
    private String reg_date;
    private String reg_number;

    public Document(
            String description,
            String doc_id,
            String doc_status,
            String doc_type,
            boolean importRequest,
            String owner_inn,
            String participant_inn,
            String producer_inn,
            String production_date,
            String production_type,
            String reg_date,
            String reg_number) {
        this.description = description;
        this.doc_id = doc_id;
        this.doc_status = doc_status;
        this.doc_type = doc_type;
        this.importRequest = importRequest;
        this.owner_inn = owner_inn;
        this.participant_inn = participant_inn;
        this.producer_inn = producer_inn;
        this.production_date = production_date;
        this.production_type = production_type;
        this.reg_date = reg_date;
        this.reg_number = reg_number;
    }
}