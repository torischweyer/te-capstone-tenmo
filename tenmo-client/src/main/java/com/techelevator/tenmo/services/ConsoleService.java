package com.techelevator.tenmo.services;


import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.model.UserCredentials;

import java.math.BigDecimal;
import java.util.Scanner;

public class ConsoleService {

    private final Scanner scanner = new Scanner(System.in);

    public int promptForMenuSelection(String prompt) {
        int menuSelection;
        System.out.print(prompt);
        try {
            menuSelection = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            menuSelection = -1;
        }
        return menuSelection;
    }

    public void printGreeting() {
        System.out.println("*********************");
        System.out.println("* Welcome to TEnmo! *");
        System.out.println("*********************");
    }

    public void printLoginMenu() {
        System.out.println();
        System.out.println("1: Register");
        System.out.println("2: Login");
        System.out.println("0: Exit");
        System.out.println();
    }

    public void printMainMenu() {
        System.out.println();
        System.out.println("1: View your current balance");
        System.out.println("2: View your past transfers");
        System.out.println("3: View your pending requests");
        System.out.println("4: Send TE bucks");
        System.out.println("5: Request TE bucks");
        System.out.println("0: Exit");
        System.out.println();
    }

    public void printCurrentBalance(BigDecimal balance) {
        // Your current account balance is: $9999.99
        System.out.println("Your current account balance is: $" + balance);
    }

    public void printListOfUsers(User[] users, int userId) {

        System.out.println("-------------------------------------------");
        System.out.println("Users");
        System.out.printf("%-10s %-10s", "ID", "Name");
        System.out.println();
        System.out.println("-------------------------------------------");

        for (User user : users) {
            if (user.getId() != userId) {
                System.out.printf("%-10s %-10s", user.getId() , user.getUsername());
                System.out.println();
            }
        }

        System.out.println("---------");

    }

    public UserCredentials promptForCredentials() {
        String username = promptForString("Username: ");
        String password = promptForString("Password: ");
        return new UserCredentials(username, password);
    }

    public String promptForString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public int promptForInt(String prompt) {
        System.out.print(prompt);
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }
    public void printListOfTransfers(Transfer[] transfers, String username){
        if (transfers == null) {
            System.out.println("No Transfers at this time.");
            return;
        }
        System.out.println("-------------------------------------------");
        System.out.println("Transfers");
        System.out.printf("%-10s %-15s %-10s", "ID", "From/To", "Amount");
        System.out.println();
        System.out.println("-------------------------------------------");

        // loop through array
        for(Transfer transfer : transfers) {
            if (!transfer.getSenderUsername().equals(username)){
                System.out.printf("%-10s %-15s %-10s", transfer.getTransferId(), "From: " + transfer.getSenderUsername(), "$" + transfer.getAmount());
                System.out.println();
            } else {
                System.out.printf("%-10s %-15s %-10s", transfer.getTransferId(), "To: " + transfer.getRecipientUsername(), "$" + transfer.getAmount());
                System.out.println();
            }
        }

        System.out.println("---------");
    }

    public void printListOfPendingTransfers(Transfer[] transfers){
        System.out.println("-------------------------------------------");
        System.out.println("Pending Transfers");
        System.out.printf("%-10s %-15s %-10s", "ID", "To", "Amount");
        System.out.println();
        System.out.println("-------------------------------------------");

        // loop through array
        for(Transfer transfer : transfers) {
            System.out.printf("%-10s %-15s %-10s", transfer.getTransferId(), transfer.getRecipientUsername(), "$ " + transfer.getAmount());
            System.out.println();
        }

        System.out.println("---------");
    }
    public void printApproveRejectMenu() {
        System.out.println();
        System.out.println("1: Approve");
        System.out.println("2: Reject");
        System.out.println("0: Don't approve or reject");
        System.out.println("---------");
    }

    public void printTransactionDetails(Transfer transfer) {

        System.out.println("-------------------------------------------");
        System.out.println("Transfer Details");
        System.out.println("-------------------------------------------");

        System.out.println("Id: " + transfer.getTransferId());
        System.out.println("From: " + transfer.getSenderUsername());
        System.out.println("To: " + transfer.getRecipientUsername());
        System.out.println("Type: " + transfer.getType());
        System.out.println("Status: "+ transfer.getStatus());
        System.out.println("Amount: $" + transfer.getAmount());
    }

    public BigDecimal promptForBigDecimal(String prompt) {
        System.out.print(prompt);
        while (true) {
            try {
                return new BigDecimal(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a decimal number.");
            }
        }
    }

    public void pause() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    public void printErrorMessage() {
        System.out.println("An error occurred. Check the log for details.");
    }

    public void printValidationMessage(String message){
        System.out.println(message);
    }

}
