package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")
public class TenmoController {
    @Autowired
    private TransferDao dao;


    @RequestMapping(path = "/accounts", method = RequestMethod.GET)
    public Account retrieveAccountBalance(Principal principal){
        Account account = dao.retrieveAccountBalance(principal.getName());
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account was not found for that Username");
        }
        return account;
    }

    @RequestMapping(path = "/users", method = RequestMethod.GET)
    public List<User> retrieveListOfUsers() {
        List<User> users = dao.retrieveListOfUsers();

        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No users were found");
        }

        return users;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path="/transfers", method = RequestMethod.POST)
    public Transfer createTransfer(@Valid @RequestBody Transfer transfer, Principal principal) {

        // Checking that the user is not targeting themselves for the request or send
        if (transfer.getRecipientId() == transfer.getSenderId()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You tried to send or request money to / from yourself :(");
        }

        // Path for sending money
        if (transfer.getType().equalsIgnoreCase("Send")) {
            // Validate that the sender has enough money
            boolean canTransfer = dao.validateTransfer(transfer);
            if (!canTransfer) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough money in the account to send.");
            }

            // Update both accounts' balances
            boolean updateSuccessful = dao.updateAccountBalances(transfer);
            if (!updateSuccessful) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error occurred while updating balances.");
            }
        }

        // Insert into transfer table no matter what the type is
        transfer = dao.createTransfer(transfer);
        return transfer;
    }

    @RequestMapping(path = "/transfers", method = RequestMethod.GET)
    public List<Transfer> retrieveListOfTransfers(@RequestParam int userId, @RequestParam(required = false) boolean wantsPending){
        List<Transfer> transfers = new ArrayList<>();

        if (wantsPending) {
            transfers =  dao.retrieveListOfPendingTransfers(userId);
        }
        else {
            transfers = dao.retrieveListOfTransfers(userId);
        }

        if (transfers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to get list of transfers.");
        }

         return transfers;
    }

    @RequestMapping(path = "/transfers/{id}", method = RequestMethod.GET)
    public Transfer retrieveTransferById(@PathVariable("id") int transferId){
        Transfer transfer = null;
        transfer = dao.retrieveTransferById(transferId);

        if (transfer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to locate specific transfer.");
        }

        return transfer;
    }

    @RequestMapping(path = "/transfers/{id}", method = RequestMethod.PUT)
    public boolean updateTransferById(@PathVariable("id") int transferId, @RequestParam String status){
        Transfer transfer = dao.retrieveTransferById(transferId);

        if (status.equals("Approved")) {
            // Validate balance
            boolean canTransfer = dao.validateTransfer(transfer);
            if (!canTransfer) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough money in the account to send.");
            }

            // Updating both accounts' balances
            boolean wasUpdated = dao.updateAccountBalances(transfer);
            if (!wasUpdated) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account balances were not updated successfully.");
            }

            // Updating transfer status in database
            transfer.setStatus("Approved");
            int rowsAffected = dao.updateTransferStatus(transfer);
            if (rowsAffected == 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to locate transfer in database,.");
            }

            return true;
        }

        else if (status.equals("Rejected")) {
            // Updating transfer status in database.
            transfer.setStatus("Rejected");
            int rowsAffected = dao.updateTransferStatus(transfer);
            if (rowsAffected == 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to locate transfer in database,.");
            }

            return true;
        }

        return false;
    }

}
