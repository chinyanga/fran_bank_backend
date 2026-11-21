
package com.core.banking.service.impl;

import com.core.banking.dto.response.TransactionResponse;
import com.core.banking.entity.IdempotencyRecord;
import com.core.banking.entity.User;
import com.core.banking.enums.IdempotencyStatus;
import com.core.banking.exception.IdempotencyConflictException;
import com.core.banking.repository.BankTransactionRepository;
import com.core.banking.repository.IdempotencyRecordRepository;
import com.core.banking.repository.UserRepository;
import com.core.banking.security.CurrentUserService;
import com.core.banking.service.IdempotencyService;
import tools.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdempotencyServiceImpl implements IdempotencyService {

    private final IdempotencyRecordRepository idempotencyRepository;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final BankTransactionRepository transactionRepository;

    public IdempotencyServiceImpl(
            IdempotencyRecordRepository idempotencyRepository,
            CurrentUserService currentUserService, UserRepository userRepository, ObjectMapper objectMapper, BankTransactionRepository transactionRepository
    ) {
        this.idempotencyRepository = idempotencyRepository;
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.transactionRepository = transactionRepository;
    }



    @Override
    @Transactional
    public TransactionResponse execute(
            String idempotencyKey,
            String requestHash,
            TransactionOperation operation
    ) {
        User currentUser = currentUserService.getCurrentUser();

        User user = userRepository.findByIdForUpdate(currentUser.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated customer could not be found."
                ));

        IdempotencyRecord existing = idempotencyRepository
                .findByUserAndKeyForUpdate(
                        user.getId(),
                        idempotencyKey
                )
                .orElse(null);

        if (existing != null) {

            if (!existing.getRequestHash().equals(requestHash)) {
                throw new IdempotencyConflictException(
                        "This idempotency key has already been used "
                                + "with a different request."
                );
            }

            if (existing.getStatus() == IdempotencyStatus.COMPLETED) {
                try {
                    return objectMapper.readValue(
                            existing.getResponseBody(),
                            TransactionResponse.class
                    );
                } catch (Exception exception) {
                    throw new IllegalStateException(
                            "Unable to retrieve the original transaction response.",
                            exception
                    );
                }
            }

            throw new IllegalStateException(
                    "The operation associated with this key is not completed."
            );
        }

        IdempotencyRecord record = IdempotencyRecord.create(
                user,
                idempotencyKey,
                requestHash
        );

        record = idempotencyRepository.saveAndFlush(record);

        TransactionResponse response = operation.execute();

        try {
            String responseBody = objectMapper.writeValueAsString(response);

            var transaction = transactionRepository.findById(response.id())
                    .orElseThrow(() -> new IllegalStateException(
                            "Completed bank transaction could not be found."
                    ));

            record.complete(
                    200,
                    responseBody,
                    transaction
            );

            idempotencyRepository.save(record);

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to store the original transaction response.",
                    exception
            );
        }

        return response;
    }


}
