package model;

import org.json.JSONObject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DecimalFormat;

public class WeatherForecast {

    // declaram constanta pentru conversia din Kelvin in Celsius
    private static final double KELVIN_CONST = 273.15;
    // declaram cheia de la API-ul OpenWeatherMap si URL-ul de la care facem requesturile
    private static final String API_KEY = "3aea749de9a0d097507e76af3fe709c7";
    // URL-ul de la care facem requesturile
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather";

    // constructorul clasei
    public WeatherForecast() {}

    // metoda care face conversia din Kelvin in Celsius
    public double toCelsius(double temp) {
        DecimalFormat df = new DecimalFormat("#.#");
        return Double.parseDouble(df.format(temp - KELVIN_CONST));
    }

    // metoda care face requestul catre API-ul OpenWeatherMap si returneaza un obiect de tip JSONObject
    public JSONObject getWeatherData(String city) {
        // cream un obiect de tip HttpClient care va fi folosit pentru a face requesturi
        HttpClient client = HttpClient.newHttpClient();
        // construim un obiect de tip HttpRequest care va fi folosit pentru a face requestul catre API-ul OpenWeatherMap
        // setam parametrii requestului q pt orasul din care vrem sa aflam vremea si appid = API_KEY
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "?q=" + city + "&appid=" + API_KEY))
                .build();

        // incercam sa facem requestul catre API-ul OpenWeatherMap si sa primim raspunsul
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new JSONObject(response.body());
        }
        // in caz de eroare afisam un mesaj de eroare si returnam null
        catch (IOException | InterruptedException e) {
            System.out.println("Error" + e.getMessage());
            return null;
        }
    }

    // metoda care returneaza starea vremii pentru un oras dat
    public String getWeatherStatus(String city) {
        // obtinem rasounsul de la API-ul OpenWeatherMap
        JSONObject response = this.getWeatherData(city);
        // extragem informatiile despre temperatura (Kelvin) si starea vremii
        JSONObject main = response.getJSONObject("main");
        // convertim temperatura din Kelvin in Celsius
        double temperature = toCelsius(main.getDouble("temp"));
        double feelsLike = toCelsius(main.getDouble("feels_like"));

        // construim un string care contine informatiile despre vreme
        String weatherStatus = "";
        weatherStatus += "The weather in " + city + " is currently " + response.getJSONArray("weather").getJSONObject(0).getString("description") + "\n";
        weatherStatus += "Now the current temperature is " + temperature + "°C and it feels like " + feelsLike + "°C";

        // returnam string-ul cu informatiile despre vreme
        return weatherStatus;
    }
}