package com.hatcherdev.cockroach.hibernate.services;

import com.hatcherdev.cockroach.hibernate.objects.Account;
import org.hibernate.JDBCException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.util.Random;
import java.util.function.Function;

public class AccountServiceBasic {



    private static final Random RAND = new Random();
    private static final String RETRY_SQL_STATE = "40001";
    private static final int MAX_ATTEMPT_COUNT = 6;

    public BigDecimal addAccountsTypical(Session session) {

        //**********************************************************************************************************************
        //  EXAMPLE OF SIMPLE DATA ACCESS METHOD
        //**********************************************************************************************************************

        Transaction txn = session.beginTransaction();
        System.out.printf("APP: BEGIN;\n");

        BigDecimal rv = new BigDecimal(0);

        try {
            session.save(new Account(1, 1000));
            session.save(new Account(2, 250));
            session.save(new Account(3, 314159));

            System.out.printf("APP: addAccounts() --> %.2f\n", rv);

            txn.commit();
            rv = BigDecimal.valueOf(1);

            System.out.printf("APP: COMMIT;\n");

        } catch (JDBCException e) {
            throw e;
        }

        return rv;
    }

    public BigDecimal addAccounts(Session session) {

        Transaction txn = session.beginTransaction();
        System.out.printf("APP: BEGIN;\n");

        int attemptCount = 0;

        BigDecimal rv = new BigDecimal(0);

        //**********************************************************************************************************************
        //  WRAP IN A LOOP TO ALLOW FOR RETRIES
        //**********************************************************************************************************************
        while (attemptCount < MAX_ATTEMPT_COUNT) {
            attemptCount++;

            //**********************************************************************************************************************
            //  GUTS OF THE DATA ACCESS LOGIC HERE
            //**********************************************************************************************************************
            try {
                Account account1 = new Account();
                account1.id = 101;
                account1.setBalance(BigDecimal.valueOf(1000));
                session.save(account1);
                System.out.printf("APP: account id :" + account1.getId() + "\n");

                Account account2 = new Account();
                account2.id = 102;
                account2.setBalance(BigDecimal.valueOf(250));
                session.save(account2);
                System.out.printf("APP: account id :" + account2.getId() + "\n");

                Account account3 = new Account();
                account3.id = 103;
                account3.setBalance(BigDecimal.valueOf(3.14159));
                session.save(account3);
                System.out.printf("APP: account id :" + account3.getId() + "\n");

                txn.commit();
                System.out.printf("APP: COMMIT;\n");

                rv = BigDecimal.valueOf(1);
                System.out.printf("APP: addAccounts() --> %.2f\n", rv);

                break;

            } catch (JDBCException e) {
                //**********************************************************************************************************************
                //  LOOK FOR SERIALIZATION ERROR CODE
                //**********************************************************************************************************************
                if (RETRY_SQL_STATE.equals(e.getSQLState())) {
                    // Since this is a transaction retry error, we
                    // roll back the transaction and sleep a little
                    // before trying again.  Each time through the
                    // loop we sleep for a little longer than the last
                    // time (A.K.A. exponential backoff).
                    System.out.printf("APP: retryable exception occurred:\n    sql state = [%s]\n    message = [%s]\n    retry counter = %s\n", e.getSQLState(), e.getMessage(), attemptCount);
                    System.out.printf("APP: ROLLBACK;\n");
                    txn.rollback();
                    int sleepMillis = (int) (Math.pow(2, attemptCount) * 100) + RAND.nextInt(100);
                    System.out.printf("APP: Hit 40001 transaction retry error, sleeping %s milliseconds\n", sleepMillis);
                    try {
                        Thread.sleep(sleepMillis);
                    } catch (InterruptedException ignored) {
                        // no-op
                    }
                    rv = BigDecimal.valueOf(-1);
                } else {
                    throw e;
                }
            }

        }
        return rv;
    }

    public BigDecimal deleteAccount(Session session, BigDecimal id) {

        //**********************************************************************************************************************
        //IN EVERY DATA ACCESS METHOD, YOU NEED TO CODE UP ALL THE SAME RETRY LOGIC
        //**********************************************************************************************************************

        return new BigDecimal(0);
    }
}