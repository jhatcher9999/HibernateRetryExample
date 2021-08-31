package com.hatcherdev.cockroach.hibernate;

import com.hatcherdev.cockroach.hibernate.services.*;
import com.hatcherdev.cockroach.hibernate.objects.*;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateRetryCLI {

    public static void main(String[] args) {

        SessionFactory sessionFactory = new Configuration()
                .configure("hibernate.cfg.xml")
                .addAnnotatedClass(Account.class)
                .buildSessionFactory();


        AccountServiceBasic accountServiceBasic = new AccountServiceBasic();
        AccountServiceCentralizedLogic accountServiceCentralizedLogic = new AccountServiceCentralizedLogic();
        AccountServiceAOP accountServiceAOP = new AccountServiceAOP(false);

        try (Session session = sessionFactory.openSession()) {

//            session.addEventListeners();
//
//            session.setHibernateFlushMode(FlushMode.AUTO);

            accountServiceBasic.addAccounts(session);
            //accountServiceCentralizedLogic.addAccounts(session);
            //accountServiceAOP.addAccounts(session);
        } finally {
            sessionFactory.close();
        }
    }

}
