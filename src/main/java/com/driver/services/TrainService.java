package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto) {
        Train train = new Train();
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());

        List<Station> stationList = trainEntryDto.getStationRoute();
        String route = stationList.stream()
                .map(Station::toString)
                .collect(Collectors.joining(","));
        train.setRoute(route);
i
        Train savedTrain = trainRepository.save(train);

        return savedTrain.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.


        int availableSeats = 0;
        Train train = trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();

        int totalSeats = train.getNoOfSeats();
        List<Ticket> BookedTickets = train.getBookedTickets();

        String fromStation = seatAvailabilityEntryDto.getFromStation().toString();
        String toStation = seatAvailabilityEntryDto.getToStation().toString();

        int availableBetStations = 0;

        for(Ticket ticket : BookedTickets){
            if(ticket.getToStation().toString().equals(fromStation)){
                availableBetStations+=ticket.getPassengersList().size();
            }
            if(ticket.getFromStation().toString().equals(toStation)){
                availableBetStations+=ticket.getPassengersList().size();
            }
        }

        Integer totalpassengers =0;
        for(Ticket ticket : BookedTickets){
            totalpassengers+=ticket.getPassengersList().size();
        }

        Integer SeatssNotavailable = totalpassengers - availableBetStations;
        availableSeats= totalSeats-SeatssNotavailable;

        return availableSeats;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.
        Train train;
        try{
            train = trainRepository.findById(trainId).get();
        }
        catch (Exception e){
            throw new Exception("Train is not passing from this station");
        }
        Integer count = 0;

        List<Ticket> bookedTickets = train.getBookedTickets();

        for(Ticket ticket : bookedTickets){
            if(ticket.getFromStation().toString().equals(station.toString())){
                List<Passenger> passengers = ticket.getPassengersList();
                for(int i =0;i<passengers.size();i++){
                    count++;
                }
            }
        }
        return count;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0
        Integer oldestpesrsonAge=0;

        Train train = trainRepository.findById(trainId).get();
        List<Ticket> bookedTickets = train.getBookedTickets();

        //If there are no people travelling in that train you can return 0
        if(bookedTickets.size()==0){
            return 0;
        }

        for(Ticket ticket : bookedTickets){
            for(Passenger passenger: ticket.getPassengersList()){
                oldestpesrsonAge= Math.max(oldestpesrsonAge,passenger.getAge());
            }
        }

        return oldestpesrsonAge;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.
        List<Integer> passingtrains = new ArrayList<>();

        List<Train> trains = trainRepository.findAll();

        for(Train train : trains){
            if(train.getRoute().contains(station.toString())){
                LocalTime departure = train.getDepartureTime();
                String route = train.getRoute();
                String [] stations = route.split(",");

                if(stations[0].equals(station.toString()) && (departure.isAfter(startTime)||departure.toString().equals(startTime.toString()) ) && (departure.isBefore(endTime)||departure.toString().equals(endTime.toString()))){
                    passingtrains.add(train.getTrainId());
                    continue;
                }

                for(int i=1;i<stations.length;i++){
                    departure.plusHours(1);
                    if(stations[i].equals(station.toString())){
                        break;
                    }
                }
                if((departure.isAfter(startTime)||departure.toString().equals(startTime.toString()) ) && (departure.isBefore(endTime)||departure.toString().equals(endTime.toString()))){
                    passingtrains.add(train.getTrainId());
                }
            }
        }

        return passingtrains;
    }

}