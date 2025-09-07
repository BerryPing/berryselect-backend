package com.berryselect.backend.transaction.controller;

import com.berryselect.backend.transaction.dto.request.TransactionRequest;
import com.berryselect.backend.transaction.dto.response.TransactionResponse;
import com.berryselect.backend.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public TransactionResponse createTransaction(
            @RequestBody TransactionRequest request,
            @RequestHeader("X-USER-ID") Long userId
    ) {
        return transactionService.createTransaction(request, userId);
    }
}