package com.challenge.caleta.model;

import com.challenge.caleta.controller.PlayerController;
import jakarta.persistence.*;

@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer txn;

    private double value;

    private boolean active;

    @Enumerated(EnumType.STRING)
    private PlayerController.TransactionType type;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;

    public Transaction() {
    }

    public Transaction(Player player, double value, PlayerController.TransactionType type) {
        this.player = player;
        this.value = value;
        this.type = type;
    }

    public Transaction(Integer txn, double value) {
        this.txn = txn;
        this.value = value;
    }

    public Transaction(Long id, double betValue, PlayerController.TransactionType transactionType) {
    }

    public Transaction(Long playerId, String win, double winValue) {
    }

    public Integer getTxn() {
        return txn;
    }

    public void setTxn(Integer txn) {
        this.txn = txn;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "txn=" + txn +
                ", value=" + value +
                '}';
    }
}