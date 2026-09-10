package com.sonms.aishortcut.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Type scale from DESIGN.md section 4, mapped onto the Material 3 slots that the
// screens actually use. Font is the system sans-serif for now; Pretendard is a
// font-resource task deferred until a screen needs the exact brand face.
internal val AiShortCutTypography: Typography = Typography().let { base ->
    base.copy(
        headlineSmall = base.headlineSmall.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold), // 화면 타이틀
        titleMedium = base.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),      // 카드 헤더
        bodyMedium = base.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Normal),           // 본문
        // "Data" style: numeric metadata (likes, downloads, prices, benchmark
        // indices). Tabular figures so digits line up column-to-column.
        bodySmall = base.bodySmall.copy(
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            fontFeatureSettings = "tnum",
        ),
        labelMedium = base.labelMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),         // 캡션, 메타데이터
    )
}
