/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2010 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package org.appcelerator.kroll.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.appcelerator.kroll.KrollConverter;
import org.appcelerator.kroll.KrollDefaultValueProvider;
import org.appcelerator.kroll.KrollInvocation;
import org.appcelerator.kroll.KrollNativeConverter;
import org.appcelerator.kroll.KrollJavascriptConverter;
import org.appcelerator.kroll.KrollProxy;

/**
 * The top level Kroll annotation.<br>
 * Using this annotation by itself will have no effect, you'll want to look at sub-annotations:
 * <ul>
 * <li>For binding a basic proxy (including a view proxy), see: {@link proxy @Kroll.proxy}</li>
 * <li>For binding modules, see: {@link module @Kroll.module}</li>
 * <li>For binding methods within a proxy/module, see: {@link method @Kroll.method}, {@link argument @Kroll.argument}</li>
 * <li>For binding properties within a proxy/module, see:
 * 		{@link constant @Kroll.constant}, {@link property @Kroll.property}, {@link getProperty @Kroll.getProperty}, {@link setProperty @Kroll.setProperty}
 * </li>
 * <li>For injecting values into your proxy/module, see: {@link inject @Kroll.inject}</li>
 * <li>For binding a proxy or a proxy method into it's own top level object (i.e. "Titanium" or "setTimeout"), see: {@link topLevel @Kroll.topLevel}</li>
 * </ul>
 * @author Marshall Culpepper
 */
@Documented
public @interface Kroll {
	/** Testing */
	public static final String DEFAULT_NAME = "__default_name__";
	
