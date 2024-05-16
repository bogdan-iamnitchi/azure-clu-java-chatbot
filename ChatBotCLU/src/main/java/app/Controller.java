package app;

import model.Assistant;
import model.WeatherForecast;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Scanner;

public class Controller {

    //declaram obicetul de tip CLUApp care va fi folosit pentru a face predictii
    CLUApp inputRecognizer;

    //declaram obiectul de tip WeatherForecast care va fi folosit pentru a obtine informatii despre vreme
    WeatherForecast weatherForecast;

    //declaram obiectul de tip Assistant care va fi folosit pentru a gestiona evenimentele
    Assistant assistant;

    //constructorul clasei
    public Controller() {
        //cream obiectul de tip ChatBot care va fi folosit pentru a face predictii
        inputRecognizer = new CLUApp();

        //cream obiectul de tip WeatherForecast care va fi folosit pentru a obtine informatii despre vreme
        weatherForecast = new WeatherForecast();

        //cream obiectul de tip Assistant care va fi folosit pentru a gestiona evenimentele
        assistant = new Assistant();
    }

    public void run() {
        //cream un obiect de tip Scanner pentru scriere si citirea din consola
        Scanner scanner = new Scanner(System.in);

        //declaram variabilele care vor fi folosite pentru a retine data, ora si textul evenimentului
        String date = "";
        String time = "";
        String text = "";

        //variabila care va tine minte daca conversatia s-a incheiat sau nu
        boolean finished = false;

        //afisam mesajul de inceput
        System.out.println("Write down your message to the chat bot: ");

        //bucla in care se face parsarea query-urilor si in care se primeste un raspuns corespunzator
        //se opreste atunci cand utilizatorul doreste sa incheie conversatia
        while(!finished){
            //se citeste enuntul introdus de utilizator de la tastatura
            System.out.print("> ");
            String utterance = scanner.nextLine();

            //se trimite enuntul catre chatBotul din CLU Azure si se primeste raspunsul
            JSONObject responseJson = this.inputRecognizer.recognize(utterance);
            //daca raspunsul este null, atunci se afiseaza un mesaj de eroare
            if(responseJson == null) {
                System.out.println("Error while making the request to CLU Azure");
                continue;
            }

            //se evalueaza intentia care are cea mai mare probabilitate
            String topIntent = responseJson.getJSONObject("prediction").getString("topIntent");

            //se cauta corespondenta cu intentia principala pentru a se putea da un raspuns din partea chatbot-ului
            switch (topIntent) {
                //daca intentia principala e cea de Greet, atunci botul raspunde cu mesajul de mai jos
                case "greet" -> System.out.println("Hello, there i am your assistant!\nHow can I help you?");

                //daca intentia principala este Weather, atunci se va extrage orasul din corpul fisierului json returnat
                case "weather" -> {
                    String city = responseJson.getJSONObject("prediction").getJSONArray("entities").getJSONObject(0).getString("text");
                    System.out.println(this.weatherForecast.getWeatherStatus(city));
                }

               //daca intentia principala este CheckAvailability, atunci se va verifica disponibilitatea pentru o anumita zi
               case "checkAvailability" -> {
                    //daca in enutul nostru nu a fost identificata o zi atunci se va verifica disponibilitatea pentru toata saptamana
                    if(getDate(responseJson).isEmpty()) {
                        //se deleaga verificarea disponibilitatii catre asistent
                        this.assistant.checkWeekEvents();
                    }
                    //daca a fost identificata o zi valida atunci se va verifica disponibilitatea pentru acea zi
                    else if (dateValid(date)){
                        //se face prima litera din zi uppercase
                        date = date.substring(0, 1).toUpperCase() + date.substring(1);
                        //se deleaga verificarea disponibilitatii catre asistent
                        this.assistant.checkEvents(date);
                    }
                    //daca ziua nu este valida, atunci se va afisa un mesaj de eroare
                    else{
                       System.out.println("I only can check the availabilty for this week");
                       System.out.println("Maybe you can provide me a day?");
                    }
               }

                case "addEvent" -> {
                    //se extrage ziua si ora din corpul fisierului json returnat
                    date = getDate(responseJson);
                    time = getTime(responseJson);

                    //daca nu a fost identificata o zi valida, atunci se va cere utilizatorului sa introduca o zi
                    if (getDate(responseJson).isEmpty()) {
                        System.out.print("Please provide a date for the event: ");
                        date = scanner.nextLine();
                    }
                    //daca data este valida, atunci se va cere utilizatorului sa introduca un mesaj pt reminder
                    if (dateValid(date)){
                        //se face prima litera din zi uppercase
                        date = date.substring(0, 1).toUpperCase() + date.substring(1);

                        //se asteapta mesajul de la utilizator
                        System.out.print("Reminder message: ");
                        text = scanner.nextLine();

                        //se afiseaza un mesaj de confirmare a datei si a mesajului
                        String timeString = !time.isEmpty() ? "at " + time : "";
                        System.out.println("You want me to add this event to your calendar?");
                        System.out.println("- when: " + date + " " + timeString);
                        System.out.println("- reminder message: " + text);
                    }
                    //daca data nu este valida, atunci se va afisa un mesaj de eroare
                    else{
                        System.out.println("Something went wrong, try again!");
                    }
                }
               //daca intentia principala este Confirmation, adica utilizatorul confirma realizarea adaugarrii unui eveniment,
               case "confirmation" -> {
                    //se adauga evenimentul in calendar
                    boolean result = this.assistant.addEvent(date, time, text);
                    //verificam daca evenimentul a fost adaugat cu succes
                    if(result)
                       System.out.println("Ok. Event Saved! Anything else?");

                    //se reseteaza variabilele pentru a putea adauga un nou eveniment
                    date = ""; time = ""; text = "";
               }

               //daca intentia principala este Negation, adica utilizatorul nu este cu datele introduse,
               //atunci se anuleaza adaugarea evenimentului si se reseteaza variabilele
               case "negation" -> {
                   System.out.println("Then can you choose another day or event to save?");
                   date = ""; time = ""; text = "";
                }
               //daca intentia principala este EndConversation, adica utilizatorul doreste incheierea conversatiei,
               //atunci se va afisa un mesaj de la revedere
               case "endConversation" -> { System.out.println("Have a nice day!\nIt's been a pleasure to help you!"); finished = true;}

               //daca intentia principala nu este recunoscuta, atunci se va afisa un mesaj de eroare
               default -> System.out.println("Sorry, i didn't understand what you meant. Can you please rephrase that?");
           }
        }
    }

