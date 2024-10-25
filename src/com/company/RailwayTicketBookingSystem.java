package com.company;

import java.util.*;

class Train {
    String name, source, destination;
    int seatsAvailable = 1;

    HashMap<String, Integer> station_map = new LinkedHashMap<String,Integer>();
    HashMap<String, Passenger> passenger_map = new HashMap<String, Passenger>();
    List<String> stations;
    List<Passenger> waiting_lst = new ArrayList<Passenger>();

    Train(String name, String source, String destination) {
        this.source = source;
        this.destination = destination;
        this.name = name;
    }

    void addStops(String stops) {
        int i=0;
        for(String str : stops.split(" ")) station_map.put(str,seatsAvailable);
        stations = new ArrayList<String>(station_map.keySet());
    }

    void displayDetails() {
        System.out.println("---------- Train Details ----------");
        System.out.printf("%-20s%s\n", "Name:", name);
        System.out.printf("%-20s%s\n", "Source:", source);
        System.out.printf("%-20s%s\n", "Destination:", destination);
        System.out.printf("%-20s%d\n\n", "Available Seats:", seatsAvailable);
        System.out.printf("%-30s%s\n", "Stations", "Seats");
        System.out.printf("%-30s%s\n", "----------", "---------");
        List<String> stations = new ArrayList<String>(station_map.keySet());
        for (String station : stations) {
            System.out.printf("%-30s%d\n", station, station_map.get(station));
        }
    }

    void bookTicket(Passenger passenger) {
        Scanner scn = new Scanner(System.in);
        System.out.println("---------- Ticket Booking Form ----------");
        System.out.print("Enter the Source Location : ");
        String passSource = scn.next();
        System.out.print("Enter the Destination Location : ");
        String passDest = scn.next();
        List<String> stations = new ArrayList<String>(station_map.keySet());
        if ((!stations.contains(passSource)) || (!stations.contains(passDest)))  System.out.println("\nERROR : Invalid Source or Destination !!");

        else{
            if (passSource.equals(passDest)) System.out.println("\nERROR : Source and Destination cannot be the Same !!");
            else {
                boolean start = false;
                boolean canAccomodate = true;

                for (Map.Entry<String, Integer> set : station_map.entrySet()) {
                    if(!start && (set.getKey()).equals(passSource)) start = true;
                    if(start){
                        if(set.getValue()<=0) canAccomodate = false;
                        if(set.getKey().equals(passDest)) break;
                    }
                }

                if(canAccomodate){
                    String key = "";
                    int value = 0;
                    start = false;
                    for (Map.Entry<String, Integer> set : station_map.entrySet()) {
                        key = set.getKey(); value = set.getValue();

                        if(!start && key.equals(passSource)) start = true;

                        if(key.equals(passDest)) break;

                        if(start) station_map.put(key,value>0?value-1:0);
                    }
                    passenger_map.put(passenger.id, passenger);
                    Ticket ticket = new Ticket(passenger, this, passSource, passDest);
                    seatsAvailable--;
                    passenger.addTicket(ticket,true);
                }
                else{
                    System.out.println("You cannot be Accommodated !! Putting you in Waiting List !!");
                    waiting_lst.add(passenger);
                    passenger.isWaiting(passSource, passDest);
                }
            }
        }
    }

    void bookTicket(Passenger passenger, String passSource, String passDest){
        boolean start = false;
        boolean canAccomodate = true;

        for (Map.Entry<String, Integer> set : station_map.entrySet()) {
            if(!start && (set.getKey()).equals(passSource)) start = true;
            if(start){
                if(set.getValue()<=0) canAccomodate = false;
                if(set.getKey().equals(passDest)) break;
            }
        }

        if(canAccomodate){
            String key = "";
            int value = 0;
            start = false;

            for (Map.Entry<String, Integer> set : station_map.entrySet()) {
                key = set.getKey(); value = set.getValue();
                if(!start && key.equals(passSource)) start = true;
                if(key.equals(passDest)) break;
                if(start) station_map.put(key,value>0?value-1:0);
            }

            passenger_map.put(passenger.id, passenger);
            Ticket new_ticket = new Ticket(passenger, this, passenger.source, passenger.destination);
            bookTicket(passenger, passSource, passDest);
            System.out.printf("\n---------- Ticket Confirmed for -  [%s]----------\n",passenger.name);

            passenger_map.put(passenger.id, passenger);
            seatsAvailable--;
            passenger.addTicket(new_ticket,false);
        }
    }

