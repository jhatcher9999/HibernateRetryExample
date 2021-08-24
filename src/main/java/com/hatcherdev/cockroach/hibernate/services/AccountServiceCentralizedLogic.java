package com.hatcherdev.cockroach.hibernate.services;

import java.math.BigDecimal;
import java.util.Random;
import java.util.function.Function;
import org.hibernate.JDBCException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.hatcherdev.cockroach.hibernate.objects.Account;

public class AccountServiceCentralizedLogic {

    private static final Random RAND = new Random();
    private static final String RETRY_SQL_STATE = "40001";
    private static final int MAX_ATTEMPT_COUNT = 6;

    private BigDecimal runTransaction(Session session, Function<Session, BigDecimal> fn) {
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
            //  CALL THE FUNCTION
            //**********************************************************************************************************************
            try {
                rv = fn.apply(session);
                if (!rv.equals(-1)) {
                    txn.commit();
                    rv = BigDecimal.valueOf(1);

                    System.out.printf("APP: COMMIT;\n");
                    break;
                }

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

    public void addAccounts(Session session) {
        runTransaction(session, getAddAccountsFunction());
    }

    public void deleteAccount(Session session) {
        runTransaction(session, getDeleteAccountFunction());
    }

    private Function<Session, BigDecimal> getAddAccountsFunction() throws JDBCException {
        Function<Session, BigDecimal> f = s -> {
            BigDecimal rv = new BigDecimal(0);
            try {
                s.save(new Account(4, 1000));
                s.save(new Account(5, 250));
                s.save(new Account(6, 314159));
                rv = BigDecimal.valueOf(1);
                System.out.printf("APP: addAccounts() --> %.2f\n", rv);
            } catch (JDBCException e) {
                throw e;
            }
            return rv;
        };
        return f;
    }

    private Function<Session, BigDecimal> getDeleteAccountFunction() throws JDBCException {
        Function<Session, BigDecimal> f = s -> {
            BigDecimal rv = new BigDecimal(0);
            try {
                s.delete(new Account(0, 0));
                rv = BigDecimal.valueOf(1);
                System.out.printf("APP: addAccounts() --> %.2f\n", rv);
            } catch (JDBCException e) {
                throw e;
            }
            return rv;
        };
        return f;
    }
}