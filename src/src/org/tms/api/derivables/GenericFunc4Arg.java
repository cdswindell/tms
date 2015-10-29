package org.tms.api.derivables;

@FunctionalInterface
public interface GenericFunc4Arg<T, U, V, W, R> 
{
    public R apply(T arg1, U arg2, V arg3, W arg4);
}
