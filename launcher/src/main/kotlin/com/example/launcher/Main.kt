package com.example.launcher

import com.example.launcher.activator.HostActivator
import com.example.launcher.sandbox.*
import org.apache.felix.framework.Felix
import org.apache.felix.framework.util.FelixConstants
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.cfg.Configuration
import org.osgi.framework.Bundle
import org.osgi.framework.BundleContext
import org.osgi.framework.BundleException
import org.osgi.framework.hooks.bundle.CollisionHook
import org.osgi.framework.hooks.bundle.FindHook
import org.osgi.framework.hooks.resolver.ResolverHookFactory
import java.io.File
import java.io.FileInputStream
import java.nio.file.Paths
import java.util.*
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root
import kotlin.collections.HashSet

val projectDirAbsolutePath = Paths.get("").toAbsolutePath().toString()
val resourcesPath = Paths.get(projectDirAbsolutePath, "/launcher/src/main/resources/").toAbsolutePath().toString()

fun main(args: Array<String>) {

    //clear felix cache
    clearFelixCache()

    //get hibernate session
    val sessionFactory = getSessionFactory()

    //treats file dirs as cpk and save them to db
    saveBundlesFromResourcesToDbAsCPKs(sessionFactory)

    val dbCPKs = readAllCPKsFromDB(sessionFactory)

    //run OSGi framework
    val (felix, context, sandboxFactory) = activateOSGiFramework()

    //install bundles to sandboxes
    var bundlesFromAllSandboxes = installSandboxesFromDBCPKs(context, sandboxFactory, dbCPKs)

    makeSandboxesAllVisibleToEachOther(sandboxFactory)

    startBundles(bundlesFromAllSandboxes)

    //Stop felix and clear cache
    felix.stop()
    felix.waitForStop(0)
    clearFelixCache()
}

fun startBundles(bundlesFromAllSandboxes: MutableList<Bundle>) {
    for (bundle in bundlesFromAllSandboxes) {
        try {
            println("about to start ${bundle.symbolicName}")
            bundle.start()
        } catch (e: BundleException) {
            println(e.message)
        }
    }
}

fun makeSandboxesAllVisibleToEachOther(sandboxFactory: SandboxFactory) {
    for (sandbox in sandboxFactory.sandboxes) {
        var currentSandbox = sandbox
        for (nextSandbox in sandboxFactory.sandboxes) {
            currentSandbox.addVisibility(nextSandbox)
        }
    }
}

/******************************************
 * **** OSGI INSTALL & START HELPERS ******
 *****************************************/

private fun activateOSGiFramework(): Triple<Felix, BundleContext, SandboxFactory> {
    val activator = HostActivator()
    val config = mapOf(
            Pair("org.osgi.service.log.admin.loglevel", "INFO"),
            Pair(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, listOf(activator)),
            Pair(FelixConstants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, "co.paralleluniverse.fibers.instrument;version=0.8.2.r3,co.paralleluniverse.common.resource;version=0.8.2.r3,co.paralleluniverse.common.asm;version=0.8.2.r3,sun.security.x509")
    )
    val felix = Felix(config)
    felix.start()
    val context = activator.context()!!

    var sandboxes = HashSet<Sandbox>()
    val sandboxFactory = SandboxFactory(sandboxes)

    context.registerService(ResolverHookFactory::class.java, IsolatingResolverHookFactory(sandboxes), null)
    context.registerService(CollisionHook::class.java, IsolatingCollisionBundleHook(sandboxes), null)
    context.registerService(FindHook::class.java, IsolatingFindHook(sandboxes), null)


    return Triple(felix, context, sandboxFactory)
}


private fun installSandboxesFromDBCPKs(context: BundleContext, sandboxFactory: SandboxFactory, dbCPKs: List<DBCpk>) : MutableList<Bundle> {
    val bundles = mutableListOf<Bundle>()

    for (dbCPK in dbCPKs) {
        var sandbox = sandboxFactory.createSandBox(dbCPK.filename.toString(), context)

        for (dbBundle in dbCPK.bundles!!) {
            println("about to install ${dbBundle.filename}")
            bundles.add(sandbox.installBundle(dbBundle.filename.toString(), dbBundle.content?.inputStream()))
        }
    }

    return bundles
}


private fun clearFelixCache(): File {
    var felixDir = File(Paths.get("felix-cache").toAbsolutePath().toString())
    felixDir.deleteRecursively()
    return felixDir
}


/******************************************
 * **** HIBERNATE READ & WRITE ***********
 *****************************************/

private fun saveBundlesFromResourcesToDbAsCPKs(sessionFactory: SessionFactory) {
    var latestId = 0
    latestId = saveBundlesAndCPkToDB(sessionFactory, "/dependencies", latestId)
    latestId = saveBundlesAndCPkToDB(sessionFactory, "/logger", latestId)
    latestId = saveBundlesAndCPkToDB(sessionFactory, "/yo", latestId)
    saveBundlesAndCPkToDB(sessionFactory, "/greetings", latestId)
}

private fun saveBundlesAndCPkToDB(sessionFactory: SessionFactory, dir: String, id: Int): Int {
    var latestId = id
    var bundles = mutableListOf<DBBundle>()
    for (file in File(resourcesPath + dir).walk()) {
        if (file.name.endsWith(".jar")) {
            var inputstream = FileInputStream(File(file.absolutePath))
            var dbBundle = DBBundle(id, inputstream.readAllBytes(), file.name)

            bundles.add(dbBundle)
        }
    }

    var cpk = DBCpk(latestId, bundles, filename = dir)
    latestId++

    sessionFactory.transaction { session ->
        session.save(cpk)
        latestId++
    }

    for (bundle in bundles) {
        bundle.cpk = cpk
        sessionFactory.transaction { session ->
            session.save(bundle)
            latestId++
        }
    }

    return latestId
}

private fun readAllCPKsFromDB(sessionFactory: SessionFactory): List<DBCpk> {
    return sessionFactory.transaction { session ->
        val cb: CriteriaBuilder = session.criteriaBuilder
        val cq: CriteriaQuery<DBCpk> = cb.createQuery(DBCpk::class.java)
        val rootEntry: Root<DBCpk> = cq.from(DBCpk::class.java)
        val all: CriteriaQuery<DBCpk> = cq.select(rootEntry)
        val allQuery: TypedQuery<DBCpk> = session.createQuery(all)
        allQuery.resultList as List<DBCpk>
    }
}


/******************************************
 * **** HIBERNATE CONFIGURATION ***********
 *****************************************/

private fun getSessionFactory(): SessionFactory {
    val properties = propertiesFromResource("/hibernate.properties")
    val configuration = buildHibernateConfiguration(properties, DBCpk::class.java, DBBundle::class.java)
    val sessionFactory = buildSessionFactory(configuration)
    return sessionFactory
}

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

