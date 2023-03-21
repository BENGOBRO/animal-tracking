package ru.bengo.animaltracking.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.bengo.animaltracking.exception.UserAlreadyExistException;
import ru.bengo.animaltracking.model.Account;

import java.util.List;
import java.util.Optional;

public interface AccountService {
    Optional<Account> findById(@NotNull @Positive Integer id);
    Optional<List<Account>> search(String firstName, String lastName, String email,
                                   @Min(0) Integer from, @Min(1) Integer size);

    Account save(Account account);

    Account register(@Valid Account account) throws UserAlreadyExistException;

    Account update(Account account) throws UserAlreadyExistException;
}
