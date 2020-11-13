package com.example.launcher

import org.apache.felix.framework.Felix
import org.apache.felix.framework.util.FelixConstants
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.cfg.Configuration
import org.osgi.framework.Bundle
import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext
import org.osgi.framework.BundleException
import java.io.File
import java.io.FileInputStream
import java.nio.file.Paths
import java.util.*
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root


class HostActivator : BundleActivator {
    private var bundleContext : BundleContext? = null

    override fun start(context: BundleContext?) {
        bundleContext = context
    }

    override fun stop(context: BundleContext?) {
        bundleContext = null
    }

    fun bundles(): List<Bundle> {
        return bundleContext?.bundles?.toList() ?: emptyList()
    }

    fun context(): BundleContext? {
        return bundleContext
    }
}

val projectDirAbsolutePath = Paths.get("").toAbsolutePath().toString()
val resourcesPath = Paths.get(projectDirAbsolutePath, "/launcher/src/main/resources/").toAbsolutePath().toString()

fun main(args: Array<String>) {

    //clear felix cache
    var felixDir = File(Paths.get("felix-cache").toAbsolutePath().toString())
    felixDir.deleteRecursively()

    val properties = propertiesFromResource("/hibernate.properties")
    val configuration = buildHibernateConfiguration(properties, DBCpk::class.java, DBBundle::class.java)
    val sessionFactory = buildSessionFactory(configuration)

    var latestId = 0
    latestId = saveBundlesToDB(sessionFactory, "/core-bundles", latestId)
    latestId = saveBundlesToDB(sessionFactory, "/logger", latestId)
    latestId = saveBundlesToDB(sessionFactory, "/yo", latestId)
    latestId = saveBundlesToDB(sessionFactory, "/greetings", latestId)

    val dbBundles = readAllBundlesFromDB(sessionFactory)

    val activator = HostActivator()
    val config = mapOf(
            Pair("org.osgi.service.log.admin.loglevel", "INFO"),
            Pair(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, listOf(activator)),
            Pair(FelixConstants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, "co.paralleluniverse.fibers.instrument;version=0.8.2.r3,co.paralleluniverse.common.resource;version=0.8.2.r3,co.paralleluniverse.common.asm;version=0.8.2.r3,kotlin.streams.jdk8,kotlin.jdk7,sun.security.x509")
    )
    val felix = Felix(config)
    felix.start()
    val context = activator.context()!!


    installAndStartFromDB(context, dbBundles)


    felix.stop()
    felix.waitForStop(0)

    //clear felix cache
    var felixDir2 = File(Paths.get("felix-cache").toAbsolutePath().toString())
    var success = felixDir2.deleteRecursively()
}


/******************************************
 * **** OSGI INSTALL & START HELPERS ******
 *****************************************/
private fun installAndStartFromDB(context: BundleContext, dbBundles: List<DBBundle>) {
    val bundles = mutableListOf<Bundle>()
    for (dbBundle in dbBundles) {
        println("about to install ${dbBundle.filename}")
        bundles.add(context.installBundle(dbBundle.filename, dbBundle.content?.inputStream()))
    }

    for (bundle in bundles) {
        try {
            println("about to start ${bundle.symbolicName}")
            bundle.start()
        } catch (e: BundleException) {
            println(e.message)
        }
    }
}

private fun installAndStartFromResourceDir(context: BundleContext, dir: String) {
    val dependencies = installBundles(context, dir)
    startBundles(dependencies)
}

private fun startBundles(dependencies: MutableList<Bundle>) {
    var nrStarted = 0
    while (nrStarted < dependencies.size) {
        for (b in dependencies) {
            println("about to start ${b.symbolicName}")
            try {
                nrStarted++
                b.start()
            } catch (e: BundleException) {
                println(e.message)
            }
        }
    }
}

private fun installBundles(context: BundleContext, dir: String): MutableList<Bundle> {

    val dependencies = mutableListOf<Bundle>()

    for (file in File(resourcesPath + dir).walk()) {
        if (file.name.endsWith(".jar")) {
            println("installing bundle from ${file.name}")
            var inputstreamDep = FileInputStream(File(file.absolutePath))
            val b = context.installBundle(file.name, inputstreamDep)
            dependencies.add(b)
        }
    }
    return dependencies
}

/******************************************
 * **** HIBERNATE READ & WRITE ***********
 *****************************************/
private fun saveBundlesToDB(sessionFactory: SessionFactory, dir: String, id: Int): Int {
    var latestId = id
    for (file in File(resourcesPath + dir).walk()) {
        if (file.name.endsWith(".jar")) {
            var inputstream = FileInputStream(File(file.absolutePath))

            sessionFactory.transaction { session ->
                session.save(DBBundle(id, inputstream.readAllBytes(), file.name))
                latestId++
            }
        }
    }

    return latestId
}

private fun readAllBundlesFromDB(sessionFactory: SessionFactory): List<DBBundle> {
    return sessionFactory.transaction { session ->
        val cb: CriteriaBuilder = session.criteriaBuilder
        val cq: CriteriaQuery<DBBundle> = cb.createQuery(DBBundle::class.java)
        val rootEntry: Root<DBBundle> = cq.from(DBBundle::class.java)
        val all: CriteriaQuery<DBBundle> = cq.select(rootEntry)
        val allQuery: TypedQuery<DBBundle> = session.createQuery(all)
        allQuery.resultList as List<DBBundle>
    }
}

/******************************************
 * **** HIBERNATE CONFIGURATION ***********
 *****************************************/
fun buildHibernateConfiguration(hibernateProperties: Properties, vararg annotatedClasses: Class<*>): Configuration {
    val configuration = Configuration()
    configuration.properties = hibernateProperties
    annotatedClasses.forEach { configuration.addAnnotatedClass(it) }
    return configuration
}

fun <T> SessionFactory.transaction(block: (session: Session) -> T): T {
    val session = openSession()
    val transaction = session.beginTransaction()

    return try {
        val rs = block.invoke(session)
        transaction.commit()
        rs
    } catch (e: Exception) {
        println("Transaction failed! Rolling back...")
        throw e
    }
}

fun buildSessionFactory(configuration: Configuration): SessionFactory {
    val serviceRegistry = StandardServiceRegistryBuilder().applySettings(configuration.properties).build()
    return configuration.buildSessionFactory(serviceRegistry)
}

fun propertiesFromResource(resource: String): Properties {
    val properties = Properties()
    var inputstream = FileInputStream(File(resourcesPath + resource))
    properties.load(inputstream)
    return properties
}

