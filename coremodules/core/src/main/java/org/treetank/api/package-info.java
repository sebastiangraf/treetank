/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * TreeTank API
 * 
 * This package contains the public TreeTank API. Users will have to connect to any TreeTank through this API.
 * Note that for common usage, only access-interfaces provided by this package should be used.
 * 
 * Treetank is based on three layers of interaction:
 * <ul>
 * <li>IStorage: This layer denotes a persistent storage. Each storage can be created using one specific
 * <code>StorageConfiguration</code>. Afterwards, this configuration is valid for the whole lifetime of the
 * database. Each IStorage can hold multiple resources accessible over ISession.</li>
 * <li>ISession: This layer denotes a runtime access on a resource stored in a storage. Only one ISession is
 * allowed at one time. The layer has ability to provide runtime-settings as well. Especially settings
 * regarding the transaction-handling can be provided.</li>
 * <li>IBucketReadTrx/IBucketWriteTrx: This layer provided direct access to the database. All access to datas
 * used either a <code>IBucketReadTrx</code> or <code>IBucketWriteTrx</code>.
 * </ul>
 * Additional to these access-interfaces, this api-packages provides direct access-methods for the
 * data-structure:
 * <ul>
 * <li>IDataFactory: This interface is for deserializing IData-instances in the correct way.</li>
 * </ul>
 * 
 * 
 * 
 * @author Marc Kramis, University of Konstanz
 * @author Sebastian Graf, University of Konstanz
 */
package org.treetank.api;

