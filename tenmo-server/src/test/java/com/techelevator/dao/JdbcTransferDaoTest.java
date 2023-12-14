package com.techelevator.dao;
import com.techelevator.tenmo.dao.JdbcTransferDao;
import com.techelevator.tenmo.dao.JdbcUserDao;
import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.junit.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class JdbcTransferDaoTest extends BaseDaoTests {

    private static final Account ACCOUNT_2001 = new Account(BigDecimal.valueOf(1000.00));
    private static final Account ACCOUNT_2002 = new Account(BigDecimal.valueOf(2000.00));
    private static final Account ACCOUNT_2003 = new Account(BigDecimal.valueOf(3000.00));

    private static final Transfer TRANSFER_3001 = new Transfer(3001, 1001, 1002, BigDecimal.valueOf(100.00), "Send", "Approved", "user1", "user2");
    private static final Transfer TRANSFER_3002 = new Transfer(3002, 1001, 1002, BigDecimal.valueOf(200.00), "Request", "Pending", "user1", "user2");
    private static final Transfer TRANSFER_3003 = new Transfer(3003, 1002, 1003, BigDecimal.valueOf(200.00), "Request", "Pending", "user2", "user3");
    private static final Transfer TRANSFER_3004 = new Transfer(3004, 1001, 1002, BigDecimal.valueOf(1000000.00), "Send", "Approved", "user1", "user2");

    private TransferDao sut;
    private Transfer testTransfer;
    private JdbcTemplate jdbcTemplate;

    @Before
    public void setup() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        sut = new JdbcTransferDao(jdbcTemplate);

        testTransfer = new Transfer(0,1002, 1001, BigDecimal.valueOf(25.00), "Send", "Approved", "user2", "user1");
    }


    @Test
    public void retrieveAccountBalance_returns_correct_balance() {
        final BigDecimal expectedValue = new BigDecimal(1000.00).setScale(2);
        BigDecimal balance =  sut.retrieveAccountBalance("user1").getBalance().setScale(2);

        Assert.assertEquals(expectedValue, balance);
    }


    @Test
    public void validateTransfer_returns_correct_boolean() {
        // Happy path
        boolean isAbleToTransfer = sut.validateTransfer(TRANSFER_3001);
        Assert.assertTrue("Transfer amount should have been validated." , isAbleToTransfer);

        // Not enough money, should fail
        isAbleToTransfer = sut.validateTransfer(TRANSFER_3004);
        Assert.assertFalse( "Transfer amount should have failed validation.", isAbleToTransfer);


    }

    @Test
    public void createTransfer_posts_transfer_to_database() {
        Transfer createdTransfer = sut.createTransfer(testTransfer);
        Assert.assertNotNull("Created transfer was null.",createdTransfer);

        int newId = createdTransfer.getTransferId();
        Assert.assertTrue("Did not return a new Id.",newId > 0);

        Transfer retrievedTransfer = sut.retrieveTransferById(newId);
        assertTransfersMatch(createdTransfer, retrievedTransfer);
    }

    @Test
    public void updateAccountBalances_should_update_both_balances_correctly() {
        Transfer transfer = sut.retrieveTransferById(3001);
        sut.updateAccountBalances(transfer);

        Assert.assertEquals("Sender's balance did not update correctly.",BigDecimal.valueOf(900.00).setScale(2), retrieveAccountBalanceByUserId(1001));
        Assert.assertEquals("Recipient's balance did not update correctly.",BigDecimal.valueOf(2100.00).setScale(2), retrieveAccountBalanceByUserId(1002));
    }


    @Test
    public void retrieveListOfUsers_should_return_full_list_of_users() {
        // Set up expected user
        User expectedUser = new User();
        expectedUser.setUsername("user1");
        expectedUser.setId(1001);

        List<User> testUsers = sut.retrieveListOfUsers();
        // Assert size
        Assert.assertEquals("List of user was incorrect size.",3, testUsers.size());

        // Assert users at the first index match
        assertUsersMatch(expectedUser, testUsers.get(0));
    }

    @Test
    public void retrieveListOfTransfers_should_return_all_transfers_associated_with_user_id() {
        List<Transfer> transfersForUserTwo = sut.retrieveListOfTransfers(1002);
        List<Transfer> transfersForUserThree = sut.retrieveListOfTransfers(1003);

        // Assert transfers come back no matter if the user is the sender or recipient
        Assert.assertEquals("List of transfers was incorrect size for given user.", 4 , transfersForUserTwo.size());
        assertTransfersMatch(TRANSFER_3001 , transfersForUserTwo.get(0));

        // Assert that the method is not just pulling all of the transfers
        Assert.assertEquals("List of transfers was incorrect size for given user.", 1 , transfersForUserThree.size());
        assertTransfersMatch(TRANSFER_3003 , transfersForUserThree.get(0));
    }

    @Test
    public void retrieveListOfPendingTransfers_should_return_only_pending_transfers_associated_with_current_user() {
        // Asserting request transfers for user one
        List<Transfer> requestTransfersForUserOne = sut.retrieveListOfPendingTransfers(1001);
        // Asserting list size
        Assert.assertEquals("List of pending transfers for given user was incorrect size.", 1, requestTransfersForUserOne.size());
        // Asserting transfers match
        assertTransfersMatch(TRANSFER_3002, requestTransfersForUserOne.get(0));

        // Asserting that a user with no pending requests receives an empty list
        List<Transfer> requestTransfersForUserThree = sut.retrieveListOfPendingTransfers(1003);
        Assert.assertEquals("List of pending transfers for given user was incorrect size.", 0, requestTransfersForUserThree.size());
    }


    @Test
    public void retrieveTransferById_should_return_correct_transfer() {
        // Asserting happy path
        Transfer pulledTransfer = sut.retrieveTransferById(3001);
        assertTransfersMatch(TRANSFER_3001, pulledTransfer);

        // Asserting that an incorrect transferId will return null.
        Transfer pulledTransferTwo = sut.retrieveTransferById(30000003);
        Assert.assertNull("Invalid transferId should not have returned any NOT NULL value.", pulledTransferTwo);
    }


    @Test
    public void updateTransferStatus_correctly_updates_status_in_database() {
        // Asserting that it updates the status to Approved from Pending
        Transfer pulledTransfer = sut.retrieveTransferById(3002);
        pulledTransfer.setStatus("Approved");

        // Checking rows affected
        int rowsAffected = sut.updateTransferStatus(pulledTransfer);
        Assert.assertTrue("Method did not affect any rows in the database.", rowsAffected > 0);

        // Asserting the transfers match
        Transfer updatedTransfer = sut.retrieveTransferById(3002);
        assertTransfersMatch(pulledTransfer, updatedTransfer);
    }

    private BigDecimal retrieveAccountBalanceByUserId(int userId) {
        String sql = "SELECT balance FROM account WHERE user_id = ?";
        BigDecimal accountBalance = new BigDecimal(0.00);

        try {
            SqlRowSet result = jdbcTemplate.queryForRowSet(sql, userId);
            if (result.next()) {
                accountBalance = result.getBigDecimal("balance");
            }
        }
        catch(Exception e) {
            throw new DaoException("There was an error grabbing a test account balance", e);
        }

        return accountBalance;
    }

    private void assertTransfersMatch(Transfer expected, Transfer actual) {
            Assert.assertEquals(expected.getTransferId(), actual.getTransferId());
            Assert.assertEquals(expected.getSenderId(), actual.getSenderId());
            Assert.assertEquals(expected.getRecipientId(), actual.getRecipientId());
            Assert.assertEquals(expected.getAmount().setScale(2), actual.getAmount().setScale(2));
            Assert.assertEquals(expected.getType(), actual.getType());
            Assert.assertEquals(expected.getStatus(), actual.getStatus());
            Assert.assertEquals(expected.getSenderUsername(), actual.getSenderUsername());
            Assert.assertEquals(expected.getRecipientUsername(), actual.getRecipientUsername());
    }

    private void assertUsersMatch(User expected, User actual) {
        Assert.assertEquals(expected.getUsername(), actual.getUsername());
        Assert.assertEquals(expected.getId(), actual.getId());
    }



}
