package org.tms.api.derivables;

@FunctionalInterface
public interface GenericFunc3Arg<T, U, V, R>
{
    public R apply(T arg1, U arg2, V arg3);
}
