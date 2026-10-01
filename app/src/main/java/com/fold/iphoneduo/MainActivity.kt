
package com.fold.iphoneduo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("iPhone Duo for Z Fold 8 PoC", style = MaterialTheme.typography.headlineSmall)
                    Text("레딧 moomanjohnny 방식: Hinge Angle -> AGSL Shader")
                    Button(onClick = { checkOverlay() }) { Text("오버레이 권한 허용") }
                    Button(onClick = { startService(Intent(this@MainActivity, DuoOverlayService::class.java)) }) { Text("애니메이션 서비스 시작") }
                    Button(onClick = { stopService(Intent(this@MainActivity, DuoOverlayService::class.java)) }) { Text("중지") }
                    Text("사용법: 서비스 시작 후 폴드를 접었다 펼치면 커버->메인 화면이 블러+스케일로 이어지는 애니메이션이 오버레이로 재생됩니다. 실제 앱 콘텐츠 연속은 삼성 접근권한이 없어 스크린샷 기반 PoC 입니다.")
                }
            }
        }
    }
    private fun checkOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }
    }
}
