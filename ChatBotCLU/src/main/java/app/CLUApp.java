package app;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;

import org.json.JSONObject;

public class CLUApp {

    //declaram cheia de predictie, id-ul aplicatiei si endpoint-ul modelului din CLU Azure
    private static final String SUBSCRIPTION_KEY = "c6d658c9c99a4557aee03b5f0a973b56";
    private static final String REQUEST_ID = "4ffcac1c-b2fc-48ba-bd6d-b69d9942995a";
    private static final String ENDPOINT = "https://clu-lft-project.cognitiveservices.azure.com/language/:analyze-conversations?api-version=2022-10-01-preview";

    //constructorul clasei
    public CLUApp() {}

    //metoda recognize care primeste ca parametru un enuntul scris de utilizator
    // si face un request catre aplicatia din CLU Azure si returneaza raspunsul
    public JSONObject recognize(String utterance) {
        //aici declaram un obiect de tip HttpClient care va fi folosit pentru a face requesturi
        HttpClient httpclient = HttpClient.newHttpClient();

        // construim un string care va reprezenta body-ul necesar requestului
        String requestBody = this.buildRequestBody(utterance);

        // initializam un obiect JSON ce va reprezenta raspunsul primit de la chatBotul din CLU Azure
        JSONObject chatBotResponse = null;

        // incercam sa facem requestul catre CLU Azure si sa primim raspunsul
        try {
            //construim un obiect de tip HttpRequest care va fi folosit pentru a face requestul catre endpointul nostru
            HttpRequest request = HttpRequest.newBuilder()
                //setam uri-ul, metoda, body-ul si headerele necesare requestului conform CLU Azure API
                .uri(URI.create(ENDPOINT))
                .POST(BodyPublishers.ofString(requestBody))
                .setHeader("Ocp-Apim-Subscription-Key", SUBSCRIPTION_KEY)
                .setHeader("Apim-Request-Id", REQUEST_ID)
                .setHeader("Content-Type", "application/json")
                .build();

            //trimitem requestul si obtinem raspunsul
            HttpResponse<String> response = httpclient.send(request, HttpResponse.BodyHandlers.ofString());

            // verificăm dacă requestul a fost făcut cu succes și dacă răspunsul nu este null
            if (response.statusCode() == 200 && response.body() != null) {
                // obținem corpul răspunsului si il trasnformam in obiect de tip JSONObject
                JSONObject jsObj = new JSONObject(response.body());

                // extragem doar rezultatul prediciti din raspuns si il transformam in string
                chatBotResponse = jsObj.getJSONObject("result");
            }
            else {
                // afisam un mesaj de eroare in caz ca requestul a esuat
                System.out.println("Response returned with error: " + response.statusCode() + response.body());
            }
        }
        catch (Exception e) {
            // afisam un mesaj de eroare in caz ca requestul a esuat
            System.out.println("Error while making the request to CLU Azure: " + e.getMessage());
        }

        //returnam raspunsul primit de la chatBotul din CLU Azure acesta poate fi gol sau contine un mesaj
        return chatBotResponse;
    }

    //---------------------------------------------------------------------------------------------------------
    // metoda privata care construieste body-ul necesar requestului catre chatBotul din CLU Azure
    private String buildRequestBody(String utterance) {
        // contruim un obiecte de tip JSONObject care se vor compune pt a face body-ul requestului
        JSONObject requestBody = new JSONObject();
        JSONObject conversationItem = new JSONObject();
        JSONObject analysisInput = new JSONObject();
        JSONObject parameters = new JSONObject();

        // setam valorile necesare pentru conversationItem
        conversationItem.put("id", "1");
        conversationItem.put("text", utterance);
        conversationItem.put("modality", "text");
        conversationItem.put("language", "EN");
        conversationItem.put("participantId", "1");

        // setam valorile necesare pentru analysisInput
        analysisInput.put("conversationItem", conversationItem);

        // setam valorile necesare pentru parameters
        parameters.put("projectName", "Weather-Chatbot");
        parameters.put("verbose", true);
        parameters.put("deploymentName", "assistant_chatbot");
        parameters.put("stringIndexType", "TextElement_V8");

        // unim toate obiectele intr-unul singur care va reprezenta body-ul requestului
        requestBody.put("kind", "Conversation");
        requestBody.put("analysisInput", analysisInput);
        requestBody.put("parameters", parameters);

        //returnam body-ul requestului sub forma de string
        return requestBody.toString();
    }
}

