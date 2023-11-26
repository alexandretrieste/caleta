package com.challenge.caleta.controller;

import com.challenge.caleta.model.Player;
import com.challenge.caleta.model.Transaction;
import com.challenge.caleta.service.PlayerService;
import com.challenge.caleta.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/player")
public class PlayerController {

    private final PlayerService playerService;
    private final TransactionService transactionService;

    public enum TransactionType {
        BET,
        WIN,
        ROLLBACK
    }

    public PlayerController(PlayerService playerService, TransactionService transactionService) {
        this.playerService = playerService;
        this.transactionService = transactionService;
    }

    @GetMapping("/balance")
    public ResponseEntity<List<Map<String, Object>>> getAllBalances() {
        List<Player> players = playerService.getAllPlayers();

        List<Map<String, Object>> balances = new ArrayList<>();

        for (Player player : players) {
            Map<String, Object> playerBalance = new HashMap<>();
            playerBalance.put("id", player.getId());
            playerBalance.put("balance", player.getBalance());
            balances.add(playerBalance);
        }

        return ResponseEntity.ok(balances);
    }

    @GetMapping("/balance/{id}")
    public ResponseEntity<Map<String, Object>> getBalance(@PathVariable Long id) {
        Player player = playerService.findById(id);

        if (player == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("player", id);
        response.put("balance", player.getBalance());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/bet")
    public ResponseEntity<Map<String, Object>> bet(@RequestBody TransactionRequest request) {
        Player player = playerService.findById(request.getPlayerId());

        if (player == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        double betAmount = request.getValue();
        double currentBalance = player.getBalance();

        if (currentBalance < betAmount) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        player.setBalance(currentBalance - betAmount);
        playerService.save(player);

        Transaction transaction = new Transaction(player.getId(), betAmount, TransactionType.BET);

        transactionService.save(transaction);

        Map<String, Object> response = new HashMap<>();
        response.put("player", player.getId());
        response.put("balance", player.getBalance());
        response.put("txn", transaction.getTxn());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/win")
    public ResponseEntity<Map<String, Object>> win(@RequestBody TransactionRequest request) {
        Player player = playerService.findById(request.getPlayerId());

        if (player == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        double winAmount = request.getValue();

        player.setBalance(player.getBalance() + winAmount);
        playerService.save(player);

        Transaction transaction = new Transaction(player.getId(), winAmount, TransactionType.WIN);
        playerService.saveTransaction(transaction);

        Map<String, Object> response = new HashMap<>();
        response.put("player", player.getId());
        response.put("balance", player.getBalance());
        response.put("txn", transaction.getTxn());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/rollback")
    public ResponseEntity<Map<String, Object>> rollback(@RequestBody RollbackRequest request) {
        Transaction transaction = transactionService.findById(request.getTransactionId());

        if (transaction == null || transaction.isActive()) {
            return ResponseEntity.ok(createResponse("Invalid", null));
        }

//        if (transaction.getType() != TransactionType.BET) {
//            return ResponseEntity.ok(createResponse("Invalid", null));
//        }

        if (transaction.getValue() != request.getTransactionAmount()) {
            return ResponseEntity.ok(createResponse("Invalid", null));
        }

        playerService.rollbackTransaction(request.getPlayerId(), request.getTransactionAmount());

        Player player = playerService.findById(request.getPlayerId());
        Map<String, Object> response = createResponse("OK", player.getBalance());
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> createResponse(String code, Double balance) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", code);
        response.put("balance", balance != null ? balance : 0);
        return response;
    }

    private static class TransactionRequest {
        private Long playerId;
        private double value;

        public Long getPlayerId() {
            return playerId;
        }

        public void setPlayerId(Long playerId) {
            this.playerId = playerId;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }
    }

    private static class RollbackRequest {
        private Long playerId;
        private Long transactionId;
        private double transactionAmount;

        public Long getPlayerId() {
            return playerId;
        }

        public void setPlayerId(Long playerId) {
            this.playerId = playerId;
        }

        public Long getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(Long transactionId) {
            this.transactionId = transactionId;
        }

        public double getTransactionAmount() {
            return transactionAmount;
        }

        public void setTransactionAmount(double transactionAmount) {
            this.transactionAmount = transactionAmount;
        }
    }
}
