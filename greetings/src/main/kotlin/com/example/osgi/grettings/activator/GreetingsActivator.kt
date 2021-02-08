package com.example.osgi.grettings.activator

import com.example.osgi.sandbox.service.SandboxService
import org.osgi.framework.BundleContext
import org.osgi.service.component.annotations.Activate
import org.osgi.service.component.annotations.Component
import org.osgi.service.component.annotations.Reference

@Component
class GreetingsActivator @Activate constructor(
        @Reference(service = org.osgi.service.log.LoggerFactory::class)
        private val logger: org.osgi.service.log.Logger
) {

    @Activate
    fun doIt(context: BundleContext) {
        logger.info("Activated GreetingsActivator {}", this::class.java)

        val ref = context.getServiceReference(SandboxService::class.java.name)
        if (ref != null) {
            val sandboxService: SandboxService = context.getService(ref) as SandboxService
            sandboxService.printHello(context)
        }
    }

}