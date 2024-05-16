package model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Assistant {

    // declaram un map care va contine o lista de evenimente pentru fiecare zi a saptamanii
    private final Map<DayOfWeek, List<Event>> calendar;

    // constructorul clasei
    public Assistant() {
        // initializam calendarul
        calendar = new HashMap<>();
        // apelam metoda care initializeaza calendarul
        initializeCalendar();
    }

    // metoda care initializeaza calendarul cu evenimente
    private void initializeCalendar() {
        // pentru fiecare zi a saptamanii adaugam o lista goala de evenimente
        for (DayOfWeek day : DayOfWeek.values()) {
            calendar.put(day, new ArrayList<>());
        }
        // adaugam cateva evenimente predefinite
        calendar.get(DayOfWeek.WEDNESDAY).add(new Event("meeting with manager", LocalTime.of(15, 30)));
        calendar.get(DayOfWeek.SATURDAY).add(new Event("remind me to water flowers", null));
    }

    // metoda care adauga un eveniment in calendar
    public boolean addEvent(String dayString, String timeString, String reminderMessage) {
        // convertim stringul care reprezinta ziua in enum de tip DayOfWeek
        DayOfWeek dayOfWeek;
        // verificam daca ziua este "today" atunci ziua curenta este ziua de referinta
        if (dayString.equalsIgnoreCase("today")) {
            dayOfWeek = LocalDate.now().getDayOfWeek();
        // daca ziua este "tomorrow" atunci ziua de maine este ziua de referinta
        } else if (dayString.equalsIgnoreCase("tomorrow")) {
            dayOfWeek = LocalDate.now().plusDays(1).getDayOfWeek();
        // altfel convertim stringul in enum de tip DayOfWeek
        } else {
            dayOfWeek = DayOfWeek.valueOf(dayString.toUpperCase());
        }

        // verificam daca ziua este in trecut
        if (isPastDay(dayOfWeek)) {
            System.out.println("You cannot add events for past days.");
            return false;
        }

        // verificam daca ziua este duminica
        if (dayOfWeek == DayOfWeek.SUNDAY) {
            System.out.println("You cannot add events for Sundays. Enjoy your day off!");
            return false;
        }

        // verificam daca exista deja 2 evenimente pentru ziua respectiva
        if (calendar.get(dayOfWeek).size() >= 2) {
            System.out.println("You can only add up to 2 events per day.");
            return false;
        }

        // verificam daca ora este valida daca nu o setam la null
        LocalTime time = null;
        if(!timeString.isEmpty()) {
            try {
                // convertim stringul care reprezinta ora in LocalTime
                time = LocalTime.parse(timeString);
            } catch (Exception e) {
                // in caz de eroare afisam un mesaj si returnam false
                System.out.println("Invalid time format. Please use HH:mm format (24-hour).");
                return false;
            }
        }

        // adaugam evenimentul in calendar
        calendar.get(dayOfWeek).add(new Event(reminderMessage, time));
        return true;
    }

    // metoda care afiseaza evenimentele pentru o zi specificata
    public void checkEvents(String daySpecifier) {
        //declaram un enum de tip DayOfWeek
        DayOfWeek dayOfWeek;
        // verificam daca ziua este "today" atunci ziua curenta este ziua de referinta
        if (daySpecifier.equalsIgnoreCase("Today")) {
            dayOfWeek = LocalDate.now().getDayOfWeek();
        // daca ziua este "tomorrow" atunci ziua de maine este ziua de referinta
        } else if (daySpecifier.equalsIgnoreCase("Tomorrow")) {
            dayOfWeek = LocalDate.now().plusDays(1).getDayOfWeek();
        // altfel convertim stringul in enum de tip DayOfWeek
        } else {
            try {
                // convertim stringul care reprezinta ziua in enum de tip DayOfWeek
                dayOfWeek = DayOfWeek.valueOf(daySpecifier.toUpperCase());
            } catch (IllegalArgumentException e) {
                // in caz de eroare afisam un mesaj si returnam
                System.out.println("Invalid day specifier.");
                return;
            }
        }

        // verificam daca avem evenimente pentru ziua respectiva
        List<Event> events = calendar.get(dayOfWeek);
        if (events == null || events.isEmpty()) {
            System.out.println("You don't have any events scheduled for " + dayOfWeek + ".");
            return;
        }

        // daca am ajuns aici ca avem evenimente pentru ziua respectiva si le afisam
        System.out.println("Events for " + dayOfWeek + ":");
        // parcurgem lista de evenimente si le afisam
        for (Event event : events) {
            // verificam daca ora evenimentului este setata
            String timeString = event.getTime() != null ? "at " + event.getTime() : "";
            System.out.println("- " + event.getMessage() + " " + timeString);
        }
    }

    // metoda care afiseaza evenimentele pentru o saptamana
    public void checkWeekEvents() {
        // obtinem ziua curenta
        DayOfWeek startDay = LocalDate.now().getDayOfWeek();

        // afisam cand incepe primul eveniment
        System.out.println("First event starting from " + startDay + ":");

        // parcurgem toate zilele saptamanii si afisam evenimentele pentru fiecare zi
        for (int i = 0; i < DayOfWeek.values().length; i++) {
            // calculam ziua curenta
            DayOfWeek currentDay = startDay.plus(i);
            // obtinem evenimentele pentru ziua respectiva
            List<Event> events = calendar.get(currentDay);

            // verificam daca avem evenimente pentru ziua respectiva
            if (events != null && !events.isEmpty()) {
                System.out.println("\nEvents for " + currentDay + ":");
                // parcurgem lista de evenimente si le afisam
                for (Event event : events) {
                    // verificam daca ora evenimentului este setata
                    String timeString = event.getTime() != null ? "at " + event.getTime() : "";
                    System.out.println("- " + event.getMessage() + " " + timeString);
                }
            }
        }
        System.out.println();
    }

    // metoda care verifica daca o zi este in trecut
    private boolean isPastDay(DayOfWeek dayOfWeek) {
        LocalDate today = LocalDate.now();
        return today.getDayOfWeek().compareTo(dayOfWeek) > 0;
    }

    //------------------------------------------------------------------------------------------------------------------
    // clasa interna care reprezinta un eveniment
    private static class Event {
        // un eveniment are un mesaj si o ora care poate fi null
        private String message;
        private LocalTime time;

        // constructorul clasei
        public Event(String message, LocalTime time) {
            this.message = message;
            this.time = time;
        }

        // metodele getter pentru a accesa atributele private ale clasei
        public String getMessage() {
            return message;
        }

        public LocalTime getTime() {
            return time;
        }
    }
}
