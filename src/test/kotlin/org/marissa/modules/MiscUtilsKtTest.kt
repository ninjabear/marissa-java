package org.marissa.modules

import org.junit.Before
import org.marissa.lib.Response
import org.junit.Test

import org.mockito.Mockito

class MiscUtilsKtTest {

    private val mock = Mockito.mock(Response::class.java);

    @Before
    fun beforeEach() {
        Mockito.reset(mock);
    }

    @Test
    fun tellTheTimeTest() {
        tellTheTime("", mock);
        Mockito.verify(mock).send(Mockito.anyString());
    }

    @Test
    fun selfieTest() {
        selfie("", mock);
        Mockito.verify(mock).send(Mockito.anyString());
    }

    @Test
    fun echoTest() {
        echo("test", mock);
        Mockito.verify(mock).send("test");
    }

    @Test
    fun pingTest() {
        ping("", mock);
        Mockito.verify(mock).send("pong");
    }

}