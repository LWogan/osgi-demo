package com.example.osgi.sandbox

import org.osgi.framework.hooks.resolver.ResolverHook
import org.osgi.framework.hooks.resolver.ResolverHookFactory
import org.osgi.framework.wiring.BundleCapability
import org.osgi.framework.wiring.BundleRequirement
import org.osgi.framework.wiring.BundleRevision

class IsolatingResolverHook(private val currentSandbox: Sandbox?, private var sandboxes: HashSet<Sandbox>) : ResolverHook {

    /**
     * Filter bundles that don't match the current installed (but not yet started) bundle's sandbox.
     * @param candidates bundles that have not yet been started and can be resolved.
     */
    override fun filterResolvable(candidates: MutableCollection<BundleRevision>?) {
        filterRevisionCandidatesBySandbox(candidates)
    }

    override fun filterSingletonCollisions(singleton: BundleCapability, collisionCandidates: MutableCollection<BundleCapability>) {
        filterCapabilityCandidatesBySandbox(collisionCandidates)
    }

    /**
     * Filter the candidates for this bundles requirements capabilities and imported packages by removing candidates which
     * are already in a sandbox.
     *
     * @param requirement the package required by this bundle
     * @param candidates bundles that export the current bundle's imports and other requirements.
     */
    override fun filterMatches(requirement: BundleRequirement?, candidates: MutableCollection<BundleCapability>) {
        filterCapabilityCandidatesBySandbox(candidates)
    }

    override fun end() {
    }

    /**
     * Candidates contain all bundles installed but not started from all sandboxes.
     * If something is removed here then this triggers some felix code in prepareResolverHooks to set a whitelist.
     * The whitelist only contains bundles from the candidates left and this is always missing the system bundle packages.
     * org.apache.felix.resolver.Candidates.populate
     * Removing this hook doesn't have any affect on felix finding the bundle that wants to start and allows system bundle capabilities to be found
     */
    private fun filterRevisionCandidatesBySandbox(candidates: MutableCollection<BundleRevision>? ) {
        //Do nothing
    }

    /**
     * Filter capability candidates out that are not in the same sandbox
     * or are not visible by the current sandbox.
     * If candidates are found in the current sandbox and another visible sandbox
     * then choose the candidate from it's own sandbox.
     */
    private fun filterCapabilityCandidatesBySandbox(candidates: MutableCollection<BundleCapability> ) {
        var copyCandidates = candidates.toMutableList()

        var candidateFoundInOwnSandbox : BundleCapability? = null

        for (candidate in copyCandidates) {
            val candidateBundle = candidate.revision.bundle
            val candidateSandbox = candidateBundle.owningSandbox(sandboxes)

            if (candidateSandbox == null) {
                //candidate bundle is in main bundle space
                continue
            }
            //TODO: double check null assertions for currentsandbox
            else if (currentSandbox == null) {
                //current bundle is in main bundle space but candidate is in a sandbox
                candidates.remove(candidate)
            } else if (currentSandbox.installedBundles.contains(candidateBundle.bundleId)) {
                //candidate bundle is in same sandbox and should be chosen
                candidateFoundInOwnSandbox = candidate
                break
            } else if (!currentSandbox.isVisibleNonTransitive(candidateSandbox)) {
                //candidate is in another sandbox and is not visible
                candidates.remove(candidate)
            }
        }

        if (candidateFoundInOwnSandbox != null && candidates.size > 1) {
            //only use candidate from same sandbox. list at this point will contain candidates from own sandbox and others
            for (candidate in copyCandidates) {
                if (candidate != candidateFoundInOwnSandbox) {
                    candidates.remove(candidate)
                }
            }
        }
    }
}

class IsolatingResolverHookFactory(private val sandboxes: HashSet<Sandbox>) : ResolverHookFactory {

    /**
     * Get and set the triggering bundles sandbox to the resolver hook so it can be compared
     * to candidate bundles sandboxes
     * @param triggers the bundle triggering the resolver
     */
    override fun begin(triggers: Collection<BundleRevision>): ResolverHook {
        val bundle = triggers.single().bundle
        val sandbox = bundle.owningSandbox(sandboxes)
        return IsolatingResolverHook(sandbox, sandboxes)
    }
}
