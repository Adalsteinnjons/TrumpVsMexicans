package ch.unibas.dmi.cs108.sand.network;

import org.junit.Test;
import static org.junit.Assert.*;

public class CommandListTest {
    @Test
    public void isCommandListLowerCase() throws Exception {
        assertEquals(true,CommandList.isCommandList("MEXICAN"));
        assertEquals(false,CommandList.isCommandList("mexican"));
    }
}