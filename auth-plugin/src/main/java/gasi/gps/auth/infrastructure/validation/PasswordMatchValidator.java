package gasi.gps.auth.infrastructure.validation;

import org.springframework.beans.BeanUtils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, Object> {

    private String passwordField;
    private String confirmField;

    @Override
    public void initialize(PasswordMatch annotation) {
        this.passwordField = annotation.passwordField();
        this.confirmField = annotation.confirmField();
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        try {
            Object password = BeanUtils.getPropertyDescriptor(obj.getClass(), passwordField)
                    .getReadMethod().invoke(obj);
            Object confirm = BeanUtils.getPropertyDescriptor(obj.getClass(), confirmField)
                    .getReadMethod().invoke(obj);

            boolean valid = password != null && password.equals(confirm);

            if (!valid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                        .addPropertyNode(confirmField)
                        .addConstraintViolation();
            }

            return valid;
        } catch (Exception e) {
            return false;
        }
    }
}
