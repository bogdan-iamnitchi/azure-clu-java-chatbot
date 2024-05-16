package app;

import org.json.JSONObject;
import java.util.HashMap;
import java.util.Scanner;

public class Controller {

    //aplicatia va avea un chatbot
    ChatBot chatBot;

    //un hashmap cu preturile serviciilor
    HashMap<String, Integer> prices;

    public Controller() {
        //cream obiectul de tip ChatBot care va fi folosit pentru a face predictii
        chatBot = new ChatBot();

        //se creeaza un catalog de preturi pentru servicii
        prices = new HashMap<>();
        prices.put("makeup", 250);
        prices.put("shaving", 100);
        prices.put("pedicure", 80);
        prices.put("coloring", 300);
        prices.put("manicure", 120);
    }

    public void run() {
        //cream un obiect de tip Scanner pentru scriere si citirea din consola
        Scanner scanner = new Scanner(System.in);

        //declaram si initializam datele necesare
        //numele clientului
        String name;
        //data programarii
        String date = null;
        //serviciul dorit
        String service = null;

        //variabila care va tine minte daca conversatia s-a incheiat sau nu
        boolean finished = false;
        //afisam mesajul de inceput
        System.out.println("Write down your message to the chat bot: ");

        //bucla in care se face parsarea query-urilor si in care se primeste un raspuns corespunzator
        while(!finished){
            //se citeste enuntul introdus de utilizator de la tastatura
            String utterance = scanner.nextLine();

            //se evalueaza acel enunt
            String response = this.chatBot.predict(utterance);
            System.out.println(response);

            //se obtine un obiect de tip JSONObject din string-ul primit
            JSONObject jsObj = new JSONObject(response);

            //se evalueaza intentia care are cea mai mare probabilitate
            String topIntent = jsObj.getJSONObject("prediction").getString("topIntent");

            //se cauta corespondenta cu intentia principala pentru a se putea da un raspuns din partea chatbot-ului
            switch (topIntent) {
               //daca intentia principala e cea de Greet, atunci botul raspunde cu mesajul de mai jos
               case "Greet" -> System.out.println("Hello, Salon Provider\nHow can I help you?");
               //daca intentia principala este CheckPrice, adica clientul doreste sa stie pretul unui produs,
               //atunci se va cauta pretul serviciului intr-un hashmap si se va returna un mesaj specific
               case "CheckPrice" -> {
                   service = this.getService(jsObj);
                   String finalService = service;
                   this.prices.forEach((key, value) -> {
                       if(finalService.contains(key)) {
                           System.out.println("Price for " + finalService + " is " + String.valueOf(value));
                           System.out.println("Do you want to make an appointment?");
                       }
                   });
               }
               //daca intentia principala este CheckAvailability, adica utilizatorul doreste sa stie daca o anumita
               //data este disponibila pentru serviciul pe care il doreste, atunci se va extrage data si serviciul din
               //corpul fisierului json returnat si se vor trata diverse cazuri, afisand mesaje speifice
               case "CheckAvailability" -> {
                   date = getDate(jsObj);
                   service = getService(jsObj);

                   if (date.equals("tomorrow") || date.equals("monday") || date.equals("tuesday") || date.equals("wednesday")
                       || date.equals("thursday") || date.equals("friday") || date.equals("saturday")){
                       date = date.substring(0, 1).toUpperCase() + date.substring(1);
                       System.out.println(date + " we have a free place at 10AM. It's ok for you?");
                       date += "at 10AM";
                   }
                   else if(date.contains("sunday")){
                       System.out.println("Sorry, we don't work on sundays. Please choose another day.");
                   }
                   else{
                       System.out.println("It's free on " + date);
                       System.out.println("Do you want to confirm?");
                   }
               }
               //daca intentia principala este Confirmation, adica utilizatorul confirma data programrii,
               //atunci i se va cere un nume
               case "Confirmation" ->  System.out.println("Ok. Can you tell me a name, please?");
               //daca intentia principala este Negation, adica utilizatorul nu este de acord cu data programrii,
               //atunci i se va cere sa ofere o alta varianta pentru programare
               case "Negation" -> System.out.println("Then can you choose another date?");
               //daca intentia principala este Appointment, adica utilizatorul spune data programrii si serviciul,
               //atunci i se va cere un nume pentru programare
               case "Appointment" -> {
                   date = getDate(jsObj);
                   service = getService(jsObj);

                   System.out.println("It's ok for us. Can you tell me a name, please?");
               }
               //daca intentia principala este ConfirmationName, adica utilizatorul spune numele pentru programare,
               //atunci se va afisa un mesaj de confirmare cu numele, data programari si serviciul
               case "ConfirmationName" -> {
                   name = jsObj.getJSONObject("prediction").getJSONObject("entities").getJSONObject("$instance")
                            .getJSONArray("personName").getJSONObject(0).getString("text");
                   name = name.substring(0,1).toUpperCase()+name.substring(1);
                   System.out.println("Mrs. " + name +", we will be expecting you on " + date +" for " + service +".");
               }
               //daca intentia principala este EndConversation, adica utilizatorul doreste incheierea conversatiei,
               //atunci se va afisa un mesaj de la revedere
               case "EndConversation" -> { System.out.println("Have a nice day! We are waiting for you!"); finished = true;}
               default -> System.out.println(topIntent);
           }
        }
    }

    //aceasta metoda are rolul de a returna sub forma de string data extrasa din corpul json
    private String getDate(JSONObject jsObj) {
        String date;
        date = jsObj.getJSONObject("prediction").getJSONObject("entities").getJSONObject("$instance")
                .getJSONArray("datetimeV2").getJSONObject(0).getString("text");
        return date;
    }

    //aceasta metoda are rolul de a returna sub forma de string serviciul extras din corpul json
    //serviciul poate fi de 3 tipuri: hairstyle, manicure&pedicure sau cosmetics
    private String getService(JSONObject jsObj) {
        String service = null;
        if (jsObj.getJSONObject("prediction").getJSONObject("entities").has("Hairstyle")){
            service = jsObj.getJSONObject("prediction").getJSONObject("entities").getJSONObject("$instance")
                    .getJSONArray("Hairstyle").getJSONObject(0).getString("text");
        }
        if(jsObj.getJSONObject("prediction").getJSONObject("entities").has("Manicure&Pedicure")){
            service = jsObj.getJSONObject("prediction").getJSONObject("entities").getJSONObject("$instance")
                    .getJSONArray("Manicure&Pedicure").getJSONObject(0).getString("text");
        }
        if(jsObj.getJSONObject("prediction").getJSONObject("entities").has("cosmetics")){
            service = jsObj.getJSONObject("prediction").getJSONObject("entities").getJSONObject("$instance")
                    .getJSONArray("cosmetics").getJSONObject(0).getString("text");
        }
        return service;
    }
}
