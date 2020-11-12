package com.example.osgi.yo.activator;

import com.example.osgi.yo.YoService
import com.example.osgi.yo.YoServiceImpl
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceRegistration
import org.osgi.service.component.annotations.Activate
import org.osgi.service.component.annotations.Component
import org.osgi.service.component.annotations.Reference
import org.osgi.service.log.Logger
import org.osgi.service.log.LoggerFactory

@Component(immediate = true)
class YoActivator @Activate constructor(
        @Reference(service = LoggerFactory::class)
        private val logger: Logger
) {

    @Activate
    fun doIt(context: BundleContext) {
        val yoServiceRegistration = context.registerService(YoService::class.java.canonicalName, YoServiceImpl(logger), null)
        logger.info("Registered YoService {}", this::class.java)
    }
}
