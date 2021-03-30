package sk.kubena.fakenews.user;

import sk.kubena.fakenews.error.UserAlreadyExistException;

public interface IUserService {
    User registerNewUserAccount(UserDto userDto)
            throws UserAlreadyExistException;
}
