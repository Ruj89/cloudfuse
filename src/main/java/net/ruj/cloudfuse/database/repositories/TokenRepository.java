package net.ruj.cloudfuse.database.repositories;

import net.ruj.cloudfuse.database.models.Token;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface TokenRepository extends CrudRepository<Token, Long> {
    Optional<Token> findFirstByOrderByCreatedDateDesc();

    void deleteByExpirationDateIsLessThan(Date date);
}