    void cancelTicket(Passenger passenger, Ticket ticket) {
        System.out.println("Ticket Cancelled for "+passenger.name);
        passenger.ticket_map.remove(ticket.id);
        passenger_map.remove(passenger.id);
        passenger.cancelTicket(ticket.id);
        seatsAvailable++;
        System.out.println("\nTicket Cancelled Successfully !!");

        String key;
        int value = 0;
        boolean start = false;

        String passSource = ticket.source;
        String passDest = ticket.destination;
        System.out.println(station_map);

        for (Map.Entry<String, Integer> set : station_map.entrySet()) {
            key = set.getKey(); value = set.getValue();
            if(!start && key.equals(passSource)) start = true;
            if(key.equals(passDest)) break;
            if(start) station_map.put(key,value+1);
        }

        System.out.println(station_map);

        int ticket_source_ind = ticket.train.stations.indexOf(ticket.source);
        int ticket_dest_ind = ticket.train.stations.indexOf(ticket.destination);
        int pass_source_ind, pass_dest_ind;

        if(waiting_lst.size()>0) {
            for(Passenger pass:waiting_lst){
                pass_source_ind = ticket.train.stations.indexOf(pass.source);
                pass_dest_ind = ticket.train.stations.indexOf(pass.destination);
                if(pass_source_ind>=ticket_source_ind && pass_dest_ind<=ticket_dest_ind){
                    passSource = pass.source;
                    passDest = pass.destination;
                    waiting_lst.remove(pass);
                    bookTicket(pass,passSource,passDest);
                    break;
                }
            }
        }
    }

}

class Ticket {
    String id, source, destination;
    Passenger passenger;
    Train train;
    int bookedSeats = 1;
    double fare;

    Ticket(Passenger passenger, Train train, String source, String destination) {
        id = (passenger.name.toLowerCase())+"-00-"+train.name +"-00-"+source+"-"+destination;
        this.passenger = passenger;
        this.train = train;
        this.source = source;
        this.destination = destination;
        fare = bookedSeats * 200.0;
    }

    void displayDetails() {
        System.out.println("---------- Ticket Details ----------");
        System.out.printf("%-20s%s\n", "Ticket ID:", id);
        System.out.printf("%-20s%s\n", "Passenger ID:", passenger.id);
        System.out.printf("%-20s%s\n", "Passenger Name:", passenger.name);
        System.out.printf("%-20s%s\n", "Train Name:", train.name);
        System.out.printf("%-20s%s\n", "Source:", source);
        System.out.printf("%-20s%s\n", "Destination:", destination);
        System.out.printf("%-20s%s\n", "Total Fare:", fare);
    }
}

class Admin {
    String name = "admin", password = "admin123";
    Scanner scn = new Scanner(System.in);

    void viewPassengers(Train train) {
        System.out.println("\n---------- PASSENGERS ----------");
        ArrayList<String> passenger_lst = new ArrayList<String>(train.passenger_map.keySet());
        if (passenger_lst.size() == 0) System.out.printf("%20s", "No Passengers !!\n");
        else {
            for (int i = 0; i < passenger_lst.size(); i++) System.out.println((i + 1) + ". " + passenger_lst.get(i));
            System.out.println((passenger_lst.size() + 1) + ". Back");
            System.out.print("\nSelect a Passenger : ");
            int num = scn.nextInt();
            if (num <= passenger_lst.size()) {
                Passenger passenger = train.passenger_map.get(passenger_lst.get(num - 1));
                passenger.displayDetails();
                viewPassengers(train);
            }
        }
    }

