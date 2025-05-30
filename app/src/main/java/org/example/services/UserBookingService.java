package org.example.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entities.Train;
import org.example.entities.User;
import org.example.util.UserServiceUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserBookingService {
    private User user;
    private List<User> userList;
    private ObjectMapper objectMapper = new ObjectMapper();

    private static final String USERS_PATH = "C:\\Users\\Sudarshan Potdar\\IdeaProjects\\IRCTC\\app\\src\\main\\java\\org\\example\\localDB\\users.json";

    public UserBookingService() throws IOException {
        loadUsers();
    }

    public UserBookingService(User user1) throws IOException {
        this.user = user1;
        loadUsers();
    }

    public void loadUsers() throws IOException {
        File users = new File(USERS_PATH);

        // Check if the file exists
        if (!users.exists()) {
            System.out.println("File not found at path: " + USERS_PATH);
            throw new FileNotFoundException("Could not find the file: " + USERS_PATH);
        }

        // Log absolute path for better clarity
        System.out.println("Attempting to read file: " + users.getAbsolutePath());

        try {
            // Attempt to read the file and parse the JSON
            userList = objectMapper.readValue(users, new TypeReference<List<User>>() {});
            System.out.println("User list successfully loaded. Size: " + userList.size());
        } catch (IOException ex) {
            // Catch and log any deserialization errors
            System.out.println("Failed to deserialize JSON from file: " + USERS_PATH);
            System.out.println("Error message: " + ex.getMessage());
            ex.printStackTrace();
            throw ex; // Optionally re-throw the exception for upstream handling
        }
    }

    public Boolean loginUser(){
        Optional<User> foundUser = userList.stream().filter(user1 -> {
            return user1.getName().equals(user.getName()) && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword());
        }).findFirst();
        return foundUser.isPresent();
    }

    public Boolean signUp(User user1){
        try{
            userList.add(user1);
            saveUserListToFile();
            return Boolean.TRUE;
        }catch (IOException ex){
            return Boolean.FALSE;
        }
    }

    private void saveUserListToFile() throws IOException {
        File userFile = new File(USERS_PATH);
        objectMapper.writeValue(userFile, userList);
    }

    public void fetchBooking() {
        user.printTickets();
    }

    public Boolean cancelBooking(String ticketId) throws IOException {
        try{
            user.setTicketsBooked(
                    user.getTicketsBooked().stream()
                            .filter(ticket -> !ticket.getTicketId()
                            .equals(ticketId))
                            .collect(Collectors.toList())
            );
            saveUserListToFile();
            return Boolean.TRUE;
        }
        catch (IOException ex){
            return Boolean.FALSE;
        }
    }

    public List<Train> getTrains(String source, String destination){
        try{
            TrainService trainService = new TrainService();
            return trainService.searchTrains(source, destination);
        }catch(IOException ex){
            return new ArrayList<>();
        }
    }

    public List<List<Integer>> fetchSeats(Train train){
        return train.getSeats();
    }

    public Boolean bookTrainSeat(Train train, int row, int seat) {
        try{
            TrainService trainService = new TrainService();
            List<List<Integer>> seats = train.getSeats();
            if (row >= 0 && row < seats.size() && seat >= 0 && seat < seats.get(row).size()) {
                if (seats.get(row).get(seat) == 0) {
                    seats.get(row).set(seat, 1);
                    train.setSeats(seats);
                    trainService.addTrain(train);
                    return true; // Booking successful
                } else {
                    return false; // Seat is already booked
                }
            } else {
                return false; // Invalid row or seat index
            }
        }catch (IOException ex){
            return Boolean.FALSE;
        }
    }
}
