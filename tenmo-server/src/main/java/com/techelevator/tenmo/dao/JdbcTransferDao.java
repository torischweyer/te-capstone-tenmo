package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao{

    private final JdbcTemplate jdbcTemplate;

    public JdbcTransferDao(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Account retrieveAccountBalance(String username) {
        Account account = null;
        String sql = "SELECT balance FROM account " +
                "JOIN tenmo_user ON account.user_id = tenmo_user.user_id " +
                "WHERE username ILIKE ?";

        try {
            SqlRowSet result = jdbcTemplate.queryForRowSet(sql, username);
            if (result.next()){
                account = mapRowToAccount(result);
            }
        } catch (Exception e){
            throw new DaoException("Unable to reach database or account was not found", e);
        }

        return account;
    }

    @Override
    public boolean validateTransfer(Transfer transfer) {
        BigDecimal balance = new BigDecimal("0");
        String sql = "SELECT balance FROM account WHERE user_id = ?";

        try {
            SqlRowSet result = jdbcTemplate.queryForRowSet(sql, transfer.getSenderId());
            if (result.next()) {
                balance = result.getBigDecimal("balance");
            }
            if (balance.compareTo(transfer.getAmount()) >= 0){
                return true;
            }
        } catch (Exception e) {
            throw new DaoException("Was not able to get account balance", e);
        }

        return false;
    }

    @Override
    public Transfer createTransfer(Transfer transfer) {

        String createSQL = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "VALUES ((SELECT transfer_type_id FROM transfer_type WHERE transfer_type_desc ILIKE ?)," +
                "(SELECT transfer_status_id FROM transfer_status WHERE transfer_status_desc ILIKE ?), " +
                "(SELECT account_id FROM account WHERE user_id = ?), " +
                "(SELECT account_id FROM account WHERE user_id = ?), " +
                "?) RETURNING transfer_id";

        try {
            int transferID = jdbcTemplate.queryForObject(createSQL, int.class, transfer.getType(), transfer.getStatus(),
                    transfer.getSenderId(), transfer.getRecipientId(), transfer.getAmount());
            transfer.setTransferId(transferID);
        } catch (Exception e) {
            throw new DaoException("There was an error.", e);
        }

        return transfer;
    }


    @Override
    public boolean updateAccountBalances(Transfer transfer) {
        //  Grabbing initial balance from database SQL
        String grabBalanceSql = "SELECT balance FROM account WHERE user_id = ?";

        // Updating account balance SQL
        String updateBalanceSql = "UPDATE account SET balance = ? WHERE user_id = ?";

        int senderAffected = 0;
        int recipientAffected = 0;

        try {
            BigDecimal senderBalance = new BigDecimal(0);
            BigDecimal recipientBalance = new BigDecimal(0);

            // SENDER:
            SqlRowSet result = jdbcTemplate.queryForRowSet(grabBalanceSql, transfer.getSenderId());
            if (result.next()) {
                senderBalance = result.getBigDecimal("balance");
                senderBalance = senderBalance.subtract(transfer.getAmount());
                senderAffected = jdbcTemplate.update(updateBalanceSql, senderBalance, transfer.getSenderId());
            }

            // RECIPIENT:
            result = jdbcTemplate.queryForRowSet(grabBalanceSql, transfer.getRecipientId());
            if (result.next()) {
                recipientBalance = result.getBigDecimal("balance");
                recipientBalance = recipientBalance.add(transfer.getAmount());
                recipientAffected = jdbcTemplate.update(updateBalanceSql, recipientBalance, transfer.getRecipientId());
            }
        }
        catch (Exception e) {
            throw new DaoException("There was an error with updating the balances.", e);
        }

        return senderAffected == 1 && recipientAffected == 1;
    }

    @Override
    public List<User> retrieveListOfUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username FROM tenmo_user ORDER BY user_id";

        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
            while(results.next()) {
                users.add(mapRowToUser(results));
            }
        }
        catch (Exception e) {
            throw new DaoException("There was an error fetching the list of users.", e);
        }

        return users;
    }

    @Override
    public List<Transfer> retrieveListOfTransfers(int userId) {
        List<Transfer> transfers = new ArrayList<>();
        String sql = "SELECT t.transfer_id, tt.transfer_type_desc, ts.transfer_status_desc, t.amount, " +
                "tuf.username AS sender_username, tut.username AS recipient_username, " +
                "af.user_id AS sender_id, at.user_id AS recipient_id " +
                "FROM transfer AS t " +
                "JOIN transfer_type AS tt ON t.transfer_type_id = tt.transfer_type_id " +
                "JOIN transfer_status AS ts ON t.transfer_status_id = ts.transfer_status_id " +
                "JOIN account AS af ON t.account_from = af.account_id " +
                "JOIN account AS at ON t.account_to = at.account_id " +
                "JOIN tenmo_user AS tuf ON af.user_id = tuf.user_id " +
                "JOIN tenmo_user AS tut ON at.user_id = tut.user_id " +
                "WHERE af.user_id = ? OR at.user_id = ? " +
                "ORDER BY t.transfer_id";

        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId, userId);
            while (results.next()){
                transfers.add(mapRowToTransfer(results));
            }
        } catch (Exception e ) {
            throw new DaoException("There was an error getting transfers.", e);
        }

        return transfers;
    }

    public List<Transfer> retrieveListOfPendingTransfers(int userId) {
        List<Transfer> transfers = new ArrayList<>();
        String sql = "SELECT t.transfer_id, tt.transfer_type_desc, ts.transfer_status_desc, t.amount, " +
                "tuf.username AS sender_username, tut.username AS recipient_username, " +
                "af.user_id AS sender_id, at.user_id AS recipient_id " +
                "FROM transfer AS t " +
                "JOIN transfer_type AS tt ON t.transfer_type_id = tt.transfer_type_id " +
                "JOIN transfer_status AS ts ON t.transfer_status_id = ts.transfer_status_id " +
                "JOIN account AS af ON t.account_from = af.account_id " +
                "JOIN account AS at ON t.account_to = at.account_id " +
                "JOIN tenmo_user AS tuf ON af.user_id = tuf.user_id " +
                "JOIN tenmo_user AS tut ON at.user_id = tut.user_id " +
                "WHERE af.user_id = ? AND ts.transfer_status_desc ILIKE 'Pending' " +
                "ORDER BY t.transfer_id";

        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
            while (results.next()) {
                transfers.add(mapRowToTransfer(results));
            }
        }
        catch (Exception e) {
            throw  new DaoException("There was a problem with fetching the pending transactions.", e);
        }

        return transfers;
    }

    public Transfer retrieveTransferById(int transferId){
        Transfer transfer = null;
        String sql = "SELECT t.transfer_id, tt.transfer_type_desc, ts.transfer_status_desc, t.amount, " +
                "tuf.username AS sender_username, tut.username AS recipient_username, " +
                "af.user_id AS sender_id, at.user_id AS recipient_id " +
                "FROM transfer AS t " +
                "JOIN transfer_type AS tt ON t.transfer_type_id = tt.transfer_type_id " +
                "JOIN transfer_status AS ts ON t.transfer_status_id = ts.transfer_status_id " +
                "JOIN account AS af ON t.account_from = af.account_id " +
                "JOIN account AS at ON t.account_to = at.account_id " +
                "JOIN tenmo_user AS tuf ON af.user_id = tuf.user_id " +
                "JOIN tenmo_user AS tut ON at.user_id = tut.user_id " +
                "WHERE t.transfer_id = ?";
        try {
            SqlRowSet result = jdbcTemplate.queryForRowSet(sql, transferId);
            if (result.next()) {
                transfer = mapRowToTransfer(result);
            }
        } catch (Exception e) {
            throw new DaoException("There was an error locating specific transfer.", e);
        }

        return transfer;
    }

    public int updateTransferStatus(Transfer transfer){
        int rowsAffected = 0;
        String sql = "UPDATE transfer " +
                "SET transfer_status_id = (SELECT transfer_status_id FROM transfer_status WHERE transfer_status_desc ILIKE ?) " +
                "WHERE transfer_id = ?";
        try {
            rowsAffected = jdbcTemplate.update(sql, transfer.getStatus(), transfer.getTransferId());
            if (rowsAffected == 0) {
                throw new DaoException("No rows were updated.");
            }
        } catch (Exception e) {
            throw new DaoException("There was an error updating the transfer.", e);
        }

        return rowsAffected;
    }

    private Account mapRowToAccount(SqlRowSet rowSet){
        Account account = new Account();
        account.setBalance(rowSet.getBigDecimal("balance"));
        return account;
    }

    private User mapRowToUser(SqlRowSet rowSet) {
        User user = new User();
        user.setId(rowSet.getInt("user_id"));
        user.setUsername(rowSet.getString("username"));
        return user;
    }

    private Transfer mapRowToTransfer(SqlRowSet rowSet){
        Transfer transfer = new Transfer();
        transfer.setTransferId(rowSet.getInt("transfer_id"));
        transfer.setSenderUsername(rowSet.getString("sender_username"));
        transfer.setSenderId(rowSet.getInt("sender_id"));
        transfer.setRecipientId(rowSet.getInt("recipient_id"));
        transfer.setRecipientUsername(rowSet.getString("recipient_username"));
        transfer.setAmount(rowSet.getBigDecimal("amount"));
        transfer.setType(rowSet.getString("transfer_type_desc"));
        transfer.setStatus(rowSet.getString("transfer_status_desc"));
        return transfer;
    }
}
