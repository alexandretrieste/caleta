package com.challenge.caleta.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.challenge.caleta.repository.PlayerRepository;
import com.challenge.caleta.repository.TransactionRepository;
import com.challenge.caleta.dto.RollbackRequest;
import com.challenge.caleta.dto.WalletResponse;
import com.challenge.caleta.dto.BetRequest;
import com.challenge.caleta.dto.WinRequest;
import com.challenge.caleta.model.Player;
import com.challenge.caleta.model.Transaction;
import java.util.Optional;

@Service
public class WalletService {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public WalletResponse getBalance(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        return new WalletResponse(playerId, player.getBalance());
    }

    public WalletResponse makeBet(BetRequest request) {
        Optional<Player> optionalPlayer = playerRepository.findById(request.getPlayer());

        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();

            if (player.getBalance() >= request.getValue()) {
                // Atualiza o saldo do jogador
                player.setBalance(player.getBalance() - request.getValue());
                playerRepository.save(player);

                // Registra a transação
                Transaction transaction = new Transaction();
                transaction.setPlayerId(request.getPlayer());
                transaction.setValue(request.getValue());
                transaction.setType("bet");
                transaction.setCanceled(false);
                transactionRepository.save(transaction);

                return new WalletResponse(request.getPlayer(), player.getBalance(), transaction.getId());
            } else {
                // Saldo insuficiente
                throw new RuntimeException("Insufficient balance");
            }
        } else {
            // Jogador não encontrado
            throw new RuntimeException("Player not found");
        }
    }

    public WalletResponse makeWin(WinRequest request) {
        Optional<Player> optionalPlayer = playerRepository.findById(request.getPlayer());

        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();

            // Atualiza o saldo do jogador
            player.setBalance(player.getBalance() + request.getValue());
            playerRepository.save(player);

            // Registra a transação
            Transaction transaction = new Transaction();
            transaction.setPlayerId(request.getPlayer());
            transaction.setValue(request.getValue());
            transaction.setType("win");
            transaction.setCanceled(false);
            transactionRepository.save(transaction);

            return new WalletResponse(request.getPlayer(), player.getBalance(), transaction.getId());
        } else {
            // Jogador não encontrado
            throw new RuntimeException("Player not found");
        }
    }

    public WalletResponse rollbackTransaction(RollbackRequest request) {
        Optional<Transaction> optionalTransaction = transactionRepository.findById(request.getTxn());

        if (optionalTransaction.isPresent()) {
            Transaction transaction = optionalTransaction.get();

            if (!transaction.isCanceled()) {
                // Reverte a transação
                if ("bet".equals(transaction.getType())) {
                    // Se a transação for uma aposta, adiciona o valor de volta ao saldo do jogador
                    Optional<Player> optionalPlayer = playerRepository.findById(request.getPlayer());

                    if (optionalPlayer.isPresent()) {
                        Player player = optionalPlayer.get();
                        player.setBalance(player.getBalance() + request.getValue());
                        playerRepository.save(player);
                    }
                }

                // Marca a transação como cancelada
                transaction.setCanceled(true);
                transactionRepository.save(transaction);

                return new WalletResponse(request.getPlayer(), playerRepository.findById(request.getPlayer()).get().getBalance(), request.getTxn());
            } else {
                // Transação já cancelada anteriormente
                return new WalletResponse(request.getPlayer(), playerRepository.findById(request.getPlayer()).get().getBalance(), request.getTxn());
            }
        } else {
            // Transação não encontrada
            throw new RuntimeException("Transaction not found");
        }
    }
}
