package app;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;

import org.json.JSONObject;

public class ChatBot {

    //declaram cheia de predictie, id-ul aplicatiei si endpoint-ul modelului din CLU Azure
    private static final String SUBSCRIPTION_KEY = "c6d658c9c99a4557aee03b5f0a973b56";
    private static final String REQUEST_ID = "4ffcac1c-b2fc-48ba-bd6d-b69d9942995a";
    private static final String ENDPOINT = "https://clu-lft-project.cognitiveservices.azure.com/language/:analyze-conversations?api-version=2022-10-01-preview";

    //constructorul clasei
    public ChatBot() {}

    //metoda predict care primeste ca parametru un enuntul scris de utilizator
    // si face un request catre aplicatia din CLU Azure si returneaza raspunsul
    public String predict(String utterance) {
        //aici declaram un obiect de tip HttpClient care va fi folosit pentru a face requesturi
        HttpClient httpclient = HttpClient.newHttpClient();

        //Cream endpoint-ul pentru predictie
        String requestBody = this.buildRequestBody(utterance);

        //initializam un string gol ce va reprezenta raspunsul primit de la chatBotul din CLU Azure
        String chatBotResponse = "";

        //incercam sa facem requestul catre CLU Azure si sa primim raspunsul
        try {
            //construim un obiect de tip HttpRequest care va fi folosit pentru a face requestul catre endpointul nostru
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT))
                .POST(BodyPublishers.ofString(requestBody))
                .setHeader("Ocp-Apim-Subscription-Key", SUBSCRIPTION_KEY)
                .setHeader("Apim-Request-Id", REQUEST_ID)
                .setHeader("Content-Type", "application/json")
                .build();

            //trimitem requestul si asteptam raspunsul
            HttpResponse<String> response = httpclient.send(request, HttpResponse.BodyHandlers.ofString());

            // verificăm dacă requestul a fost făcut cu succes și dacă răspunsul nu este gol
            if (response.statusCode() == 200 && response.body() != null) {
                // obținem corpul răspunsului si il trasnformam in obiect de tip JSONObject
                JSONObject jsObj = new JSONObject(response.body());

                // extragem rezultatul prediciti din raspuns
                chatBotResponse = jsObj.getJSONObject("result").toString();;
            }
            else {
                System.out.println("Response returned with error: " + response.statusCode() + response.body());
            }
        }
        catch (Exception e) {
            System.out.println("Error while making the request to CLU Azure: " + e.getMessage());
        }

        //returnam raspunsul primit de la chatBotul din CLU Azure acesta poate fi gol sau contine un mesaj
        return chatBotResponse;
    }

    //---------------------------------------------------------------------------------------------------------
    private String buildRequestBody(String utterance) {
        JSONObject requestBody = new JSONObject();
        JSONObject conversationItem = new JSONObject();
        JSONObject analysisInput = new JSONObject();
        JSONObject parameters = new JSONObject();

        conversationItem.put("id", "1");
        conversationItem.put("text", utterance);
        conversationItem.put("modality", "text");
        conversationItem.put("language", "EN");
        conversationItem.put("participantId", "1");

        analysisInput.put("conversationItem", conversationItem);

        parameters.put("projectName", "Weather-Chatbot");
        parameters.put("verbose", true);
        parameters.put("deploymentName", "Weather-ChatBot");
        parameters.put("stringIndexType", "TextElement_V8");

        requestBody.put("kind", "Conversation");
        requestBody.put("analysisInput", analysisInput);
        requestBody.put("parameters", parameters);

        return requestBody.toString();
    }
}

