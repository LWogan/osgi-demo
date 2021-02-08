
package com.example.osgi.sandbox.impl;

import com.example.osgi.sandbox.Sandbox
import com.example.osgi.sandbox.service.SandboxService
import org.osgi.framework.BundleContext

class SandboxServiceImpl(private val sandboxes: HashSet<Sandbox>) : SandboxService {

	override fun printHello(context: BundleContext) {
		println("Sandbox service says hello!")
	}

}