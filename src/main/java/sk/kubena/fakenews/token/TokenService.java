package sk.kubena.fakenews.token;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;

    @Autowired
    public TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public List<Token> getAllTokens() {
        return new ArrayList<>(tokenRepository.findAll());
    }

    public Token getToken(int id) {
        return tokenRepository.findById(id).orElse(null);
    }

    public void addToken(Token token) {
        tokenRepository.save(token);
    }

    public void updateToken(int id, Token token) {
        tokenRepository.save(token);
    }

    public void deleteToken(int id) {
        tokenRepository.deleteById(id);
    }

    public boolean isTokenValid(String tokenString) {
        if (tokenRepository.existsTokenByToken(tokenString)) {
            Token token = tokenRepository.findTokenByToken(tokenString);
            return token.getIsValid();
        } else {
            return false;
        }
    }

    public void invalidateToken(String tokenString) {
        if (tokenRepository.existsTokenByToken(tokenString)) {
            Token token = tokenRepository.findTokenByToken(tokenString);
            token.setIsValid(false);
            tokenRepository.save(token);
        }
    }
}
