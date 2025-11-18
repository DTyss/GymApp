# Cấu hình BASE_URL cho Android App

## Development

### Sử dụng Emulator
Mặc định app đã cấu hình cho emulator:
```kotlin
buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/\"")
```
- `10.0.2.2` là địa chỉ đặc biệt của emulator trỏ về `localhost` của máy host

### Sử dụng thiết bị thật
1. Tìm IP của máy tính (Windows: `ipconfig`, Mac/Linux: `ifconfig`)
2. Mở file `app/build.gradle.kts`
3. Trong block `buildTypes.debug`, uncomment và thay YOUR_IP:
```kotlin
buildConfigField("String", "BASE_URL", "\"http://192.168.1.100:8080/\"")
```
4. Đảm bảo máy tính và điện thoại cùng mạng WiFi

## Production

Khi deploy lên production:
1. Mở file `app/build.gradle.kts`
2. Trong block `buildTypes.release`, thay domain:
```kotlin
buildConfigField("String", "BASE_URL", "\"https://api.yourdomain.com/\"")
```
3. Build release APK/AAB

## Lưu ý
- Backend phải chạy trước khi test app
- Kiểm tra firewall không block port 8080
- Với HTTPS production, cần SSL certificate hợp lệ
