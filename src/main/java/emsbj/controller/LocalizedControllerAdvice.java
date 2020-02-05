package emsbj.controller;

import emsbj.Extensions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Locale;

@ControllerAdvice(assignableTypes = {LocalizedController.class})
public class LocalizedControllerAdvice {
    @Autowired
    private MessageSource messageSource;

    @ModelAttribute(name = "x")
    public Object addExtensions(Locale locale) {
        return new Extensions(messageSource, locale);
    }
}