    //aceasta metoda are rolul de a verifica daca data este valida
    //se verifica daca data este "tomorrow", "today" sau daca este o zi a saptamanii
    private boolean dateValid(String date){
        return date.equals("tomorrow") || date.equals("today") || date.equals("monday") || date.equals("tuesday") || date.equals("wednesday")
                || date.equals("thursday") || date.equals("friday") || date.equals("saturday") || date.equals("sunday");
    }

    //aceasta metoda are rolul de a returna sub forma de string data extrasa din corpul json
    private String getDate(JSONObject jsObj) {
        //initializam data cu un string gol
        String date = "";
        //extragem vectorul de entitati din corpul raspunsului json
        JSONArray entitiesArray = jsObj.getJSONObject("prediction").getJSONArray("entities");
        //parcurgem vectorul de entitati si cautam entitatea de tip data
        for (int i = 0; i < entitiesArray.length(); i++) {
            //extragem entitatea curenta
            JSONObject entity = entitiesArray.getJSONObject(i);
            //daca entitatea este de tip data, atunci returnam textul acesteia
            if (entity.getString("category").equals("date")) {
                return entity.getString("text");
            }
        }
        //daca nu am gasit nicio entitate de tip data, atunci returnam string gol
        return date;
    }

    //aceasta metoda are rolul de a returna sub forma de string ora extrasa din corpul json
    private String getTime(JSONObject jsObj) {
        //initializam ora cu un string gol
        String time = "";
        //extragem vectorul de entitati din corpul raspunsului json
        JSONArray entitiesArray = jsObj.getJSONObject("prediction").getJSONArray("entities");
        //parcurgem vectorul de entitati si cautam entitatea de tip ora
        for (int i = 0; i < entitiesArray.length(); i++) {
            //extragem entitatea curenta
            JSONObject entity = entitiesArray.getJSONObject(i);
            //daca entitatea este de tip ora, atunci returnam textul acesteia
            if (entity.getString("category").equals("hour")) {
                return entity.getString("text");
            }
        }
        //daca nu am gasit nicio entitate de tip ora, atunci returnam string gol
        return time;
    }
}