	/**
	 * An optional annotation for arguments of a {@link method Kroll method}.
	 * This annotation is retained at runtime so dynamic properties can check for optional arguments in their setters.
	 * <b>Example</b>:<br>
	 * <pre>&#064;Kroll.method
	 * public sayHi(&#064;Kroll.argument(optional=true) String name) {
	 *    // say hi..
	 * }
	 * </pre>
	 * @see argument#optional()
	 * @see argument#converter() 
	 * @see argument#defaultValueProvider()
	 * @see argument#name()
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.PARAMETER)
	public @interface argument {
		/**
		 * The argument's name used in error messages and source code generation.<br>
		 * @default The argument's name from Java source
		 */
		String name() default DEFAULT_NAME;
		/**
		 * Marks this argument as optional.<br>
		 * The default value for the argument is pulled from the argument's {@link argument#defaultValueProvider() default value provider}.<br>
		 * <p>
		 * <b>Warning</b>: Make sure that <i>all</i> optional arguments are annotated in your {@link method}, or source code generation / binding may fail.
		 * If the {@link method} has an optional argument in the middle of it's argument list, then all the methods after it should also be annotated as optional. 
		 * </p>
		 */
		boolean optional() default false;
		/**
		 * Converter of Rhino/Javascript objects to Java objects suitable for use in Titanium<br>
		 * <p>The value should be the fully qualified name of a class that implements {@link KrollJavascriptConverter}.
		 * The class must also implement a static instance getter called getInstance().</p>
		 * @see KrollJavascriptConverter
		 */
		Class<? extends KrollJavascriptConverter> converter() default KrollConverter.class;
		/**
		 * Provider of default values when an optional argument isn't passed in.<br>
		 * <p>The value should be the fully qualified name of a class that implements {@link KrollDefaultValueProvider}.
		 * The class must also implement a static instance getter called getInstance().</p>
		 * @see KrollDefaultValueProvider
		 */
		Class<? extends KrollDefaultValueProvider> defaultValueProvider() default KrollConverter.class;
	}
	
	/**
	 * Marks a static final field as a constant for this {@link module} or {@link proxy}.
	 * <b>Note</b>: This only works on static final fields (the value is pulled directly when generating source)
	 * <b>Example</b>:<br>
	 * <pre>&#064;Kroll.constant public static final int ID = 100;</pre>
	 * @see constant#name()
	 */
	@Documented
	@Retention(RetentionPolicy.SOURCE)
	@Target(ElementType.FIELD)
	public @interface constant {
		/**
		 * The name that this constant is bound to.<br>
		 * @default The name in Java source.
		 */
		String name() default DEFAULT_NAME;
	}
	
	/**
	 * Injects a value by {@link inject#type() type} or {@link inject#name() name} directly into a field or by calling a setter method.
	 * The currently supported injectable values are:
	 * <ul>
	 * <li><b>type</b>: {@link KrollInvocation}</li>
	 * </ul>
	 * <b>Examples</b>:<br>
	 * <pre>&#064;Kroll.inject protected KrollInvocation currentInvocation;</pre>
	 * <pre>&#064;Kroll.inject protected void setCurrentInvocation(KrollInvocation currentInvocation) { }</pre>
	 * 
	 * @see inject#name()
	 * @see inject#type()
	 * @see KrollInvocation
	 */
	@Documented
	@Retention(RetentionPolicy.SOURCE)
	@Target({ElementType.METHOD, ElementType.FIELD})
	public @interface inject {
		/**
		 * <b>Warning</b>: This is reserved for future use, and not implemented yet
		 * The name of an object to inject.
		 * @default the name of field, or the name of setter method
		 */
		String name() default DEFAULT_NAME;
		/**
		 * The type of object to inject, by class.<br>
		 * @default The type of the field or first argument of the method
		 */
		Class<?> type() default DEFAULT.class;
		public static final class DEFAULT {};
	}
	
	/**
	 * Declares a method to be exposed as part of this {@link proxy} or {@link module}.
	 * <p>Methods may optionally make their first argument a {@link KrollInvocation} object,
	 * and may also declare {@link argument#optional() optional arguments}.</p>
	 * <b>Example</b>:<br>
	 * <pre>
	 * &#064;Kroll.method
	 * public void execute(String action, &#064;Kroll.argument(optional=true) KrollDict options) {
	 * }
	 * </pre>
	 * 
	 * @see method#name()
	 * @see method#converter()
	 * @see method#runOnUiThread()
	 * @see argument @Kroll.argument
	 */
	@Documented
	@Retention(RetentionPolicy.SOURCE)
	@Target(ElementType.METHOD)
	public @interface method {
		/**
		 * The method's name in the API.<br>
		 * @default The method's name in Java source.
		 */
		String name() default DEFAULT_NAME;
		/**
		 * Converter of Java objects to Javascript objects suitable for use by the Rhino runtime<br>
		 * <p>The value should be the fully qualified name of a class that implements {@link KrollNativeConverter}.
		 * The class must also implement a static instance getter called getInstance().</p>
		 * @see KrollNativeConverter
		 */
		Class<? extends KrollNativeConverter> converter() default KrollConverter.class;
		/**
		 * <p>When set to true, this method will run on the UI thread, blocking the current thread until it finishes executing.
		 * If the current thread is the UI thread, then this simply calls the method directly.
		 * This is functionally equivalent to:</p>
		 * <pre>
		 * if (tiContext.isUIThread()) {
		 *     return callMethod();
		 * } else {
		 *     final AsyncResult result = new AsyncResult();
		 *     tiContext.getActivity().runOnUiThread(new Runnable() {
		 *         public void run() {
		 *             result.setResult(callMethod());
		 *         }
		 *     });
		 *     return result.getResult();
		 * }
		 * </pre>
		 */
		boolean runOnUiThread() default false;
	}
	
	/**
	 * Declares a Kroll module.<br>
	 * Modules differ from {@link proxy proxies} by being statically bound to an API point, and by only having a singleton instance (by default).<br>
	 * Modules may also have a {@link module#parentModule() parent module}, and are where {@link proxy} "create" methods are generated. For more on this, see {@link proxy#creatableInModule()}.<br>
	 * Module classes must extend {@link org.appcelerator.kroll.KrollModule} (which is itself an extension of {@link KrollProxy}).<br>
	 * <b>Example</b>:<br>
	 * <pre>
	 * &#064;Kroll.module
	 * public class APIModule extend KrollModule { .. }
	 * </pre>
	 * @see module#name()
	 * @see module#parentModule()
	 * @see module#contextSpecific()
	 * @see proxy#creatableInModule()
	 */
	@Documented
	@Retention(RetentionPolicy.SOURCE)
	@Target(ElementType.TYPE)
	public @interface module {
		/**
		 * <p>The name of this module in the API.
		 * If this module has a {@link module#parentModule parent module}, this name will be relative to the parent.
		 * All modules are relative to the top level <pre>Titanium</pre> object. If you wish to bind to the global object, see {@link topLevel @Kroll.topLevel}</p>
		 * @default If the class name follows the naming convention <pre>XYZModule</pre>, then the default API name is <pre>XYZ</pre>. Otherwise, the default name is the same as the class name.
		 */
		String name() default DEFAULT_NAME;
		/**
		 * This ID will be used when a user tries to load a module by using "require", for example:
		 * <pre>
		 * var MyModule = require('com.mycompany.mymodule');
		 * </pre>
		 * <pre>
		 * &#064;Kroll.module(name="MyModule", id="com.mycompany.mymodule")
		 * public class MyModule extends KrollModule { ... }
		 * </pre>
		 * @default The fully qualified class name of the module
		 */
		String id() default DEFAULT_NAME;
		/**
		 * The parent module class of this module.
		 * @default No parent module (binds directly to <pre>Titanium</pre>)
		 */
		Class<?> parentModule() default DEFAULT.class;
		public static final class DEFAULT {};
		
		/**
		 * <b>Warning</b>: This is reserved for future use, and not implemented yet
		 * When set to true, this module will be instantiated for every TiContext that is created (instead of only once for the entire application).<br>
		 */
		boolean contextSpecific() default false;
		/**
		 * Declares a list of dynamic property accessors for this module.<br>
		 * <b>Example</b>:<br>
		 * <pre>
		 * &#064;Kroll.module(propertyAccessors={"property1", "property2", "property3"})
		 * </pre>
		 * @default No dynamic property accessors are generated
		 */
		String[] propertyAccessors() default {};
	}
	
	/**
	 * Declares a property to be exposed as part of this {@link proxy} or {@link module}.<br>
	 * Standard properties are automatically written and read into instance fields on the {@link proxy} object.<br>
	 * Note that properties defined by this annotation are <b>not</b> exposed as getter or setter methods.<br>
	 * To generate both a getter/setter and property style, use a combination of {@link method} and {@link getProperty} / {@link setProperty}<br>
	 * <b>Example</b>:<br>
	 * <pre>
	 * &#064;Kroll.property protected String username;
	 * </pre>
	 * 
	 * @see property#get()
	 * @see property#set()
	 * @see property#name()
	 * @see property#nativeConverter()
	 * @see property#javascriptConverter()
	 * @see method @Kroll.method
	 * @see getProperty @Kroll.getProperty
	 * @see setProperty @Kroll.setProperty
	 */
	@Documented
	@Retention(RetentionPolicy.SOURCE)
	@Target(ElementType.FIELD)
	public @interface property {
		/**
		 * Whether or not this property has "get" or read access
		 */
		boolean get() default true;
		/**
		 * Whether or not this property has "set", or write access
		 */
		boolean set() default true;
		/**
		 * The name of this property in the API.<br>
		 * @default The property's name in java source
		 */
		String name() default DEFAULT_NAME;
		/**
		 * Converter of Java objects to Javascript objects suitable for use by the Rhino runtime<br>
		 * <p>The value should be the fully qualified name of a class that implements {@link KrollNativeConverter}.
		 * The class must also implement a static instance getter called getInstance().</p>
		 * @see KrollNativeConverter
		 */
		Class<? extends KrollNativeConverter> nativeConverter() default KrollConverter.class;
		/**
		 * Converter of Rhino/Javascript objects to Java objects suitable for use in Titanium<br>
		 * <p>The value should be the fully qualified name of a class that implements {@link KrollJavascriptConverter}.
		 * The class must also implement a static instance getter called getInstance().</p>
		 * @see KrollJavascriptConverter
		 */
		Class<? extends KrollJavascriptConverter> javascriptConverter() default KrollConverter.class;
	}

	/**
	 * Declares a method as a property getter of this {@link proxy} or {@link module}.<br>
	 * <p>Getter methods must return a value, and may optionally have a {@link KrollInvocation} as the first argument,
	 * and {@link argument#optional() optional arguments} when they are also exposed as {@link method methods}</p>
	 * 
	 * @see getProperty#name()
	 * @see getProperty#nativeConverter()
	 * @see getProperty#javascriptConverter()
	 * @see getProperty#runOnUiThread()
	 * @see KrollInvocation
	 * @see method @Kroll.method
	 * @see argument#optional()
	 */
	@Documented
	@Retention(RetentionPolicy.SOURCE)
	@Target(ElementType.METHOD)
	public @interface getProperty {
		/**
		 * The name of this property in the API.
		 * @default The method name stripped of "get", and lower-camel-cased or the method name itself.
		 */
		String name() default DEFAULT_NAME;
		/**
		 * Converter of Java objects to Javascript objects suitable for use by the Rhino runtime<br>
		 * <p>The value should be the fully qualified name of a class that implements {@link KrollNativeConverter}.
		 * The class must also implement a static instance getter called getInstance().</p>
		 * @see KrollNativeConverter
		 */
		Class<? extends KrollNativeConverter> nativeConverter() default KrollConverter.class;
		/**
		 * Converter of Rhino/Javascript objects to Java objects suitable for use in Titanium<br>
		 * <p>The value should be the fully qualified name of a class that implements {@link KrollJavascriptConverter}.
		 * The class must also implement a static instance getter called getInstance().</p>
		 * @see KrollJavascriptConverter
		 */
		Class<? extends KrollJavascriptConverter> javascriptConverter() default KrollConverter.class;
		/**
		 * When set to true, this property getter will only be executed on the UI thread.<br>
		 */
		boolean runOnUiThread() default false;
	}
	
	/**
	 * Declares a method as a property setter of this {@link proxy} or {@link module}.<br>
	 * <p>Setter methods must have at least one argument: The value to set. Optionally, setter methods may also have a
	 * {@link KrollInvocation} object as the first argument (with the value as the second), and may also have as many
	 * {@link argument#optional() optional arguments} as necessary after the value when exposed as a {@link method}.
	 * 
	 * @see setProperty#name()
	 * @see setProperty#nativeConverter()
	 * @see setProperty#javascriptConverter()
	 * @see setProperty#retain()
	 * @see setProperty#runOnUiThread()
	 * @see KrollInvocation
	 * @see argument#optional()
	 * @see method @Kroll.method
	 * </p>
	 */
	@Documented
	@Retention(RetentionPolicy.SOURCE)
	@Target(ElementType.METHOD)
	public @interface setProperty {
		/**
		 * The name of this property in the API.<br>
		 * @default The method name stripped of "set", and lower-camel-cased or the method name itself.
		 */
		String name() default DEFAULT_NAME;
		/**
		 * Converter of Java objects to Javascript objects suitable for use by the Rhino runtime<br>
		 * <p>The value should be the fully qualified name of a class that implements {@link KrollNativeConverter}.
		 * The class must also implement a static instance getter called getInstance().</p>
		 * @see KrollNativeConverter
		 */
		Class<? extends KrollNativeConverter> nativeConverter() default KrollConverter.class;
		/**
		 * Converter of Rhino/Javascript objects to Java objects suitable for use in Titanium<br>
		 * <p>The value should be the fully qualified name of a class that implements {@link KrollJavascriptConverter}.
		 * The class must also implement a static instance getter called getInstance().</p>
		 * @see KrollJavascriptConverter
		 */
		Class<? extends KrollJavascriptConverter> javascriptConverter() default KrollConverter.class;
		/**
		 * When set to true, the value of this property is retained in the internal property map of this {@link proxy}
		 */
		boolean retain() default true;
		/**
		 * When set to true, this property setter will only be executed on the UI thread.<br>
		 */
		boolean runOnUiThread() default false;
	}
	
	/**
	 * Declares a Kroll proxy.<br>
	 * <p>Proxies are the API interface between Javascript (Rhino) and Java.
	 * Proxy classes must use this or {@link module the module annotation} to expose methods and properties,
	 * and must follow a few specific source patterns:
	 * <ul>
	 * <li>Proxy classes must extend the {@link KrollProxy} class</li>
	 * <li>The proxy constructor must take only one argument, a {@link org.appcelerator.titanium.TiContext}</li>
	 * </ul>
	 * To expose a "create" method for this proxy, see {@link proxy#creatableInModule()}
	 * 
	 * @see proxy#name()
	 * @see proxy#creatableInModule()
	 * @see proxy#propertyAccessors()
	 * @see KrollProxy
	 * @see module @Kroll.module
	 * </p>
	 */
	@Documented
	@Retention(RetentionPolicy.SOURCE)
	@Target(ElementType.TYPE)
	public @interface proxy {
		/**
		 * The name of this proxy. Used in debugging, toString(), and {@link proxy#creatableInModule()}.<br>
		 * @default The name of the proxy class with the "Proxy" suffix removed.
		 */
		String name() default DEFAULT_NAME;
		/**
		 * Specify which module will have a "create" method for this proxy.<br>
		 * <p>
		 * This will generate a "create" method that follows the pattern "create" + {@link proxy#name() name of this proxy}.
		 * For instance, if the name of your proxy class is LabelProxy, the create method would be named "createLabel".
		 * </p>
		 * 
		 * @default None (don't generate a create method)
		 * @see KrollProxy#handleCreationArgs(org.appcelerator.kroll.KrollModule, Object[])
		 * @see KrollProxy#handleCreationDict(org.appcelerator.kroll.KrollDict)
		 */
		Class<?> creatableInModule() default DEFAULT.class;
		public static final class DEFAULT {};
		/**
		 * Declares a list of dynamic property accessors for this proxy.<br>
		 * <b>Example</b>:<br>
		 * <pre>
		 * &#064;Kroll.proxy(propertyAccessors={"property1", "property2", "property3"})
		 * </pre>
		 * @default No dynamic property accessors are generated
		 */
		String[] propertyAccessors() default {};
	}
	
	/**
	 * Declares a module or proxy method as "top level".<br>
	 * <p>
	 * Methods and modules with this annotation will be exposed at the top level Javascript global scope.
	 * This provides a convenient way to expose custom APIs that don't live under the "Ti" or "Titanium" namespace,
	 * and also supports extending existing objects with new methods.
	 * </p>
	 * <b>Examples</b>:<br>
	 * <pre>
	 * &#064;Kroll.topLevel("setTimeout") &#064;Kroll.method
	 * public void setTimeout(KrollCallback fn, long timeout) { }
	 * 
	 * &#064;Kroll.topLevel("Ti") &#064;Kroll.module
	 * public class TitaniumModule extends KrollModule { }
	 * 
	 * &#064;Kroll.topLevel("String.format") &#064;Kroll.method
	 * public void stringFormat(String format, String[] arguments) { }
	 * </pre>
	 */
	@Documented
	@Retention(RetentionPolicy.SOURCE)
	@Target({ElementType.METHOD, ElementType.TYPE})
	public @interface topLevel {
		/**
		 * An array of top level names to expose this {@link method} or {@link module} as.<br>
		 * @default The method name or module name
		 */
		String[] value() default DEFAULT_NAME;
	}
}