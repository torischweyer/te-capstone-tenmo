package com.techelevator.tenmo.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

public class Transfer {

    private int transferId;
    @NotNull(message = "senderId field must contain a value.")
    private int senderId;
    @NotNull(message = "recipientId field must contain a value.")
    private int recipientId;
    @Positive(message = "You must enter an amount that is greater than zero.")
    private BigDecimal amount;
    @NotBlank(message = "You can not leave the type field empty.")
    private String type;
    @NotBlank(message = "You can not leave the status field empty.")
    private String status;
    private String senderUsername;
    private String recipientUsername;

    public Transfer(){
    }
    // FOR TESTING:
        // WITH USERNAMES:



    public Transfer(int transferId, int senderId, int recipientId, BigDecimal amount, String type, String status, String senderUsername, String recipientUsername){
        this.transferId = transferId;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.amount = amount;
        this.type = type;
        this.status = status;
        this.senderUsername = senderUsername;
        this.recipientUsername = recipientUsername;
    }
 
    public int getTransferId() {
        return transferId;
    }

    public void setTransferId(int transferId) {
        this.transferId = transferId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(int recipientId) {
        this.recipientId = recipientId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getRecipientUsername() {
        return recipientUsername;
    }

    public void setRecipientUsername(String recipientUsername) {
        this.recipientUsername = recipientUsername;
    }
}
