package com.example.osgi.activator

import com.example.osgi.sandbox.Sandbox
import com.example.osgi.sandbox.service.SandboxService
import com.example.osgi.sandbox.impl.SandboxServiceImpl
import org.osgi.framework.BundleContext
import org.osgi.service.component.annotations.Activate
import org.osgi.service.component.annotations.Component

@Component
class SandboxActivator {

    @Activate
    fun doIt(context: BundleContext) {
        var sandboxes = HashSet<Sandbox>()
        context.registerService(SandboxService::class.java, SandboxServiceImpl(sandboxes), null)
    }

}