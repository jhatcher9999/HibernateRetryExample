package com.hatcherdev.cockroach.hibernate.services;

import com.hatcherdev.cockroach.hibernate.aop.SerializationRetry;
import com.hatcherdev.cockroach.hibernate.objects.Account;
import org.hibernate.JDBCException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;

public class AccountServiceAOP {

    private boolean _forceRetry;

    public AccountServiceAOP() {
        _forceRetry = false;
    }

    public AccountServiceAOP(boolean forceRetry) {
        _forceRetry = forceRetry;
    }

    @SerializationRetry
    public BigDecimal forceRetryLogic(Session session) {
        BigDecimal rv = new BigDecimal(-1);
        try {
            System.out.printf("APP: testRetryLogic: BEFORE EXCEPTION\n");
            session.createNativeQuery("SELECT crdb_internal.force_retry('30s')").executeUpdate();
            //session.createNativeQuery("SELECT now();").list();
        } catch (JDBCException e) {
            System.out.printf("APP: testRetryLogic: AFTER EXCEPTION\n");
            throw e;
        }
        return rv;
    }

    @SerializationRetry
    public BigDecimal addAccounts(Session session) {

        BigDecimal rv = new BigDecimal(0);

        try {

            Transaction txn = session.beginTransaction();
            System.out.printf("APP: BEGIN;\n");

            if (_forceRetry) {
                forceRetryLogic(session);
            }

            session.save(new Account(7, 1000));
            session.save(new Account(8, 250));
            session.save(new Account(9, 314159));

            txn.commit();
            System.out.printf("APP: COMMIT;\n");

            rv = BigDecimal.valueOf(1);
            System.out.printf("APP: addAccounts() --> %.2f\n", rv);
        }
        catch(JDBCException e) {
            throw e;
        }

        return rv;
    }

}
