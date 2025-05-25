package put.plane.boarding.passengers_generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Generator {

    public static List<Passenger> generatePassengers(int plane_row, int plane_col, int numberPassengers){

        if (numberPassengers > plane_col * plane_row){
            System.out.println("NUMBER OF PASSENGERS IS BIGGER THAN THE PLANE SIZE");
            return new ArrayList<>();
        }
        //Integer plane[][] = new Integer[plane_row][plane_col];
        List<Passenger> passengers = new ArrayList<>();
        Random rand = new Random();

        // int curr_pass = 0;

        for (int i = 0;i<plane_row;i++){
            for(int j = 0;j<plane_col;j++){

                boolean luggage = rand.nextBoolean();

                passengers.add(new Passenger(

                        (i+1)+"_"+(j+1),
                        1.0f + rand.nextDouble(),
                        1.0f + rand.nextDouble(),
                        luggage,
                        luggage ? (i+1)+"_"+(j+1) : null
                ));
            }
        }


        return passengers;
    }

}