    void viewWaitingList(Train train) {
        System.out.println("\n---------- WAITING LIST ----------");
        List<Passenger> waiting_lst = train.waiting_lst;
        if (waiting_lst.size() == 0) System.out.printf("%20s", "No Passengers !!\n");
        else {
            for (int i = 0; i < waiting_lst.size(); i++) System.out.println((i + 1) + ". " + (waiting_lst.get(i).id));
            System.out.println((waiting_lst.size() + 1) + ". Back");
            System.out.print("\nSelect a Passenger : ");
            int num = scn.nextInt();
            if (num <= waiting_lst.size()) {
                Passenger passenger = waiting_lst.get(num - 1);
                passenger.displayDetails();
                viewWaitingList(train);
            }
        }
    }
}

class Passenger {
    String id, name, age, gender, contact;
    String source, destination;

    HashMap<String, Ticket> ticket_map = new HashMap<String, Ticket>();
    Scanner scn = new Scanner(System.in);

    Passenger(String name, String age, String gender, String contact) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.contact = contact;
        id = "00000" + (name.toLowerCase()) + contact.substring(5, 10);
    }

    void addTicket(Ticket ticket, boolean flag) {
        System.out.println("\n"+ticket.id);
        if(ticket_map.containsKey(ticket.id)) System.out.println("\nERROR : Ticket Already Booked!!");
        else{
            ticket_map.put(ticket.id, ticket);
            if(flag) {
                System.out.println("\nSuccessfully Booked !!\n");
                ticket.displayDetails();
            }
        }
    }

    void cancelTicket(String ticketID){
        ticket_map.remove(ticketID);
    }

    void displayDetails() {
        System.out.println("---------- Passenger Details ----------");
        System.out.printf("%-20s%s\n", "Passenger ID:", id);
        System.out.printf("%-20s%s\n", "Name:", name);
        System.out.printf("%-20s%s\n", "Age:", age);
        System.out.printf("%-20s%s\n", "Gender:", gender);
       System.out.printf("%-20s%s\n", "Contact:", contact);
    }

    void isWaiting(String source, String destination){
        this.source = source;
        this.destination = destination;
    }

    void viewTickets() {
        System.out.println("\n---------- MY TICKETS ----------");
        ArrayList<String> ticket_lst = new ArrayList<String>(ticket_map.keySet());
        if (ticket_lst.size() == 0) System.out.printf("%20s", "No Tickets !!\n");
        else {
            for (int i = 0; i < ticket_lst.size(); i++) System.out.println((i + 1) + ". " + ticket_lst.get(i));
            System.out.println((ticket_lst.size() + 1) + ". Back");
            System.out.print("\nSelect a Ticket : ");
            int num = scn.nextInt();
            if (num <= ticket_lst.size()) {
                Ticket ticket = ticket_map.get(ticket_lst.get(num - 1));
                ticket.displayDetails();
                System.out.println("\n1. Cancel Ticket\n2. Back\nChoose an Option : ");
                int op = scn.nextInt();
                if(op==1) ticket.train.cancelTicket(ticket.passenger,ticket);
                viewTickets();
            }
        }
    }
}

public class RailwayTicketBookingSystem {

    static HashMap<String, Train> train_map = new HashMap<String, Train>();
    static Admin admin = new Admin();
    static Scanner scn = new Scanner(System.in);
    static String admin_id = admin.name, admin_pass = admin.password;
    static HashMap<String, Passenger> passenger_map = new HashMap<String, Passenger>();

