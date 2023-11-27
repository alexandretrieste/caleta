package com.challenge.caleta.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.challenge.caleta.service.WalletService;
import com.challenge.caleta.dto.RollbackRequest;
import com.challenge.caleta.dto.WalletResponse;
import com.challenge.caleta.dto.BetRequest;
import com.challenge.caleta.dto.WinRequest;


@RestController
@RequestMapping("/player")
public class WalletController {
    @Autowired
    private WalletService walletService;

    @GetMapping("/balance/{playerId}")
    public WalletResponse getBalance(@PathVariable Long playerId) {
        return walletService.getBalance(playerId);
    }

    @PostMapping("/bet")
    public WalletResponse makeBet(@RequestBody BetRequest request) {
        return walletService.makeBet(request);
    }

    @PostMapping("/win")
    public WalletResponse makeWin(@RequestBody WinRequest request) {
        return walletService.makeWin(request);
    }

    @PostMapping("/rollback")
    public WalletResponse rollbackTransaction(@RequestBody RollbackRequest request) {
        return walletService.rollbackTransaction(request);
    }
}

