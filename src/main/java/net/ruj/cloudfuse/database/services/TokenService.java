package net.ruj.cloudfuse.database.services;

import net.ruj.cloudfuse.database.models.Token;
import net.ruj.cloudfuse.database.repositories.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.Date;
import java.util.Optional;

@Service
public class TokenService {
    private final TokenRepository tokenRepository;

    @Autowired
    public TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public void update(Token token) {
        tokenRepository.save(token);
    }

    //TODO: Handling token expiration
    public String getTokenString() throws EntityNotFoundException {
        return getLastToken()
                .map(Token::getToken)
                .orElseThrow(EntityNotFoundException::new);
    }

    private Optional<Token> getLastToken() {
        tokenRepository.deleteByExpirationDateIsLessThan(new Date());
        return tokenRepository.findFirstByOrderByCreatedDateDesc();
    }
}
