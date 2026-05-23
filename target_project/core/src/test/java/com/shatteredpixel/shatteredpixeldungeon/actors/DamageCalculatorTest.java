package com.shatteredpixel.shatteredpixeldungeon.actors;

import org.junit.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Unit tests for DamageCalculator.
 *
 * ISO/IEC 25010 Quality Attributes verified:
 *  - Maintainability: centralized logic is easy to test in isolation
 *  - Reliability:     consistent results across all property combinations
 *  - Testability:     pure-function design enables full branch coverage
 *
 * SQA coverage:
 *  - Every public method in DamageCalculator is exercised
 *  - All four DamageProperty values are tested individually and combined
 *  - Boundary / edge-case inputs (0, negative, null set) are covered
 */
public class DamageCalculatorTest {

    // -----------------------------------------------------------------------
    // calculateDamage — basic behaviour
    // -----------------------------------------------------------------------

    @Test
    public void testCalculateDamage_positiveDamageNoProperties_returnsUnchanged() {
        Set<DamageProperty> none = EnumSet.noneOf(DamageProperty.class);
        assertEquals(10, DamageCalculator.calculateDamage(10, none));
    }

    @Test
    public void testCalculateDamage_zeroDamage_returnsZero() {
        Set<DamageProperty> none = EnumSet.noneOf(DamageProperty.class);
        assertEquals(0, DamageCalculator.calculateDamage(0, none));
    }

    @Test
    public void testCalculateDamage_negativeDamage_returnsZero() {
        Set<DamageProperty> none = EnumSet.noneOf(DamageProperty.class);
        assertEquals(0, DamageCalculator.calculateDamage(-5, none));
    }

    @Test
    public void testCalculateDamage_trueDamage_returnsBaseDamageUnmodified() {
        // TRUE_DAMAGE should short-circuit all other modifiers and return the base value
        Set<DamageProperty> props = EnumSet.of(DamageProperty.TRUE_DAMAGE);
        assertEquals(50, DamageCalculator.calculateDamage(50, props));
    }

    @Test
    public void testCalculateDamage_ignoresShields_sameAsBase() {
        // IGNORES_SHIELDS does not alter the raw damage number itself
        Set<DamageProperty> props = EnumSet.of(DamageProperty.IGNORES_SHIELDS);
        assertEquals(20, DamageCalculator.calculateDamage(20, props));
    }

    @Test
    public void testCalculateDamage_ignoresResistance_sameAsBase() {
        Set<DamageProperty> props = EnumSet.of(DamageProperty.IGNORES_RESISTANCE);
        assertEquals(30, DamageCalculator.calculateDamage(30, props));
    }

    @Test
    public void testCalculateDamage_nullProperties_treatedAsSafeDefault() {
        // null property sets should not crash; guard in hasProperty handles this
        int result = DamageCalculator.calculateDamage(15, null);
        assertEquals(15, result);
    }

    // -----------------------------------------------------------------------
    // bypassesShields
    // -----------------------------------------------------------------------

    @Test
    public void testBypassesShields_emptySet_returnsFalse() {
        assertFalse(DamageCalculator.bypassesShields(EnumSet.noneOf(DamageProperty.class)));
    }

    @Test
    public void testBypassesShields_ignoresShields_returnsTrue() {
        assertTrue(DamageCalculator.bypassesShields(
                EnumSet.of(DamageProperty.IGNORES_SHIELDS)));
    }

    @Test
    public void testBypassesShields_trueDamage_returnsTrue() {
        assertTrue(DamageCalculator.bypassesShields(
                EnumSet.of(DamageProperty.TRUE_DAMAGE)));
    }

    @Test
    public void testBypassesShields_unblockableOnly_returnsFalse() {
        assertFalse(DamageCalculator.bypassesShields(
                EnumSet.of(DamageProperty.UNBLOCKABLE)));
    }

    @Test
    public void testBypassesShields_ignoresResistanceOnly_returnsFalse() {
        assertFalse(DamageCalculator.bypassesShields(
                EnumSet.of(DamageProperty.IGNORES_RESISTANCE)));
    }

