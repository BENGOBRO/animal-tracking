package ru.bengo.animaltracking.api.annotation;

import jakarta.validation.Constraint;
import ru.bengo.animaltracking.api.annotation.validator.LifeStatusValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = LifeStatusValidator.class)

@Documented
public @interface LifeStatusAnnotation {
    String message() default "{LifeStatus.invalid}";
}
