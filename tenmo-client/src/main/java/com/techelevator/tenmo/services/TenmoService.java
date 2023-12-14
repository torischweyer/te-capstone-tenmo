package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import com.techelevator.util.BasicLogger;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TenmoService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String API_BASE_URL = "http://localhost:8080/";
    private String authToken = null;

    public void setAuthToken(String authToken){
        this.authToken = authToken;
    }

    public Account retrieveAccountBalance() {
        Account retreivedAccount = null;

        try {
            ResponseEntity<Account> response = restTemplate.exchange(API_BASE_URL + "accounts", HttpMethod.GET, makeAuthEntity(), Account.class);
            retreivedAccount = response.getBody();
        }
        catch (Exception e) {
            BasicLogger.log(e.getMessage());
        }

        return retreivedAccount;
    }

    public User[] retrieveListOfUsers() {
        User[] users = null;

        try {
            ResponseEntity<User[]> response = restTemplate.exchange(API_BASE_URL + "users", HttpMethod.GET, makeAuthEntity(), User[].class);
            users = response.getBody();
        }
        catch (Exception e) {
            BasicLogger.log(e.getMessage());
        }

        return users;
    }


    public Transfer createTransfer(Transfer transfer) {

        try {
            ResponseEntity<Transfer> response = restTemplate.exchange(API_BASE_URL + "transfers", HttpMethod.POST, makeTransferEntity(transfer), Transfer.class);
            transfer = response.getBody();
        }

        catch (Exception e) {
            BasicLogger.log(e.getMessage());
        }

        return transfer;
    }

    public Transfer[] retrieveListOfTransfers(int userId){
        Transfer[] transfers = null;
        try {
            ResponseEntity<Transfer[]> response = restTemplate.exchange(API_BASE_URL + "transfers?userId=" + userId, HttpMethod.GET, makeAuthEntity(), Transfer[].class);
            transfers = response.getBody();

        } catch(Exception e) {
            BasicLogger.log(e.getMessage());
        }
        return transfers;
    }

    public Transfer[] retrieveListOfPendingTransfers(int userId){
        Transfer[] transfers = null;
        try {
            ResponseEntity<Transfer[]> response = restTemplate.exchange(API_BASE_URL + "transfers?userId=" + userId + "&wantsPending=true", HttpMethod.GET, makeAuthEntity(), Transfer[].class);
            transfers = response.getBody();

        } catch(Exception e) {
            BasicLogger.log(e.getMessage());
        }
        return transfers;
    }

    public Transfer retrieveTransferById(int transferId){
        Transfer transfer = null;

        try {
            ResponseEntity<Transfer> response = restTemplate.exchange(API_BASE_URL + "transfers/" + transferId, HttpMethod.GET, makeAuthEntity(), Transfer.class);
            transfer = response.getBody();
        }
        catch(Exception e) {
            BasicLogger.log(e.getMessage());
        }

        return transfer;
    }

    public boolean updateTransferById(int transferId, String status){
        try{
            ResponseEntity<Boolean> response = restTemplate.exchange(API_BASE_URL + "transfers/" + transferId + "?status=" + status, HttpMethod.PUT, makeAuthEntity(), Boolean.class);
            return response.getBody();
        } catch (Exception e) {
            BasicLogger.log(e.getMessage());
        }
        return false;
    }

    private HttpEntity<Transfer> makeTransferEntity(Transfer transfer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(transfer, headers);
    }

    private HttpEntity<Void> makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(headers);
    }


}
