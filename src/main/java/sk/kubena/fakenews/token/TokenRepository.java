package sk.kubena.fakenews.token;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token, Integer> {

    Boolean existsTokenByToken(String tokenString);

    Token findTokenByToken(String tokenString);
}
