package org.tms.teq.ops;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.tms.api.derivables.Operator;
import org.tms.api.utils.AsynchronousOp;
import org.tms.api.utils.RegisterOp;
import org.tms.api.utils.SynchronousOp;
import org.tms.tds.TokenMapper;

public class ClassOp  
{
	private static final Set<String> sf_ExcludedMethods = new HashSet<String>();
	static {
		sf_ExcludedMethods.add("equals");
		sf_ExcludedMethods.add("hashCode");
		sf_ExcludedMethods.add("getClass");
		sf_ExcludedMethods.add("toString");
	}
	
	/**
	 * Parse the constructors and methods from the class and
	 * register them as operators
	 * @param tm TokenMapper to register ops with
	 * @param clazz class to extract ops from
	 */
	public static void processClass(TokenMapper tm, Class<?> clazz) 
	{
		/*
		 * process constructors first
		 */
		Constructor<?>[] makers = clazz.getConstructors();
		for (Constructor<?> m : makers) {
			RegisterOp anno = m.getAnnotation(RegisterOp.class);
			if (anno != null && !anno.exclude()) {
				String label = anno.token().length() > 0 ? anno.token() : "make" + clazz.getSimpleName();
				Class<?>[] argTypes = m.getParameterTypes();
				
				Operator op = new ConstructClassOp(label, argTypes, clazz, m);
				tm.registerOperator(op);
			}
		}
		
		/*
		 * then process methods
		 */
		Method [] methods = clazz.getMethods();
		for (Method m : methods) {
			Class<?> retType = m.getReturnType();
			if (retType != null && retType != void.class) {
				RegisterOp anno = m.getAnnotation(RegisterOp.class);
				if (anno == null || !anno.exclude()) {
					String methodName = m.getName();
					if (!sf_ExcludedMethods.contains(methodName) || (anno != null && !anno.exclude())) {
						boolean async = anno != null ? anno.async() : false;
						String label = anno != null && anno.token().length() > 0 ? anno.token() : methodName;
						Class<?>[] argTypes = m.getParameterTypes();
						
						Class<?>[] opTypes = new Class<?>[argTypes.length + 1];
						opTypes[0] = clazz;
						
						for (int i = 0; i < argTypes.length; i++) {
							opTypes[i + 1] = argTypes[i];
						}
						
						Operator op = null;
						if (async)
							op = new AsyncClassOp(label, opTypes, retType, m);
						else
							op = new SyncClassOp(label, opTypes, retType, m);
						
						tm.registerOperator(op);
					}
				}
			}
		}
	}
	
	protected static class ConstructClassOp extends SynchronousOp
	{
		private Constructor<?> m_constructor;

		public ConstructClassOp(String label, Class<?>[] argTypes, Class<?> resultType, Constructor<?> maker) 
		{
			super(label, argTypes, resultType);
			m_constructor = maker;
		}

		@Override
		public Object performCalculation(Object[] mArgs) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException  
		{			
			Object o = m_constructor.newInstance(mArgs);
			return o;
		}		
	}
	
	protected static class SyncClassOp extends SynchronousOp
	{
		private Method m_method;

		public SyncClassOp(String label, Class<?>[] argTypes, Class<?> resultType, Method method) 
		{
			super(label, argTypes, resultType);
			m_method = method;
		}

		@Override
		public Object performCalculation(Object[] mArgs) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException 
		{
			Object baseObj = mArgs[0];
			
			Object [] opArgs = null;
			if (numArgs() > 1) {
				opArgs = new Object [numArgs() - 1];
				for (int i = 1; i < numArgs(); i++) {
					opArgs[i - 1] = mArgs[i];
				}
			}
			
			return m_method.invoke(baseObj, opArgs);
		}		
	}
	
	protected static class AsyncClassOp extends AsynchronousOp
	{
		private Method m_method;

		public AsyncClassOp(String label, Class<?>[] argTypes, Class<?> resultType, Method method) 
		{
			super(label, argTypes, resultType);
			m_method = method;
		}

		@Override
		public Object performCalculation(Object[] mArgs) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException 
		{
			Object baseObj = mArgs[0];
			
			Object [] opArgs = null;
			if (numArgs() > 1) {
				opArgs = new Object [numArgs() - 1];
				for (int i = 1; i < numArgs(); i++) {
					opArgs[i - 1] = mArgs[i];
				}
			}
			
			return m_method.invoke(baseObj, opArgs);
		}		
	}
}