    static {
        Train train_1 = new Train("16127 - Ms Guruvayur Express", "CHENNAI", "GURUVAYUR");
        train_1.addStops("CHENNAI TAMBARAM CHENGALPATTU TINDIVANAM VILLUPURAM ARIYALUR SRIRANGAM TIRUCHIRAPPALLI DINDIGUL MADURAI TRIVANDRUM VARKALA KOLLAM ERNAKULAM THRISSUR GURUVAYUR");
        train_map.put(train_1.name, train_1);

        Train train_2 = new Train("12637 - Pandian Express", "CHENNAI", "MADURAI");
        train_2.addStops("CHENNAI TAMBARAM CHENGALPATTU VILLUPURAM VRIDHACHALAM TIRUCHIRAPPALLI DINDIGUL AMBATURAI KODAIKANAL MADURAI");
        train_map.put(train_2.name, train_2);

        Train train_3 = new Train("22668 - Nagercoil Express", "COIMBATORE", "NAGERCOIL");
        train_3.addStops("COIMBATORE TIRUPPUR ERODE KARUR DINDIGUL MADURAI VIRUDUNAGAR KOVILPATTI VANCHIMANIYACHI TIRUNELVELI VALLIYUR NAGERCOIL");
        train_map.put(train_3.name, train_3);

        Train train_4 = new Train("12632 - Nellai Express", "TIRUNELVELI", "CHENNAI");
        train_4.addStops("TIRUNELVELI KOVILPATTI SATUR VIRUDUNAGAR MADURAI SHOLAVANDAN DINDIGUL TIRUCHIRAPPALLI VRIDHACHALAM VILLUPURAM TINDIVANAM MELMARUVATTUR CHENGALPATTU TAMBARAM MAMBALAM CHENNAI EGMORE");
        train_map.put(train_4.name, train_4);

        Train train_5 = new Train("22675 - Chozhan Express", "CHENNAI", "TIRUCHIRAPPALLI");
        train_5.addStops("CHENNAI TAMBARAM CHENGALPATTU MELMARUVATTUR TINDIVANAM VILLUPURAM PANRUTI TIRUPADRIPULYUR CUDDALORE CHIDAMBARAM SIRKAZHI KUMBAKONAM PAPANASAM THANJAVUR BUDALUR TIRUCHIRAPPALLI");
        train_map.put(train_5.name, train_5);
    }

    static boolean validateForm(String name, String age, String gender, String contact) {

        if (!name.matches("[A-z]+") || name.length() < 3) return false;
        if (!age.matches("[0-9]+")) return false;
        if (!gender.matches("[MFNB]")) return false;
        if (!contact.matches("[0-9]{10}")) return false;

        return true;
    }

    static void passengerSignUp() {
        System.out.println("\n---------- Passenger SignUp ----------");
        System.out.print("Enter your Name : ");
        String name = scn.next();
        System.out.print("Enter your password : ");
        String pass = scn.next();
        System.out.print("Enter your age : ");
        String age = scn.next();
        System.out.print("Enter your gender (M / F / NB): ");
        String gender = scn.next();
        System.out.print("Enter your Contact : +91 ");
        String contact = scn.next();

        boolean flag = validateForm(name, age, gender, contact);
        if (flag) {
            if (passenger_map.containsKey(name + pass)) System.out.println("User Already Exists!!");
            else {
                Passenger passenger = new Passenger(name, age, gender.equals("M") ? "Male" : gender.equals("F") ? "Female" : "Non-Binary", contact);
                passenger_map.put(name + pass, passenger);
                System.out.println("\nRegistration Successfull!!");
            }
        } else System.out.println("\nERROR : Invalid Credentials!!");
    }