    @Test
    public void testBypassesShields_nullSet_returnsFalse() {
        assertFalse(DamageCalculator.bypassesShields(null));
    }

    // -----------------------------------------------------------------------
    // bypassesResistance
    // -----------------------------------------------------------------------

    @Test
    public void testBypassesResistance_emptySet_returnsFalse() {
        assertFalse(DamageCalculator.bypassesResistance(EnumSet.noneOf(DamageProperty.class)));
    }

    @Test
    public void testBypassesResistance_ignoresResistance_returnsTrue() {
        assertTrue(DamageCalculator.bypassesResistance(
                EnumSet.of(DamageProperty.IGNORES_RESISTANCE)));
    }

    @Test
    public void testBypassesResistance_trueDamage_returnsTrue() {
        assertTrue(DamageCalculator.bypassesResistance(
                EnumSet.of(DamageProperty.TRUE_DAMAGE)));
    }

    @Test
    public void testBypassesResistance_ignoresShieldsOnly_returnsFalse() {
        assertFalse(DamageCalculator.bypassesResistance(
                EnumSet.of(DamageProperty.IGNORES_SHIELDS)));
    }

    @Test
    public void testBypassesResistance_nullSet_returnsFalse() {
        assertFalse(DamageCalculator.bypassesResistance(null));
    }

    // -----------------------------------------------------------------------
    // isUnblockable
    // -----------------------------------------------------------------------

    @Test
    public void testIsUnblockable_emptySet_returnsFalse() {
        assertFalse(DamageCalculator.isUnblockable(EnumSet.noneOf(DamageProperty.class)));
    }

    @Test
    public void testIsUnblockable_unblockable_returnsTrue() {
        assertTrue(DamageCalculator.isUnblockable(
                EnumSet.of(DamageProperty.UNBLOCKABLE)));
    }

    @Test
    public void testIsUnblockable_trueDamage_returnsTrue() {
        assertTrue(DamageCalculator.isUnblockable(
                EnumSet.of(DamageProperty.TRUE_DAMAGE)));
    }

    @Test
    public void testIsUnblockable_ignoresShieldsOnly_returnsFalse() {
        assertFalse(DamageCalculator.isUnblockable(
                EnumSet.of(DamageProperty.IGNORES_SHIELDS)));
    }

    @Test
    public void testIsUnblockable_nullSet_returnsFalse() {
        assertFalse(DamageCalculator.isUnblockable(null));
    }

    // -----------------------------------------------------------------------
    // Combination / integration
    // -----------------------------------------------------------------------

    @Test
    public void testTrueDamageSet_bypassesAll() {
        Set<DamageProperty> trueDmg = DamageProperty.TRUE_DAMAGE_SET;
        assertTrue("TRUE_DAMAGE should bypass shields",     DamageCalculator.bypassesShields(trueDmg));
        assertTrue("TRUE_DAMAGE should bypass resistance",  DamageCalculator.bypassesResistance(trueDmg));
        assertTrue("TRUE_DAMAGE should be unblockable",     DamageCalculator.isUnblockable(trueDmg));
    }

    @Test
    public void testIgnoresShieldsSet_onlyBypassesShields() {
        Set<DamageProperty> shieldBypass = DamageProperty.IGNORES_SHIELDS_SET;
        assertTrue("Should bypass shields",         DamageCalculator.bypassesShields(shieldBypass));
        assertFalse("Should not bypass resistance", DamageCalculator.bypassesResistance(shieldBypass));
        assertFalse("Should not be unblockable",    DamageCalculator.isUnblockable(shieldBypass));
    }

    @Test
    public void testInstantiation_throwsUnsupportedOperationException() {
        // Utility classes must not be instantiatable
        try {
            java.lang.reflect.Constructor<?> ctor =
                    DamageCalculator.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            ctor.newInstance();
            fail("Expected UnsupportedOperationException from private constructor");
        } catch (Exception e) {
            // Unwrap the InvocationTargetException to get the real cause
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            assertTrue("Should throw UnsupportedOperationException",
                    cause instanceof UnsupportedOperationException);
        }
    }
}
