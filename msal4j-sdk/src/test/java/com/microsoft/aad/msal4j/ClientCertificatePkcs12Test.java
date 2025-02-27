// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.aad.msal4j;

import org.easymock.EasyMock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.security.KeyStore;
import java.security.KeyStoreSpi;
import java.util.Arrays;
import java.util.Collections;

import static org.testng.AssertJUnit.assertEquals;

@Test
public class ClientCertificatePkcs12Test extends AbstractMsalTests {

    private KeyStoreSpi keyStoreSpi;
    private KeyStore keystore;

    @BeforeMethod
    public void setUp() throws Exception {
        keyStoreSpi = EasyMock.createMock(KeyStoreSpi.class);
        keystore = new KeyStore(keyStoreSpi, null, "PKCS12") {};
        keystore.load(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "certificate not loaded from input stream")
    public void testNoEntries() throws Exception {
        EasyMock.expect(keyStoreSpi.engineAliases())
                .andReturn(Collections.enumeration(Collections.emptyList())).times(1);
        EasyMock.replay(keyStoreSpi);

        ClientCertificate.getPrivateKeyAlias(keystore);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "certificate not loaded from input stream")
    public void testNoPrivateKey() throws Exception {
        EasyMock.expect(keyStoreSpi.engineAliases())
                .andReturn(Collections.enumeration(Arrays.asList("CA_cert1", "CA_cert2"))).times(1);
        EasyMock.expect(keyStoreSpi.engineEntryInstanceOf("CA_cert1", KeyStore.PrivateKeyEntry.class)).andReturn(false).times(1);
        EasyMock.expect(keyStoreSpi.engineEntryInstanceOf("CA_cert2", KeyStore.PrivateKeyEntry.class)).andReturn(false).times(1);
        EasyMock.replay(keyStoreSpi);

        ClientCertificate.getPrivateKeyAlias(keystore);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "more than one certificate alias found in input stream")
    public void testMultiplePrivateKeyAliases() throws Exception {
        EasyMock.expect(keyStoreSpi.engineAliases())
                .andReturn(Collections.enumeration(Arrays.asList("private_key1", "private_key2", "CA_cert"))).times(1);
        EasyMock.expect(keyStoreSpi.engineEntryInstanceOf("private_key1", KeyStore.PrivateKeyEntry.class)).andReturn(true).times(1);
        EasyMock.expect(keyStoreSpi.engineEntryInstanceOf("private_key2", KeyStore.PrivateKeyEntry.class)).andReturn(true).times(1);
        EasyMock.expect(keyStoreSpi.engineEntryInstanceOf("CA_cert", KeyStore.PrivateKeyEntry.class)).andReturn(false).times(1);
        EasyMock.replay(keyStoreSpi);

        ClientCertificate.getPrivateKeyAlias(keystore);
    }

    @Test
    public void testMultipleEntriesButOnlyOnePrivateKey() throws Exception {
        EasyMock.expect(keyStoreSpi.engineAliases())
                .andReturn(Collections.enumeration(Arrays.asList("CA_cert1", "private_key", "CA_cert2"))).times(1);
        EasyMock.expect(keyStoreSpi.engineEntryInstanceOf("CA_cert1", KeyStore.PrivateKeyEntry.class)).andReturn(false).times(1);
        EasyMock.expect(keyStoreSpi.engineEntryInstanceOf("private_key", KeyStore.PrivateKeyEntry.class)).andReturn(true).times(1);
        EasyMock.expect(keyStoreSpi.engineEntryInstanceOf("CA_cert2", KeyStore.PrivateKeyEntry.class)).andReturn(false).times(1);
        EasyMock.replay(keyStoreSpi);

        String privateKeyAlias = ClientCertificate.getPrivateKeyAlias(keystore);
        assertEquals("private_key", privateKeyAlias);
    }

}