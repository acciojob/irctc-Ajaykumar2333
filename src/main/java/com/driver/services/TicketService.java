package com.driver.services;



import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
        //And the end return the ticketId that has come from db
        Train train = trainRepository.findById(bookTicketEntryDto.getTrainId()).get();

        //creating SeatAvailabilityEntryDto
//        SeatAvailabilityEntryDto seatAvailabilityEntryDto = new SeatAvailabilityEntryDto();
//        seatAvailabilityEntryDto.setTrainId(train.getTrainId());
//        seatAvailabilityEntryDto.setFromStation(bookTicketEntryDto.getFromStation());
//        seatAvailabilityEntryDto.setToStation(bookTicketEntryDto.getToStation());
//
//        TrainService trainService = new TrainService();
//
//        Integer availableSeats = trainService.calculateAvailableSeats(seatAvailabilityEntryDto);
        Integer availableSeats =0;

        int totalSeats = train.getNoOfSeats();
        List<Ticket> BookedTickets = train.getBookedTickets();

        String fromStation = bookTicketEntryDto.getFromStation().toString();
        String toStation = bookTicketEntryDto.getToStation().toString();

        Integer availableBetStations = 0;

        for(Ticket ticket : BookedTickets){
            if(ticket.getToStation().toString().equals(fromStation)){
                availableBetStations+=ticket.getPassengersList().size();
            }
            else if(ticket.getFromStation().toString().equals(toStation)){
                availableBetStations+=ticket.getPassengersList().size();
            }
        }

        Integer totalpassengers =0;
        for(Ticket ticket : BookedTickets){
            totalpassengers+=ticket.getPassengersList().size();
        }

        Integer SeatssNotavailable = totalpassengers - availableBetStations;
        availableSeats= totalSeats-SeatssNotavailable;

        if(availableSeats<bookTicketEntryDto.getNoOfSeats()){
            throw new Exception("Less tickets are available");
        }

        //checking station are in route
        String route = train.getRoute();

//        String [] stations = route.split(",");
//
        boolean isfisrtStation=false;
        boolean isDestination=false;
//
//        for (String station : stations) {
//            if(station.equals(bookTicketEntryDto.getFromStation().toString())){
//                isfisrtStation=true;
//            }
//            else if(station.equals(bookTicketEntryDto.getToStation().toString())){
//                isDestination=true;
//            }
//        }
        if(route.contains(bookTicketEntryDto.getFromStation().toString())){
            isfisrtStation=true;
        }
        if(route.contains(bookTicketEntryDto.getToStation().toString())){
            isDestination=true;
        }

        if(isfisrtStation==false || isDestination == false){
            throw new Exception("Invalid stations");
        }


        //booking ticket
        Ticket ticket = new Ticket();

        //adding passengers to passengers list
        List<Passenger> passengersList = new ArrayList<>();

        for(Integer id : bookTicketEntryDto.getPassengerIds()){
            Passenger passenger = passengerRepository.findById(id).get();
            passengersList.add(passenger);
        }

        ticket.setPassengersList(passengersList);
        ticket.setTrain(train);
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());

        String routeOftrain = train.getRoute();
        String [] stationsOftrain = routeOftrain.split(",");
        int indexOfstartStation =0;
        int indexOflastStation = stationsOftrain.length-1;
        for(int i=0;i<stationsOftrain.length;i++){
            if(stationsOftrain[i].equals(bookTicketEntryDto.getFromStation().toString())){
                indexOfstartStation=i;
            }
            else if(stationsOftrain[i].equals(bookTicketEntryDto.getToStation().toString())){
                indexOflastStation=i;
            }
        }

        int totalConsecutiveStations = indexOflastStation-indexOfstartStation;
        int totalfare = bookTicketEntryDto.getNoOfSeats()*totalConsecutiveStations*300;
        ticket.setTotalFare(totalfare);


        //Save the bookedTickets in the train Object
        train.getBookedTickets().add(ticket);

        //updating bookedtickets list in person entity
        int bookingPesonID= bookTicketEntryDto.getBookingPersonId();
        Passenger passenger1 = passengerRepository.findById(bookingPesonID).get();
        passenger1.getBookedTickets().add(ticket);

        trainRepository.save(train);

        int ticketId= ticket.getTicketId();
        return ticketId;
    }
}