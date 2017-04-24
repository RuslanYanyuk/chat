package com.chat.services.matchers;

import com.chat.models.Response;
import com.chat.models.ResponseType;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.function.Function;

/**
 * @author Ruslan Yaniuk
 * @date April 2017
 */
public class ResponseMatcher extends BaseMatcher<Response> {
    private Function<? super Object, Boolean> strategy;

    private ResponseMatcher(Function<? super Object, Boolean> strategy) {
        this.strategy = strategy;
    }

    @Override
    public boolean matches(Object item) {
        return strategy.apply(item);
    }

    @Override
    public void describeTo(Description description) {
    }

    public static Matcher<Response> hasResponseSuccess() {
        return new ResponseMatcher(r -> ((Response) r).getType().equals(ResponseType.SUCCESS));
    }
}

