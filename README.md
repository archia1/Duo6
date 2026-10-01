
# iPhone Duo Animation for Z Fold 8 - PoC

레딧 @moomanjohnny 원리 기반: 힌지 각도 -> AGSL 셰이더 진행도 매핑
- Presentation API로 커버/메인 디스플레이에 오버레이
- 힌지 센서 값으로 애니메이션 진행

## 빌드 방법
1. Android Studio Ladybug 이상
2. SDK 34
3. gradle sync -> Run

필요 권한: SYSTEM_ALERT_WINDOW (오버레이)

Z Fold 8에서만 힌지 센서 정밀도 충분. Fold 6/7도 동작하지만 약간 떨림 있음.
