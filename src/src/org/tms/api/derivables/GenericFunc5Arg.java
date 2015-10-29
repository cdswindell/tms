package org.tms.api.derivables;

@FunctionalInterface
public interface GenericFunc5Arg<T, U, V, W, X, R> 
{
    public R apply(T arg1, U arg2, V arg3, W arg4, X arg5);
}
