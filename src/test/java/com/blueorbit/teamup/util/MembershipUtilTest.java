package com.blueorbit.teamup.util;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MembershipUtilTest {

    @Test
    void appendUniqueShouldAvoidDuplicates() {
        String stored = "1;2;";
        String updated = MembershipUtil.appendUnique(stored, 2L);
        assertEquals("1;2;", updated);
    }

    @Test
    void parseAndStoreShouldRoundTrip() {
        Set<Long> ids = MembershipUtil.parseIdSet("1;3;5;");
        assertTrue(ids.contains(1L));
        assertTrue(ids.contains(3L));
        assertTrue(ids.contains(5L));
        assertEquals("1;3;5;", MembershipUtil.toStorageString(ids));
    }
}
