package put.plane.boarding.passengers.generator;

import java.util.*;

public class PassengerGenerator {

    public static List<Passenger> generatePassengers(int numberOfRowsInPlane, int numberOfColumnsInPlane, int numberPassengers, int passengersLuggagePercentage){

        if (numberPassengers > numberOfColumnsInPlane * numberOfRowsInPlane) {
            throw new IllegalArgumentException("NUMBER OF PASSENGERS IS BIGGER THAN THE PLANE SIZE");
        }

        List<Passenger> passengers = new ArrayList<>();
        Random rand = new Random();

        // generate unique seats
        List<String> seatPositions = new ArrayList<>(numberPassengers);
        for (int i = 0;i<numberOfRowsInPlane;i++){
            for(int j = 0;j<numberOfColumnsInPlane;j++){

                seatPositions.add((i + 1) + "_" + (j + 1));
            }
        }


        // taking first N seat
        Collections.shuffle(seatPositions, rand);
        List<String> assignedSeats = seatPositions.subList(0, numberOfColumnsInPlane* numberOfRowsInPlane);



        int numWithLuggage = (int) Math.round(numberPassengers * (passengersLuggagePercentage / 100.0));


        // shuffle first N to get luggage
        List<Integer> luggageIndexes = new ArrayList<>();
        for(int i=0;i<numberPassengers;i++){
            luggageIndexes.add(i);
        }

        Set<Integer> luggageSet = new HashSet<>(luggageIndexes.subList(0, numWithLuggage));

        for (int i =0; i<numberPassengers;i++){
            String seat = assignedSeats.get(i);
            boolean hasLuggage = luggageSet.contains(i);

            passengers.add(new Passenger(
                    seat,
                    1,
                    2,
                    hasLuggage,
                    hasLuggage ? seat: null
            ));
        }

        passengers.sort(Comparator.comparingInt(passenger -> {
            String[] l = passenger.getSeatLocation().split("_");
            return numberOfRowsInPlane * Integer.parseInt(l[0]) + Integer.parseInt(l[1]);
        }));

        return passengers;
    }

}