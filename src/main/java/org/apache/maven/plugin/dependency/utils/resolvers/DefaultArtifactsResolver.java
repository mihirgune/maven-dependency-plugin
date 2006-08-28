/*
 *  Copyright 2005-2006 Brian Fox (brianefox@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package org.apache.maven.plugin.dependency.utils.resolvers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

/**
 * @author brianf
 *
 */
public class DefaultArtifactsResolver
    implements ArtifactsResolver
{
    ArtifactResolver resolver;

    ArtifactRepository local;

    List remoteRepositories;

    boolean stopOnFailure;

    public DefaultArtifactsResolver( ArtifactResolver resolver, ArtifactRepository local, List remoteRepositories,
                                    boolean stopOnFailure )
    {
        this.resolver = resolver;
        this.local = local;
        this.remoteRepositories = remoteRepositories;
        this.stopOnFailure = stopOnFailure;
    }

    /* (non-Javadoc)
     * @see org.apache.mojo.dependency.utils.resolvers.ArtifactsResolver#resolve(java.util.Set, org.apache.maven.plugin.logging.Log)
     */
    public Set resolve( Set artifacts, Log log )
        throws MojoExecutionException
    {

        Set resolvedArtifacts = new HashSet();
        Iterator iter = artifacts.iterator();
        while ( iter.hasNext() )
        {
            Artifact artifact = (Artifact) iter.next();
            try
            {
                resolver.resolve( artifact, remoteRepositories, local );
                resolvedArtifacts.add( artifact );
            }
            catch ( ArtifactResolutionException ex )
            {
                // an error occurred during resolution, log it an continue
                log.debug( "error resolving: " + artifact.getId() );
                log.debug( ex );
                if (stopOnFailure)
                {
                    throw new MojoExecutionException("error resolving: " + artifact.getId(),ex);
                }
            }
            catch ( ArtifactNotFoundException ex )
            {
                // not found, log it and continue
                log.debug( "not found in any repository: " + artifact.getId() );
                if (stopOnFailure)
                {
                    throw new MojoExecutionException("not found in any repository: " + artifact.getId(),ex);
                }
            }
        }
        return resolvedArtifacts;
    }

}