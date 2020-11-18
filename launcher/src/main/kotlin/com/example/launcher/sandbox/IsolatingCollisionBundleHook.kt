package com.example.launcher.sandbox


import org.osgi.framework.Bundle
import org.osgi.framework.hooks.bundle.CollisionHook

/**
* Filter collisions caused by installing a bundle with the same symbolic name.
* The target (current bundle installing) bundles sandbox will be null as it won't be added to it's sandbox until after it is installed.
* Collision candidates may have a sandbox or may not (e.g system bundles or bundles without sandboxes).
* Note: it is not possible to determine at this stage if we are installing a bundle to the same sandbox twice.
*/
class IsolatingCollisionBundleHook(private var sandboxes: HashSet<Sandbox>) : CollisionHook {

    override fun filterCollisions(operationType: Int, target: Bundle?, collisionCandidates: MutableCollection<Bundle>?) {
        var copyCandidates = collisionCandidates?.toMutableList()

        if (copyCandidates != null) {
            for (candidate in copyCandidates) {
                var candidateSandbox = candidate.owningSandbox(sandboxes)

                //candidate lives in main bundle space so should be possible to find
                if (candidateSandbox == null) {
                    continue
                }
                else  {
                    //if a bundle is being installed it wont have a sandbox yet until install is finished
                    //remove bundles in existing sandboxes.
                    //this will not catch a user from installing a bundle to the same sandbox twice. That will result in an error later on.
                    collisionCandidates?.remove(candidate)
                }
            }
        }
    }
}