    static String askFirst() {
        System.out.println("\n----- Railway Booking System -----\n    1. Admin\n    2. Passenger\n    3. EXIT");
        System.out.print("\nLogin as : ");
        int op = scn.nextInt();

        if (op == 1) {
            System.out.println("\n----- ADMIN Login -----");
            System.out.print("Enter Admin ID : ");
            String id = scn.next();
            System.out.print("Enter Admin Password : ");
            String pass = scn.next();

            if (!(id.equals(admin_id) && pass.equals(admin_pass))) {
                System.out.println("ERROR : Invalid Credentials !!");
                return "null";
            }
            return "admin";
        }
        else if (op == 2) {
            System.out.println("\n----- PASSENGER Portal -----\n1. Sign In\n2. Sign Up\n3. Back");
            System.out.print("\nChoose an Option : ");
            int ch = scn.nextInt();
            if (ch == 1) {
                System.out.println("\n----- PASSENGER SignIn -----");
                System.out.print("Enter Name : ");
                String name = scn.next();
                System.out.print("Enter Password : ");
                String pass = scn.next();
                if (passenger_map.containsKey(name + pass)) return name + pass;
                else {
                    System.out.println("\nERROR : Invalid Credentials!!");
                }
            } else if (ch == 2) {
                passengerSignUp();
            } else return "null";
        } else return "exit";

        return "null";
    }

    static void viewTrains(String user) {
        System.out.println("\n---------- TRAINS ----------");
        ArrayList<String> train_lst = new ArrayList<String>(train_map.keySet());
        for (int i = 0; i < train_lst.size(); i++) System.out.println((i + 1) + ". " + train_lst.get(i));
        System.out.println((train_lst.size() + 1) + ". Back");
        System.out.print("\nSelect a Train : ");
        int num = scn.nextInt();
        if (num <= train_lst.size()) {
            Train train = train_map.get(train_lst.get(num - 1));
            train.displayDetails();
            boolean flag = true;
            while (flag) {
                if (user.equals("admin")) {
                    Admin admin = new Admin();
                    System.out.println("\n1. View Passengers\n2. View Waiting List\n3. Back");
                    System.out.print("\nChoose an Option : ");
                    int op = scn.nextInt();
                    switch (op) {
                        case 1:
                            admin.viewPassengers(train);
                            break;
                        case 2:
                            admin.viewWaitingList(train);
                            break;
                        case 3:
                            flag = false;
                            break;
                    }
                } else {
                    Passenger passenger = passenger_map.get(user);
                    System.out.println("\n1. Book Ticket\n2. Back\n");
                    System.out.print("\nChoose an Option : ");
                    int op = scn.nextInt();
                    switch (op) {
                        case 1:
                            train.bookTicket(passenger);
                            break;
                        case 2:
                            flag = false;
                            break;
                    }
                }
            }
            viewTrains(user);
        }
    }

    public static void main(String[] args) {
        String cur_login;
        boolean flag;

        while (true) {
//            try {
                cur_login = askFirst();

                if (cur_login.equals("admin")) {

                    flag = true;

                    while (flag) {
                        System.out.println("\n----- ADMIN Portal -----\n1. View Trains");
                        System.out.println("2. Logout");
                        System.out.print("\nChoose an Operation : ");

                        int op = scn.nextInt();

                        switch (op) {
                            case 1:
                                viewTrains("admin");
                                break;

                            case 2:
                                System.out.println("Logging Out...");
                                flag = false;
                                break;
                        }
                    }
                } else if (cur_login.equals("null")) ;

                else if (cur_login.equals("exit")) {
                    System.out.println("\nShutting the System Down!!");
                    break;
                } else {
                    flag = true;
                    Passenger passenger = passenger_map.get(cur_login);
                    while (flag) {
                        System.out.println("\n----- PASSENGER Portal -----\n1. View train");
                        System.out.println("2. My Tickets");
                        System.out.println("3. Logout");
                        System.out.print("\nChoose an Operation : ");

                        int op = scn.nextInt();

                        switch (op) {
                            case 1:
                                viewTrains(cur_login);
                                break;

                            case 2:
                                passenger.viewTickets();
                                break;

                            case 3:
                                System.out.println("Logging Out...");
                                System.out.println("Thank you for Visiting Us!!");
                                flag = false;
                                break;
                        }
                    }
                }
//            } catch (Exception e) {
//                System.out.println("ERROR : Invalid Input!!");
//                break;
//            }
        }
    }
}
