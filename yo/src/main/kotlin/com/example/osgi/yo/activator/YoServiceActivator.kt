package com.example.osgi.yo.activator;

import com.example.osgi.yo.service.YoService
import com.example.osgi.yo.service.YoServiceImpl
import org.osgi.framework.BundleContext
import org.osgi.service.component.annotations.Activate
import org.osgi.service.component.annotations.Component
import org.osgi.service.component.annotations.Reference
import org.osgi.service.log.Logger
import org.osgi.service.log.LoggerFactory

@Component(immediate = true)
class YoServiceActivator @Activate constructor(
        @Reference(service = LoggerFactory::class)
        private val logger: Logger
) {

    @Activate
    fun doIt(context: BundleContext) {
        context.registerService(YoService::class.java.canonicalName, YoServiceImpl(logger), null)
        logger.info("Registered YoService {}", this::class.java)
    }
}
