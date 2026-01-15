package put.plane.boarding.passengers.generator;

import java.util.*;

public class PassengerGenerator {

    private static final int MIN_TIME_QUEUE = 1;
    private static final int MAX_TIME_QUEUE = 2;

    private static final int MIN_TIME_EXITING = 2;
    private static final int MAX_TIME_EXITING = 3;

    private static final int MIN_LUGGAGE_PICKUP_TIME = 1;
    private static final int MAX_LUGGAGE_PICKUP_TIME = 3;

    public static List<Passenger> generatePassengers(int numberOfRowsInPlane, int numberOfColumnsInPlane,
                                                     int numberPassengers, int passengersLuggagePercentage) {

        int speedCoefficient = (numberOfColumnsInPlane < 3) ? 1 : 0;

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
        int numWithAdjacentLuggage = (int) Math.round(numWithLuggage * (getLuggageAtAdjacentSeatPercentage(passengersLuggagePercentage) / 100.0));
        List<Integer> passengersWithLuggage = new ArrayList<>(luggageSet);
        Collections.shuffle(passengersWithLuggage, rand);
        Set<Integer> adjacentLuggageSet = new HashSet<>(passengersWithLuggage.subList(0, Math.min(numWithAdjacentLuggage, passengersWithLuggage.size())));

        for (int i =0; i<numberPassengers;i++){
            String seat = assignedSeats.get(i);
            boolean hasLuggage = luggageSet.contains(i);
            Integer speedQueue = rand.nextInt(MIN_TIME_QUEUE+speedCoefficient,MAX_TIME_QUEUE+1+speedCoefficient);
            Integer speedExiting = rand.nextInt(MIN_TIME_EXITING+speedCoefficient,MAX_TIME_EXITING+1+speedCoefficient);
            Integer luggagePickUpTime = hasLuggage ? rand.nextInt(MIN_LUGGAGE_PICKUP_TIME,MAX_LUGGAGE_PICKUP_TIME+1) : null;

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

    private static double getLuggageAtAdjacentSeatPercentage(double peopleWithCabinLuggagePercentage) {
        // A standard aircraft fuselage assumes ~1.5 bags per row bin.
        // this roughly translates to space for 65% of passengers.
        // this is the "Critical Mass" for cabin luggage.
        double overheadBinLimit = 65.0;

        if (peopleWithCabinLuggagePercentage <= 0) return 0.0;
        if (peopleWithCabinLuggagePercentage > 100) peopleWithCabinLuggagePercentage = 100.0;

        // if the amount of people with bags is lower than the bin limit,
        // 0% of luggage ends up at the seat. It all fits up top.
        if (peopleWithCabinLuggagePercentage <= overheadBinLimit) {
            return 0.0;
        }

        // calculate how many "percentage points" of bags don't fit.
        double overflowPoints = peopleWithCabinLuggagePercentage - overheadBinLimit;

        // we calculate the ratio relative to the total luggage brought.
        double displacedLuggagePercentage = (overflowPoints / peopleWithCabinLuggagePercentage) * 100;

        return displacedLuggagePercentage;
    }

    public static void main(String[] args) {
        for(int i=100;i>=20;i-=5) {
            System.out.println("\"passengersLuggagePercentage\":"+ i +", \"luggageAtAdjacentSeatPercentage\":"+
                    (int) getLuggageAtAdjacentSeatPercentage(i)+",");
        }
    }

}