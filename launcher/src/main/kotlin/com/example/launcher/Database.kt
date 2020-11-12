package com.example.launcher

import java.security.PublicKey
import java.time.Instant
import javax.persistence.*


@Entity
@Table(name = "greetings", indexes = [(Index(name = "bundle_id_idx", columnList = "bundle_id"))])
class DBBundle(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "bundle_id", nullable = false)
        val id: Int,

        @Column(name = "bundle_content", nullable = true)
        @Lob
        var content: ByteArray? = null,

        @Column(name = "bundle_filename", updatable = false, nullable = true)
        var filename: String? = null,

        //*could change this to many-to-many relationship to remove duplicate bundles
        //*need to record bundle artifact version then in case of multiple bundles with same file name but are different bundles. When there is a filename or symbolic name clash with existing bundles compare them at install time. A hash of the bundle could be compared to see if its an identical bundle or new one.
        //*safer to store different Cpk bundles separately.
        @ManyToOne(fetch = FetchType.LAZY, optional = true)
        @JoinColumn(name = "fk_cpk_id", referencedColumnName = "cpk_id", nullable = true)
        var cpk: DBCpk? = null
)


@Entity
@Table(name = "cpk", indexes = [(Index(name = "cpk_id_idx", columnList = "cpk_id"))])
class DBCpk(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "cpk_id", nullable = false)
        val id: Int,

        @OneToMany(mappedBy = "cpk", fetch = FetchType.LAZY)
        @Column(name = "cpk_bundles", nullable = false)
        var bundles: MutableList<DBBundle>? = null,

        @Column(name = "cpk_filename", updatable = false, nullable = true)
        var filename: String? = null,

        @Column(name = "cpk_insertion_date", nullable = false, updatable = false)
        var insertionDate: Instant = Instant.now(),

        @ElementCollection(targetClass = PublicKey::class, fetch = FetchType.EAGER)
        @Column(name = "cpk_signer", nullable = false)
        @CollectionTable(name = "cpk_signers", joinColumns = [(JoinColumn(name = "cpk_id", referencedColumnName = "cpk_id"))],
                foreignKey = ForeignKey(name = "FK__signers__cpk"))
        var signers: List<PublicKey>? = null,

        //version unknown defaults to 1
        @Column(name = "cpk_version", nullable = false)
        var version: String = "1",

        //enabled Cpks will be loaded on node restart. Disabled Cpks are not loaded.
        //* perhaps change this to string status for more complex restart scenarios
        @Column(name = "cpk_enabled", nullable = false)
        var enabled: Boolean? = true
) {
        fun addBundle(bundle: DBBundle) {
                bundle.cpk = this
                if (this.bundles == null) {
                        this.bundles = mutableListOf()
                }
                this.bundles?.add(bundle)
        }

        fun removeBundle(bundle: DBBundle) {
                bundle.cpk = null
                bundles?.remove(bundle)
        }
}

