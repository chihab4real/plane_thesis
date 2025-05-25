package put.plane.boarding.passengers_generator;

import java.security.SecureRandom;

public class Passenger {

    private String ID;
    private String seatLocation;
    private Double speedQueue;
    private Double speedExiting;
    private boolean hasLuggage;
    private String luggageLocation;

    Passenger(){

    }

    public Passenger(String seatLocation, Double speedQueue, Double speedExiting, boolean hasLuggage, String luggageLocation) {
        this.seatLocation = seatLocation;
        this.ID = generateID();
        this.speedQueue = speedQueue;
        this.speedExiting = speedExiting;
        this.hasLuggage = hasLuggage;
        this.luggageLocation = luggageLocation;
    }


    public String getSeatLocation() {
        return seatLocation;
    }

    public void setSeatLocation(String seatLocation) {
        this.seatLocation = seatLocation;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Double getSpeedQueue() {
        return speedQueue;
    }

    public void setSpeedQueue(Double speedQueue) {
        this.speedQueue = speedQueue;
    }

    public Double getSpeedExiting() {
        return speedExiting;
    }

    public void setSpeedExiting(Double speedExiting) {
        this.speedExiting = speedExiting;
    }

    public boolean isHasLuggage() {
        return hasLuggage;
    }

    public void setHasLuggage(boolean hasLuggage) {
        this.hasLuggage = hasLuggage;
    }

    public String getLuggageLocation() {
        return luggageLocation;
    }

    public void setLuggageLocation(String luggageLocation) {
        this.luggageLocation = luggageLocation;
    }

    private String generateID(){
        final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        int ID_LENGTH = 10;
        SecureRandom random = new SecureRandom();


        StringBuilder sb = new StringBuilder(ID_LENGTH);
        for (int i = 0; i < ID_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
            return sb.toString();
        }

}
