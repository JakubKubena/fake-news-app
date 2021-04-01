package sk.kubena.fakenews.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import sk.kubena.fakenews.user.UserDTO;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

    @Override
    public void initialize(final PasswordMatches constraintAnnotation) {
        //
    }

    @Override
    public boolean isValid(final Object obj, final ConstraintValidatorContext context) {
        final UserDTO user = (UserDTO) obj;
        return user.getPassword().equals(user.getMatchingPassword());
    }

}
