package com.ntn.utils;

import com.ntn.pojo.HealthData;

/**
 * Tiện ích tính BMI dùng chung (đồng bộ công thức & ngưỡng với AiRecoController).
 * Ngưỡng theo chuẩn Á: &lt;18.5 Thiếu cân, 18.5–22.9 Bình thường, 23–24.9 Thừa cân, ≥25 Béo phì.
 */
public final class HealthUtils {

    private HealthUtils() {}

    /** BMI = kg / (m^2). Trả null nếu thiếu/không hợp lệ dữ liệu. */
    public static Double bmiOf(HealthData h) {
        if (h == null || h.getHeight() == null || h.getWeight() == null) return null;
        double hCm = h.getHeight().doubleValue();
        double wKg = h.getWeight().doubleValue();
        if (hCm <= 0 || wKg <= 0) return null;
        return wKg / Math.pow(hCm / 100.0, 2);
    }

    /** Nhãn BMI tiếng Việt. */
    public static String bmiLabel(double bmi) {
        if (bmi < 18.5) return "Thiếu cân";
        if (bmi >= 25) return "Béo phì";
        if (bmi >= 23) return "Thừa cân";
        return "Bình thường";
    }
}
