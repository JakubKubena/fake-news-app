package sk.kubena.fakenews.user;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sk.kubena.fakenews.ajax.AJAXController;
import sk.kubena.fakenews.error.UserAlreadyExistException;
import sk.kubena.fakenews.role.RoleRepository;
import sk.kubena.fakenews.ajax.TokenGenerator;


@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AJAXController.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUser(int id) {
        return userRepository.findById(id).orElse(null);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User getUserByToken(String token) {
        return userRepository.findByToken(token);
    }

    public void addUser(User user) {
        userRepository.save(user);
    }

    public void updateUser(int id, User user) {
        userRepository.save(user);
    }

    public void deleteUser(int id) {
        userRepository.deleteById(id);
    }

    @Transactional
//    @Override
    public User registerNewUserAccount(UserDTO userDTO)
            throws UserAlreadyExistException {

        if (emailExists(userDTO.getEmail())) {
            throw new UserAlreadyExistException("There is an account with that email address: " + userDTO.getEmail());
        }

        User user = new User();
        user.setPassword(userDTO.getPassword());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setEmail(userDTO.getEmail());
        user.setEnabled(true);
        user.setToken(TokenGenerator.generateType1UUID().toString());
        user.setRole(roleRepository.findRoleByName("ADMIN"));
//        user.setRole(Arrays.asList("ROLE_USER"));
        return userRepository.save(user);
    }

    public String authenticateUser(UserDTO userDTO) {
        User user = userRepository.findByEmail(userDTO.getEmail());
        if (user == null) {
            LOGGER.warn("User {} doesnt exist!", userDTO.getEmail());
            return null;
        } else if (passwordEncoder.matches(userDTO.getPassword(), user.getPassword())) {
            LOGGER.info("Found user {} and returning token {}.", user.getEmail(), user.getToken());
            return user.getToken();
        } else {
            LOGGER.warn("Wrong password!");
            return null;
        }
    }

    private boolean emailExists(String email) {
        return userRepository.findByEmail(email) != null;
    }

    public boolean tokenExists(String token) {
        return userRepository.findByToken(token) != null;
    }
}
