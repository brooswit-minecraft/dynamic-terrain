package io.github.brooswitminecraft.dynamicterrain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class TransitionTableTest {
    @Test
    void declaredTransitionResolves() {
        TransitionTable table = new TransitionTable();
        table.put("moisture", "minecraft:cobblestone", "minecraft:mossy_cobblestone");
        assertEquals(Optional.of("minecraft:mossy_cobblestone"), table.resultFor("moisture", "minecraft:cobblestone"));
    }

    @Test
    void unknownTransitionOrBlockIsNoResult() {
        TransitionTable table = new TransitionTable();
        table.put("moisture", "minecraft:cobblestone", "minecraft:mossy_cobblestone");
        assertTrue(table.resultFor("ash", "minecraft:cobblestone").isEmpty());
        assertTrue(table.resultFor("moisture", "minecraft:stone").isEmpty());
    }

    @Test
    void laterDeclarationWins() {
        TransitionTable table = new TransitionTable();
        table.put("damage", "a:b", "a:c");
        table.put("damage", "a:b", "a:d");
        assertEquals(Optional.of("a:d"), table.resultFor("damage", "a:b"));
        assertEquals(1, table.size());
    }

    @Test
    void transitionsAreIndependent() {
        TransitionTable table = new TransitionTable();
        table.put("moisture", "a:b", "a:wet");
        table.put("ash", "a:b", "a:burnt");
        assertEquals(Optional.of("a:burnt"), table.resultFor("ash", "a:b"));
        assertEquals(2, table.size());
    }
}
