
package com.fold.iphoneduo

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.view.*
import android.widget.FrameLayout
import androidx.window.layout.WindowInfoTracker
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build

class DuoOverlayService : Service(), SensorEventListener {
    private var windowManager: WindowManager? = null
    private var overlayView: DuoView? = null
    private var sensorManager: SensorManager? = null
    private var hingeSensor: Sensor? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var lastAngle = 0f

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        hingeSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_HINGE_ANGLE)

        overlayView = DuoView(this)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        windowManager?.addView(overlayView, params)

        hingeSensor?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }

        // androidx.window fallback - Fold 상태로도 각도 추정
        scope.launch {
            WindowInfoTracker.getOrCreate(this@DuoOverlayService).windowLayoutInfo(this@DuoOverlayService).collect { info ->
                val foldingFeature = info.displayFeatures.firstOrNull()
                // foldingFeature 있으면 접힘 상태로 progress 추정
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_HINGE_ANGLE) {
            val angle = event.values[0] // 0~180
            lastAngle = angle
            // 0도(접힘) -> 180도(펼침) 을 0~1 progress로 매핑
            val progress = (angle / 180f).coerceIn(0f, 1f)
            overlayView?.setProgress(progress, angle)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        sensorManager?.unregisterListener(this)
        overlayView?.let { windowManager?.removeView(it) }
        scope.cancel()
    }

    class DuoView(context: Context) : FrameLayout(context) {
        private val shaderCode = """
            uniform shader content;
            uniform float progress; // 0~1
            uniform float blurAmount;
            uniform vec2 resolution;
            half4 main(vec2 fragCoord) {
                vec2 uv = fragCoord / resolution;
                // iPhone Duo 효과: 가운데서 양쪽으로 퍼지는 스케일 + 블러
                float scale = mix(1.3, 1.0, progress);
                vec2 centered = (uv - 0.5) * scale + 0.5;
                // 책 펼치듯 좌우로 찢어지는 왜곡
                float fold = abs(uv.x - 0.5) * (1.0 - progress) * 0.3;
                centered.y += fold;
                return content.eval(centered * resolution);
            }
        """.trimIndent()

        private var runtimeShader: RuntimeShader? = null
        private var progress: Float = 0f

        init {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                runtimeShader = RuntimeShader(shaderCode)
            }
            setBackgroundColor(0x00000000)
            // 여기에 스크린샷 2장을 합성. PoC는 단색 + 텍스트로 대체
        }

        fun setProgress(p: Float, angle: Float) {
            progress = p
            // progress <0.05 or >0.95 일 때만 오버레이 보이게 해서 배터리 절약
            visibility = if (p in 0.02f..0.98f) VISIBLE else GONE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (p in 0.02f..0.98f) {
                    // 블러는 RenderEffect로 더 간단하게 - AGSL 대신
                    val blur = (1f - p) * 25f + p * 0f
                    // 실제 구현에서는 두 디스플레이 캡처본에 적용
                    setRenderEffect(RenderEffect.createBlurEffect(blur, blur, android.graphics.Shader.TileMode.CLAMP))
                } else {
                    setRenderEffect(null)
                }
            }
            // 스케일 애니메이션
            scaleX = 0.9f + p * 0.1f
            scaleY = 0.9f + p * 0.1f
            alpha = if (p > 0.02f) 1f else 0f
            invalidate()
        }
    }
}
