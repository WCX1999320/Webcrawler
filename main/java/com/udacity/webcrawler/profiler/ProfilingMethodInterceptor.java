package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

    private final Clock clock;
    //  private final ZonedDateTime startTime;
    private final ProfilingState state;
    private final Object targetObject;

    // TODO: You will need to add more instance fields and constructor arguments to this class.
    ProfilingMethodInterceptor(Clock clock, ProfilingState state, Object targetObject) {
        this.clock = Objects.requireNonNull(clock);
//    this.startTime = ZonedDateTime.now(clock);
        this.state = state;
        this.targetObject = targetObject;
    }
    private boolean isMethodProfiled(Method method) {
        return method.getAnnotation(Profiled.class) != null;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // TODO: This method interceptor should inspect the called method to see if it is a profiled
        //       method. For profiled methods, the interceptor should record the start time, then
        //       invoke the method using the object that is being profiled. Finally, for profiled
        //       methods, the interceptor should record how long the method call took, using the
        //       ProfilingState methods.
        Object toReturn = null;
        ZonedDateTime startTime=null;
        boolean isProfiled = isMethodProfiled(method);
        if (isProfiled) {
            startTime = ZonedDateTime.now(clock);
        }
        try {
            toReturn = method.invoke(targetObject, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        finally {
            if(isProfiled){
                Duration durationElapsed = Duration.between(startTime, clock.instant().atZone(Clock.systemDefaultZone().getZone()));
                state.record(targetObject.getClass(), method, durationElapsed);
            }
        }
        return toReturn;
    }
}
