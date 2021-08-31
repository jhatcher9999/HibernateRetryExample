package com.hatcherdev.cockroach.hibernate.objects;

import org.hibernate.annotations.Formula;

import javax.persistence.*;
import java.math.BigDecimal;

// Account is our model, which corresponds to the "accounts" database table.
@Entity
@Table(name = "accounts2")
public class Account {

    @Id
    //@GeneratedValue(strategy = GenerationType.AUTO) // this creates a sequence
    //@GeneratedValue(strategy = GenerationType.TABLE) // this creates a table called hibernate_sequences which seems to be a hibernate-managed sequence
    @GeneratedValue(strategy = GenerationType.SEQUENCE) // this creates a sequence
    @SequenceGenerator(name = "hibernate_sequence", allocationSize = 51)
    //@GeneratedValue(strategy = GenerationType.IDENTITY) // this creates a SERIAL8 data type (which means INT data type with a default of unique_rowid() )
    @Column(name = "id")
    public long id;

    public long getId() {
        return id;
    }

    @Column(name = "balance")
    public BigDecimal balance;

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal newBalance) {
        this.balance = newBalance;
    }

    // Convenience constructor.
    public Account(int id, int balance) {
        this.id = id;
        this.balance = BigDecimal.valueOf(balance);
    }

    // Hibernate needs a default (no-arg) constructor to create model objects.
    public Account() {
    }

}