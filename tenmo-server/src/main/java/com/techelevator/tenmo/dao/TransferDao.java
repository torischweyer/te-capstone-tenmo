package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;

import java.util.List;

public interface TransferDao {

    Account retrieveAccountBalance(String username);

    boolean validateTransfer(Transfer transfer);

    Transfer createTransfer(Transfer transfer);

    boolean updateAccountBalances(Transfer transfer);

    List<User> retrieveListOfUsers();

    List<Transfer> retrieveListOfTransfers(int userId);

    List<Transfer> retrieveListOfPendingTransfers(int userId);

    Transfer retrieveTransferById(int transferId);

    int updateTransferStatus(Transfer transfer);



}
