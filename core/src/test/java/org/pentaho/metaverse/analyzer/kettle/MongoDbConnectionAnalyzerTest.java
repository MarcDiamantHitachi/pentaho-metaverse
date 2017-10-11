/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.metaverse.analyzer.kettle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.pentaho.di.trans.steps.mongodb.MongoDbMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IAnalysisContext;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.testutils.MetaverseTestUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: RFellows Date: 3/6/15
 */
@RunWith( MockitoJUnitRunner.class )
public class MongoDbConnectionAnalyzerTest {

  MongoDbConnectionAnalyzer analyzer;

  @Mock private IMetaverseBuilder mockBuilder;
  @Mock private MongoDbMeta mongoDbMeta;
  @Mock private IComponentDescriptor mockDescriptor;

  @Before
  public void setUp() throws Exception {
    IMetaverseObjectFactory factory = MetaverseTestUtils.getMetaverseObjectFactory();
    when( mockBuilder.getMetaverseObjectFactory() ).thenReturn( factory );

    analyzer = new MongoDbConnectionAnalyzer();
    analyzer.setMetaverseBuilder( mockBuilder );

    when( mockDescriptor.getNamespace() ).thenReturn( mock( INamespace.class) );
    when( mockDescriptor.getContext() ).thenReturn( mock( IAnalysisContext.class ) );

    when( mongoDbMeta.getHostnames() ).thenReturn( "localhost" );
    when( mongoDbMeta.getDbName() ).thenReturn( "db" );
    when( mongoDbMeta.getAuthenticationUser() ).thenReturn( "user" );
    when( mongoDbMeta.getPort() ).thenReturn( "12345" );
  }

  @Test
  public void testAnalyze() throws Exception {
    when( mockBuilder.addNode( any( IMetaverseNode.class ) ) ).thenAnswer( new Answer<Object>() {
      @Override public Object answer( InvocationOnMock invocation ) throws Throwable {
        Object[] args = invocation.getArguments();
        // add the logicalId to the node like it does in the real builder
        IMetaverseNode node = (IMetaverseNode)args[0];
        node.setProperty( DictionaryConst.PROPERTY_LOGICAL_ID, node.getLogicalId() );
        return mockBuilder;
      }
    } );

    IMetaverseNode node = analyzer.analyze( mockDescriptor, mongoDbMeta );
    assertNotNull( node );
    assertEquals( "localhost", node.getProperty( MongoDbConnectionAnalyzer.HOST_NAMES ) );
    assertEquals( "db", node.getProperty( MongoDbConnectionAnalyzer.DATABASE_NAME ) );
    assertEquals( "user", node.getProperty( DictionaryConst.PROPERTY_USER_NAME ) );
    assertEquals( "12345", node.getProperty( DictionaryConst.PROPERTY_PORT ) );

  }

  @Test
  public void testGetUsedConnections() throws Exception {
    List<MongoDbMeta> dbMetaList = analyzer.getUsedConnections( mongoDbMeta );
    assertEquals( 1, dbMetaList.size() );

    // should just return the same MongoDbMeta object in list form as the only entry
    assertEquals( mongoDbMeta, dbMetaList.get( 0 ) );
  }

  @Test
  public void testBuildComponentDescriptor() throws Exception {
    IComponentDescriptor dbDesc = analyzer.buildComponentDescriptor( mockDescriptor, mongoDbMeta );
    assertNotNull( dbDesc );
    assertEquals( "db", dbDesc.getName() );
  }
}
