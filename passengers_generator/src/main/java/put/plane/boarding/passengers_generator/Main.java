package put.plane.boarding.passengers_generator;

import java.util.List;

public class Main {
    public static void main(String[] args) {


        List<Passenger> passengers = Generator.generatePassengers(10,2, 21);


        for (Passenger p: passengers){
            System.out.println(
                    "\nPassenger ID: " + p.getID() + "\n" +
                            "SpeedQueue: " + p.getSpeedQueue() + "\n" +
                            "SpeedExiting: " + p.getSpeedExiting() + "\n" +
                            "Luggage: " + p.isHasLuggage() +
                            (p.isHasLuggage() ? ("\nLuggage Location: " + p.getLuggageLocation()) : "")
            );

        }
    }
}