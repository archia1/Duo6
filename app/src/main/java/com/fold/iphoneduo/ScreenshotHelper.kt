
package com.fold.iphoneduo

// PoC 개선용: 실제 앱 스크린샷을 뜨는 코드
// Presentation API로 두 디스플레이에 각각 그리기
import android.app.Presentation
import android.content.Context
import android.os.Bundle
import android.view.Display

class DuoPresentation(context: Context, display: Display) : Presentation(context, display) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 여기에 AGSL 셰이더를 적용한 ImageView를 두면 양쪽 화면 동시 제어 가능
        // moomanjohnny가 언급한 방식: Presentation API + AGSL
        val view = DuoOverlayService.DuoView(context)
        setContentView(view)
    }
}
