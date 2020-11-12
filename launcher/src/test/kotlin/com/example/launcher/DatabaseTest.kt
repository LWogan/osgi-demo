package com.example.launcher

import org.hibernate.cfg.Configuration
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase
import org.hibernate.testing.transaction.TransactionUtil.doInHibernate
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.util.*


class DatabaseTest : BaseCoreFunctionalTestCase() {

    private val properties: Properties
        @Throws(IOException::class)
        get() {
            val properties = Properties()
            properties.load(javaClass.classLoader.getResourceAsStream("hibernate.properties"))
            return properties
        }

    override fun getAnnotatedClasses(): Array<Class<*>> {
        return arrayOf(DBCpk::class.java , DBBundle::class.java)
    }

    override fun configure(configuration: Configuration) {
        super.configure(configuration)
        configuration.properties = properties
    }

    /**
     * Create and persist a Bundle. Create a Cpk and assign the bundle to it.
     * Verify Cpk and bundle are both updated to reflect their relationship and match the original objects
     */
    @Test
    fun createAndRetrieveCpkAndBundle() {
        doInHibernate(({ this.sessionFactory() }), { session ->

            val cpkToSave = DBCpk(0)
            var bundleStream = Thread.currentThread().contextClassLoader.getResourceAsStream("bundles/corda-core.jar")
            val bundleToSave = DBBundle(0, bundleStream.readAllBytes())

            session.persist(cpkToSave)

            bundleToSave.cpk = cpkToSave
            session.persist(bundleToSave)

            val cpkFound = session.find(DBCpk::class.java, cpkToSave.id)
            session.refresh(cpkFound)
            val bundleFound = session.find(DBBundle::class.java, bundleToSave.id)
            session.refresh(bundleFound)

            commonBundleAndCpkAssertions(cpkToSave, cpkFound, bundleToSave, bundleFound)
        })
    }

    /**
     * Create and persist a Cpk. Create a bundle and assign a Cpk to it.
     * Verify Cpk and bundle are both updated to reflect their relationship and match the original objects
     */
    @Test
    fun createAndRetrieveBundleAndCpk() {
        doInHibernate(({ this.sessionFactory() }), { session ->

            val cpkToSave = DBCpk(0)
            var bundleStream = Thread.currentThread().contextClassLoader.getResourceAsStream("bundles/corda-core.jar")
            val bundleToSave = DBBundle(0, bundleStream.readAllBytes())

            session.persist(cpkToSave)

            bundleToSave.cpk = cpkToSave
            session.persist(bundleToSave)

            val cpkFound = session.find(DBCpk::class.java, cpkToSave.id)
            session.refresh(cpkFound)
            val bundleFound = session.find(DBBundle::class.java, bundleToSave.id)
            session.refresh(bundleFound)

            commonBundleAndCpkAssertions(cpkToSave, cpkFound, bundleToSave, bundleFound)
        })
    }

    /**
     * Common assertions for bundle and cpk updates
     */
    private fun commonBundleAndCpkAssertions(cpkToSave: DBCpk, cpkFound: DBCpk, bundleToSave: DBBundle, bundleFound: DBBundle) {
        assertTrue(cpkToSave == cpkFound)
        assertTrue(bundleToSave == bundleFound)
        assertTrue(bundleFound.cpk == cpkFound)

        assertTrue(cpkFound.bundles != null)
        assertTrue(cpkFound.bundles?.contains(bundleFound)!!)

        assertTrue(bundleFound.cpk == cpkFound)
    }
}