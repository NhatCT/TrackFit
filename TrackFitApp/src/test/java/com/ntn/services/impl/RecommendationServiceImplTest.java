package com.ntn.services.impl;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class RecommendationServiceImplTest {

    private final RecommendationServiceImpl service = new RecommendationServiceImpl();

    @Test
    void adaptIntensity_lowCompletionRate_decreasesIntensity() throws Exception {
        Method m = RecommendationServiceImpl.class.getDeclaredMethod("adaptIntensity", String.class, double.class);
        m.setAccessible(true);
        String result = (String) m.invoke(service, "Medium", 0.3);
        assertEquals("Low", result);
    }

    @Test
    void adaptIntensity_highCompletionRate_increasesIntensity() throws Exception {
        Method m = RecommendationServiceImpl.class.getDeclaredMethod("adaptIntensity", String.class, double.class);
        m.setAccessible(true);
        String result = (String) m.invoke(service, "Medium", 0.9);
        assertEquals("High", result);
    }

    @Test
    void adaptIntensity_normalCompletionRate_keepsIntensity() throws Exception {
        Method m = RecommendationServiceImpl.class.getDeclaredMethod("adaptIntensity", String.class, double.class);
        m.setAccessible(true);
        String result = (String) m.invoke(service, "Medium", 0.6);
        assertEquals("Medium", result);
    }

    @Test
    void adaptIntensity_alreadyLow_doesNotGoLower() throws Exception {
        Method m = RecommendationServiceImpl.class.getDeclaredMethod("adaptIntensity", String.class, double.class);
        m.setAccessible(true);
        String result = (String) m.invoke(service, "Low", 0.3);
        assertEquals("Low", result);
    }

    @Test
    void adaptIntensity_alreadyHigh_doesNotGoHigher() throws Exception {
        Method m = RecommendationServiceImpl.class.getDeclaredMethod("adaptIntensity", String.class, double.class);
        m.setAccessible(true);
        String result = (String) m.invoke(service, "High", 0.9);
        assertEquals("High", result);
    }

    @Test
    void pick_returnsFirstNonBlank() throws Exception {
        Method m = RecommendationServiceImpl.class.getDeclaredMethod("pick", String[].class);
        m.setAccessible(true);
        String result = (String) m.invoke(service, (Object) new String[]{null, "", "  ", "valid", "other"});
        assertEquals("valid", result);
    }

    @Test
    void pick_allNull_returnsNull() throws Exception {
        Method m = RecommendationServiceImpl.class.getDeclaredMethod("pick", String[].class);
        m.setAccessible(true);
        String result = (String) m.invoke(service, (Object) new String[]{null, null});
        assertNull(result);
    }

    @Test
    void parseMinutesFromTargetGoal_withApostrophe() throws Exception {
        Method m = RecommendationServiceImpl.class.getDeclaredMethod("parseMinutesFromTargetGoal", String.class);
        m.setAccessible(true);
        assertEquals(30, (Integer) m.invoke(service, "30'"));
    }

    @Test
    void parseMinutesFromTargetGoal_withPrimeSymbol() throws Exception {
        Method m = RecommendationServiceImpl.class.getDeclaredMethod("parseMinutesFromTargetGoal", String.class);
        m.setAccessible(true);
        assertEquals(60, (Integer) m.invoke(service, "60\u2032"));
    }

    @Test
    void parseMinutesFromTargetGoal_embeddedInText() throws Exception {
        Method m = RecommendationServiceImpl.class.getDeclaredMethod("parseMinutesFromTargetGoal", String.class);
        m.setAccessible(true);
        assertEquals(45, (Integer) m.invoke(service, "some text 45 more"));
    }

    @Test
    void parseMinutesFromTargetGoal_null_returnsNull() throws Exception {
        Method m = RecommendationServiceImpl.class.getDeclaredMethod("parseMinutesFromTargetGoal", String.class);
        m.setAccessible(true);
        Integer result = (Integer) m.invoke(service, (Object) null);
        assertNull(result);
    }

    @Test
    void parseMinutesFromTargetGoal_noDigits_returnsNull() throws Exception {
        Method m = RecommendationServiceImpl.class.getDeclaredMethod("parseMinutesFromTargetGoal", String.class);
        m.setAccessible(true);
        Integer result = (Integer) m.invoke(service, "no digits here");
        assertNull(result);
    }

    @Test
    void normalizeUrl_baseWithoutTrailingSlash() throws Exception {
        Method m = RecommendationServiceImpl.class.getDeclaredMethod("normalizeUrl", String.class, String.class);
        m.setAccessible(true);
        assertEquals("http://localhost:8000/rank", (String) m.invoke(service, "http://localhost:8000", "/rank"));
    }

    @Test
    void normalizeUrl_baseWithTrailingSlash() throws Exception {
        Method m = RecommendationServiceImpl.class.getDeclaredMethod("normalizeUrl", String.class, String.class);
        m.setAccessible(true);
        assertEquals("http://localhost:8000/rank", (String) m.invoke(service, "http://localhost:8000/", "/rank"));
    }

    @Test
    void normalizeUrl_pathWithoutLeadingSlash() throws Exception {
        Method m = RecommendationServiceImpl.class.getDeclaredMethod("normalizeUrl", String.class, String.class);
        m.setAccessible(true);
        assertEquals("http://localhost:8000/rank", (String) m.invoke(service, "http://localhost:8000/", "rank"));
    }

    @Test
    void normalizeUrl_emptyBase_returnsPath() throws Exception {
        Method m = RecommendationServiceImpl.class.getDeclaredMethod("normalizeUrl", String.class, String.class);
        m.setAccessible(true);
        assertEquals("/rank", (String) m.invoke(service, "", "/rank"));
    }

    @Test
    void normalizeUrl_nullBase_returnsPath() throws Exception {
        Method m = RecommendationServiceImpl.class.getDeclaredMethod("normalizeUrl", String.class, String.class);
        m.setAccessible(true);
        String result = (String) m.invoke(service, null, "/rank");
        assertEquals("/rank", result);
    }

    @Test
    void normalizeUrl_nullPath_returnsBase() throws Exception {
        Method m = RecommendationServiceImpl.class.getDeclaredMethod("normalizeUrl", String.class, String.class);
        m.setAccessible(true);
        String result = (String) m.invoke(service, "http://host", null);
        assertEquals("http://host", result);
    }
}
