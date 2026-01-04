package put.plane.boarding.passengers.generator;

import java.util.*;

public class PassengerGenerator {

    public static List<Passenger> generatePassengers(int numberOfRowsInPlane, int numberOfColumnsInPlane,
                                                     int numberPassengers, int passengersLuggagePercentage,
                                                     int luggageAtAdjacentSeatPercentage) {

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

        // determine which passengers with luggage will have it at adjacent seats
        int numWithAdjacentLuggage = (int) Math.round(numWithLuggage * (luggageAtAdjacentSeatPercentage / 100.0));
        List<Integer> passengersWithLuggage = new ArrayList<>(luggageSet);
        Collections.shuffle(passengersWithLuggage, rand);
        Set<Integer> adjacentLuggageSet = new HashSet<>(passengersWithLuggage.subList(0, Math.min(numWithAdjacentLuggage, passengersWithLuggage.size())));

        for (int i =0; i<numberPassengers;i++){
            String seat = assignedSeats.get(i);
            boolean hasLuggage = luggageSet.contains(i);
            Integer speedQueue = rand.nextInt(1,2);
            Integer speedExiting = rand.nextInt(2,3);
            Integer luggagePickUpTime = hasLuggage ? rand.nextInt(1,2) : null;

            String luggageLocation = null;
            if (hasLuggage) {
                if (adjacentLuggageSet.contains(i)) {
                    luggageLocation = getNearbyLuggageLocation(seat, numberOfRowsInPlane, numberOfColumnsInPlane, rand);
                } else {
                    luggageLocation = seat;
                }
            }

            passengers.add(new Passenger(
                    seat,
                    speedQueue,
                    speedExiting,
                    hasLuggage,
                    luggageLocation,
                    luggagePickUpTime
            ));
        }

        passengers.sort(Comparator.comparingInt(passenger -> {
            String[] l = passenger.getSeatLocation().split("_");
            return numberOfRowsInPlane * Integer.parseInt(l[0]) + Integer.parseInt(l[1]);
        }));

        return passengers;
    }

    private static String getNearbyLuggageLocation(String seatLocation, int numberOfRowsInPlane, int numberOfColumnsInPlane, Random rand) {
        String[] parts = seatLocation.split("_");
        int row = Integer.parseInt(parts[0]);
        int col = Integer.parseInt(parts[1]);

        List<String> possibleLocations = new ArrayList<>();

        // left column
        if (col > 1) {
            possibleLocations.add(row + "_" + (col - 1));
        }
        // right column
        if (col < numberOfColumnsInPlane) {
            possibleLocations.add(row + "_" + (col + 1));
        }
        // previous row
        if (row > 1) {
            possibleLocations.add((row - 1) + "_" + col);
        }
        // next row
        if (row < numberOfRowsInPlane) {
            possibleLocations.add((row + 1) + "_" + col);
        }

        if (possibleLocations.isEmpty()) {
            return seatLocation;
        }

        return possibleLocations.get(rand.nextInt(possibleLocations.size()));
    }

}