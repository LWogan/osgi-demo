
package com.example.osgi.sandbox.service;

import org.osgi.framework.BundleContext;


/**
 * OSGi Framework Bundle Context Hook Service.
 * 
 * <p>
 * Bundles registering this service will be called during framework bundle find
 * (get bundles) operations.
 * 
 * @ThreadSafe
 * @author $Id: 1029e10212f150304095fc99433197083cc00e9e $
 */
interface SandboxService {
	fun printHello(context: BundleContext);
}
