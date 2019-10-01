package net.sattler22.stats.util;

import java.util.Collections;
import java.util.Map;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import net.jcip.annotations.Immutable;

/**
 * JavaScript Object Notation (JSON) Error Message Converter
 *
 * @author Peter Sattler
 */
@Immutable
public final class JsonError {

    private final String message;

    /**
     * Constructs a new JSON error converter
     *
     * @param message The error message
     */
    public JsonError(String message) {
        this.message = message;
    }

    /**
     * Converts error message into a Spring {@code ModelAndView} object
     *
     * @return The JSON error as an MVC model/view
     */
    public ModelAndView asModelAndView() {
        final MappingJackson2JsonView jsonView = new MappingJackson2JsonView();
        final Map<String, String> map = Collections.singletonMap("error", message);
        return new ModelAndView(jsonView, Collections.unmodifiableMap(map));
    }

    @Override
    public String toString() {
        return String.format("%s [message=%s]", getClass().getSimpleName(), message);
    }
}
